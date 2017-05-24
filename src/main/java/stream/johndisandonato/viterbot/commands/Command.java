package stream.johndisandonato.viterbot.commands;

import java.util.*;

import stream.johndisandonato.viterbot.engine.*;

public interface Command {

  public void processCommand(IRCEngine irc, String cmd);
  public Set<String> allowedCommands();

}
