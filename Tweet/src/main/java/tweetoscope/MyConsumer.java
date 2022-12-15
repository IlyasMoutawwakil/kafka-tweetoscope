package tweetoscope;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import com.twitter.clientlib.model.Tweet;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class MyConsumer {

    public static void main(String[] args) {
        new MyConsumer();
    }

    MyConsumer() {
        // records handled by this consumer will have no key, and a Product value
        KafkaConsumer<Void, Tweet> kafkaConsumer;
        // create the Kafka consumer with the appropriate configuration
        kafkaConsumer = new KafkaConsumer<Void, Tweet>(configureKafkaConsumer());
        // subscribes to the 'test' topic
        kafkaConsumer.subscribe(Collections.singletonList("tweetoscope"));
        // reads from the topic
        //try {
            Duration timeout = Duration.ofMillis(1000);
            ConsumerRecords<Void, Tweet> records = null;
            for(int i=0;i<20;i++) { // I'm a machine, I can work forever :-)
                records = kafkaConsumer.poll(timeout);
                for (ConsumerRecord<Void,Tweet> record : records)
                    System.out.println(record.value().getLang());
            }
        //} catch (Exception e) {
        //     System.err.println("something went wrong... " + e.getMessage());
        //} finally {
            kafkaConsumer.close();
        //}
    }

    private Properties configureKafkaConsumer() {
        Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.VoidDeserializer");
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                //"org.apache.kafka.common.serialization.StringDeserializer");
                TweetDeserializer.class);
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // from beginning
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "myOwnPrivateJavaGroup");
        return consumerProperties;
    }
}
