package controllers;

import controllers.helpers.HelperMethods;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ResultsController extends GeneraMethodsController implements Initializable {

    @FXML
    private AnchorPane resultsPane;

    @FXML
    private Label myLabel;

    @FXML
    private TreeView<String> myTreeView;

    /**
     * Implements the onClick functionality of the "Close" menuItem, by calling the logout() method
     */
    public void onLogoutMenuClick() {
        logout(resultsPane);
    }

    public void selectItem() {
        TreeItem<String> item = myTreeView.getSelectionModel().getSelectedItem();

        if (item != null)
            myLabel.setText(item.getValue());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        URL packageIconLocation = getClass().getResource("/images/package_icon.png");
        URL fileIconLocation = getClass().getResource("/images/java_icon.png");
        URL methodIconLocation = getClass().getResource("/images/method_icon.png");

        Image packageIcon = new Image(String.valueOf(packageIconLocation));
        Image fileIcon = new Image(String.valueOf(fileIconLocation));
        Image methodIcon = new Image(String.valueOf(methodIconLocation));

        TreeItem<String> rootItem = new TreeItem<>("Root", new ImageView(packageIcon));

        TreeItem<String> packageItem = new TreeItem<>("Package", new ImageView(packageIcon));
        TreeItem<String> fileItem = new TreeItem<>("Java File", new ImageView(fileIcon));

        HelperMethods helperMethods = new HelperMethods();
        List<String> methodNames = new ArrayList<>();
        try {
            methodNames = helperMethods.displayFilesWithSpecificExt("-secsum");
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String methodName: methodNames) {
            TreeItem<String> methodItem = new TreeItem<>(methodName, new ImageView(methodIcon));
            fileItem.getChildren().addAll(methodItem);
        }
//        TreeItem<String> methodItem = new TreeItem<>("Java Method", new ImageView(methodIcon));

//        fileItem.getChildren().addAll(methodItem);
        packageItem.getChildren().addAll(fileItem);

        rootItem.getChildren().addAll(packageItem);

//        myTreeView.setShowRoot(false);    // for not showing the root
        myTreeView.setRoot(rootItem);
    }
}
