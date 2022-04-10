package controllers.helpers;

import java.io.IOException;
import java.util.List;

public class TestHelperMethods {

    private static final String secsumFileExt = ".secsum";
    private static final String detailedSecsumFileExt = "-secsum";
    private static final String methFileExt = "meth";

    public static void main(String[] args) throws IOException {
        HelperMethods helperMethods = new HelperMethods();
//        helperMethods.displayFiles();
        List<String> secsumFiles = helperMethods.displayFilesWithSpecificExt(secsumFileExt);
        List<String> detailedSecsumFiles = helperMethods.displayFilesWithSpecificExt(detailedSecsumFileExt);
        List<String> methFiles = helperMethods.displayFilesWithSpecificExt(methFileExt);

        List<String> newDetailedSecsumFiles = detailedSecsumFiles.stream()
                .map(str -> str.split("\\.")[str.split("\\.").length -2]).toList();

        newDetailedSecsumFiles.forEach(System.out::println);

        System.out.println("There are " + secsumFiles.size() + " secsum files");
        System.out.println("There are " + newDetailedSecsumFiles.size() + " detailed secsum files");
        System.out.println("There are " + methFiles.size() + " meth files");
    }
}
