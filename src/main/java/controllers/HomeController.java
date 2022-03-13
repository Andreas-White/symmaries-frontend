package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class HomeController {

    private Stage stage;

    @FXML
    private AnchorPane homePane;

    @FXML
    private Label myLabel;

    public void logout(Pane scene) {
        try {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Logout");
            alert.setHeaderText("You are about to logout");
            alert.setContentText("Do you want to save before exiting");

            if (alert.showAndWait().get() == ButtonType.OK) {
                stage = (Stage) scene.getScene().getWindow();
                stage.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Logout the user and terminates the program after confirmation
     */
    @FXML
    public void onLogoutMenuClick() {
        logout(homePane);
    }

    public void initialize() {
    }
}
