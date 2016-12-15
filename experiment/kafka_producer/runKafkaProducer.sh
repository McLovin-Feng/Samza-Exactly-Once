#!/usr/bin/env bash

cd ./kafka_producer/
rm *.class
echo '-------------- cleaned up -------------'
echo '---------- start compilation ----------'
javac -cp "./*:../deploy/kafka/libs/*" WordKafkaProducer.java
echo '------------ start running ------------'
java -cp "./*:../deploy/kafka/libs/*:." WordKafkaProducer "$@"
