package se.ramn.bottfarmen.engine.impl

import collection.JavaConverters._
import collection.immutable.Iterable

import se.ramn.bottfarmen.api.BotCommander
import se.ramn.bottfarmen.api.GameState
import se.ramn.bottfarmen.api.Bot
import se.ramn.bottfarmen.engine.BotCommanderArbiter


class BotCommanderArbiterImpl(commanders: Set[BotCommander])
  extends BotCommanderArbiter {

  var botsByCommanderId: Map[Int, Set[Bot]] = Map()
  val commanderToId = commanders.zipWithIndex.toMap

  initialSetup()

  override def doTurn: Unit = {
    val commandsByCommander = commanders.map { commander =>
      val commands = commander.update(gameStateFor(commander))
      (commander -> commands)
    }
    // evaluate commands ...
  }

  override def bots = {
    botsByCommanderId.values.toList.flatten
  }

  protected def initialSetup() = {
    commanders foreach { commander =>
      val bot = new Bot {
        val id = 1
        def position = (50, 50)
        def hitpoints = 100
      }
      setBotsFor(commanderToId(commander), Set(bot))
    }
  }

  protected def setBotsFor(commanderId: Int, bots: Set[Bot]) = {
    botsByCommanderId = botsByCommanderId.updated(commanderId, bots)
  }

  protected def gameStateFor(commander: BotCommander): GameState = {
    new GameState {
      def turn = 0
      def bots = botsByCommanderId(commanderToId(commander)).toList.asJava
    }
  }
}
