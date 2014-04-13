package se.ramn.bottfarmen.simulation

import se.ramn.bottfarmen.simulation.entity.Bot
import se.ramn.bottfarmen.simulation.entity.Position
import se.ramn.bottfarmen.simulation.TileMap.Tile


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

    type Movers = Map[Bot, Position]

    def tile(position: Position): Option[Tile] = {
      scenario.tilemap.tile(position)
    }

    def groupByPosition(movers: Movers): Map[Position, Set[Bot]] = {
      val foldInit = Map.empty[Position, Set[Bot]]
      val moversByPosition = movers.foldLeft(foldInit) { (memo, elem) =>
        val (bot, pos) = elem
        memo.updated(pos, memo.getOrElse(pos, Set.empty[Bot]) + bot)
      }
      moversByPosition
    }

    def partiallyResolve(
      thunk: (Movers, Position, Set[Bot]) => Movers
    ): Function1[Movers, Movers] = {
      (moversLeft) => {
        val byPosition = groupByPosition(moversLeft)
        byPosition.foldLeft(moversLeft) { (memo, elem) =>
          val (targetPos, bots) = elem
          thunk(memo, targetPos, bots)
        }
      }
    }

    val resolveWater = partiallyResolve { (moversLeft, targetPos, bots) =>
      val targetTile = tile(targetPos).get
      if (targetTile == '~') {
        bots foreach { bot =>
          bot.hitpoints = 0
          bot.commander.bots -= bot
        }
        moversLeft -- bots
      } else {
        moversLeft
      }
    }

    val resolveTileHasNonmovingOccupant =
      partiallyResolve { (moversLeft, targetPos, bots) =>
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
          moversLeft -- bots
        } else {
          moversLeft
        }
      }

    val resolveCompetingForTile =
      partiallyResolve { (moversLeft, targetPos, bots) =>
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
          moversLeft -- bots
        } else {
          moversLeft
        }
      }

    val resolveLoneOccupant =
      partiallyResolve { (moversLeft, targetPos, bots) =>
        val targetTileHasBotWithAbortedMove =
          movers.keys.exists(_.position == targetPos)
        if (bots.size == 1 && !targetTileHasBotWithAbortedMove) {
          bots foreach { bot =>
            bot.row = targetPos.row
            bot.col = targetPos.col
          }
          moversLeft -- bots
        } else {
          moversLeft
        }
      }

    val pipe =
      resolveWater andThen
      resolveTileHasNonmovingOccupant andThen
      resolveCompetingForTile andThen
      resolveLoneOccupant

    val unhandledMovers = pipe(movers)
    unhandledMovers
  }
}
