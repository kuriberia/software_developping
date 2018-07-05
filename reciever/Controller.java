package reciever;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import javafx.scene.paint.Color;

import java.util.Date;

public class Controller {
    private Connector cnct;

    @FXML private Button btnSend;
    @FXML private Button btnConnect;
    @FXML private Label lbStatus;
    @FXML private TextField tfMessage;
    @FXML private ListView<String> lvComment;
    @FXML private TextField tfIpAddress;

    @FXML private boolean isOnline;

    public Controller() {
        isOnline = false;
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1),  //Drawing
                new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent actionEvent) {
                        if (cnct != null) {
                            isOnline = cnct.getIsOnline();
                            if (isOnline) {
                                lbStatus.setText("ONLINE");
                                lbStatus.setTextFill(Color.RED);
                                btnConnect.setText("disconnect");
                            }else{
                                lbStatus.setText("OFFLINE");
                                lbStatus.setTextFill(Color.BLACK);
                                btnConnect.setText("connect");
                            }
                        }
                    }
                }
        ));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    @FXML
    private void switchConnection(){
        if(!isOnline) {
            String destIP = tfIpAddress.getText();
            lbStatus.setText("ONLINE");
            lbStatus.setTextFill(Color.RED);
            btnConnect.setText("disconnect");
            cnct = new Connector(destIP);
            cnct.start();
            lvComment.setItems(cnct.getLog());
        }else{
            lbStatus.setText("OFFLINE");
            lbStatus.setTextFill(Color.BLACK);
            btnConnect.setText("connect");
            this.shutdown();
        }
    }

    @FXML
    private void sendMessage() {
        if(isOnline) {
            String str = tfMessage.getText();
            System.out.println(str);
            if (str.equals("")) return;
            String msg ="<" + new Date().toString() + "> : " + str;
            cnct.sendMessage(msg);
            tfMessage.clear();
//            for (Node node : lvComment.lookupAll(".scroll-bar")) { //Put bottom
//                if (node instanceof ScrollBar) {
//                    ScrollBar bar = (ScrollBar) node;
//                    bar.setValue(1);
//                }
//            }
        }
    }

    @FXML
    private void keyPressed(KeyEvent ke) { //コメント欄でEnterキーが押されたら送信
        if(ke.getCode().equals(KeyCode.ENTER)) {
            sendMessage();
            System.out.println("EnterKey Pressed."); // TODO: Remove this.
        }
    }

    public void shutdown(){
        if(isOnline){
            cnct.connectStop();
        }
    }
}
