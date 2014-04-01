package se.ramn.bottfarmen.api


trait EnemyBot {
  val commanderId: Int
  val id: Int
  val row: Int
  val col: Int
  val hitpoints: Int
}
