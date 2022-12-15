#!/bin/sh

# tag all images
docker tag kafka-stream ilyasodysseus/kafka-strea
docker tag kafka-filter ilyasodysseus/kafka-filter
docker tag kafka-visualizer ilyasodysseus/kafka-visualizer

# publish all images
docker push ilyasodysseus/kafka-stream
docker push ilyasodysseus/kafka-filter
docker push ilyasodysseus/kafka-visualizer