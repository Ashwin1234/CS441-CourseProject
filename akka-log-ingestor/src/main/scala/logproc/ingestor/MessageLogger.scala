package logproc.ingestor

import logproc.data._
// Streamlet for logging the Message
class MessageLogger extends LoggerStreamlet[LogFile]("Message received: {}", "message-in")
