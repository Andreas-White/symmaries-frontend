import logic.tool.GraphicalUIHelper;

import java.io.File;
import java.net.URL;

/**
 * Only used for testing the logic, specifically the soot functionality
 */
public class NewMain {

    public static void main(String[] args) {
        URL jarJceLocation = NewMain.class.getResource("C:\\Users\\PC\\Desktop\\dist\\lib\\jce.jar");
        URL jarRtLocation = NewMain.class.getResource("C:\\Users\\PC\\Desktop\\dist\\jre\\lib\\rt.jar");
        URL txtLocation = NewMain.class.getResource("C:\\Users\\PC\\Desktop\\dist\\jre\\lib\\test.txt");
        URL txt2Location = NewMain.class.getResource("C:\\Users\\PC\\Desktop\\qs.txt");

        System.out.println(jarJceLocation);
        System.out.println(jarRtLocation);

        File fileJce = new File("C:\\Users\\PC\\Desktop\\dist\\lib\\jce.jar");
        File fileRt = new File("C:\\Users\\PC\\Desktop\\dist\\jre\\lib\\rt.jar");
        File txt = new File("C:\\Users\\PC\\Desktop\\dist\\jre\\lib\\test.txt");

        if (fileJce.exists() && fileRt.exists()) {
            System.out.println("it exists");
            GraphicalUIHelper graphicalUIHelper = new GraphicalUIHelper(true, false, false);
            graphicalUIHelper.setInputPath("C:\\Users\\PC\\Downloads\\InsectureStore.jar");
            graphicalUIHelper.setTargetPath("C:\\Users\\PC\\Desktop\\Project_Degree\\out");
            graphicalUIHelper.loadApplicationIntoSoot();

            graphicalUIHelper.setSourcesAndSinksPath("C:\\Users\\PC\\Desktop\\Project_Degree\\samples\\sources_sinks\\sampleXML.xml");
            graphicalUIHelper.setSecsumFilePath("C:\\Users\\PC\\Desktop\\Project_Degree\\samples\\secstubs\\all.secstubs");
            graphicalUIHelper.setSymmariesPath("C:\\Users\\PC\\Desktop\\Project_Degree\\samples\\symmaries\\JisymCompiler.jar");
            graphicalUIHelper.setMethodSkipParameter(30);
            graphicalUIHelper.setExceptionEnabeled(false);
            graphicalUIHelper.setTaintCheckingEnabeled(false);
            graphicalUIHelper.runInputGenerator();
        }
        if (fileJce.exists()) {
            System.out.println("jce.jar exists");
        }
        if (fileRt.exists()) {
            System.out.println("rt.jar exists");
        }
        if (txt.exists()) {
            System.out.println("txt exists");
        }
    }
}
