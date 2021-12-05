package logproc.ingestor

import logproc.data._

class StatLogger extends LoggerStreamlet[LogStats]("Stats: {}", "stats-in")