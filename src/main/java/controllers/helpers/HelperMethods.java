package controllers.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HelperMethods {

    private final String DEFAULT_PATH = "C:\\Users\\PC\\Desktop\\Project_Degree\\ova_files\\symmaries-ant-pr\\Examples\\RadioCRM\\Meth";

    public void displayFiles() {
        // creates a file object
        File file = new File(DEFAULT_PATH);

        // returns an array of all files
        String[] fileList = file.list();

        assert fileList != null;
        for (String str : fileList) {
            System.out.println(str);
        }
        System.out.println(fileList.length);
    }

    public List<String> displaySecsumFiles()
            throws IOException {

        Path path = Paths.get(DEFAULT_PATH);
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path must be a directory!");
        }

        List<String> result;

        try (Stream<Path> walk = Files.walk(path)) {
            result = walk
                    .filter(p -> !Files.isDirectory(p))
                    .map(Path::toString)
                    .filter(f -> f.endsWith("-secsum"))
                    .collect(Collectors.toList());
        }

        return result;
    }

    public List<String> getMethodAndClassNames() throws IOException {
        List<String> result = displaySecsumFiles();

        List<String> newResults = result.stream()
                .map(str -> str.split("\\.")[str.split("\\.").length - 2]).toList();
        return newResults;
    }

    public List<String> getClassAndPackageNames() throws IOException {
        List<String> result = displaySecsumFiles();

        List<String> reducedResult = new ArrayList<>();

        for (String s: result) {
            reducedResult.add(s.split("\\.")[s.split("\\.").length - 3] + "."
                    + s.split("\\.")[s.split("\\.").length - 2]);
        }

        List<String> newResults = new ArrayList<>();

        for (String s: reducedResult) {
            if (!s.contains("_"))
                newResults.add(s);
        }
        return newResults;
    }

    public List<String> getPackageNames() throws IOException {
        List<String> result = displaySecsumFiles();

        List<String> newResults = result.stream()
                .map(str -> str.split("\\.")[str.split("\\.").length - 3]).toList();

        Set<String> uniqueNames = new HashSet<>(newResults);

        List<String> newUniqueResults = new ArrayList<>(uniqueNames);

        newUniqueResults.forEach(System.out::println);
        return newUniqueResults;
    }
}
