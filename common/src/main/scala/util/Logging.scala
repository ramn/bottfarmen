package se.ramn.bottfarmen.util


trait Logging {
  lazy val logger: Logger = new SimpleLogger
}


trait Logger {
  def error(message: => String): Unit
  def warn(message: => String): Unit
  def info(message: => String): Unit
  def debug(message: => String): Unit
}


object Level extends Enumeration {
  type LogLevel = Value
  val Trace, Debug, Info, Warning, Error, Fatal = Value
}


class SimpleLogger extends Logger {
  val logLevelFromEnvOpt =
    sys.env.get("LOG_LEVEL").map(_.capitalize).map(Level.withName)
  var logLevel: Level.LogLevel = logLevelFromEnvOpt.getOrElse(Level.Info)

  override def error(message: => String): Unit = log(Level.Error, message)
  override def warn(message: => String): Unit = log(Level.Warning, message)
  override def info(message: => String): Unit = log(Level.Info, message)
  override def debug(message: => String): Unit = log(Level.Debug, message)

  def log(messageLevel: Level.LogLevel, message: => String): Unit = {
    if (logLevel <= messageLevel) {
      write(message)
    }
  }

  def write(message: => String): Unit = {
    val logOutputStream = System.err
    //val logOutputStream = System.out
    logOutputStream.println(message)
  }
}
