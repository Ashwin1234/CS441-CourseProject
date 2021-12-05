package log.ingestor

class KeyLogger extends LoggerStreamlet[LogKey]("S3 Key received: {}", "key-in")
