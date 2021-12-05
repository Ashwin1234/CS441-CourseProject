package logproc.ingestor

import logproc.data._

class MessageLogger extends LoggerStreamlet[LogMessage]("Message Read received: {}", "message-in")
