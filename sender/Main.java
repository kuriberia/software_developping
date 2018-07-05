package sender;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Sender");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        primaryStage.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                Controller controller = loader.getController();
                controller.shutdown();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
