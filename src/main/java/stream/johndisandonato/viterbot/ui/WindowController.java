package stream.johndisandonato.viterbot.ui;

import java.util.*;
import javafx.animation.FillTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.*;
import javafx.scene.text.*;
import javafx.scene.paint.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.geometry.*;
import javafx.collections.*;
import javafx.collections.transformation.*;
import javafx.scene.Node;
import javafx.util.Duration;

import stream.johndisandonato.viterbot.engine.*;

public class WindowController {

  @FXML private ListView<Privmsg> chatList;
  @FXML private ListView<Viewer>  viewersList;
  @FXML private Label             viewerCount;
  @FXML private TextField         chatMsg;

  private List<MessageListener> msgEnterListeners;

  public WindowController() {

    msgEnterListeners = new ArrayList();
  }

  @FXML
  private void initialize() {
  
    chatList.setCellFactory((list) -> new PrivmsgCell());
    viewersList.setCellFactory((list) -> new ViewerCell());

  }

  @FXML
  public void onChatMsgKeyPressed(KeyEvent e) {
  
    if(e.getCode().toString().equals("ENTER")) {
    
      msgEnterListeners.forEach(i -> i.onMessage(chatMsg.getText()));
      chatMsg.setText("");
    }

  }

  public void addMessageListener(MessageListener c) {
    msgEnterListeners.add(c);
  }

  public void setChat(ObservableList<Privmsg> l) {
    l.addListener((ListChangeListener)c -> {
    
      chatList.scrollTo(l.size());

    });
    chatList.setItems(l);
  }

  public void setViewers(ObservableList<Viewer> l) {

    viewersList.setItems(l.sorted((a,b) -> {
      
      return a.compareTo(b);
      
    }));
  }

  public void setViewerCount(SimpleIntegerProperty vc) {
  
    SimpleIntegerProperty maxv = new SimpleIntegerProperty();

    vc.addListener((obs, oldv, newv) -> {
    
      maxv.set(Math.max(vc.getValue(), maxv.getValue()));

    });

    viewerCount.textProperty().bind(
      Bindings.when(Bindings.equal(vc, -1))
        .then("Offline")
        .otherwise(Bindings.concat(vc.asString(), "/", maxv.asString(), " viewers"))
    );
  }

  private class PrivmsgCell extends ListCell<Privmsg> {

    private TextFlow tf;
    private boolean created = false;

    public PrivmsgCell() {
      super();

      setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
      setText(null);

      tf = new TextFlow();
      tf.getStyleClass().add("cell-text-flow");

      prefWidthProperty().bind(chatList.widthProperty().subtract(32));
      setMaxWidth(Control.USE_PREF_SIZE);
    }

    @Override
    public void updateItem(Privmsg item, boolean empty) {
      super.updateItem(item, empty);
      if(item != null) {
                
        Text nickText = new Text(item.viewer.nick + " ");

        nickText.getStyleClass().add("nick");
        nickText.setFill(item.viewer.color);

        List<Node> uiNodes = item.getUINodes();
        ObservableList<Node> ch = tf.getChildren();
        
        ch.clear();
        ch.addAll(nickText);
        uiNodes.stream().forEach(i -> {
          if(i instanceof Text)
            i.getStyleClass().add("msg");
          
        });
        ch.addAll(uiNodes);
                
        /*if(!created) {
          created = true;
          TranslateTransition t = new TranslateTransition();
          t.setNode(tf);
          t.setFromX(chatList.widthProperty().get());
          t.setToX(0);
          t.setInterpolator(Interpolator.SPLINE(0.25, 0.0, 0.5, 0.0));
          t.setDuration(new Duration(200));
          t.playFromStart();
        }*/
        
        setGraphic(tf);
      } else {
        setGraphic(null);
      }
    }
  }

  private class ViewerCell extends ListCell<Viewer> {

    private Label nick;
    private final Color notWatching = Color.web("#666666ff");

    public ViewerCell() {
      super();

      setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
      setText(null);

      nick = new Label();
      nick.getStyleClass().add("viewer-label");
    }

    @Override
    public void updateItem(Viewer item, boolean empty) {
      super.updateItem(item, empty);
      if(item != null) {
        
        nick.setText(item.nick);

        //System.out.println("Updated " + item.nick);
        if(!item.watching.get()) {
          nick.setTextFill(notWatching);
        } else {
          nick.setTextFill(item.color);
        }

        setGraphic(nick);
      } else {
        setGraphic(null);
      }
    }
  }
}
