package se.ramn.bottfarmen.engine


trait Scenario {
  def mapRows: Int
  def mapCols: Int
  def startingPositions: Seq[(Int, Int)]
}
