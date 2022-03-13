import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        URL imgLocation = getClass().getResource("images/logo.png");
        Image icon = new Image(String.valueOf(imgLocation));

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("fxml/home.fxml")));
        primaryStage.setTitle("Symmaries");
        primaryStage.getIcons().add(icon);
        primaryStage.setHeight(800);
        primaryStage.setWidth(1200);
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        primaryStage.setOnCloseRequest(windowEvent -> {
            windowEvent.consume(); // It consumes the event in case the user presses CANCEL
            logout(primaryStage);
        });
    }

    /**
     * Prompts the user to confirm in order to terminate the program
     * @param stage the stage to be closed
     */
    public void logout(Stage stage) {
        try {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Logout");
            alert.setHeaderText("You are about to logout");
            alert.setContentText("If you log out without saving you will lose all your progress");

            if (alert.showAndWait().get() == ButtonType.OK) {
                stage.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
