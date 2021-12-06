package logproc.ingestor

import logproc.data._
// Streamlet for logging the key
class KeyLogger extends LoggerStreamlet[LogKey]("S3 Key received: {}", "key-in")
