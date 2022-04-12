package controllers.helpers;

import java.io.IOException;
import java.util.List;

public class TestHelperMethods {

    public static void main(String[] args) throws IOException {
        HelperMethods helperMethods = new HelperMethods();

        List<String> detailedSecsumFiles = helperMethods.displaySecsumFiles();

        List<String> newDetailedSecsumFiles = detailedSecsumFiles.stream()
                .map(str -> str.split("\\.")[str.split("\\.").length -2]).toList();

        newDetailedSecsumFiles.forEach(System.out::println);

        System.out.println("There are " + newDetailedSecsumFiles.size() + " detailed secsum files");
    }
}
