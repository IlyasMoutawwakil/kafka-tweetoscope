package tweetoscope.tweetsFilter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.twitter.clientlib.model.Tweet;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TextLengthFilterTest {


    String miniFileName  = "TestBases/miniTestBase.txt";
    String largeFileName = "TestBases/largeTestBase.txt";
    public  String fileReader(){
        File file=new File(miniFileName);
        String content= null;
        try {
            content = FileUtils.readFileToString(file,"UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return content ;
    }

    public Map<String,String> jsonParse(JsonObject nonParsedJson){
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



    @Test
    public void match() {
        TextLengthFilter filter=new TextLengthFilter(200);
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

        Vector<Tweet> filteredTweet = new Vector<Tweet>();
        for(int i = 0;i<tweets.length;i++){
            if(tweets[i].getText().length()<=filter.length){
                filteredTweet.add(tweets[i]);
            }
        }


        boolean flag = true;
        for(int i = 0;i<filteredTweet.size();i++){
            if(filteredTweet.get(i).getText().length()>filter.length){
                flag = false;
            }
        }
        assertTrue(flag);
    }
}