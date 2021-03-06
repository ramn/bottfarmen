package se.ramn.bottfarmen


package object util {
  implicit class Times(limit: Int) {
    def timesDo(thunk: => Unit) = {
      require(limit >= 0)
      for (i <- limit to 1 by -1) {
        thunk
      }
    }
  }

  def loadTextFileFromClassPath(path: String): String = {
    io.Source.fromInputStream(getClass.getResourceAsStream(path)).mkString
  }
}
