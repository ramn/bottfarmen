package se.ramn.bottfarmen.util


object Timer extends Logging {
  def time[T](message: String, thunk: => T): T = {
    val start = System.nanoTime
    val result = thunk
    val duration = (System.nanoTime - start) / 1E6
    logger.debug(s"$message, duration: $duration ms")
    result
  }
}
