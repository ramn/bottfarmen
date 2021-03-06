package se.ramn.bottfarmen.api


trait GameState {
  def turn: Int
  def bots: java.util.List[Bot]
  def terrain: String
  def rowCount: Int
  def colCount: Int
  def homeBase: Base
  def botVisibilityRange: Int
}
