package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class HelpController extends GeneraMethodsController {


    private final String text1 = "This page presents scgs, a prototype static analysis tool whose primary goal is to automatically detect leaks of confidential data in object-oriented programs, and infer the spread of such secrets over method calls. It is designed to target the specific challenges posed by the programming models often encountered on mobile platforms such as Android: object-oriented programming and message-passing with serialized structured data require the development of dedicated techniques to efficiently and precisely capture the flow of secret information within applications.";
    private final String text2 = "scgs is currently developed in the Department of Computer Science of the University of Liverpool by Nicolas Berthier, in collaboration with Narges Khakpour from the Department of Computer Science and Media Technology at Linnaeus University. This work started as a new approach to Narges' previous work in the PROSSES project.";
    private final String text3 = "The technique that we employ within scgs is modular in the sense that, in order to compute a security summary for a method m that calls other methods, scgs reuses the security summaries inferred for the latter to construct the model for the former. This way, ensuring data confidentiality in an application boils down to solve multiple small symbolic discrete controller synthesis problems instead of a single one that would consider information flow within the whole application. The prototype supports further use cases. For instance, it also accepts security summary stubs that are concise descriptions of the behavior of third-party methods provided by the user (see Section Security Summary Stubs for some details). It also features a means to compute (an over-approximation of) an application's callgraph from a given set of methods so as to: (i) compute a security summary for each of the methods involved; and (ii) handle recursion. The technique that we employ within scgs is modular in the sense that, in order to compute a security summary for a method m that calls other methods, scgs reuses the security summaries inferred for the latter to construct the model for the former. This way, ensuring data confidentiality in an application boils down to solve multiple small symbolic discrete controller synthesis problems instead of a single one that would consider information flow within the whole application. The prototype supports further use cases. For instance, it also accepts security summary stubs that are concise descriptions of the behavior of third-party methods provided by the user (see Section Security Summary Stubs for some details). It also features a means to compute (an over-approximation of) an application's callgraph from a given set of methods so as to: (i) compute a security summary for each of the methods involved; and (ii) handle recursion. The technique that we employ within scgs is modular in the sense that, in order to compute a security summary for a method m that calls other methods, scgs reuses the security summaries inferred for the latter to construct the model for the former. This way, ensuring data confidentiality in an application boils down to solve multiple small symbolic discrete controller synthesis problems instead of a single one that would consider information flow within the whole application. The prototype supports further use cases. For instance, it also accepts security summary stubs that are concise descriptions of the behavior of third-party methods provided by the user (see Section Security Summary Stubs for some details). It also features a means to compute (an over-approximation of) an application's callgraph from a given set of methods so as to: (i) compute a security summary for each of the methods involved; and (ii) handle recursion.";
    private final String text4 = "The post-condition expressed in a security summary describes both the spread of secrets and heap effects of the method onto its returned value or object, any object referenced by its arguments, and thrown exceptions. The spread of secrets denotes the change of security levels, whereas the heap effect denotes the impact of the method on both alias and transitive points-to relations. These bindings are all expressed in terms of the security level of the context (denoted pc) and reference arguments, as well as the relations between them, upon method call.";

    @FXML
    public RadioButton radioBtn1, radioBtn2, radioBtn3, radioBtn4;

    @FXML
    private TextArea myTextArea;

    @FXML
    private TextFlow myTextFlow;

    @FXML
    private Text myText;

    @FXML
    private AnchorPane helpPane;

    public void displayText() {
        if (radioBtn1.isSelected()) {
            myText.setText(text1);
            myTextArea.setText(text1);
            if (myTextFlow.getChildren() != null) {
                myTextFlow.getChildren().removeAll(myTextFlow.getChildren());
                myTextFlow.getChildren().add(myText);
            } else
                myTextFlow.getChildren().add(myText);
        } else if (radioBtn2.isSelected()) {
            myText.setText(text2);
            myTextArea.setText(text2);
            if (myTextFlow.getChildren() != null) {
                myTextFlow.getChildren().removeAll(myTextFlow.getChildren());
                myTextFlow.getChildren().add(myText);
            } else
                myTextFlow.getChildren().add(myText);
        } else if (radioBtn3.isSelected()) {
            myText.setText(text3);
            myTextArea.setText(text3);
            if (myTextFlow.getChildren() != null) {
                myTextFlow.getChildren().removeAll(myTextFlow.getChildren());
                myTextFlow.getChildren().add(myText);
            } else
                myTextFlow.getChildren().add(myText);
        } else if (radioBtn4.isSelected()) {
            myText.setText(text4);
            myTextArea.setText(text4);
            if (myTextFlow.getChildren() != null) {
                myTextFlow.getChildren().removeAll(myTextFlow.getChildren());
                myTextFlow.getChildren().add(myText);
            } else
                myTextFlow.getChildren().add(myText);
        }
    }

    /**
     * Implements the onClick functionality of the "Close" menuItem, by calling the logout() method
     */
    public void onLogoutMenuClick() {
        logout(helpPane);
    }
}
