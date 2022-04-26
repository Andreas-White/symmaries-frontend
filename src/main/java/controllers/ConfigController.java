package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

import java.io.File;

public class ConfigController extends GeneraMethodsController {

    private final String SYMMARIES_PATH = "C:\\Users\\PC\\Desktop\\Project_Degree\\samples\\symmaries\\JisymCompiler.jar";
    private String sourcesAndSinksFilePath;
    private String secstubsFilePath;
    int methodSkipParameter;

    @FXML
    private AnchorPane configPane;

    @FXML
    private TextField txtFieldSourcesAndSinks, txtFieldSecstubsFile;

    @FXML
    private RadioButton exceptionsRadBtn, taintRadBtn, expConfidentialityRadBtn, impConfidentialityRadBtn;

    @FXML
    private Slider parameterSlider;

    /**
     * Loads the secstubs file
     */
    public void onSelectSecstubsFileButton() {
        String title = "Select the .secstubs file";
        secstubsFilePath = getFilePath(title, txtFieldSecstubsFile, new FileChooser.ExtensionFilter("secstubs",
                "*.secstubs"));
    }

    /**
     * Loads the xml file for the Sources and Sinks
     */
    public void onSelectSourcesAndSinksFileButton() {
        String title = "Select the Sources and Sinks xml file";
        sourcesAndSinksFilePath = getFilePath(title, txtFieldSourcesAndSinks, new FileChooser.ExtensionFilter("Sources and Sinks file",
                "*.xml"));
    }

    /**
     * Gets the file path of the loaded file
     *
     * @param title           a String for displaying information to the user on FileChooser
     * @param textField       the TextField that displays the path of the loaded file
     * @param extensionFilter the file extension to restrict the available type of files
     * @return a String for the path of the loaded file
     */
    private String getFilePath(String title,
                               TextField textField,
                               FileChooser.ExtensionFilter extensionFilter) {
        FileChooser fileChooser = new FileChooser();
        File file;
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(extensionFilter);
        file = fileChooser.showOpenDialog(stage);
        if (isRightPath(textField, file))
            return file.getAbsolutePath();
        return "Incorrect file path";
    }

    /**
     * Gets the value of slider
     *
     * @return an Integer
     */
    private Integer getMethodSkipParameter() {
        parameterSlider.valueProperty()
                .addListener((observableValue, number, t1) -> methodSkipParameter = ((int) parameterSlider.getValue()));
        return methodSkipParameter;
    }

    /**
     * Checks if the exception radio button is selected
     *
     * @return true if it is selected
     */
    public boolean onExceptionSelected() {
        return exceptionsRadBtn.isSelected();
    }

    /**
     * Checks if the taint radio button is selected
     *
     * @return true if it is selected
     */
    public boolean onTaintSelection() {
        return taintRadBtn.isSelected();
    }

    /**
     * Checks if the Explicit Confidentiality radio button is selected
     *
     * @return true if it is selected
     */
    public boolean onExplicitConfidentialitySelection() {
        return expConfidentialityRadBtn.isSelected();
    }

    /**
     * Checks if the Implicit Confidentiality radio button is selected
     *
     * @return true if it is selected
     */
    public boolean onImplicitConfidentialitySelection() {
        return impConfidentialityRadBtn.isSelected();
    }

    /**
     * Sets sources and sinks file path, secstubs file path, method skip parameter, exception and taint. Then runs
     * Input Generator
     */
    public void onInputGeneratorButton() {
        getGraphicalUIHelper().setSourcesAndSinksPath(sourcesAndSinksFilePath);
        getGraphicalUIHelper().setSecsumFilePath(secstubsFilePath);
        getGraphicalUIHelper().setSymmariesPath(SYMMARIES_PATH);
        getGraphicalUIHelper().setMethodSkipParameter(getMethodSkipParameter());
        getGraphicalUIHelper().setExceptionEnabeled(onExceptionSelected());
        getGraphicalUIHelper().setTaintCheckingEnabeled(onTaintSelection());
        getGraphicalUIHelper().setExplicitConfEnabled(onExplicitConfidentialitySelection());
        getGraphicalUIHelper().setImplicitConfEnabled(onImplicitConfidentialitySelection());
        getGraphicalUIHelper().runInputGenerator();
//        getGraphicalUIHelper().runSymmaries();
    }

    /**
     * Implements the onClick functionality of the "Close" menuItem, by calling the logout() method
     */
    public void onLogoutMenuClick() {
        logout(configPane);
    }
}
