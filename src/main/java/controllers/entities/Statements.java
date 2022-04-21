package controllers.entities;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Statements {

    private StringProperty statement;
    private StringProperty description;
    private StringProperty validity;

    public void setStatement(String value) { statementProperty().set(value); }

    public String getStatement() { return statementProperty().get(); }

    public StringProperty statementProperty() {
        if (statement == null) statement = new SimpleStringProperty(this, "statement");
        return statement;
    }

    public void setDescription(String value) { descriptionProperty().set(value); }

    public String getDescription() { return descriptionProperty().get(); }

    public StringProperty descriptionProperty() {
        if (description == null) description = new SimpleStringProperty(this, "description");
        return description;
    }

    public void setValidity(String value) { validityProperty().set(value); }

    public String getValidity() { return validityProperty().get(); }

    public StringProperty validityProperty() {
        if (validity == null) validity = new SimpleStringProperty(this, "validity");
        return validity;
    }

    public Statements(String statement, String description, String validity) {
        setStatement(statement);
        setDescription(description);
        setValidity(validity);
    }
}
