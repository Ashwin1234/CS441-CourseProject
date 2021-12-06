package logproc.ingestor

import logproc.data._

class KeyLogger extends LoggerStreamlet[LogKey]("S3 Key received: {}", "key-in")
