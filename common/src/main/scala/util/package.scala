package se.ramn.bottfarmen


package object util {
  implicit class Times(limit: Int) {
    def times(thunk: => Unit) = {
      require(limit >= 0)
      for (i <- limit to 1 by -1) {
        thunk
      }
    }
  }
}
