package logproc.ingestor

import logproc.data._
// Streamlet for logging the Log stats
class StatLogger extends LoggerStreamlet[LogStats]("Stat received: {}", "stats-in")