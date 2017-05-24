package stream.johndisandonato.viterbot;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

import javafx.application.*;
import javafx.fxml.*;
import javafx.event.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.*;
import javafx.concurrent.*;

import java.awt.*;
import javafx.geometry.Rectangle2D;
import javax.imageio.ImageIO;
import javax.swing.*;

//import org.jnativehook.*;
//import org.jnativehook.keyboard.*;

import stream.johndisandonato.viterbot.ui.*;
import stream.johndisandonato.viterbot.engine.*;
import stream.johndisandonato.viterbot.commands.*;

public class ViterBot extends Application {

  private Properties settings;
  
  @Override
  public void start(Stage stage) {
  
    if(SystemTray.isSupported()) {
      
      try {
      
        final PopupMenu menu = new PopupMenu();
        final java.awt.Image img = ImageIO.read(this.getClass().getResource("/images/VinseroBattaglie.png"));

        java.awt.MenuItem exit = new java.awt.MenuItem("Exit");

        menu.add(exit);

        exit.addActionListener((e) -> {
          //System.out.println("More memes");
          quit();
        });
                
        final TrayIcon icon = new TrayIcon(img, "VinseroBattaglie", menu);
        icon.setImageAutoSize(true);
      
        SystemTray.getSystemTray().add(icon);
        stage.getIcons().add(new javafx.scene.image.Image(this.getClass().getResourceAsStream("/images/VinseroBattaglie.png")));
      } catch(AWTException e) {
        e.printStackTrace();
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    
    Pane root;
    Scene scene;
    WindowController c;

    // TODO refactor properties in a class
    String nick;

    Session chatSession = new Session();
    CommandManager mgr = new CommandManager();
    MessageLogger logger = new MessageLogger();

    Jukebox jb = new Jukebox();
    TextCommand tc = new TextCommand();

    tc.addCommand("!run", "All Bosses + Ashes of Ariandel http://pastebin.com/62x7Z6iM");

    mgr.registerCommand(jb);
    mgr.registerCommand(tc);

    try {

      Properties p = new Properties();
      p.load(new FileInputStream("viterbot.cfg"));

      nick = p.getProperty("nick");

      IRCEngine irce = new IRCEngine();
      irce.addPrivmsgListener(chatSession);
      irce.addPrivmsgListener(mgr);
      irce.addPrivmsgListener(logger);

      APIEngine apie = new APIEngine();

      FXMLLoader l = new FXMLLoader();
      l.setLocation(getClass().getResource("/Window.fxml"));

      root = (Pane)l.load();
      scene = new Scene(root);

      c = l.getController();
      c.setChat(chatSession.getMessages());
      c.setViewers(chatSession.getViewers());
      c.setViewerCount(apie.viewersProperty());
      c.addMessageListener((s) -> {
      
        irce.send(s);

      });

      Rectangle2D psb = Screen.getPrimary().getVisualBounds();
      
      stage.initStyle(StageStyle.UNDECORATED);
      stage.setScene(scene);
      stage.setX(psb.getWidth() - 920);
      stage.setY(psb.getHeight() - 480);
      stage.setWidth(920);
      stage.setHeight(480);
      stage.setTitle("ViterBot2");
      stage.setOnCloseRequest((evt) -> {
        quit();
      });
      stage.show();

      irce.start();
      apie.start();

    } catch(IOException e) {

      new Alert(Alert.AlertType.ERROR, e.getMessage())
        .showAndWait()
        .ifPresent(r -> {
        
          quit();

        });

    } catch(Exception e) {
      e.printStackTrace();
    }

    /*chatSession.onJoin(null, "brambillafumagalli");
    chatSession.onPart(null, "brambillafumagalli");
    chatSession.onJoin(null, "fumillabrambagalli");
    for(int i = 0; i < 32; i++) {
      chatSession.onJoin(null, "boherino" + Integer.toString(i % 15));
      chatSession.onPart(null, "boherino" + Integer.toString(i % 15));
    }*/

  }

  private void quit() {
    //try {
    //  GlobalScreen.unregisterNativeHook();
    //} catch(Exception e) {}
    Platform.exit();
    System.exit(0);
  }

}

