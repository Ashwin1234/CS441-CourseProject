package log.ingestor

class MessageLogger extends LoggerStreamlet[LogMessage]("Message Read received: {}", "message-in")
