package tweetoscope;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KafkaVisualizor extends JFrame {
    protected final static String ROW_KEY = "hashtag";
    protected DefaultCategoryDataset dataset;
    protected int nbLeaders;

    private String bootstrapServers;

    Map<String, Long> HashTagMap = new HashMap<String, Long>();
    Map<String, Long> topHashtagsMap = new HashMap<String, Long>();

    public static void main(String[] args) {
        new KafkaVisualizor(5, args[0], args[1]);
    }

    private KafkaVisualizor(int nbLeaders, String bootstrapServers, String topicName) {
        this.bootstrapServers = bootstrapServers;
        this.nbLeaders = nbLeaders;

        dataset = new DefaultCategoryDataset();

        JFreeChart chart = ChartFactory.createBarChart(
                "Most Popular Hashtags", // title
                "", // category axis label
                "Number of occurences", // value axis label
                dataset, // category dataset
                PlotOrientation.HORIZONTAL, // orientation
                false, // legend
                true, // tooltips
                false); // urls
        chart.getCategoryPlot().setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        chart.getCategoryPlot().setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        chartPanel.setBackground(Color.white);
        chartPanel.setPreferredSize(new Dimension(500, 300));
        this.add(chartPanel);

        this.pack();
        this.setTitle(topicName);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);

        KafkaConsumer<String, Long> consumer = new KafkaConsumer<String, Long>(configurationKafkaConsumer());
        consumer.subscribe(Collections.singletonList(topicName));
        try {
            Duration timeout = Duration.ofMillis(1000);
            ConsumerRecords<String, Long> records = null;
            while (true) { // I'm a machine, I can work forever :-)
                records = consumer.poll(timeout);
                for (ConsumerRecord<String, Long> record : records) {
                    HashTagMap.put("#" + record.key(), record.value());
                    topHashtagsMap = HashTagMap.entrySet().stream()
                            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).limit(10)
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    System.out.println(record.key() + ":" + record.value());

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dataset.clear();
                            Stream<Map.Entry<String, Long>> sortedTopHashtags = topHashtagsMap.entrySet().stream()
                                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));
                            sortedTopHashtags.forEach(t -> {
                                dataset.setValue(t.getValue(), ROW_KEY, t.getKey().toString());
                            });
                            for (int i = HashTagMap.entrySet().size(); i < nbLeaders; i++) {
                                dataset.setValue(0, ROW_KEY, "");
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("somethingwentwrong." + e.getMessage());
        } finally {
            consumer.close();
        }
    }

    private Properties configurationKafkaConsumer() {
        Properties consumerProperties = new Properties();
        consumerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.LongDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "visualizer");
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return consumerProperties;
    }
}
