package se.ramn.bottfarmen.engine

import collection.immutable.IndexedSeq
import TileMap.Tile


trait TileMap {
  val rowCount: Int
  val colCount: Int
  def rows: IndexedSeq[IndexedSeq[Tile]]
}


object TileMap {
  type Tile = Char

  def loadFromFile(path: String): TileMap = {
    parse(io.Source.fromFile(path).mkString)
  }

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

    new TileMap {
      val rowCount = headers("rows").toInt
      val colCount = headers("cols").toInt
      val rows = map
    }
  }
}
