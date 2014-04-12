package se.ramn.bottfarmen.simulation

import se.ramn.bottfarmen.simulation.entity.Bot
import se.ramn.bottfarmen.simulation.entity.Position


class MoveResolver(movers: Map[Bot, Position], still: Set[Bot], scenario: Scenario) {
  def resolve() = {
    // 1. handle moves where bot dies immediately (water)
    //
    // 2. handle where there is a still bot on target tile
    //
    // 3. handle where 2+ bots move to the same tile, thus can't move
    //
    // 4. handle moves where only 1 bot moves into a certain tile. we now
    //    should know if the target tile is occupied by a blocked bot or not.
    //

    def groupByPosition(movers: Map[Bot, Position]): Map[Position, Set[Bot]] = {
      val foldInit = Map.empty[Position, Set[Bot]]
      val moversByPosition = movers.foldLeft(foldInit) { (memo, elem) =>
        val (bot, pos) = elem
        memo.updated(pos, memo.getOrElse(pos, Set.empty[Bot]) + bot)
      }
      moversByPosition
    }

    def resolveWater(moversLeft: Map[Bot, Position]): Map[Bot, Position] = {
      val byPosition = groupByPosition(moversLeft)
      byPosition.foldLeft(moversLeft) { (memo, elem) =>
        val (targetPos, bots) = elem
        val targetTile = scenario.tilemap.rows(targetPos.row)(targetPos.col)
        if (targetTile == '~') {
          bots foreach { bot =>
            bot.hitpoints = 0
            bot.commander.bots -= bot
          }
          memo -- bots
        } else {
          memo
        }
      }
    }

    def resolveTileHasNonmovingOccupant(moversLeft: Map[Bot, Position]): Map[Bot, Position] = {
      val byPosition = groupByPosition(moversLeft)
      byPosition.foldLeft(moversLeft) { (memo, elem) =>
        val (targetPos, bots) = elem
        val targetTileHasNonmovingOccupant = still.exists(_.position == targetPos)
        if (targetTileHasNonmovingOccupant) {
          // collision, can't move in but hit the nonmover. only moving bots deal damage.
          bots foreach { bot =>
            val occupants = still.filter(_.position == targetPos)
            occupants foreach { occupant =>
              occupant.hitpoints -= bot.attackStrength
              if (occupant.hitpoints <= 0) {
                occupant.commander.bots -= occupant
              }
            }
          }
          memo -- bots
        } else {
          memo
        }
      }
    }

    def resolveCompetingForTile(moversLeft: Map[Bot, Position]): Map[Bot, Position] = {
      val byPosition = groupByPosition(moversLeft)
      byPosition.foldLeft(moversLeft) { (memo, elem) =>
        val (targetPos, bots) = elem
        if (bots.size > 1) {
          // collision, can't move in but hit eachother. only moving bots deal damage.
          bots foreach { bot =>
            val others = bots - bot
            others foreach { other =>
              other.hitpoints -= bot.attackStrength
              if (other.hitpoints <= 0) {
                other.commander.bots -= other
              }
            }
          }
          memo -- bots
        } else {
          memo
        }
      }
    }

    def resolveLoneOccupant(moversLeft: Map[Bot, Position]): Map[Bot, Position] = {
      val byPosition = groupByPosition(moversLeft)
      byPosition.foldLeft(moversLeft) { (memo, elem) =>
        val (targetPos, bots) = elem
        val targetTileHasBotWithAbortedMove =
          movers.keys.exists(_.position == targetPos)
        if (bots.size == 1 && !targetTileHasBotWithAbortedMove) {
          bots foreach { bot =>
            bot.row = targetPos.row
            bot.col = targetPos.col
          }
          memo -- bots
        } else {
          memo
        }
      }
    }

    val pipe =
      (resolveWater _) andThen
      (resolveTileHasNonmovingOccupant _) andThen
      (resolveCompetingForTile _) andThen
      (resolveLoneOccupant _)

    val unhandledMovers = pipe(movers)

    unhandledMovers foreach { unhandledMover =>
      val (bot, pos) = unhandledMover
      println(s"Could not move bot $bot to $pos")
    }
  }
}
