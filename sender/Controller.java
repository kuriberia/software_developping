package sender;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

import java.util.Date;

public class Controller {

    private Sender snd;
    @FXML private Button btnConnect;
    @FXML private TextField tfMessage;
    @FXML private Label lbStatus;
    @FXML private ListView<String> lvComment;

    public void initialize() { //初期化メソッド
        Sender.setOffline();
    }

    @FXML
    private void switchConnection() { //connectボタンが押された場合の挙動
        if(!Sender.isOnline()) {
            Sender.setOnline();
            lbStatus.setText("ONLINE");
            lbStatus.setTextFill(Color.RED);
            btnConnect.setText("disconnect");
            snd = new Sender();
            lvComment.setItems(snd.getChatLog());
            Thread th = new Thread(snd);
            th.start();
        } else {
            Sender.setOffline();
            lbStatus.setText("OFFLINE");
            lbStatus.setTextFill(Color.BLACK);
            btnConnect.setText("connect");
        }
    }

    @FXML
    private void sendMessage() { //コメント送信
        if(Sender.isOnline()) {
            String str = tfMessage.getText();
            if (str.equals("")) return;
            String msg ="<" + new Date().toString() + "> : [配信者] " + str;
            snd.sendMessage(msg);
            tfMessage.clear();
        }
    }

    @FXML
    private void keyPressed(KeyEvent ke) { //コメント欄でEnterキーが押されたら送信
        if(ke.getCode().equals(KeyCode.ENTER)) {
            sendMessage();
        }
    }

    void shutdown(){
        if(Sender.isOnline()){
            Sender.setOffline();
        }
    }
}
