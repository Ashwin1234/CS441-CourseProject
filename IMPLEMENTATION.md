#Implementation
The overall architecture of our model is given by the image below
![architecture](./images/architecture.drawio).
We have written a cronjob to add files to s3 bucket every 2 minutes. This triggers an AWS Lambda function which sends the key of the updated file to an akka streamlet which performs some file processing on "WARN" and "ERROR" messages. This contents are transferred to kafka streams which are accessed by the spark streamlet which performs some aggregations on these messages and sends the outputs to clients using AWS email service.  

##Output



