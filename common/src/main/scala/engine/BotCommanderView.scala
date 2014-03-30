package se.ramn.bottfarmen.engine


trait BotCommanderView {
  val id: Int
  val name: String
  val bots: Iterable[BotView]
}
