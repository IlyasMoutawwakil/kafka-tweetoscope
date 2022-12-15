package tweetoscope;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.twitter.clientlib.model.Tweet;
import org.apache.kafka.common.serialization.Deserializer;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;


public class TweetDeserializer implements Deserializer<Tweet> {

    @Override
    public Tweet deserialize(String s, byte[] data) {
        if (data == null)
            return null;

        String jsonString = new String(data);
        //System.out.println("Tweet Deserialization :"  + jsonString);
        //Gson gson = new Gson();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new DateTimeDeserializer());
        gsonBuilder.registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeDeserializer());

        Gson gson = gsonBuilder.setPrettyPrinting().create();

        return gson.fromJson(jsonString, Tweet.class);
        //Gson gson = new GsonBuilder()
        //        .registerTypeAdapter(OffsetDateTime.class, (JsonDeserializer<OffsetDateTime>) (json, type, context) -> OffsetDateTime.parse(json.getAsString()))
        //        .create();
        //String jsonString = new String(data);
        //Tweet tweet;
        //tweet = gson.fromJson(gson.fromJson(jsonString, JsonObject.class).get("data"), Tweet.class);
        //org.json.JSONObject parsedJson = new JSONObject(jsonString);
        //System.out.println(parsedJson);
        //OffsetDateTime off  = OffsetDateTime.parse(parsedJson.get("createdAt").toString());

        //Tweet parsedTweet = new Tweet();
        //parsedTweet= new Tweet();
        //parsedTweet.setId(parsedJson.getString("id"));
        //parsedTweet.setCreatedAt(off);
        //parsedTweet.setText(parsedJson.getString("text"));
        //parsedTweet.setAuthorId(parsedJson.getString("authorId"));
        //parsedTweet.setConversationId(parsedJson.getString("conversationId"));
        //parsedTweet.setLang(parsedJson.getString("lang"));
    }
}
