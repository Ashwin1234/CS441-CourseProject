package logproc.ingestor

import logproc.data._

class MessageLogger extends LoggerStreamlet[WholeMessage]("Message received: {}", "message-in")
