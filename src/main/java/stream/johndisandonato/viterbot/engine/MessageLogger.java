package stream.johndisandonato.viterbot.engine;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageLogger implements PrivmsgListener {

  private File logf;
  private SimpleDateFormat sdf;
  
  public MessageLogger() {
    sdf = new SimpleDateFormat("HH:mm:ss");
    logf = new File("messages.log");
    
    try {
      if(!logf.exists())
        logf.createNewFile();
    } catch (IOException ex) {
      Logger.getLogger(MessageLogger.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  @Override
  public void onPrivmsg(IRCEngine irc, String nick, String user, String msg, Map<String, String> tags) {

    try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(logf, true)))) {
      pw.println(String.format("[%s] <%s> %s", sdf.format(new Date()), nick, msg));
      
    } catch(Exception e) {
      e.printStackTrace();
    }
    
  }

  @Override
  public void onJoin(IRCEngine irc, String nick) {
    
  }

  @Override
  public void onPart(IRCEngine irc, String nick) {
    
  }
  
}
