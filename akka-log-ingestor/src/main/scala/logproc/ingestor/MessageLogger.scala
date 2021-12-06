package logproc.ingestor

import logproc.data._

class MessageLogger extends LoggerStreamlet[LogFile]("Message received: {}", "message-in")
