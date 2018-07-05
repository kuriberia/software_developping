package reciever;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    //Create JavaFX window
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("App");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        primaryStage.showingProperty().addListener((observable, oldValue, newValue) -> {    //CloseEvent
            if (oldValue == true && newValue == false) {
                Controller controller = loader.getController();
                controller.shutdown();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
