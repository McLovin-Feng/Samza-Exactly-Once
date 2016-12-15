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
 * This stream task aims to consume Metric topic and parse it to get container-metrics only.
 */
public class MetricParsorStreamTask implements StreamTask {

    private static final SystemStream OUTPUT_STREAM_IDENTITY = new SystemStream("kafka", "metric-identity");
    private static final SystemStream OUTPUT_STREAM_IDENTITY_EO = new SystemStream("kafka", "metric-identity-exactly-once");

    private static final SystemStream OUTPUT_STREAM_SAMPLE = new SystemStream("kafka", "metric-sample");
    private static final SystemStream OUTPUT_STREAM_SAMPLE_EO = new SystemStream("kafka", "metric-sample-exactly-once");

    private static final SystemStream OUTPUT_STREAM_FILTER = new SystemStream("kafka", "metric-filter");
    private static final SystemStream OUTPUT_STREAM_FILTER_EO = new SystemStream("kafka", "metric-filter-exactly-once");

    private static final SystemStream OUTPUT_STREAM_PROJECT = new SystemStream("kafka", "metric-project");
    private static final SystemStream OUTPUT_STREAM_PROJECT_EO = new SystemStream("kafka", "metric-project-exactly-once");

    @SuppressWarnings("unchecked")
    @Override
    public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator) {
        JSONObject message = (JSONObject) JSONValue.parse((String) envelope.getMessage());
        JSONObject header = (JSONObject) message.get("header");
        JSONObject metrics = (JSONObject)  message.get("metrics");

        // filter out non-container metrics
        if (!metrics.containsKey("org.apache.samza.container.SamzaContainerMetrics")) {
            return;
        }

        JSONObject container_metrics = (JSONObject) metrics.get("org.apache.samza.container.SamzaContainerMetrics");

        JSONObject output_msg = new JSONObject();
        output_msg.put("header", header);
        output_msg.put("container-metrics", container_metrics);

        // choose output topic according to job name
        String jobname = (String) header.get("job-name");
        SystemStream out_stream;
        switch (jobname) {
            case "samza-identity":
                out_stream = OUTPUT_STREAM_IDENTITY;
                break;
            case "samza-identity-exactly-once":
                out_stream = OUTPUT_STREAM_IDENTITY_EO;
                break;
            case "samza-sample":
                out_stream = OUTPUT_STREAM_SAMPLE;
                break;
            case "samza-sample-exactly-once":
                out_stream = OUTPUT_STREAM_SAMPLE_EO;
                break;
            case "samza-filter":
                out_stream = OUTPUT_STREAM_FILTER;
                break;
            case "samza-filter-exactly-once":
                out_stream = OUTPUT_STREAM_FILTER_EO;
                break;
            case "samza-project":
                out_stream = OUTPUT_STREAM_PROJECT;
                break;
            case "samza-project-exactly-once":
                out_stream = OUTPUT_STREAM_PROJECT_EO;
                break;
            default:
                System.err.println("ERROR: illegal job name: " + jobname);
                return;
        }

        collector.send(new OutgoingMessageEnvelope(out_stream, output_msg.toJSONString()));
    }

}
