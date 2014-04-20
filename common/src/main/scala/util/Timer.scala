package se.ramn.bottfarmen.util


object Timer extends Logging {
  def time[T](message: String, thunk: => T): T = {
    val start = System.nanoTime
    val result = thunk
    val durationMs = (System.nanoTime - start) / 1E6
    logger.debug(s"$message, duration: $durationMs ms")
    result
  }
}
