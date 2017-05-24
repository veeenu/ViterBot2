package stream.johndisandonato.viterbot.engine;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;
import com.google.gson.*;
import javafx.application.*;
import javafx.collections.*;
import javafx.beans.Observable;

//import stream.johndisandonato.viterbot.commands.*;

public class Session implements PrivmsgListener {

  private ObservableList<Viewer> viewers;
  private ObservableList<Privmsg> messages;

  public Session() {
  
    viewers  = FXCollections.observableArrayList(v -> new javafx.beans.Observable[]{v.watching});
    messages = FXCollections.observableArrayList();

  }

  public ObservableList<Viewer> getViewers() {
    return viewers;
  }

  public ObservableList<Privmsg> getMessages() {
    return messages;
  }

  public void addMessage(String nick, String text, Map<String, String> tags) {
  
    Platform.runLater(() -> {

      Optional<Viewer> vo = viewers.stream().filter(t -> t.equals(nick)).findFirst();
      Viewer v; 

      if(vo.isPresent()) {
        v = vo.get();
      } else {
        v = new Viewer(nick, true);
        viewers.add(v);
      }

      messages.add(new Privmsg(v, text, tags));
    });

  }

  public void onPrivmsg(IRCEngine irc, String nick, String user, String pmsg, Map<String, String> tags) {
    addMessage(nick, pmsg, tags);
  }

  public void onJoin(IRCEngine irc, String nick) {

    System.out.println("Join " + nick);
    Platform.runLater(() -> {

      Optional<Viewer> vo = viewers.stream().filter(t -> t.equals(nick)).findFirst();
      Viewer v; 

      if(vo.isPresent()) {
        v = vo.get();
        v.watching.set(true);
      } else {
        v = new Viewer(nick, true);
        viewers.add(v);
      }
    });
  }

  public void onPart(IRCEngine irc, String nick) {

    System.out.println("Part " + nick);
    Platform.runLater(() -> {

      Optional<Viewer> vo = viewers.stream().filter(t -> t.equals(nick)).findFirst();
      Viewer v; 

      if(vo.isPresent()) {
        v = vo.get();
        v.watching.set(false);
      } else {
        v = new Viewer(nick, false);
        viewers.add(v);
      }
    });
  }

}
