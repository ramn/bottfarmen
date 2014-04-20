package se.ramn.bottfarmen.simulation

import collection.immutable.IndexedSeq
import TileMap.Tile
import se.ramn.bottfarmen.simulation.entity.Position
import se.ramn.bottfarmen.util.loadTextFileFromClassPath


trait TileMap {
  val rowCount: Int
  val colCount: Int
  val rows: IndexedSeq[IndexedSeq[Tile]]
  def startingPositions: Seq[StartingPosition]
  def isWithinMap(position: Position): Boolean
  def isWalkable(position: Position): Boolean
  def tile(position: Position): Option[Tile]
}


case class StartingPosition(id: Int, row: Int, col: Int)


object TileMap {
  type Tile = Char

  def loadFromClassPath(path: String): TileMap = {
    parse(loadTextFileFromClassPath(path))
  }

  def fromEnvOrDefault(defaultMapPath: String) = {
    val mapPath = sys.env.getOrElse("MAP_FILE", defaultMapPath)
    val absoluteJarPath =
      if (mapPath.startsWith("/")) mapPath
      else "/" + mapPath

    loadFromClassPath(absoluteJarPath)
  }

  /*
   * The map file uses the same format as Ants AI Challenge.
   * Comment lines start with #
   * Then there are header rows, key/value pairs.
   * Then the actual map rows, prefixed with "m "
   */
  def parse(rawMap: String): TileMap = {
    def isComment(row: String) = row.trim.startsWith("#")
    def isMap(row: String) = row.trim.startsWith("m ")

    val headers: Map[String, String] = {
      for {
        row <- rawMap.lines
        if !isComment(row)
        if !isMap(row)
        pair = row.split(" ")
        key = pair(0)
        value = pair(1)
      } yield key -> value
    }.toMap

    val map = for {
      row <- rawMap.lines.toIndexedSeq
      if isMap(row)
    } yield row.trim.drop(2).toIndexedSeq

    val startingPos = {
      for {
        (rows, rowIx) <- map.zipWithIndex
        (cell, colIx) <- rows.zipWithIndex
        if cell.isDigit
        id = cell.toString.toInt
      } yield StartingPosition(id=id, row=rowIx, col=colIx)
    }.sortBy(_.id)

    new TileMap {
      override val rowCount = headers("rows").toInt
      override val colCount = headers("cols").toInt
      override val rows = map
      override val startingPositions = startingPos
      override def isWithinMap(position: Position): Boolean = {
        val (row, col) = (position.row, position.col)
        rowCount >= row && row >= 0 && colCount >= col && col >= 0
      }
      override def tile(position: Position) = {
        if (isWithinMap(position)) {
          Some(rows(position.row)(position.col))
        } else {
          None
        }
      }
      override def isWalkable(position: Position): Boolean = {
        val nonWalkable = Set('~')
        tile(position).filterNot(nonWalkable).isDefined
      }
    }
  }
}
