package controllers;

import controllers.models.ClassObject;
import controllers.models.MethodObject;
import controllers.models.PackageObject;
import controllers.helper_methods.SecsumFilesParser;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class ResultsController extends GeneraMethodsController implements Initializable {

    private SecsumFilesParser secsumFilesParser = null;

    @FXML
    private TextArea myTextArea;

    @FXML
    private AnchorPane resultsPane;

    @FXML
    private TreeView<String> myTreeView;

    /**
     * Implements the onClick functionality of the "Close" menuItem, by calling the logout() method
     */
    public void onLogoutMenuClick() {
        logout(resultsPane);
    }

    /**
     * Implements the functionality of selected tree item, in order to display the files' context
     */
    public void selectItem() {
        long startTime = System.currentTimeMillis();
        TreeItem<String> item = myTreeView.getSelectionModel().getSelectedItem();

        try {
            secsumFilesParser = new SecsumFilesParser();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<PackageObject> packageObjectList = secsumFilesParser.getAllPackages();

        if (item != null) {
            if (item.getParent().getParent() != null    // Case where the tree item is referring to a method
                    && !Objects.equals(item.getParent().getParent().getValue(), "Root")) {
                for (PackageObject packageObject : packageObjectList) {
                    if (item.getParent().getParent().getValue().equals(packageObject.getName())) {
                        for (ClassObject classObject : packageObject.getClassesList()) {
                            if (item.getParent().getValue().equals(classObject.getName())) {
                                for (MethodObject methodObject : classObject.getMethodsList()) {
                                    if (item.getValue().equals(methodObject.getName())) {
                                        myTextArea.setText(methodObject.getContent());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (item.getParent().getParent() != null    // Case where the tree item is referring to a class
                    && Objects.equals(item.getParent().getParent().getValue(), "Root")) {
                for (PackageObject packageObject : packageObjectList) {
                    if (item.getParent().getValue().equals(packageObject.getName())) {
                        for (ClassObject classObject : packageObject.getClassesList()) {
                            if (item.getValue().equals(classObject.getName())) {
                                myTextArea.setText(classObject.getContent());
                                break;
                            }
                        }
                    }
                }
            }
        }
        System.out.println(System.currentTimeMillis() - startTime + " ms");
    }

    /**
     * Initialize the tree structure with tree items on pane load
     *
     * @param url            default value
     * @param resourceBundle default value
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        URL packageIconLocation = getClass().getResource("/images/package_icon.png");
        URL fileIconLocation = getClass().getResource("/images/java_icon.png");
        URL methodIconLocation = getClass().getResource("/images/method_icon.png");

        Image packageIcon = new Image(String.valueOf(packageIconLocation));
        Image fileIcon = new Image(String.valueOf(fileIconLocation));
        Image methodIcon = new Image(String.valueOf(methodIconLocation));

        TreeItem<String> rootItem = new TreeItem<>("Root", new ImageView(packageIcon));

        try {
            secsumFilesParser = new SecsumFilesParser();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<PackageObject> packageObjectList = secsumFilesParser.getAllPackages();

        // Create package tree items
        for (PackageObject packageObject : packageObjectList) {
            TreeItem<String> packageItem = new TreeItem<>(packageObject.getName(), new ImageView(packageIcon));
            rootItem.getChildren().addAll(packageItem);

            // Create class tree items
            for (ClassObject classObject : packageObject.getClassesList()) {
                TreeItem<String> classItem = new TreeItem<>(classObject.getName(), new ImageView(fileIcon));
                packageItem.getChildren().addAll(classItem);

                // Create method tree items
                for (MethodObject methodObject : classObject.getMethodsList()) {
                    TreeItem<String> methodItem = new TreeItem<>(methodObject.getName(), new ImageView(methodIcon));
                    classItem.getChildren().addAll(methodItem);
                }
            }
        }

        myTreeView.setShowRoot(false);    // for not showing the root
        myTreeView.setRoot(rootItem);
    }
}
