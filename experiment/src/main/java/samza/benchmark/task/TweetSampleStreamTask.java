package samza.benchmark.task;

import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.StreamTask;
import org.apache.samza.task.TaskCoordinator;

/**
 * This task is very simple. All it does is take messages that it receives, and
 * sends them according to given probability to a Kafka topic called samza-sample.
 */
public class TweetSampleStreamTask implements StreamTask {
    private static final SystemStream OUTPUT_STREAM = new SystemStream("kafka", "samza-sample");
    private static int counter = 0;

    @Override
    public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator) {
        if (counter++ % 10 == 0) {
            collector.send(new OutgoingMessageEnvelope(OUTPUT_STREAM, envelope.getMessage()));
        }
        counter %= 10;
    }
}
