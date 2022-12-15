package tweetoscope;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

class DateTimeSerializer implements JsonSerializer < LocalDateTime > {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d::MMM::uuuu HH::mm::ss");

    @Override
    public JsonElement serialize(LocalDateTime localDateTime, Type srcType, JsonSerializationContext context) {
    	//System.out.println("DateTimeSeralizer");
        return new JsonPrimitive(formatter.format(localDateTime));
    }


}

class OffsetDateTimeSerializer implements JsonSerializer < OffsetDateTime > {
   // private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d::MMM::uuuu HH::mm::ss");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    public JsonElement serialize(OffsetDateTime offsetDateTime, Type srcType, JsonSerializationContext context) {
    	//System.out.println("OffsetDateTimeSeralizer");
        return new JsonPrimitive(formatter.format(offsetDateTime));
    }


}