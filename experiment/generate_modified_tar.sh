#!/bin/bash
#
# This script generate samza package that uses exactly-once Samza core jar and 
# put it in the "target" directory.
# 
DIR=$(pwd)
mkdir $DIR/samza-exactly-once-1.0-modified-dist
echo 'decompress...'
tar -xzf $DIR/target/samza-exactly-once-1.0-dist.tar.gz -C $DIR/samza-exactly-once-1.0-modified-dist/
echo 'copy...'
cp $DIR/modified_samza_target/samza-core_2.10-0.11.1-SNAPSHOT.jar $DIR/samza-exactly-once-1.0-modified-dist/lib/
echo 're-compress...'
cd $DIR/samza-exactly-once-1.0-modified-dist && \
    tar -czf $DIR/target/samza-exactly-once-1.0-modified-dist.tar.gz ./*
rm -r $DIR/samza-exactly-once-1.0-modified-dist/
