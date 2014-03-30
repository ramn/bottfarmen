package se.ramn.bottfarmen.simulation

import collection.immutable.IndexedSeq
import TileMap.Tile


trait TileMap {
  val rowCount: Int
  val colCount: Int
  def rows: IndexedSeq[IndexedSeq[Tile]]
  def startingPositions: Seq[StartingPosition]
}


case class StartingPosition(id: Int, row: Int, col: Int)


object TileMap {
  type Tile = Char

  def loadFromFile(path: String): TileMap = {
    parse(io.Source.fromFile(path).mkString)
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
      val rowCount = headers("rows").toInt
      val colCount = headers("cols").toInt
      val rows = map
      val startingPositions = startingPos
    }
  }
}
