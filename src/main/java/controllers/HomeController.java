package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import logic.tool.GraphicalUIHelper;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    private Stage stage;

    @FXML
    private AnchorPane homePane;

    @FXML
    private TextField txtFieldApp;

    @FXML
    private TextField txtFieldOutput;

    @FXML
    private ChoiceBox<String> typesChoiceBox;

    GraphicalUIHelper graphicalUIHelper;

    final String[] applicationTypes = {"Android Application", "Jar Application", "Java Project"};
    FileChooser.ExtensionFilter extensionFilter;
    boolean isFile = false;
    boolean isOutput = false;

    boolean isJarFile = false;
    boolean isApkFile = false;
    boolean isJavaProjectDirectory = false;

    String applicationPath;
    String outputDirectoryPath;

    /**
     * Logout the user and terminates the program after confirmation
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
     * Implements the onClick functionality of the "Close" menuItem, by calling the logout() method
     */
    public void onLogoutMenuClick() {
        logout(homePane);
    }

    /**
     * Implements the onClick functionality of "Select Application" button, by calling the getFilePath method
     */
    public void onSelectApplicationButton() {
        String title = "Choose the application";
        getFilePath(title, txtFieldApp);
    }

    /**
     * Implements the onClick functionality of "Select Output Directory" button, by calling the getFilePath method.
     */
    public void onSelectOutputButton() {
        String title = "Choose the output directory";
        isOutput = true;
        isFile = false;
        getFilePath(title, txtFieldOutput);
    }

    /**
     * Extracts and assigns the absolute path of the loaded files or directories, according to their file extension
     * @param title the appropriate String for the title of the FileChooser or DirectoryChooser
     * @param textField the appropriate TextField to set its text when the File is loaded
     */
    private void getFilePath(String title, TextField textField) {
        FileChooser fileChooser = new FileChooser();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file;

        if (isFile) {
            fileChooser.setTitle(title);
            fileChooser.getExtensionFilters().add(extensionFilter);
            file = fileChooser.showOpenDialog(stage);
            if (isRightPath(textField, file))
                applicationPath = file.getAbsolutePath();
        } else if(isOutput) {
            directoryChooser.setTitle(title);
            file = directoryChooser.showDialog(stage);
            if (isRightPath(textField, file))
                outputDirectoryPath = file.getAbsolutePath();
        } else {
            directoryChooser.setTitle(title);
            file = directoryChooser.showDialog(stage);
            if (isRightPath(textField, file))
                applicationPath = file.getAbsolutePath();
        }
    }

    /**
     * Checks if the file/directory was loaded successfully
     * @param textField the appropriate TextField to set its text when the File is loaded (same as in getFilePath() method)
     * @param file the loaded file/directory
     * @return true if the file/directory was loaded successfully
     */
    private boolean isRightPath(TextField textField, File file) {
        if (file != null) {
            textField.setText(file.getAbsolutePath());
            return true;
        }
        else {
            textField.setText("Path was not defined, please try again");

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Wrong Path");
            alert.setHeaderText("There was no path selected");
            alert.setContentText("There was an issue with the file's or directory's path selection, please retry or make sure" +
                    " the file or directory exists");

            alert.showAndWait();
        }
        return false;
    }

    /**
     * Assigns the appropriate values to the ChoiceBox
     * @param url the url (not used here directly)
     * @param resourceBundle the resourceBundle (not used here directly)
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        typesChoiceBox.getItems().addAll(applicationTypes);
        typesChoiceBox.setOnAction(this::setApplicationType);
    }

    /**
     * Sets the appropriate file extension according to the selected item of the ChoiceBox
     * @param event to set the onClick event
     */
    private void setApplicationType(ActionEvent event) {
        String applicationType = typesChoiceBox.getValue();
        if (applicationType.equals(applicationTypes[0])) {
            extensionFilter = new FileChooser.ExtensionFilter("APK file", "*.apk", "*.xapk");
            isApkFile = true;
            isFile = true;
        } else if (applicationType.equals(applicationTypes[1])) {
            isJarFile = true;
            extensionFilter = new FileChooser.ExtensionFilter("Jar file", "*.jar");
            isFile = true;
        } else if (applicationType.equals(applicationTypes[2])) {
            isJavaProjectDirectory = true;
            isFile = false;
        }
    }

    /**
     * Set the application and output paths and loads them into Soot
     */
    public void onLoadFilesButton() {
        graphicalUIHelper = new GraphicalUIHelper(isJarFile, isJavaProjectDirectory, isApkFile);
        graphicalUIHelper.setInputPath(applicationPath);
        graphicalUIHelper.setTargetPath(outputDirectoryPath);
        graphicalUIHelper.loadApplicationIntoSoot();
    }
}
