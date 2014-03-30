package se.ramn.bottfarmen.engine


trait BotView {
  val id: Int
  val commanderId: Int
  def position: (Int, Int)
  val hitpoints: Int
}
