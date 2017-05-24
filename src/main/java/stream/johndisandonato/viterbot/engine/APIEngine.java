package stream.johndisandonato.viterbot.engine;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;
import com.google.gson.*;

import javafx.beans.property.*;
import javafx.application.*;

public class APIEngine {

  private static final String 
    chattersURIfmt = "https://tmi.twitch.tv/group/user/%s/chatters",
    streamURIfmt   = "https://api.twitch.tv/kraken/streams/%s";
  private final String user, secret, clientID,
                       chattersURI, streamURI;

  private Gson gson;
  private Thread chattersThread, streamThread;
  private SimpleIntegerProperty viewers;
  private SimpleListProperty<String> chatters;

  public APIEngine() throws IOException {
  
    try {
      Properties p = new Properties();
      p.load(new FileInputStream("viterbot.cfg"));

      user = p.getProperty("nick");
      secret = p.getProperty("secret");
      clientID = p.getProperty("clientid");

      chattersURI = String.format(chattersURIfmt, user);
      streamURI = String.format(streamURIfmt, user);

      viewers = new SimpleIntegerProperty();
      chatters = new SimpleListProperty<>();

      gson = new Gson();
    } catch(IOException e) {
      throw new IOException("There was an error with the properties file.");
    } 
  }

  public void start() {

    streamThread = (new Thread(() -> {
    
      try {

        while(true) {
          
          int v = getViewers();

          Platform.runLater(() -> {
            viewers.set(v);
          });

          Thread.sleep(5000);
        }

      } catch(InterruptedException e) {
        e.printStackTrace();
      }

    }));
    
    chattersThread = (new Thread(() -> {
    
      try {

        while(true) {
          
          List<String> c = getChatters();

          Platform.runLater(() -> {
            chatters.setAll(c);
          });

          Thread.sleep(5000);
        }

      } catch(InterruptedException e) {
        e.printStackTrace();
      }

    }));

    streamThread.start();

  }

  public void stop() {

    streamThread.interrupt();
  }

  public SimpleIntegerProperty viewersProperty() {
    return viewers;
  }
  
  public SimpleListProperty<String> chattersProperty() {
    return chatters;
  }

  private int getViewers() {
    
    try {
      HttpURLConnection con = (HttpURLConnection) (new URL(streamURI)).openConnection();
      con.setRequestMethod("GET");
      con.setRequestProperty("Client-ID", clientID);
      con.setRequestProperty("Accept", "application/vnd.twitchtv.v3.json");
      int resp = con.getResponseCode();

      if(resp == HttpURLConnection.HTTP_OK) {

        StringBuilder sb = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

        String l;
        while((l = in.readLine()) != null) 
          sb.append(l);

        in.close();

        JsonParser parser = new JsonParser();
        JsonElement root  = parser.parse(sb.toString());

        if(root.isJsonObject() && root.getAsJsonObject().get("stream").isJsonObject()) {
        
          int viewers = root.getAsJsonObject().get("stream").getAsJsonObject().get("viewers").getAsInt();

          return viewers;

        }

      } else {
        System.out.println(resp);
      }
    } catch(Exception e) {
      e.printStackTrace();
    }

    return -1;

  }

  private List<String> getChatters() {
  
    try {
      HttpURLConnection con = (HttpURLConnection) (new URL(chattersURI)).openConnection();
      con.setRequestMethod("GET");
      con.setRequestProperty("Client-ID", clientID);
      con.setRequestProperty("Accept", "application/vnd.twitchtv.v3.json");
      int resp = con.getResponseCode();

      if(resp == HttpURLConnection.HTTP_OK) {

        StringBuilder sb = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

        String l;
        while((l = in.readLine()) != null) 
          sb.append(l);

        in.close();

        Chatters ch = gson.fromJson(sb.toString(), Chatters.class);

        System.out.println(ch.getAll());
        
        return ch.getAll();
      } else {
        System.out.println(resp);
      }
    } catch(Exception e) {
      e.printStackTrace();
    }

    return null;

  }

  private class Chatters {
  
    public ChattersObject chatters;

    public ArrayList<String> getAll() {
      return chatters.getAll();
    }

    private class ChattersObject {
    
      public ArrayList<String> moderators, staff, admins, global_mods, viewers;
      
      public ArrayList<String> getAll() {
        ArrayList<String> s = new ArrayList<>();

        s.addAll(moderators);
        s.addAll(staff);
        s.addAll(admins);
        s.addAll(global_mods);
        s.addAll(viewers);

        return s;
      }
    }

  }

}
