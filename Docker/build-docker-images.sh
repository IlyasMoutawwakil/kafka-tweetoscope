#!/bin/sh
# build all docker images

docker build -t kafka-stream -f ./Docker/Dockerfile.KafkaStream .

docker build -t kafka-filter -f ./Docker/Dockerfile.KafkaFilter .

docker build -t kafka-visualizer -f ./Docker/Dockerfile.KafkaVisualizer .