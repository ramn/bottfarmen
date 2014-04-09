package se.ramn.bottfarmen.simulation

import collection.immutable.Iterable

import se.ramn.bottfarmen.api
import se.ramn.bottfarmen.simulation.impl.SimulationImpl
import se.ramn.bottfarmen.simulation.entity.Bot
import se.ramn.bottfarmen.simulation.entity.Base
import se.ramn.bottfarmen.simulation.entity.BotCommander


trait Simulation {
  def doTurn: Unit
  def botCommanders: Iterable[BotCommanderView]
  def bots: Iterable[BotView]
}


object Simulation {
  def apply(
    playerCommanders: Set[api.BotCommander],
    scenario: Scenario
  ): Simulation = {
    val commanders = buildCommanders(playerCommanders, scenario)
    new SimulationImpl(commanders, scenario)
  }

  protected def buildCommanders(
    playerCommanders: Set[api.BotCommander],
    scenario: Scenario
  ): Set[BotCommander] = {
    require(playerCommanders.size <= scenario.map.startingPositions.length)
    val commanderId = playerCommanders.zipWithIndex.toMap
    val startingPositions = scenario.map.startingPositions.iterator
    playerCommanders map { playerCommander =>
      val pos = startingPositions.next
      val commander = BotCommander(
        playerCommander,
        id=commanderId(playerCommander),
        homeBase=new Base(hitpoints=800, row=pos.row, col=pos.col)
      )
      val bot = new Bot(1, commander) {
        var row = pos.row
        var col = pos.col
        var hitpoints = 100
      }
      commander.bots = Set(bot)
      commander
    }
  }
}
