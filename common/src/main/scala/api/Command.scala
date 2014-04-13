package se.ramn.bottfarmen.api


sealed trait Command

/*
 * @param steps is a string of move directions n, s, e, w
 */
case class Move(botId: Int, step: Char) extends Command
