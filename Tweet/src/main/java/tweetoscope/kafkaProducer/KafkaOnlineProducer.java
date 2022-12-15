package tweetoscope.kafkaProducer;

public class KafkaOnlineProducer {
    protected static String BEARER_TOKEN;

    KafkaOnlineProducer() {
        // gets the Twitter access token from environment variables
        BEARER_TOKEN = System.getenv("BEARER_TOKEN");
        
        if (BEARER_TOKEN == null) {
            System.err.println("There was a problem getting your bearer token."
                    + " Please make sure you set the BEARER_TOKEN environment variable");
            System.exit(-1);
        }
    }
}
