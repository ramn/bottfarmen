package se.ramn.bottfarmen.simulation

import collection.JavaConverters._

import se.ramn.bottfarmen.api
import se.ramn.bottfarmen.simulation.entity.BotCommander


class GameStateApiGateway(
  commanders: Set[BotCommander],
  scenario: Scenario
) {
  def forCommander(commander: BotCommander): api.GameState = {
    val immutableBots: Seq[api.Bot] = commander.bots.toList.map { bot =>
      val otherCommanders = commanders.filterNot(_ == commander)
      val visibleTiles = Geography.positionsWithinRange(bot.position, range=5)
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
      new api.Bot {
        val id = bot.id
        val row = bot.row
        val col = bot.col
        val hitpoints = bot.hitpoints
        val enemiesInSight = visibleEnemyBots.toList.asJava
      }
    }
    new api.GameState {
      val turn = 0
      val bots = immutableBots.asJava
      val terrain = scenario.map.rows.map(_.mkString).mkString("\n")
      val rowCount = scenario.map.rowCount
      val colCount = scenario.map.colCount
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
