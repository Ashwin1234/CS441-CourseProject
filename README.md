# Course Project
### The goal of this course project is to gain experience with creating a streaming data pipeline with cloud computing technologies by designing and implementing an actor-model service using [Akka](https://akka.io/) that ingests logfile generated data in real time and delivers it via an event-based service called [Kafka](https://kafka.apache.org/) to [Spark](https://spark.apache.org/) for further processing. This is a group project with each group consisting of one to six students. No student can participate in more than one group.
### Grade: 20%

## Team Members
+ S
+ T
+ A
+ R

## Development Environment
+ Language : Scala v
+ IDE : Intellij IDEA Community Edition 
+ Build Tool : SBT 1.5.2
+ Frameworks Used : CloudFlow,Akka,Spark, Kafka
+ Deployment : AWS EKS, AWS Lambda

## Installatins
+ Install and build [cloudflow](https://cloudflow.io/docs/dev/administration/installing-cloudflow.html)
+ Installing [Kafka and Strimzi](https://cloudflow.io/docs/dev/administration/how-to-install-and-use-strimzi.html)
+ Adding [Spark support](https://cloudflow.io/docs/dev/administration/installing-spark-operator.html)
+ Install and setup [docker](https://docs.docker.com/get-docker/)

## Deployment
+ Create a docker repo
```
ThisBuild / cloudflowDockerRegistry := Some("docker.io")
ThisBuild / cloudflowDockerRepository := Some("<your docker hub username>")
  ```

+ Build and publish the app using 
```
sbt buildApp
```

+ To deploy the applications to kubernetes cluster 
```
$ kubectl cloudflow deploy /path/to/CS441-CourseProject/target/CS441-CourseProject.json --no-registry-credentials
```
+ After executing these commands you can see the streamlets running in different pods.

## Project Structure
+ Logprocessor - Write the logs to s3.


[Introduction](./INTRODUCTION.md)
[Implementation](./IMPLEMENTATION.md)

## Running the application
+ Run the cron job to add files to s3. After files have been added to s3 you should see the following output

## Tests








