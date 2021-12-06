package logproc.ingestor

import logproc.data._

class StatLogger extends LoggerStreamlet[LogStats]("Stat received: {}", "stats-in")