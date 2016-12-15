

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.String;
import java.lang.System;
import java.lang.Thread;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Created by fengbo on 10/01/16.
 */
public class WordKafkaProducer {

    private final static String URL_STR = "https://raw.githubusercontent.com/dwyl/english-words/master/words.txt";
    private final static String OUT_TOPIC = "word-dict-input";
    private final static String BROKERS = "localhost:9092";

    private static void init_cmd(String[] args, Options options) {
        options.addOption("help", "display usage.");
        options.addOption(OptionBuilder.withDescription("how many words sent in one batch")
               .hasArg()
               .withArgName("SIZE")
               .withLongOpt("batch_size")
               .create());
        options.addOption(OptionBuilder.withDescription("start from which word to produce")
               .hasArg()
               .withArgName("word")
               .withLongOpt("start_word")
               .create());
        options.addOption(OptionBuilder.withDescription("period value to send a batch of words")
               .hasArg()
               .withArgName("MILLISEC")
               .withLongOpt("period")
               .create());
    }


    public static void main(String[] args) throws Exception {

        // create the Options
        Options options = new Options();
        init_cmd(args, options);

        // create the commandline parser
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "WordKafkaProducer", options);
            return;
        }

        String startWord = cmd.hasOption("start_word") ? cmd.getOptionValue("start_word") : "a";
        int batch_size = cmd.hasOption("batch_size") ? Integer.parseInt(cmd.getOptionValue("batch_size")) : 1;


        System.out.println("startWord: " + startWord);
        System.out.println("batch size: " + batch_size);


        /* Load webpage */
        List<String> word_dict = new ArrayList<>();
        ReadUrl(word_dict);

        int start_idx = searchList(word_dict, startWord);
        if (start_idx < 0) {
            System.err.println("invalid word!");
            System.exit(1);
        }

        /* create producer */
        Producer<String, String> producer = getProducer();

        if (cmd.hasOption("period")) {
            long period = Long.parseLong(cmd.getOptionValue("period"));
            System.out.println("period: " + period + " ms");

            while (start_idx < word_dict.size()) {
                int end_idx = Math.min(start_idx + batch_size, word_dict.size());
                for (int i = start_idx; i < end_idx; ++i) {
                    sendWord(word_dict.get(i), producer);
                    System.out.println("send: " + word_dict.get(i));
                }
                start_idx = end_idx;

                Thread.sleep(period);
            }
            System.out.println("Consumed All Words!");
        } else {
            int end_idx = Math.min(start_idx + batch_size, word_dict.size());
            sendRangeToKafka(word_dict, start_idx, end_idx, producer);
            if (end_idx < word_dict.size()) {
                System.out.println("Next word should be: " + word_dict.get(end_idx));
            } else {
                System.out.println("Consumed All Words!");
            }
        }

        producer.close();
    }

    private static void sendWord(String word, Producer<String, String> producer) {
        String msg = word + "\t" + (new Date());
        ProducerRecord<String, String> record = new ProducerRecord<>(OUT_TOPIC, msg);
        producer.send(record);
    }

    /**
     * Send [start, end) range in word_dict to kafka broker @topic "word-dict".
     * @param word_dict
     * @param start_idx
     * @param end_idx
     * @param producer
     */
    public static void sendRangeToKafka(List<String> word_dict, int start_idx, int end_idx, Producer<String, String> producer) {
        /* send records */
        int idx = start_idx;
        while (idx < end_idx) {
            sendWord(word_dict.get(idx), producer);
            idx++;
        }
    }

    private static Producer<String, String> getProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", BROKERS);
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("acks", "all");
        props.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");

        return new KafkaProducer<String, String>(props);
    }

    /**
     * Search index of current word.
     * @param word_dict
     * @param target
     * @return
     */
    public static int searchList(List<String> word_dict, String target) {
        for (int i = 0; i < word_dict.size(); i++) {
            if (word_dict.get(i).equals(target)) {
                System.out.println("Get index: " + i);
                return i;
            }
        }
        return -1;
    }

    /**
     * Utility function to read a webpage of words to list.
     * @param word_dict
     * @throws Exception
     */
    public static void ReadUrl (List<String> word_dict) throws Exception {
        URL url = new URL(URL_STR);
        HttpURLConnection urlcon = (HttpURLConnection)url.openConnection();
        urlcon.setRequestMethod("GET");
        urlcon.connect();
        InputStream is = urlcon.getInputStream();
        BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while((line = buffer.readLine()) != null){
            word_dict.add(line);
        }
    }
}
