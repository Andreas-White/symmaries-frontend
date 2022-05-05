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

    private static boolean isJarFile;
    private static boolean isApkFile;
    private static boolean isJavaProjectDirectory;

    private static String applicationPath;
    private static String outputDirectoryPath;

    @FXML
    private MenuItem menuItemLogin, menuItemConfig, menuItemResults, menuItemScene4, menuItemAbout , menuItemStatements;

    public GraphicalUIHelper getGraphicalUIHelper() {
        return graphicalUIHelper;
    }

    public void setGraphicalUIHelper(GraphicalUIHelper graphicalUIHelper) {
        GeneraMethodsController.graphicalUIHelper = graphicalUIHelper;
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

    public static String getApplicationPath() {
        return applicationPath;
    }

    public static void setApplicationPath(String applicationPath) {
        GeneraMethodsController.applicationPath = applicationPath;
    }

    public static String getOutputDirectoryPath() {
        return outputDirectoryPath;
    }

    public static void setOutputDirectoryPath(String outputDirectoryPath) {
        GeneraMethodsController.outputDirectoryPath = outputDirectoryPath;
    }

    /**
     * Initialise the primary stage, used to load different stages and other files like css.
     * It is triggered on button action
     *
     * @param event    the event triggered by the button action
     * @param fxmlPath the fxml file path to be loaded
     */
    public void initialiseStage(ActionEvent event, String fxmlPath) {

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

    /**
     * Initialise the primary stage, used to load different stages and other files like css.
     * It is triggered on menu-item action.
     *
     * @param fxmlPath the fxml file path to be loaded
     * @param menuItem the event triggered by the menu-item action
     */
    public void initialiseStageFromMenuItem(String fxmlPath, MenuItem menuItem) {

        try {
            URL fxmlLocation = getClass().getResource(fxmlPath);
            String cssLocation = Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm();

            assert fxmlLocation != null;
            Parent root = FXMLLoader.load(fxmlLocation);

            stage = (Stage) menuItem.getParentPopup().getOwnerWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(cssLocation);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Implements the logout functionality and alerts the user
     * Works with exit button and Alt+F4
     *
     * @param scene
     */
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
     * Gets to the Load Project pane
     */
    @FXML
    public void getToLoadProject() {
        String fxmlPath = "/fxml/home.fxml";
        initialiseStageFromMenuItem(fxmlPath, menuItemLogin);
    }

    /**
     * Gets to the Configuration pane
     */
    @FXML
    public void getToConfig() {
        String fxmlPath = "/fxml/config.fxml";
        initialiseStageFromMenuItem(fxmlPath, menuItemConfig);
    }

    /**
     * Gets to the Results pane
     */
    @FXML
    public void getToResults() {
        String fxmlPath = "/fxml/results.fxml";
        initialiseStageFromMenuItem(fxmlPath, menuItemResults);
    }

    /**
     * To be implemented
     */
    @FXML
    public void getToScene4() {
        String fxmlPath = "/fxml/scene4.fxml";
        initialiseStageFromMenuItem(fxmlPath, menuItemScene4);
    }

    /**
     * Gets to the About pane
     */
    @FXML
    public void getToAbout() {
        String fxmlPath = "/fxml/about.fxml";
        initialiseStageFromMenuItem(fxmlPath, menuItemAbout);
    }

    /**
     * Gets to the Statements pane
     */
    @FXML
    public void getToStatements() {
        String fxmlPath = "/fxml/statements.fxml";
        initialiseStageFromMenuItem(fxmlPath, menuItemStatements);
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
