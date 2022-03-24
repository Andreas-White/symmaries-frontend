package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import logic.tool.GraphicalUIHelper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class GeneraMethodsController {

    protected Stage stage;
    private static GraphicalUIHelper graphicalUIHelper;

    private boolean isJarFile;
    private boolean isApkFile;
    private boolean isJavaProjectDirectory;

    public GeneraMethodsController() {
    }

    public GraphicalUIHelper getGraphicalUIHelper() {
        return graphicalUIHelper;
    }

    public void setGraphicalUIHelper(GraphicalUIHelper graphicalUIHelper) {
        this.graphicalUIHelper = graphicalUIHelper;
    }

    public boolean isJarFile() {
        return isJarFile;
    }

    public boolean isApkFile() {
        return isApkFile;
    }

    public boolean isJavaProjectDirectory() {
        return isJavaProjectDirectory;
    }

    public void setJarFile(boolean jarFile) {
        isJarFile = jarFile;
    }

    public void setApkFile(boolean apkFile) {
        isApkFile = apkFile;
    }

    public void setJavaProjectDirectory(boolean javaProjectDirectory) {
        isJavaProjectDirectory = javaProjectDirectory;
    }

    @FXML
    private MenuItem menuItemLogin, menuItemScene2, menuItemScene3, menuItemScene4;

    public void initialiseStage(ActionEvent event, String fxmlPath ) {

        try {
            URL fxmlLocation = getClass().getResource(fxmlPath);
            String cssLocation = Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm();

            assert fxmlLocation != null;
            Parent root = FXMLLoader.load(fxmlLocation);

            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(cssLocation);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initialiseStageFromMenuItem(String fxmlPath, MenuItem menuItem) {

        try {
            URL fxmlLocation = getClass().getResource(fxmlPath);
            String cssLocation = Objects.requireNonNull(getClass().getResource("src/main/resources/css/style.css")).toExternalForm();

            assert fxmlLocation != null;
            Parent root = FXMLLoader.load(fxmlLocation);

            stage = (Stage)menuItem.getParentPopup().getOwnerWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(cssLocation);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    @FXML
    public void getToLogin() {
        String fxmlPath = "/com/example/hellofx/hello-view.fxml";
        initialiseStageFromMenuItem(fxmlPath, menuItemLogin);
    }

    @FXML
    public void getToScene2() {
        String fxmlPath = "/com/example/hellofx/fxml/scene2.fxml";
        initialiseStageFromMenuItem(fxmlPath, menuItemScene2);
    }

    @FXML
    public void getToScene3() {
        String fxmlPath = "/com/example/hellofx/fxml/scene3.fxml";
        initialiseStageFromMenuItem(fxmlPath, menuItemScene3);
    }

    @FXML
    public void getToScene4() {
        String fxmlPath = "/com/example/hellofx/fxml/scene4.fxml";
        initialiseStageFromMenuItem(fxmlPath, menuItemScene4);
    }

    /**
     * Checks if the file/directory was loaded successfully
     *
     * @param textField the appropriate TextField to set its text when the File is loaded (same as in getFilePath() method)
     * @param file      the loaded file/directory
     * @return true if the file/directory was loaded successfully
     */
    protected boolean isRightPath(TextField textField, File file) {
        if (file != null) {
            textField.setText(file.getAbsolutePath());
            return true;
        } else {
            textField.setText("Path was not defined, please try again");

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Path");
            alert.setHeaderText("There was no path selected");
            alert.setContentText("There was an issue with the file's or directory's path selection, please retry or make sure" +
                    " the file or directory exists");

            alert.showAndWait();
        }
        return false;
    }
}
