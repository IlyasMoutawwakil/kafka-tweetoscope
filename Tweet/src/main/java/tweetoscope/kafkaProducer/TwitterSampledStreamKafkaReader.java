package tweetoscope.kafkaProducer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.twitter.clientlib.model.Tweet;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import tweetoscope.TweetSerializer;
import tweetoscope.tweetsProducer.TwitterSampledStreamReaderSingleton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.Properties;

public class TwitterSampledStreamKafkaReader extends KafkaOnlineProducer {
    private static KafkaProducer<Void, Tweet> kafkaProducer;
    private static volatile TwitterSampledStreamReaderSingleton instance;

    private String bootstrapServers;
    private String topicName;

    public static void main(String[] args) throws IOException, URISyntaxException {
        new TwitterSampledStreamKafkaReader(args[0], args[1]);
    }

    private TwitterSampledStreamKafkaReader(String bootstrapServers, String topicName) {
        super();
        this.bootstrapServers = bootstrapServers;
        this.topicName = topicName;
        try {
            connectStream(BEARER_TOKEN);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void connectStream(String bearerToken) throws IOException, URISyntaxException {
        kafkaProducer = new KafkaProducer<Void, Tweet>(configureKafkaProducer());
        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build();

        URIBuilder uriBuilder = new URIBuilder("https://api.twitter.com/2/tweets/sample/stream?"
                + "tweet.fields=author_id,conversation_id,created_at,geo,lang");
        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.setHeader("Authorization", String.format("Bearer %s", bearerToken));

        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        if (null != entity) {
            // TypeAdapter required to deserialize OffsetDateTime
            // see
            // https://stackoverflow.com/questions/60723739/offsetdatetime-deserialization-using-gson
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(OffsetDateTime.class, (JsonDeserializer<OffsetDateTime>) (json, type,
                            context) -> OffsetDateTime.parse(json.getAsString()))
                    .create();

            Tweet tweet;

            BufferedReader reader = new BufferedReader(new InputStreamReader((entity.getContent())));
            String line = reader.readLine();
            int nbTweets = 0;
            long startTime = System.currentTimeMillis();
            long observingPeriod = 10000; // 10s
            while (line != null) {
                if (System.currentTimeMillis() - startTime >= observingPeriod) {
                    System.out.println("Sampled stream: " + nbTweets + " tweets received in " + observingPeriod
                            + " milliseconds.");
                    nbTweets = 0;
                    startTime = System.currentTimeMillis();
                }
                if (!line.equals("")) {
                    if (line.equals("Rate limit exceeded")) {
                        System.out.println(
                                "Twitter sampled stream rate limit exceeded (50 requests per 15-minute window, "
                                        + "see https://developer.twitter.com/en/docs/twitter-api/rate-limits). "
                                        + "Try again in 15 minutes.");
                        System.exit(0);
                    }
                    tweet = gson.fromJson(gson.fromJson(line, JsonObject.class).get("data"), Tweet.class);
                    kafkaProducer.send(new ProducerRecord<Void, Tweet>(topicName, null, tweet));
                    nbTweets++;
                }
                line = reader.readLine();
            }
        }
        kafkaProducer.close();
    }

    private Properties configureKafkaProducer() {
        Properties producerProperties = new Properties();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers);
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.VoidSerializer");
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                TweetSerializer.class);
        return producerProperties;
    }

    public static TwitterSampledStreamReaderSingleton getInstance() {
        // double-checked locking (DCL)
        // for a thread-safe implementation of the Singleton pattern
        // read more about DLC: https://refactoring.guru/java-dcl-issue
        TwitterSampledStreamReaderSingleton result = instance;
        if (result != null) {
            return result;
        }
        synchronized (TwitterSampledStreamReaderSingleton.class) {
            if (instance == null) {
                instance = new TwitterSampledStreamReaderSingleton();
            }
            return instance;
        }
    }

}
