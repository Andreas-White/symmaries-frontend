package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import logic.tool.GraphicalUIHelper;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class HomeController extends GeneraMethodsController implements Initializable {

    private final String[] applicationTypes = {"Android Application", "Jar Application", "Java Project"};
    FileChooser.ExtensionFilter extensionFilter;
    private boolean isFile = false;
    private boolean isOutput = false;

    @FXML
    private AnchorPane loadPane;

    @FXML
    private TextField txtFieldApp;

    @FXML
    private TextField txtFieldOutput;

    @FXML
    private ChoiceBox<String> typesChoiceBox;

    /**
     * Implements the onClick functionality of the "Close" menuItem, by calling the logout() method
     */
    public void onLogoutMenuClick() {
        logout(loadPane);
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
     *
     * @param title     the appropriate String for the title of the FileChooser or DirectoryChooser
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
                setApplicationPath(file.getAbsolutePath());
        } else if (isOutput) {
            directoryChooser.setTitle(title);
            file = directoryChooser.showDialog(stage);
            if (isRightPath(textField, file))
                setOutputDirectoryPath(file.getAbsolutePath());
        } else {
            directoryChooser.setTitle(title);
            file = directoryChooser.showDialog(stage);
            if (isRightPath(textField, file))
                setApplicationPath(file.getAbsolutePath());
        }
    }

    /**
     * Assigns the appropriate values to the ChoiceBox
     *
     * @param url            the url (not used here directly)
     * @param resourceBundle the resourceBundle (not used here directly)
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        typesChoiceBox.getItems().addAll(applicationTypes);
        typesChoiceBox.setOnAction(this::setApplicationType);
    }

    /**
     * Sets the appropriate file extension according to the selected item of the ChoiceBox
     *
     * @param event to set the onClick event
     */
    private void setApplicationType(ActionEvent event) {
        String applicationType = typesChoiceBox.getValue();
        if (applicationType.equals(applicationTypes[0])) {
            extensionFilter = new FileChooser.ExtensionFilter("APK file", "*.apk", "*.xapk");
            setApkFile(true);
            setJarFile(false);
            setJavaProjectDirectory(false);
            isFile = true;
        } else if (applicationType.equals(applicationTypes[1])) {
            setJarFile(true);
            setApkFile(false);
            setJavaProjectDirectory(false);
            extensionFilter = new FileChooser.ExtensionFilter("Jar file", "*.jar", "*.war");
            isFile = true;
        } else if (applicationType.equals(applicationTypes[2])) {
            setJavaProjectDirectory(true);
            setApkFile(false);
            setJarFile(false);
            isFile = false;
        }
    }

    /**
     * Set the application and output paths and loads them into Soot
     */
    public void onLoadFilesButton(ActionEvent event) {
        GraphicalUIHelper graphicalUIHelper = new GraphicalUIHelper(isJarFile(),isJavaProjectDirectory(),isApkFile());
        setGraphicalUIHelper(graphicalUIHelper);
        getGraphicalUIHelper().setInputPath(getApplicationPath());
        getGraphicalUIHelper().setTargetPath(getOutputDirectoryPath());
        getGraphicalUIHelper().loadApplicationIntoSoot();
        System.out.println("in load");

        String fxmlPath = "/fxml/config.fxml";
        initialiseStage(event, fxmlPath);
        stage.setTitle("Configurations");
    }
}
