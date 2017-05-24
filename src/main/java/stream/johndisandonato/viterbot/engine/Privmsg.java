package stream.johndisandonato.viterbot.engine;

import java.util.*;
import java.util.stream.*;

import javafx.scene.*;
import javafx.scene.text.*;
import javafx.scene.image.*;

public class Privmsg {

  public Viewer viewer;
  public String msg;
  public Map<String, String> tags;
  
  private List<MessagePart> msgParts;

  public Privmsg(Viewer v, String m, Map<String, String> t) {
    viewer = v;
    msg = m;
    tags = t;
    
    msgParts = makeMessageParts();
  }
  
  private List<MessagePart> makeMessageParts() {
  
    List<MessagePart> mps = new ArrayList<>();
    
    if(tags == null) {
      mps.add(new MessagePart(msg, MessagePartType.TEXT));
      return mps;
    }
    
    String emotesText = tags.get("emotes");
    
    if(emotesText != null) {
    
      List<MessageEmote> emotes = new ArrayList<>();
      Arrays
        .asList(emotesText.split("/"))
        .stream()
        .forEach(i -> {
          String[] ss = i.split(":");
          
          int id = Integer.parseInt(ss[0]);
          Arrays
            .asList(ss[1].split(","))
            .stream()
            .forEach(j -> {
              String[] fl= j.split("-");
              emotes.add(new MessageEmote(id, Integer.parseInt(fl[0]), Integer.parseInt(fl[1])));
            });
        });
      
      emotes.sort((Comparator<MessageEmote>) (a, b) -> { 
        if(a.first < b.first) return -1;
        else if(a.first > b.first) return 1;
        return 0;
      });
      emotes.stream().forEach(i -> System.out.println(i.getURI()));

      if(emotes.get(0).first > 0)
        mps.add(new MessagePart(msg.substring(0, emotes.get(0).first), MessagePartType.TEXT));
      
      for(int i = 0; i < emotes.size() - 1; i++) {
      
        MessageEmote e1 = emotes.get(i), e2 = emotes.get(i + 1);
        
        mps.add(new MessagePart(e1.getURI(), MessagePartType.EMOTE));
        mps.add(new MessagePart(msg.substring(e1.last + 1, e2.first), MessagePartType.TEXT));
        
      }
      
      MessageEmote lastE = emotes.get(emotes.size() - 1);
      mps.add(new MessagePart(lastE.getURI(), MessagePartType.EMOTE));
      if(lastE.last < msg.length())
        mps.add(new MessagePart(msg.substring(emotes.get(emotes.size() - 1).last + 1), MessagePartType.TEXT));
      
      mps.stream().forEach(i -> System.out.print(i.content));
      System.out.println("");
      
    } else {
      mps.add(new MessagePart(msg, MessagePartType.TEXT));
    }
    
    return mps;
    
  }
  
  public List<Node> getUINodes() {
    
    if(msgParts == null)
      msgParts = makeMessageParts();
    
    return msgParts.stream().map(i -> {
    
      if(i.type == MessagePartType.EMOTE) {
      
        ImageView iv = ImageViewBuilder.create()
          .image(new Image(i.content))
          .build();
        
        return iv;
        
      } else {
        return new Text(i.content);
      }
      
    }).collect(Collectors.toList());
    
  }
  
  enum MessagePartType { EMOTE, TEXT };
  
  private class MessagePart {
    public MessagePartType type;
    public String content;
    
    public MessagePart(String c, MessagePartType t) {
      content = c;
      type = t;
    }
  }
  
  private class MessageEmote {
 
    public int id, first, last;

    public MessageEmote(int a, int b, int c) {
      id = a; first = b; last = c;
    }

    public String getURI() {
      return String.format("http://static-cdn.jtvnw.net/emoticons/v1/%d/1.0", id);
    }
  }

}
