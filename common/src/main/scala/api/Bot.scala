package se.ramn.bottfarmen.api


trait Bot {
  val id: Int
  def row: Int
  def col: Int
  def hitpoints: Int
  def enemiesInSight: java.util.List[EnemyBot]
}

trait EnemyBot {
  val commanderId: Int
  val id: Int
  val row: Int
  val col: Int
  val hitpoints: Int
}
