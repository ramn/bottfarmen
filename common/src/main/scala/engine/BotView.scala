package se.ramn.bottfarmen.engine


trait BotView {
  val id: Int
  val commanderId: Int
  val row: Int
  val col: Int
  val hitpoints: Int
}
