package se.ramn.bottfarmen.api


trait Bot {
  val id: Int
  def row: Int
  def col: Int
  def hitpoints: Int
  def enemiesInSight: java.util.Set[EnemyBot]
  def foodInSight: java.util.Set[Food]
}
