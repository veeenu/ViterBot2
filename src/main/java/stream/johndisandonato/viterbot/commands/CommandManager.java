package stream.johndisandonato.viterbot.commands;

import java.util.*;
import java.lang.annotation.*;

import stream.johndisandonato.viterbot.engine.*;

public class CommandManager implements PrivmsgListener {

  private HashMap<String, Command> commands;

  public CommandManager() {
    commands = new HashMap<>();
  }

  public void registerCommand(Command c) {
  
    c.allowedCommands().forEach(i -> commands.put(i, c));

  }

  public void onPrivmsg(IRCEngine irc, String nick, String user, String msg, Map<String, String> tags) {

    if(commands.containsKey(msg)) {
      commands.get(msg).processCommand(irc, msg);
    } else if(msg.equals("!help")) {
      irc.send(String.join(" ", commands.keySet()));
    }

  }

  public void onJoin(IRCEngine irc, String nick) {}
  public void onPart(IRCEngine irc, String nick) {}

}
