package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import logic.tool.GraphicalUIHelper;

import java.io.File;

public class ConfigController extends GeneraMethodsController {

    private String sourcesAndSinksFilePath;
    private String secstubsFilePath;
    int methodSkipParameter;

    @FXML
    private AnchorPane configPane;

    @FXML
    private TextField txtFieldSourcesAndSinks, txtFieldSecstubsFile;

    @FXML
    private RadioButton exceptionsRadBtn, taintRadBtn;

    @FXML
    private Slider parameterSlider;

    public void onSelectSecstubsFileButton() {
        String title = "Select the .secstubs file";
        secstubsFilePath = getFilePath(title, txtFieldSecstubsFile, new FileChooser.ExtensionFilter("secstubs",
                "*.secstubs"));
    }

    public void onSelectSourcesAndSinksFileButton() {
        String title = "Select the Sources and Sinks xml file";
        sourcesAndSinksFilePath = getFilePath(title, txtFieldSourcesAndSinks, new FileChooser.ExtensionFilter("Sources and Sinks file",
                "*.xml"));
    }

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

    private Integer getMethodSkipParameter() {
        parameterSlider.valueProperty()
                .addListener((observableValue, number, t1) -> methodSkipParameter = ((int) parameterSlider.getValue()));
        return methodSkipParameter;
    }

    public boolean onExceptionSelected() {
        return exceptionsRadBtn.isSelected();
    }

    public boolean onTaintSelection() {
        return taintRadBtn.isSelected();
    }

    public void onInputGeneratorButton() {
        getGraphicalUIHelper().setSourcesAndSinksPath(sourcesAndSinksFilePath);
        getGraphicalUIHelper().setSecsumFilePath(secstubsFilePath);
        getGraphicalUIHelper().setSymmariesPath("C:\\Users\\PC\\Desktop\\Project_Degree\\samples\\symmaries\\JisymCompiler.jar");
        getGraphicalUIHelper().setMethodSkipParameter(getMethodSkipParameter());
        getGraphicalUIHelper().setExceptionEnabeled(onExceptionSelected());
        getGraphicalUIHelper().setTaintCheckingEnabeled(onTaintSelection());
        getGraphicalUIHelper().runInputGenerator();
    }

    public void onLogoutMenuClick() {
        logout(configPane);
    }
}
