package tweetoscope;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

class DateTimeDeserializer implements JsonDeserializer < LocalDateTime > {
    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
    throws JsonParseException {
    	//System.out.println("DateTimeDeSeralizer");
        return LocalDateTime.parse(json.getAsString(),
            DateTimeFormatter.ofPattern("dd::MMM::uuuu HH::mm::ss").withLocale(Locale.FRENCH));
    }
}

class OffsetDateTimeDeserializer implements JsonDeserializer < OffsetDateTime > {
    @Override
    public OffsetDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    	//System.out.println("OffsetDateTimeDeSeralizer");

    	return OffsetDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    	
    	//return OffsetDateTime.parse(json.getAsString(),
        //    DateTimeFormatter.ofPattern("dd::MMM::uuuu HH::mm::ss").withLocale(Locale.FRENCH));
    }
}