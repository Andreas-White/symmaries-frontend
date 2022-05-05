package controllers.help_controllers;

import controllers.GeneraMethodsController;
import controllers.models.Statements;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class StatementsController extends GeneraMethodsController implements Initializable {

    @FXML
    private AnchorPane statementsPane;

    @FXML
    private TextFlow myTextFlow, myTextFlow2;

    /**
     * Implements the onClick functionality of the "Close" menuItem, by calling the logout() method
     */
    public void onLogoutMenuClick() {
        logout(statementsPane);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        myTextFlow.getChildren().add(createTableForStatements());

        Text text = new Text();
        text.setText("The table below describes the statements involved in security summary stubs. In statements, ℓ denotes an optional security level (\"⊥\" or \"⊤\" or their synonyms), \"⊥\" if absent, and τ is an optional taint marker (\"?\"); at least one of them must be present unless a ? is appended. \u03b1  is the least upper bound between the security levels of every argument (primitive value, reference, and referenced objects) of the method, including o\u20D7.");
        text.getStyleClass().add("statement-header");

        myTextFlow2.getChildren().add(text);
    }

    public TableView<Statements> createTableForStatements() {

        List<Statements> statements = List.of(
                new Statements("-<~",
                        "… does not perform any object mutation1",
                        "any method"),
                new Statements("@<~@",
                        "… may only mutate an object referenced by any of its arguments (excluding o\u20D7 )\n" +
                                "with data of security level at most α",
                        "any method"),
                new Statements("@<~ℓτ",
                        "… may only mutate an object referenced by any of its arguments (excluding o\u20D7 )\n" +
                                "with data of security level at most ℓ",
                        "any method"),
                new Statements(".*<~@",
                        "… may only mutate o\u20D7 with data of security level at most α",
                        "non-static"),
                new Statements(".*<~ℓτ",
                        "… may only mutate o\u20D7 with data of security level at most ℓ",
                        "non-static"),
                new Statements("*<~@",
                        "… may only mutate o\u20D7 or an object referenced by any of its arguments with\n" +
                                "data of security level at most α",
                        "any method"),
                new Statements("*<~ℓτ",
                        "… may only mutate o\u20D7 or an object referenced by any of its arguments with\n" +
                                "data of security level at most ℓ",
                        "any method"),
                new Statements("return @",
                        "… returns one of its arguments (including o\u20D7 ), of security level at most α",
                        "non-void"),
                new Statements("return new_ℓτ",
                        "… returns a value, or reference and object, that is distinct from its\n" +
                                "arguments (and that does not depend on any them), of security level at most ℓ",
                        "non-void"),
                new Statements("return new",
                        "… returns a value, or reference and object, that is distinct from its\n" +
                                "arguments (and that does not depend on any them)",
                        "non-void"),
                new Statements("return .*ℓτ?",
                        "… returns a field of → o of security level at most ℓ ⊔ o\u20D7",
                        "non-void-static"),
                new Statements("return *ℓτ?",
                        "… returns a field of → o or one of its arguments, of security level at\n" +
                                "most ℓ ⊔ α ⊔ o\u20D7",
                        "non-void"),
                new Statements("output_⊥",
                        "… may output any of its arguments (including o\u20D7 ) on a public channel",
                        "any method"),
                new Statements("output_⊤",
                        "… may output any of its arguments (including o\u20D7 ) on a private channel",
                        "any method")
        );

        ObservableList<Statements> statementsObservableList = FXCollections.observableArrayList(statements);

        TableView<Statements> table = new TableView<>(statementsObservableList);

        TableColumn<Statements, String> statement = new TableColumn<>("Statement");
        statement.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatement()));
        TableColumn<Statements, String> description = new TableColumn<>("Description: The method…");
        description.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        TableColumn<Statements, String> validity = new TableColumn<>("Validity");
        validity.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValidity()));

        statement.getStyleClass().add("statement");
        table.getColumns().setAll(statement, description, validity);
        table.setPrefWidth(800.0);
        table.setPrefHeight(560.0);
        table.getStyleClass().add("table-view");

        return table;
    }
}
