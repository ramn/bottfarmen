package se.ramn.bottfarmen.simulation

import collection.immutable.Iterable

import se.ramn.bottfarmen.api
import se.ramn.bottfarmen.simulation.entity.Bot
import se.ramn.bottfarmen.simulation.entity.Base
import se.ramn.bottfarmen.simulation.entity.BotCommander
import se.ramn.bottfarmen.simulation.entity.Position
import se.ramn.bottfarmen.simulation.view.BotCommanderView
import se.ramn.bottfarmen.simulation.view.BotView


trait Simulation {
  def doTurn: Unit
  def botCommanders: Iterable[BotCommanderView]
  def bots: Iterable[BotView]
  def turnNo: Int
  def isGameOver: Boolean
  def victor: Option[BotCommander]
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
    require(playerCommanders.size <= scenario.tilemap.startingPositions.length)
    val commanderId = playerCommanders.zipWithIndex.toMap
    val startingPositions = scenario.tilemap.startingPositions.iterator
    playerCommanders map { playerCommander =>
      val pos = startingPositions.next
      val commander = BotCommander(
        playerCommander,
        id=commanderId(playerCommander),
        homeBase=new Base(hitpoints=800, row=pos.row, col=pos.col)
      )
      commander.spawnBot(Position(row=pos.row, col=pos.col))
      commander
    }
  }
}
