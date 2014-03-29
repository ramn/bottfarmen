package se.ramn.bottfarmen.api


trait GameState {
  def turn: Int
  def bots: java.util.List[Bot]
}
