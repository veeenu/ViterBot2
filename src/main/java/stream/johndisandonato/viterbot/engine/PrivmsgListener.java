package stream.johndisandonato.viterbot.engine;

import java.util.Map;

public interface PrivmsgListener {

  public void onPrivmsg(IRCEngine irc, String nick, String user, String msg, Map<String, String> tags);
  public void onJoin(IRCEngine irc, String nick);
  public void onPart(IRCEngine irc, String nick);

}
