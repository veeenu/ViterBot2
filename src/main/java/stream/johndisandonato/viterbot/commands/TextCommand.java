package stream.johndisandonato.viterbot.commands;

import java.io.*;
import java.util.*;

import stream.johndisandonato.viterbot.engine.*;

public class TextCommand implements Command {

  private HashMap<String, String> texts;

  public TextCommand() {
  
    texts = new HashMap<>();

  }

  public void addCommand(String k, String v) {
    texts.put(k,v);
  }

  public void processCommand(IRCEngine irc, String cmd) {
    
    if(texts.containsKey(cmd))
      irc.send(texts.get(cmd));
  }

  public Set<String> allowedCommands() {

    return Collections.unmodifiableSet(texts.keySet());
  }

}
