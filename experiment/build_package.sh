#!/usr/bin/env bash
mkdir -p deploy/samza
mvn clean package && \
echo '-------- generate exactly-once dist tar --------'
./generate_modified_tar.sh && \
echo '-------- put dist into deploy directory --------'
tar -xzf target/samza-exactly-once-1.0-dist.tar.gz -C deploy/samza && \
echo 'SUCCESS'
