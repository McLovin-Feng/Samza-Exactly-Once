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
 * Parse tweet input and send text body as output.
 */
public class TweetProjectStreamTask implements StreamTask {
    private static final SystemStream OUTPUT_STREAM = new SystemStream("kafka", "samza-project");

    @Override
    public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator) {
        JSONObject tweet = (JSONObject) JSONValue.parse((String) envelope.getMessage());
        String text = (String) tweet.get("text");
        collector.send(new OutgoingMessageEnvelope(OUTPUT_STREAM, text));
    }
}
