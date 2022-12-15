package tweetoscope.kafkaFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.*;

import com.google.gson.*;
import com.twitter.clientlib.TwitterCredentialsBearer;
import com.twitter.clientlib.api.TwitterApi;
import com.twitter.clientlib.model.Place;
import com.twitter.clientlib.model.Tweet;
import com.twitter.twittertext.Extractor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import tweetoscope.TweetDeserializer;
import tweetoscope.TweetSerializer;

public class StreamFilterService {
    private String UpStream;
    private String DownStream;
    private KafkaStreams tweetStream;
    private String bootstrapServers;

    protected static String BEARER_TOKEN;

    public static TwitterApi twitterApiInstance;

    final Serializer<Tweet> tweetSerializer = new TweetSerializer();
    final Deserializer<Tweet> tweetDeserializer = new TweetDeserializer();
    final Serde<Tweet> tweetSerde = Serdes.serdeFrom(tweetSerializer, tweetDeserializer);

    final Extractor twitterTextExtractor = new Extractor();

    public static void main(String[] args) {
        new StreamFilterService(args[0], args[1], args[2]);
    }

    StreamFilterService(String bootstrapServers, String UpStream, String DownStream) {
        try {
            configureCredentials();
        } catch (Exception e) {
            System.err.println("Error while authentifacting: " + e);
        }

        this.bootstrapServers = bootstrapServers;
        this.UpStream = UpStream; // from topic
        this.DownStream = DownStream; // to topic
        

        Topology FilterForTweet = createFilterForTweet();
        tweetStream = new KafkaStreams(FilterForTweet, configureTweetKafkaStreams());
        tweetStream.start();
    }

    private Properties configureTweetKafkaStreams() {
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "TweetsFilterService");
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return properties;
    }

    private String configureCredentials() {
        twitterApiInstance = new TwitterApi();
        BEARER_TOKEN = System.getenv("BEARER_TOKEN");
        TwitterCredentialsBearer credentials = new TwitterCredentialsBearer(BEARER_TOKEN);
        twitterApiInstance.setTwitterCredentials(credentials);

        return null;
    }

    protected String getCountryCode(Tweet tweet) {
        if (tweet.getGeo() == null)
            return null;

        try {
            // to get the country_code from the placeId set in the Tweet, a new request to
            // the Twitter API is required
            // see
            // https://developer.twitter.com/en/docs/twitter-api/data-dictionary/object-model/place
            HttpClient httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
                    .build();
            URIBuilder uriBuilder = new URIBuilder(
                    "https://api.twitter.com/2/tweets?ids=" + tweet.getId() + "&expansions=geo.place_id"
                            + "&place.fields=contained_within,country,country_code,full_name,geo,id,name,place_type");
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            httpGet.setHeader("Authorization", String.format("Bearer %s", BEARER_TOKEN));
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (null != entity) {
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(OffsetDateTime.class, (JsonDeserializer<OffsetDateTime>) (json, type,
                                context) -> OffsetDateTime.parse(json.getAsString()))
                        .create();
                BufferedReader reader = new BufferedReader(new InputStreamReader((entity.getContent())));
                String line = reader.readLine();
                while (line != null) {
                    JsonElement include = gson.fromJson(line, JsonObject.class).get("includes");
                    if (include != null) {
                        JsonElement jsonPlace = include.getAsJsonObject().get("places").getAsJsonArray().get(0);
                        Place place = gson.fromJson(jsonPlace, Place.class);
                        return place.getCountryCode();
                    }
                    line = reader.readLine();
                }
            }
        } catch (URISyntaxException | IOException e) {
            System.err.println("error while retreiving the country code: " + e);
        }
        return null;
    }

    private Topology createFilterForTweet() {
        StreamsBuilder streamBuilder = new StreamsBuilder();
        KStream<Void, Tweet> source = streamBuilder.stream(UpStream, Consumed.with(Serdes.Void(), tweetSerde));
        // ------------- First Filter Service; CountryCode filter -----------
        Predicate<Void, Tweet> CountryFilter = new Predicate<Void, Tweet>() {
            @Override
            public boolean test(Void unused, Tweet tweet) {
                // String countryCode = getCountryCode(tweet);
                // Most of the countrycodes are null, so here comment this filter.
                // return (countryCode != null) && (countryCode.equals("US"));
                return true;
            }
        };
        KStream<Void, Tweet> CountrySource = source.filter(CountryFilter);
        // ----------- First Filter Service; Language filter ----------------
        Predicate<Void, Tweet> languageFilter = new Predicate<Void, Tweet>() {
            @Override
            public boolean test(Void unused, Tweet tweet) {
                String language = tweet.getLang();
                if (language == null)
                    return false;
                return language.equals("en");
            }
        };
        KStream<Void, Tweet> LanguageSource = CountrySource.filter(languageFilter);
        // ------------------ HashTagExtractor -----------------------
        ValueMapper<Tweet, List<String>> hashTagValueMapper = new ValueMapper<Tweet, List<String>>() {
            public List<String> apply(Tweet tweet) {
                List<String> hashtags = twitterTextExtractor.extractHashtags(tweet.getText());
                return hashtags;
            }
        };
        KStream<Void, String> HashTagExtractorSource = LanguageSource.flatMapValues(hashTagValueMapper);
        // ----------------- HashTagCounter ----------------------------
        KeyValueMapper<Void, String, String> selector = new KeyValueMapper<Void, String, String>() {
            public String apply(Void key, String value) {
                return value;
            }
        };
        KGroupedStream<String, String> HashTagGroupedStream = HashTagExtractorSource.groupBy(selector);
        KTable<String, Long> HashTagCountTable = HashTagGroupedStream.count();
        // ---------------------------------------------------------------
        HashTagCountTable.toStream().to(DownStream, Produced.with(Serdes.String(), Serdes.Long()));
        HashTagCountTable.toStream().foreach((k, v) -> {
            System.out.println("key : " + k + "\t\t" + "value : " + v);
        });
        return streamBuilder.build();
    }

}
