package controllers;

import controllers.helpers.HelperMethods;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class ResultsController extends GeneraMethodsController implements Initializable {

    @FXML
    private TextArea myTextArea;

    @FXML
    private AnchorPane resultsPane;

    @FXML
    private TreeView<String> myTreeView;

    private HelperMethods helperMethods = null;

    /**
     * Implements the onClick functionality of the "Close" menuItem, by calling the logout() method
     */
    public void onLogoutMenuClick() {
        logout(resultsPane);
    }

    public void selectItem() {
        TreeItem<String> item = myTreeView.getSelectionModel().getSelectedItem();

        try {
            helperMethods = new HelperMethods();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (item != null) {
            if (item.getParent() == null) {
                myTextArea.setText(item.getValue());
            } else {
                if (item.getParent().getParent() != null
                        && !Objects.equals(item.getParent().getParent().getValue(), "Root")) {
                    assert helperMethods != null;
                    StringBuilder br = helperMethods.readSecsumFile(item.getParent().getParent().getValue() + '.'
                            + item.getParent().getValue() + '_'
                            + item.getValue(), true);
                    myTextArea.setText(br.toString());
                } else if (item.getParent().getParent() != null
                        && Objects.equals(item.getParent().getParent().getValue(), "Root")) {
                    StringBuilder br = helperMethods.readSecsumFile(item.getParent().getValue() + '.'
                            + item.getValue() + ".secsum", false);
                    myTextArea.setText(br.toString());
                }
            }
        }
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

        try {
            helperMethods = new HelperMethods();
        } catch (IOException e) {
            e.printStackTrace();
        }


        List<String> packageNames = new ArrayList<>();
        try {
            assert helperMethods != null;
            packageNames = helperMethods.getPackageNames();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> classNames = new ArrayList<>();
        try {
            classNames = helperMethods.getClassAndPackageNames();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> methodNames = new ArrayList<>();
        try {
            methodNames = helperMethods.getMethodAndClassNames();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String packageName : packageNames) {
            TreeItem<String> packageItem = new TreeItem<>(packageName, new ImageView(packageIcon));
            rootItem.getChildren().addAll(packageItem);

            for (String classAndPackageName : classNames) {
                String className = classAndPackageName.split("\\.")[1];
                if (packageName.equals(classAndPackageName.split("\\.")[0])) {
                    TreeItem<String> classItem = new TreeItem<>(className, new ImageView(fileIcon));
                    packageItem.getChildren().addAll(classItem);

                    for (String methodName : methodNames) {
                        if (className.equals(methodName.split("_")[0]) && methodName.contains("_")) {
                            TreeItem<String> methodItem =
                                    new TreeItem<>(methodName.substring(className.length() + 1), new ImageView(methodIcon));
                            classItem.getChildren().addAll(methodItem);
                        }
                    }
                }
            }
        }

//        myTreeView.setShowRoot(false);    // for not showing the root
        myTreeView.setRoot(rootItem);
    }
}
