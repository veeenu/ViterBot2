package stream.johndisandonato.viterbot.engine;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;
import com.google.gson.*;

//import stream.johndisandonato.viterbot.commands.*;

public class IRCEngine {

  private static final Pattern 
    privmsgPattern = Pattern.compile("^@(\\S+)\\s+:([^!]+)!([^@]+)@\\S+\\s+\\w+\\s+[:\\S]+\\s*:(.+)$"),
    actionPattern = Pattern.compile("^:([^!]+)!([^@]+)@\\S+\\s+\\w+.+$");

  private BufferedWriter writer;
  private BufferedReader reader;
  private Thread readerThread, writerThread;
  private Socket socket;
  private Message msg;

  private List<PrivmsgListener> pmsgListeners;

  private String  server    = "irc.twitch.tv", 
                  usersURI  = "https://tmi.twitch.tv/group/user/%s/chatters", 
                  nick, 
                  user      = "%s 0 * :%s", 
                  pass, 
                  chan;
  private static final int port = 6667;

  public IRCEngine() throws IOException {

    pmsgListeners = new ArrayList<>();
  
    try {
      Properties p = new Properties();
      p.load(new FileInputStream("viterbot.cfg"));

      nick     = p.getProperty("nick");
      pass     = p.getProperty("pass");
      chan     = "#" + nick;
      user     = String.format(user, nick, p.getProperty("name"));
      usersURI = String.format(usersURI, nick);
    } catch(IOException e) {
      throw new IOException("There was an error with the properties file.");
    }
  }

  public void addPrivmsgListener(PrivmsgListener pml) {
    pmsgListeners.add(pml);
  }

  public void start() {
  
    msg = new Message();

    try {
      socket = new Socket(server, port);
      writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
      reader = new BufferedReader(new InputStreamReader (socket.getInputStream() , "UTF-8"));

      sockwrite("PASS " + pass);
      sockwrite("NICK " + nick);
      sockwrite("USER " + user);
      sockwrite("CAP REQ :twitch.tv/membership");
      sockwrite("CAP REQ :twitch.tv/tags");

    } catch(Exception e) {
      e.printStackTrace();
    }

    readerThread = (new Thread(() -> {

      String t = "";

      while(t != null && !socket.isClosed()) { 
        try {
          t = reader.readLine();
          msg.put(t);
        } catch(IOException e) {
          e.printStackTrace();
        }
      }

      System.out.println("* Reader closed");

    }));

    writerThread = (new Thread(() -> {
      while(!socket.isClosed()) {
        String t = msg.get();
        parse(t);
      }

      System.out.println("* Writer closed");
    }));

    readerThread.start();
    writerThread.start();
  }

  public void stop() {

    msg.finished = true;
    try {
      socket.close();
    } catch(IOException e) {
      e.printStackTrace();
    }
    readerThread.interrupt();
    writerThread.interrupt();
  }

  public void send(String s) {
    pmsgListeners.forEach(l -> l.onPrivmsg(this, nick, user, s, null));
    sockwrite("PRIVMSG " + chan + " :" + s);
  }

  private void parse(String s) {
  
    String fmt = String.format("< %s", s);
    System.out.println(fmt);

    if(s == null) return;

    ArrayList<String> toks = tokenize(s);
    
    if(toks.get(0).equals("PING")) {

      sockwrite("PONG " + toks.get(1));
    } else if(toks.get(2).equals("PRIVMSG")) {

      Matcher m = privmsgPattern.matcher(s);

      String nick, user, pmsg;

      if(m.find()) {
        Map<String, String> tags = parseTags(m.group(1));
        nick = m.group(2);
        user = m.group(3);
        pmsg = m.group(4);

        String fm = String.format("* <%s> %s", nick, pmsg);
        System.out.println(fm);

        pmsgListeners.forEach(l -> l.onPrivmsg(this, nick, user, pmsg, tags));
        
      } else {
        System.out.println("Couldn't parse " + s);
      }

    } else if(toks.get(1).equals("JOIN")) {

      Matcher m = actionPattern.matcher(s);

      String nick, user;

      if(m.find()) {
        nick = m.group(1);
        user = m.group(2);

        pmsgListeners.forEach(l -> l.onJoin(this, nick));

      } else {
        System.out.println("Couldn't parse " + s);
      }

    } else if(toks.get(1).equals("PART")) {

      Matcher m = actionPattern.matcher(s);

      String nick, user;

      if(m.find()) {
        nick = m.group(1);
        user = m.group(2);

        pmsgListeners.forEach(l -> l.onPart(this, nick));

      } else {
        System.out.println("Couldn't parse " + s);
      }

    } else if(s.equals(":tmi.twitch.tv CAP * ACK :twitch.tv/membership")) {
      sockwrite("JOIN " + chan);
    }
  }
  
  private Map<String,String> parseTags(String tagstr) {
  
    List<String> tags = Arrays.asList(tagstr.split(";"));
    HashMap<String, String> tagmap = new HashMap<>();
    
    tags.stream().forEach(i -> {
    
      String[] ss = i.split("=");
      if(ss.length > 1)
        tagmap.put(ss[0], ss[1]);
      
    });
    
    return tagmap;
    
  }

  private void sockwrite(String s) {
  
    String fmt = String.format("> %s\n", s);
    System.out.println(fmt);

    try {
      writer.write(s + "\r\n");
      writer.flush();
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  private ArrayList<String> tokenize(String s) {
    Matcher m = Pattern.compile("\\S+").matcher(s);
    ArrayList<String> toks = new ArrayList<>();

    while(m.find())
      toks.add(m.group());

    return toks;
  }

  /*
   * Synchronized producer/consumer object
   */
  private class Message {

    private String message;
    private boolean empty = true;
    public volatile boolean finished = false;

    public synchronized String get() {
      while(empty && !finished) {
        try { 
          wait(); 
        } catch(InterruptedException e) {
        }
      }

      if(finished) return null;

      empty = true;

      notifyAll();
      return message;
    }

    public synchronized void put(String m) {
      while(!empty && !finished) {
        try { 
          wait(); 
        } catch(InterruptedException e) {
        }
      }

      if(finished) return;

      empty = false;
      message = m;

      notifyAll();
    }

  }

}
