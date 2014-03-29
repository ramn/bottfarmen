package se.ramn.bottfarmen

import api.BotCommander


object BotCommanderLoader {
  def load(commanderClassNames: Seq[String]): Set[BotCommander] = {
    val classes = commanderClassNames.map(Class.forName)
    val commanders = classes.map(_.newInstance.asInstanceOf[BotCommander])
    commanders.toSet
  }
}
