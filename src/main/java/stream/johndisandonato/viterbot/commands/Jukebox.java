package stream.johndisandonato.viterbot.commands;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.sound.sampled.*;

import stream.johndisandonato.viterbot.engine.*;

public class Jukebox implements Command {

  private HashMap<String, Clip> sounds = new HashMap<>();
  private Timer timeoutTimer = new Timer();
  private TimerTask timeoutTimerTask;
  private boolean started = false,
                  timeout = false;

  public Jukebox() {

    File audioDir = new File("./audio");
    Stream<File> fs = Arrays.asList(audioDir.listFiles()).stream();
    
    fs
      .filter(
        i -> i.isFile() && i.getName().toLowerCase().endsWith(".au")
      )
      .forEach((f) -> {
        String cmd  = "!" + f.getName().replaceFirst(".au$", ""),
               file = "./audio/" + f.getName();
        addSound(cmd, file);
      });
    
  }

  private void addSound(String name, String uri) {
    
    try {
      //InputStream f = new BufferedInputStream(this.getClass().getResourceAsStream(uri));
      InputStream f = new BufferedInputStream(new FileInputStream(uri));
      AudioInputStream snd = AudioSystem.getAudioInputStream(f);

      DataLine.Info info = new DataLine.Info(Clip.class, snd.getFormat());
      Clip clip = (Clip) AudioSystem.getLine(info);
      clip.open(snd);

      ((FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN)).setValue(-8.f);
      clip.addLineListener((e) -> {
        if(e.getType() == LineEvent.Type.START)
          started = true;

        if(e.getType() == LineEvent.Type.STOP) {
          started = false;
          ((Clip)e.getLine()).setFramePosition(0);
        }
      });

      sounds.put(name, clip);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  public void play(String name) {
    
    System.out.println("Playing " + name);
    Clip c = sounds.get(name);

    if(c == null || started || timeout) return;

    c.start();
  }

  public void processCommand(IRCEngine irc, String cmd) {
    
    if(sounds.containsKey(cmd))
      play(cmd);
  }

  public Set<String> allowedCommands() {

    return Collections.unmodifiableSet(sounds.keySet());
  }

}
