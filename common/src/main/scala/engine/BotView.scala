package se.ramn.bottfarmen.simulation


trait BotView {
  val id: Int
  val commanderId: Int
  val row: Int
  val col: Int
  val hitpoints: Int
}
