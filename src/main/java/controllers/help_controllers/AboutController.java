package controllers.help_controllers;

import controllers.GeneraMethodsController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.util.ResourceBundle;

public class AboutController extends GeneraMethodsController implements Initializable {

    /***************** Presentation Strings *******************/
    private final String text1Paragraph1 = "Symmaries, a prototype static analysis tool whose primary goal is to automatically detect leaks of confidential data in object-oriented programs, and infer the spread of such secrets over method calls. It is designed to target the specific challenges posed by the programming models often encountered on mobile platforms such as Android: object-oriented programming and message-passing with serialized structured data require the development of dedicated techniques to efficiently and precisely capture the flow of secret information within applications.\n\n";
    private final String text1Title2 = "Context\n\n";
    private final String text1Paragraph2 = "\nSymmaries is currently developed in the Department of Computer Science of the University of Liverpool by Nicolas Berthier, in collaboration with Narges Khakpour from the Department of Computer Science and Media Technology at Linnaeus University. This work started as a new approach to Narges' previous work in the PROSSES project.\n\n";
    private final String text1Title3 = "Working Principles and Techniques\n\n";
    private final String text1MidSentence = "\nThe working principles of symmaries are as follows:\n";
    private final String text1Bullet1 = "\n\u2022 Input methods are given in an assembly-style object-oriented language very similar to Soot's Jimple language, that closely reflects Java bytecode. This allows Symmaries to handle already compiled and optimized applications (e.g. jar, apk);\n";
    private final String text1Bullet2 = "\n\u2022 Each method is considered separately and first translated into a symbolic model. This model assigns security levels to variables and objects, and encodes the control-flow of the method under consideration, as well as intra-procedural aliasing and shape-style analyzes that help capturing direct and indirect information flow through object mutations;\n";
    private final String text1Bullet3 = "\n\u2022 Security concerns (such as data confidentiality) are then translated into objectives expressed in terms of symbols of the model;\n";
    private final String text1Bullet4 = "\n\u2022 Lastly, a Symbolic Discrete Controller Synthesis solver is used to enforce the above objectives. For the model produced by Symmaries, the controller it produces can be interpreted as pre- and post-conditions for a method to ensure the security concerns. These pre- and post-conditions form the security summaries of the method; the security summary of a method m is then re-used to build models and enforce security concerns for methods that may call m. \nSynthesized security summaries enjoy strong properties that stem from guarantees provided by the underlying symbolic discrete controller synthesis algorithm, in the sense that guards are guaranteed to be the weakest pre-conditions, and the post-conditions are the strongest (w.r.t. approximations induced by the aliasing analysis encoded in the models).\n";
    private final String text1Bullet5 = "\n\u2022 The computed security summaries provide useful feedback to application developers in terms of the potential leaks of confidential data in their code. They can also be used to construct dynamic monitors that ensure the absence of such leaks at run-time, in a way similar to what has already been done in the PROSSES project.\n";
    private final String text1MidSentence2 = "\nSymmaries relies on ReaTK to solve the symbolic discrete controller synthesis problems.";

    /***************** Basic Work-flow Strings *******************/
    private final String text2Paragraph1 = "Symmaries can receive as input a description of a complete class and interface hierarchy (file .classes), and a set of methods (files .meth) to be analyzed. It then computes one security summary per method (files .secsum), that consists of:\n\n";
    private final String text2Bullet1 = "\u2022 a guard that represents a pre-condition on the security level of the calling context (pc) and the level of each argument, whose satisfaction guarantees the absence of leak.\n";
    private final String text2Bullet2 = "\u2022 a post-condition given as a set of bindings that symbolically describes:\n";
    private final String text2SubBullet1 = "\t\u25E6 the potential spread of secrets over the returned entity and any object given as argument;\n";
    private final String text2SubBullet2 = "\t\u25E6 the potential heap effect (object mutations and updates to aliasing relations) of the method over any object given as argument and/or returned.\n";
    private final String text2Paragraph2 = "\nThe technique that we employ within Symmaries is modular in the sense that, in order to compute a security summary for a method m that calls other methods, Symmaries reuses the security summaries inferred for the latter to construct the model for the former. This way, ensuring data confidentiality in an application boils down to solve multiple small symbolic discrete controller synthesis problems instead of a single one that would consider information flow within the whole application.\nThe prototype supports further use cases. For instance, it also accepts security summary stubs that are concise descriptions of the behavior of third-party methods provided by the user (see Section Security Summary Stubs for some details). It also features a means to compute (an over-approximation of) an application's callgraph from a given set of methods so as to: (i) compute a security summary for each of the methods involved; and (ii) handle recursion.\n";

    /***************** Preliminary Notations Strings *******************/
    private final String text3Paragraph1 = "This Section gives some preliminary notations required for the detailed description of the results computed by Symmaries\n\n";
    private final String text3Title1 = "References and Associated Relations: \nAlias and Transitive Points-to Atoms\n\n";
    private final String text3Paragraph2 = "\nIn the descriptions below, c\u20D7 denotes an object referenced by a variable c, and as a special case, o\u20D7 denotes the instance object referenced by this in non-static methods. Given two reference variables a and b, a~b denotes that a\u20D7 and b\u20D7 may be the same object (i.e. a and b alias each other). Similarly, a.~>b denotes that a field (or cell) of a\u20D7 may (transitively) reference b\u20D7\n\n";
    private final String text3Title2 = "Security Levels\n\n";
    private final String text3Paragraph3 = "\nSecurity levels belong to the set { ⊥ , ⊤ } . ⊥ denotes low or public level, and ⊤ denotes high or private level. ⊔ is the least upper bound between two security levels (i.e. in our case, a ⊔ b = ⊤ iff a = ⊤ or b = ⊤ ). The inferred security summaries involve the security level of variables (noted level(v) for a primitive or reference variable v) as well as of potentially referenced objects (denoted obj_level(r) for a reference variable r).\n";
    private final String text3SmallerTitle1 = "\nSynonyms:\n";
    private final String text3Paragraph4 = "\nFor convenience Symmaries understands several synonyms for security levels, that can be used in place of ⊤ and ⊥ whenever a constant security level denotation is expected:\n";
    private final String text3Bullet1 = "\n\u2022 ⊥ can be substituted with L, l, B, or b;\n";
    private final String text3Bullet2 = "\n\u2022 ⊤ can be substituted with H, h, T, or t.\n";
    private final String text3Title3 = "\nBindings in Security Summaries\n";
    private final String text3Paragraph5 = "\nThe post-condition expressed in a security summary describes both the spread of secrets and heap effects of the method onto its returned value or object, any object referenced by its arguments, and thrown exceptions. The spread of secrets denotes the change of security levels, whereas the heap effect denotes the impact of the method on both alias and transitive points-to relations. These bindings are all expressed in terms of the security level of the context (denoted pc) and reference arguments, as well as the relations between them, upon method call.";
    private final String text3MidSentence = "The post-condition consists in the two following sets of bindings:\n";
    private final String text3Bullet3 = "\n\u2022 bindings of the form \"ret(…)\" describe the methods' effect when it terminates successfully;\n";
    private final String text3Bullet4 = "\n\u2022 bindings of the form \"exc(…)\" describe the methods' effect when it raises an exception (these bindings are only present when support for exceptions is enabled).\n";
    private final String text3Paragraph6 = "\nWhen appropriate, the bindings also feature the security level of any returned value or reference (as a binding to ret(level())) and object (as a binding to ret(obj_level())). Security levels of thrown exceptions show as bindings to exc(level()) and exc(obj_level()).\n";
    private final String text3Paragraph7 = "For methods that return a reference, bindings of the form ret(~a), for an argument reference a, indicates whether the method's resulting reference may be an alias of argument a. Similarly, ret(.~>a) denotes whether a field or cell of any returned object may transitively point to a\u20D7 ; and conversely for ret(a.~>), that a field or cell of a\u20D7 may transitively point to any returned object.\n";
    private final String text3SmallerTitle2 = "\nRecursive Bindings:\n";
    private final String text3Paragraph8 = "\nIn generated security summaries (especially those generated from stubs — see Section Security Summary Stubs), bound symbols may appear in right-hand side expressions. In such cases, such symbol occurrences can be directly substituted with their corresponding expressions.";

    @FXML
    public RadioButton radioBtn1, radioBtn2, radioBtn3;

    @FXML
    private ImageView myImageView;

    @FXML
    private TextFlow myTextFlow;

    @FXML
    private AnchorPane helpPane;

    public void displayText() {

        /***************** Presentation NODES *******************/
        Text text1 = new Text(text1Paragraph1);
        text1.getStyleClass().add("text-paragraph");

        Label text2 = new Label(text1Title2);
        text2.getStyleClass().add("text-title");

        Text text3 = new Text(text1Paragraph2);
        text3.getStyleClass().add("text-paragraph");

        Label text4 = new Label(text1Title3);
        text4.getStyleClass().add("text-title");

        Text text5 = new Text(text1MidSentence);
        text5.getStyleClass().add("text-paragraph");

        Text text6 = new Text(text1Bullet1);
        text6.getStyleClass().add("text-paragraph");

        Text text7 = new Text(text1Bullet2);
        text7.getStyleClass().add("text-paragraph");

        Text text8 = new Text(text1Bullet3);
        text8.getStyleClass().add("text-paragraph");

        Text text9 = new Text(text1Bullet4);
        text9.getStyleClass().add("text-paragraph");

        Text text10 = new Text(text1Bullet5);
        text10.getStyleClass().add("text-paragraph");

        Text text11 = new Text(text1MidSentence2);
        text11.getStyleClass().add("text-paragraph");

        /***************** Basic Work-flow NODES *******************/
        Text text2_1 = new Text(text2Paragraph1);
        text2_1.getStyleClass().add("text-paragraph");

        Text text2_2 = new Text(text2Bullet1);
        text2_2.getStyleClass().add("text-paragraph");

        Text text2_3 = new Text(text2Bullet2);
        text2_3.getStyleClass().add("text-paragraph");

        Text text2_4 = new Text(text2SubBullet1);
        text2_4.getStyleClass().add("text-paragraph");

        Text text2_5 = new Text(text2SubBullet2);
        text2_5.getStyleClass().add("text-paragraph");

        Text text2_6 = new Text(text2Paragraph2);
        text2_6.getStyleClass().add("text-paragraph");

        /***************** Preliminary Notations NODES *******************/
        Text text3_1 = new Text(text3Paragraph1);
        text3_1.getStyleClass().add("text-paragraph");

        Label text3_2 = new Label(text3Title1);
        text3_2.getStyleClass().add("text-title");

        Text text3_3 = new Text(text3Paragraph2);
        text3_3.getStyleClass().add("text-paragraph");

        Label text3_4 = new Label(text3Title2);
        text3_4.getStyleClass().add("text-title");

        Text text3_5 = new Text(text3Paragraph3);
        text3_5.getStyleClass().add("text-paragraph");

        Label text3_6 = new Label(text3SmallerTitle1);
        text3_6.getStyleClass().add("text-title-sm");

        Text text3_7 = new Text(text3Paragraph4);
        text3_7.getStyleClass().add("text-paragraph");

        Text text3_8 = new Text(text3Bullet1);
        text3_8.getStyleClass().add("text-paragraph");

        Text text3_9 = new Text(text3Bullet2);
        text3_9.getStyleClass().add("text-paragraph");

        Label text3_10 = new Label(text3Title3);
        text3_10.getStyleClass().add("text-title");

        Text text3_11 = new Text(text3Paragraph5);
        text3_11.getStyleClass().add("text-paragraph");

        Text text3_12 = new Text(text3MidSentence);
        text3_12.getStyleClass().add("text-paragraph");

        Text text3_13 = new Text(text3Bullet3);
        text3_13.getStyleClass().add("text-paragraph");

        Text text3_14 = new Text(text3Bullet4);
        text3_14.getStyleClass().add("text-paragraph");

        Text text3_15 = new Text(text3Paragraph6);
        text3_15.getStyleClass().add("text-paragraph");

        Text text3_16 = new Text(text3Paragraph7);
        text3_16.getStyleClass().add("text-paragraph");

        Label text3_17 = new Label(text3SmallerTitle2);
        text3_17.getStyleClass().add("text-title-sm");

        Text text3_18 = new Text(text3Paragraph8);
        text3_18.getStyleClass().add("text-paragraph");

        if (radioBtn1.isSelected()) {
            if (myTextFlow.getChildren() != null) {
                myTextFlow.getChildren().removeAll(myTextFlow.getChildren());
                myTextFlow.getChildren().addAll(text1, text2, text3, text4, text5, text6, text7, text8, text9, text10, text11);
            } else {
                myTextFlow.getChildren().addAll(text1, text2, text3, text4, text5, text6, text7, text8, text9, text10, text11);
            }
        } else if (radioBtn2.isSelected()) {
            if (myTextFlow.getChildren() != null) {
                myTextFlow.getChildren().removeAll(myTextFlow.getChildren());
                myTextFlow.getChildren().addAll(text2_1, text2_2, text2_3, text2_4, text2_5, text2_6);
            } else
                myTextFlow.getChildren().addAll(text2_1, text2_2, text2_3, text2_4, text2_5, text2_6);
        } else if (radioBtn3.isSelected()) {
            if (myTextFlow.getChildren() != null) {
                myTextFlow.getChildren().removeAll(myTextFlow.getChildren());
                myTextFlow.getChildren().addAll(text3_1, text3_2, text3_3, text3_4, text3_5, text3_6, text3_7, text3_8, text3_9, text3_10,
                        text3_11, text3_12, text3_13, text3_14, text3_15, text3_16, text3_17, text3_18);
            } else
                myTextFlow.getChildren().addAll(text3_1, text3_2, text3_3, text3_4, text3_5, text3_6, text3_7, text3_8, text3_9, text3_10,
                        text3_11, text3_12, text3_13, text3_14, text3_15, text3_16, text3_17, text3_18);
        }
    }

    /**
     * Implements the onClick functionality of the "Close" menuItem, by calling the logout() method
     */
    public void onLogoutMenuClick() {
        logout(helpPane);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        URL symmariesImageUrl = getClass().getResource("/images/symmaries.png");

        Image symmariesImage = new Image(String.valueOf(symmariesImageUrl));

        myImageView.setImage(symmariesImage);
    }
}
