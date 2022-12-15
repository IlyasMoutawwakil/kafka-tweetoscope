package tweetoscope;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.twitter.clientlib.model.Tweet;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class TweetSerializer implements Serializer<Tweet>{
    @Override
    public byte[] serialize(String s, Tweet tweet) {
        if (tweet == null)
            return null;

        //org.json.JSONObject obj = new org.json.JSONObject(tweet);
        //String json = obj.toString();
        //return json.getBytes();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new DateTimeSerializer());
        gsonBuilder.registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeSerializer());
        Gson gson = gsonBuilder.setPrettyPrinting().create();

        String jsonString = gson.toJson(tweet);
        return jsonString.getBytes();

    }
}
