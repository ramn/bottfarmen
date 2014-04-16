package se.ramn.bottfarmen.example.baseraider

import collection.immutable.Seq

import se.ramn.bottfarmen.api
import se.ramn.bottfarmen.api.GameState
import se.ramn.bottfarmen.api.Command
import se.ramn.bottfarmen.api.Move
import se.ramn.bottfarmen.example.BaseBot


class Bot(var underlying: api.Bot) extends BaseBot {
  def isAlive = underlying.hitpoints > 0
}
