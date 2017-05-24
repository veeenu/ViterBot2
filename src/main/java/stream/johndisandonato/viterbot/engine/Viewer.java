package stream.johndisandonato.viterbot.engine;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.paint.*;

public class Viewer {
  public final String nick;
  public final Color color;
  public SimpleBooleanProperty watching;

  private static int instanceNumber = 1;

  public Viewer(String n, boolean w) {
    nick = n;
    watching = new SimpleBooleanProperty(w);
    int number = instanceNumber++;
    color = Color.hsb((number * 48 + 12) % 360, 0.7, 1.0);
  }

  public boolean equals(Viewer u) {
    return u.nick.toLowerCase().equals(nick.toLowerCase());
  }

  public boolean equals(String s) {
    return s.toLowerCase().equals(nick.toLowerCase());
  }
  
  public int compareTo(Viewer b) {
    
    if(this.watching.get() && !b.watching.get()) {
      return -1;
    } else if(!this.watching.get() && b.watching.get()) {
      return 1;
    } else {
      return this.nick.toLowerCase().compareTo(b.nick.toLowerCase());
    }
  }
}

