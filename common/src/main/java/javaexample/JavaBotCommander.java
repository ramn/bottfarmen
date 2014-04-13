package se.ramn.bottfarmen.javaexample;

import java.util.List;
import java.util.Collections;
import se.ramn.bottfarmen.api.BotCommander;
import se.ramn.bottfarmen.api.GameState;
import se.ramn.bottfarmen.api.Command;
import se.ramn.bottfarmen.api.Move;


class JavaBotCommander implements BotCommander {
  public JavaBotCommander() {
  }

  public String name() {
    return "Java Commander";
  }

  public List<Command> update(GameState gameState) {
    final Command command = new Move(1, 'n');
    return Collections.singletonList(command);
  }
}
