# Tweetoscope Project 
# GROUP : 8
# By : Hao Wang, Youness Laklouch & Ilyas Moutawwakil

## Launching with Docker Compose

To lanch our Tweetoscope app with `docker compose`, make sure zookeeper and kafka aren't running in your machine, then :

1. Save your `BEARER_TOKEN` in `Docker-Compose/env.txt` file.

2. Run the first docker-compose file which starts **zookeeper** and a **kafka** broker with topics `Tweets` and `Filtered-Tweets`:
```
docker compose -f Docker-Compose/docker-compose-middleware.yml up
```

3. Run the second docker-compose file which starts the tweets stream (sampled) app, the tweets (by language) filter app and the hashtags (top 5) visualization app :
```
docker compose -f Docker-Compose/docker-compose-services.yml up
```

4. Open your browser and go to `http://localhost:5800` where you will have access to the visualization app. if it's run on a remote machine, `localhost` should be replaced with its `ip`.

## Launching with Kubernetes
To lanch our Tweetoscope app with `kubernetes`, make sure zookeeper and kafka aren't running in your machine, then :
Start `minikube` and wait 
```
minikube start
```
```
kubectl apply -f Kubernetes/zookeeper-and-kafka.yml
```
Before runnig the following command , make sure that you have your `BEARER_TOKEN` in your environment variables
```
kubectl create secret generic btoken --from-literal=BEARER_TOKEN=$BEARER_TOKEN
```
```
 kubectl apply -f Kubernetes/tweetdeploy.yml 
``` 
```
kubectl get pods
```
```
kubectl port-forward visualizer-deployment-xxxxxxxxxx-xxxxx 5800:5800
```
Open a Browser and type : `http://127.0.0.1:5800`


## Launching with InterCell
Change john and cpusdi1_x by your account and intercell node
```
scp zookeeper-and-kafka_cpusdi1_xx.yml john@home.metz.supelec.fr:
scp tweetodeploy_cpusdi1_xx.yml john@home.metz.supelec.fr:
ssh john@home.metz.supelec.fr
```
```
scp zookeeper-and-kafka_cpusdi1_xx.yml cpusdi1_x@icx:
scp tweetodeploy_cpusdi1_xx.yml cpusdi1_x@icx:
ssh cpusdi1_x@icx
```
```
kubectl apply -f zookeeper-and-kafka_cpusdi1_xx.yml
```
```
kubectl create secret generic btoken --from-literal=BEARER_TOKEN=$BEARER_TOKEN
```
```
kubectl apply -f tweetodeploy_cpusdi1_xx.yml
kubectl get pods
```


## Read our report
Our report can be accessed [here](https://2019wangh.pages-student.centralesupelec.fr/tweetoscope22_group-8_haowang_younesslaklouch_ilyasmoutawwakil/Report/report.pdf).
The test coverage report can be accessed [here](https://2019wangh.pages-student.centralesupelec.fr/tweetoscope22_group-8_haowang_younesslaklouch_ilyasmoutawwakil/testCoverageReport/index.html)
