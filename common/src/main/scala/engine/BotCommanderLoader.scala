package se.ramn.bottfarmen.simulation

import se.ramn.bottfarmen.api.BotCommander


object BotCommanderLoader {
  def load(commanderClassNames: Seq[String]): Set[BotCommander] = {
    val classes = commanderClassNames.map(Class.forName)
    val commanders = classes.map(_.newInstance.asInstanceOf[BotCommander])
    commanders.toSet
  }

  def loadFromEnv: Set[BotCommander] = {
    load(commanderNamesFromEnv)
  }

  def commanderNamesFromEnv: Seq[String] = {
    val envVarKey = "COMMANDERS"
    val separator = ","
    sys.env.get(envVarKey).toList
      .flatMap(_.split(separator).toList)
  }
}
