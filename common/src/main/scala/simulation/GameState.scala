package se.ramn.bottfarmen.simulation

import collection.JavaConverters._

import se.ramn.bottfarmen.api
import se.ramn.bottfarmen.simulation.entity.BotCommander
import se.ramn.bottfarmen.simulation.entity.Position


class GameStateApiGateway(
  commanders: Set[BotCommander],
  scenario: Scenario
) {
  def forCommander(
    commander: BotCommander,
    turnNo: Int,
    spawnedFood: Set[Position]
  ): api.GameState = {
    val immutableBots: Seq[api.Bot] = commander.bots.toList.map { bot =>
      val otherCommanders = commanders.filterNot(_ == commander)
      val visibleTiles = Geography.positionsWithinRange(bot.position, range=8)
      val visibleEnemyBots = for {
        commander <- otherCommanders
        bot <- commander.bots
        if visibleTiles(bot.position)
      } yield new api.EnemyBot {
        val commanderId = commander.id
        val id = bot.id
        val row = bot.row
        val col = bot.col
        val hitpoints = bot.hitpoints
      }
      val visibleFood: Set[api.Food] =
        spawnedFood.filter(visibleTiles).map{ pos =>
          new api.Food {
            val row = pos.row
            val col = pos.col
          }
        }
      new api.Bot {
        val id = bot.id
        val row = bot.row
        val col = bot.col
        val hitpoints = bot.hitpoints
        val enemiesInSight = visibleEnemyBots.toList.asJava
        override val foodInSight = visibleFood.asJava
      }
    }
    new api.GameState {
      val turn = turnNo
      val bots = immutableBots.asJava
      val terrain = scenario.tilemap.rows.map(_.mkString).mkString("\n")
      val rowCount = scenario.tilemap.rowCount
      val colCount = scenario.tilemap.colCount
      val homeBase = apiBaseFrom(commander)
    }
  }

  protected def apiBaseFrom(commander: BotCommander): api.Base =
    new api.Base {
      val hitpoints = commander.homeBase.hitpoints
      val row = commander.homeBase.row
      val col = commander.homeBase.col
    }
}
