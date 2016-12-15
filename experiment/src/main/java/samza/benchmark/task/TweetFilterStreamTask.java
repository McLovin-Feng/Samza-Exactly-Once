package samza.benchmark.task;

import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.StreamTask;
import org.apache.samza.task.TaskCoordinator;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Only send English tweets.
 */
public class TweetFilterStreamTask implements StreamTask {
    private static final SystemStream OUTPUT_STREAM = new SystemStream("kafka", "samza-filter");

    @Override
    public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator) {
        JSONObject tweet = (JSONObject) JSONValue.parse((String) envelope.getMessage());
        if (tweet.get("lang").equals("en")) {
            collector.send(new OutgoingMessageEnvelope(OUTPUT_STREAM, envelope.getMessage()));
        }
    }
}
