package se.ramn.bottfarmen.simulation

import collection.immutable.Iterable
import collection.immutable.Seq
import collection.immutable.IndexedSeq
import util.Random

import se.ramn.bottfarmen.simulation.entity.Position


class FoodSpawner(scenario: Scenario) {
  lazy val foodMaxCount = scenario.maxFoodTilesCount
  val spawnIntervalTurns = 40

  private var mySpawnedFood = Set.empty[Position]

  def spawnedFood: Set[Position] = mySpawnedFood

  def positionHasFood(position: Position): Boolean = mySpawnedFood(position)

  def consumeFood(position: Position): Unit = {
    mySpawnedFood -= position
  }

  def update(tilemap: TileMap, turnNo: Int) {
    val shouldSpawn = turnNo % spawnIntervalTurns == 1
    val foodsToSpawn =
      (foodMaxCount - mySpawnedFood.size) % (foodMaxCount + 1)
    if (shouldSpawn && foodsToSpawn > 0) {
      val positionIterator = buildPositionIterator(tilemap)
      val selectedFoodPositions = positionIterator.take(foodsToSpawn)
      mySpawnedFood ++= selectedFoodPositions
    }
  }

  protected def buildPositionIterator(tilemap: TileMap) = {
    def makeRandomPos =
      Position(
        row=Random.nextInt(tilemap.rowCount),
        col=Random.nextInt(tilemap.colCount))
    val posIter = Iterator.continually(makeRandomPos)
    def isLand(position: Position) = {
      // Excludes starting positions
      tilemap.tile(position).exists(_ == Tiles.Land)
    }
    posIter
      .filter(isLand)
      .filterNot(mySpawnedFood)
  }
}
