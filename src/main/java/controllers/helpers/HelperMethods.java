package controllers.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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

    public List<String> displayFilesWithSpecificExt(String fileExtension)
            throws IOException {

        Path path = Paths.get(DEFAULT_PATH);
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path must be a directory!");
        }

        List<String> result;

        try (Stream<Path> walk = Files.walk(path)) {
            result = walk
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> p.toString().toLowerCase())
                    .filter(f -> f.endsWith(fileExtension))
                    .collect(Collectors.toList());
        }

        return result;
    }
}
