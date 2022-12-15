package tweetoscope.kafkaProducer;
import com.twitter.clientlib.model.Tweet;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

import java.util.Map;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import org.apache.kafka.clients.producer.ProducerRecord;
import tweetoscope.TweetSerializer;
public class MockTwitterStreamRecordedKafkaProducer {


    private static String largeFileName = "TestBases/largeTestBase.txt";
    private static String miniFileName = "TestBases/miniTestBase.txt";
    private static String scenarioFileName = "TestBases/scenarioTestBase.txt";
    private KafkaProducer<Void, Tweet> kafkaProducer;
    private String TweetsTopicName = "Tweets";


    //Main Thread;
    public static void main(String[] args) {
        new MockTwitterStreamRecordedKafkaProducer();
    }

    MockTwitterStreamRecordedKafkaProducer(){
        try {
            kafkaProducer = new KafkaProducer<Void, Tweet>(configureKafkaProducer());
            String content = fileReader();

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(content, JsonObject.class);
            JsonArray jsonObjectTweets = jsonObject.getAsJsonArray("tweets");
            Tweet[] tweets = new Tweet[jsonObjectTweets.size()];

            for (int i = 0; i <jsonObjectTweets.size(); i++) {
                tweets[i] = new Tweet();
                JsonObject currentJson = (JsonObject) jsonObjectTweets.get(i);
                Map<String,String > parsedJson = jsonParse(currentJson);

                tweets[i].setId(parsedJson.get("id"));
                tweets[i].setCreatedAt(OffsetDateTime.parse(parsedJson.get("created_at")));
                tweets[i].setText(parsedJson.get("text"));
                tweets[i].setAuthorId(parsedJson.get("author_id"));
                tweets[i].setConversationId(parsedJson.get("conversation_id"));
                tweets[i].getGeo();
                tweets[i].setLang(parsedJson.get("lang"));
            }
            Tweet randomTweet;
            //boolean flag = true;
            //int counter  = 0;
            while(true){
                randomTweet = tweets[(int)(Math.random()*jsonObjectTweets.size())];
                kafkaProducer.send(new ProducerRecord<Void, Tweet>(TweetsTopicName, null,randomTweet));
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
              //  counter=+1;
              //  if (counter >5) flag = false;
            }
        }catch (Exception e){
           System.err.println("something went wrong... " + e);
       }finally {
            kafkaProducer.close();
           }
    }


   //Properties
    private Properties configureKafkaProducer() {
        Properties producerProperties = new Properties();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.VoidSerializer");
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                TweetSerializer.class);
                //"org.apache.kafka.common.serialization.StringSerializer");
        return producerProperties;
    }

    private static String fileReader(){
        File file=new File(miniFileName);
        String content= null;
        try {
            content = FileUtils.readFileToString(file,"UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return content ;
    }


    private Map<String,String> jsonParse(JsonObject nonParsedJson){
        Map<String,String> parsedJson = new HashMap<>();

        String id = nonParsedJson.getAsJsonPrimitive("id").getAsString();
        String created_at = nonParsedJson.getAsJsonPrimitive("created_at").getAsString();
        String text = nonParsedJson.getAsJsonPrimitive("text").getAsString();
        String author_id = nonParsedJson.getAsJsonPrimitive("author_id").getAsString();
        String conversation_id = nonParsedJson.getAsJsonPrimitive("conversation_id").getAsString();
        //String geo = nonParsedJson.getAsJsonPrimitive("geo").getAsString();
        String lang = nonParsedJson.getAsJsonPrimitive("lang").getAsString();

        parsedJson.put("id",id);
        parsedJson.put("created_at",created_at);
        parsedJson.put("text",text);
        parsedJson.put("author_id",author_id);
        parsedJson.put("conversation_id",conversation_id);
        //parsedJson.put("geo",geo);
        parsedJson.put("lang",lang);

        return parsedJson;
    }
}
