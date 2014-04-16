package se.ramn.bottfarmen.example.baseraider


case class Position(row: Int, col: Int) {
  def neighbours: Set[Position] =
    Set(
      copy(row=row + 1),
      copy(row=row - 1),
      copy(col=col + 1),
      copy(col=col - 1))
}
