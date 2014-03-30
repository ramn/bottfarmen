package se.ramn.bottfarmen.engine.impl

import collection.JavaConverters._

import se.ramn.bottfarmen.api.BotCommander
import se.ramn.bottfarmen.api.GameState
import se.ramn.bottfarmen.api.Bot
import se.ramn.bottfarmen.engine.BotCommanderArbiter
import se.ramn.bottfarmen.engine.RenderableBot
import se.ramn.bottfarmen.engine.Scenario


class BotCommanderArbiterImpl(
  commanders: Set[BotCommander],
  scenario: Scenario
) extends BotCommanderArbiter {

  var botsByCommanderId: Map[Int, Set[Bot]] = Map()
  val commanderToId = commanders.zipWithIndex.toMap

  initialSetup()

  override def doTurn: Unit = {
    val commandsByCommander = commanders.map { commander =>
      val commands = commander.update(gameStateFor(commander))
      (commander -> commands)
    }
    // TODO: evaluate commands ...
  }

  override def bots = {
    for {
      (cmdrId, bots) <- botsByCommanderId
      bot <- bots
    } yield new RenderableBot {
      val id = bot.id
      val commanderId = cmdrId
      val position = bot.position
    }
  }

  protected def initialSetup() = {
    require(commanders.size <= scenario.startingPositions.length)
    val startingPositions = scenario.startingPositions.iterator
    commanders foreach { commander =>
      val bot = new Bot {
        val id = 1
        val position = startingPositions.next
        def hitpoints = 100
      }
      setBotsFor(commanderToId(commander), Set(bot))
    }
  }

  protected def setBotsFor(commanderId: Int, bots: Set[Bot]) = {
    botsByCommanderId = botsByCommanderId.updated(commanderId, bots)
  }

  protected def gameStateFor(commander: BotCommander): GameState = {
    // TODO: build proper game state
    new GameState {
      def turn = 0
      def bots = botsByCommanderId(commanderToId(commander)).toList.asJava
    }
  }
}
