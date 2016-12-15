#Experimental Platform For Exactly-Once Samza
======================================
This project provides experimental platform for Apache Samza. There are basically two types of experiment included: failure test and performance test. For detailed information, please refer to capstone paper report *Evaluation of Exactly Once Delivery on Stream Processing*.

##Description of Contents
`./bin`: contains `grid` executable for platform installation, start, stop, etc.

`./conf`: contains configuration for YARN

`./gradle`: gradle wrapper for build

`./kafka_producer`: Kafka producer for failure test

`./modified_samza_target`: contains a complied exactly-once Samza jar, for convenience to build dist tar package.

`./src/main/assebly`: xml configuration for assembling package for Samza job

`./src/main/config`: property files for Samza job configuration

`./src/main/java/samza`: stream task codes

##Guidance
(Refer to **Hello Samza** project: <em>https://samza.apache.org/startup/hello-samza/latest/</em>)

###Start a Grid
* `bin/grid bootstrap`: This command will download, install, and start ZooKeeper, Kafka, and YARN. It will also check out the latest version of Samza and build it. All package files will be put in a sub-directory called “deploy” inside hello-samza’s root folder.

###Build Package
Use provided script to build both original and exactly-once samza (**these packages are what YARN uses to deploy your jobs on the grid**):

* `./build_package.sh`

Generated package tars are store in `./target` directory:

- Original

	* `samza-exactly-once-1.0-dist.tar.gz`             

- Exactly Once 
	* `samza-exactly-once-1.0-modified-dist.tar.gz`

Then, you can continue w/ the following command in the project:

```
mkdir -p deploy/samza
tar -xvf samza-exactly-once-1.0-dist.tar.gz -C deploy/samza
```

By extracting dist tar to deploy directory, we can run script command for Samza job easily.

###Failure test

1. **Samza Task**: After cluster is started, launch `word-dict-feed-*.properties` Samza tasks.

2. **Kafka Producer**: Use scripts in `./kafka_producer` to generate load of words.

3. **Kafka Topics**: `word-dict-input` and `word-dict-output`.


###Performance test

1. **Samza Task**: After cluster is started, launch `samza-filter`, `samza-identity`, `samza-project`, `samza-sample` Samza tasks;

2. **Load Generator**: Please refer to Chao's work (*https://github.com/ComboZhc/StreamBenchmark*). For our test, we mainly use `chao.cmu.capstone.ThroughputLoadGenerator` for tweet input generation.

3. **Kafka Topics**: 
    * `tweet-input`: input topic for all tasks
    * `samza-filter`, `samza-sample`, `samza-identity`, `samza-project`: output topic for our testing applications

4. **Metrics**:
    * Stream Task: consumes original `metrics` topic and output only `container-metric` for throughput monitoring
    	* `metrics-parsor`
    * Topics: container metrics after parsing for each application
    	* `metric-identity`
    	* `metric-project`
    	* `metric-sample`
    	* `metric-filter`

###Shutdown
`bin/grid stop all`

##Common command lines references
###Kafak

* list topic

```
./deploy/kafka/bin/kafka-topics.sh --list --zookeeper localhost:2181
```

* delete topic

```
./deploy/kafka/bin/kafka-topics.sh --zookeeper localhost:2181 --delete --topic <topic>
```

* consume to console

```
./deploy/kafka/bin/kafka-console-consumer.sh --topic <topic> --zookeeper localhost 2181 [--from-beginning]
```

* create topic

```
./deploy/kafka/bin/kafka-topics.sh --create --zookeeper localhost:2181 --topic <topic> --partition 1 \
 --replication-factor 1
```


###Samza
* deploy task

```
deploy/samza/bin/run-job.sh --config-factory=org.apache.samza.config.factories.PropertiesConfigFactory \
 --config-path=file://$PWD/deploy/samza/config/<job-name>.properties 
```

* list jobs

```
./deploy/samza/bin/list-yarn-job.sh
```

* kill job

```
./deploy/samza/bin/kill-yarn-job.sh <application-id>
```

###Overall

```
  $ grid
  $ grid bootstrap
  $ grid install [yarn|kafka|zookeeper|samza|all]
  $ grid start [yarn|kafka|zookeeper|all]
  $ grid stop [yarn|kafka|zookeeper|all]

```







