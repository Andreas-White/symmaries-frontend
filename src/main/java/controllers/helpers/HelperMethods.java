package controllers.helpers;

import java.io.BufferedReader;
import java.io.FileReader;
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
    private final List<String> secsumFiles = displaySecsumFiles("-secsum");
    private final List<String> secsumClassFiles = displaySecsumFiles(".secsum");

    public HelperMethods() throws IOException {
    }

    /**
     * Reads all the files in directory according to the specified file extension
     *
     * @param fileExt the type of file extension of the files to be displayed
     * @return a list of strings representing all the files in the directory
     */
    public List<String> displaySecsumFiles(String fileExt) {

        Path path = Paths.get(DEFAULT_PATH);
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path must be a directory!");
        }

        List<String> result = new ArrayList<>();

        try (Stream<Path> walk = Files.walk(path)) {
            result = walk
                    .filter(p -> !Files.isDirectory(p))
                    .map(Path::toString)
                    .filter(f -> f.endsWith(fileExt))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Reads the context of the file, so it can be displayed
     * @param fileName a string representing the name of the file
     * @param isMethod a boolean value to check if it is a method or class
     * @return a StringBuilder object containing the context of the file
     */
    public StringBuilder readSecsumFile(String fileName, boolean isMethod) {

        String fullPathFile = isMethod ?
                secsumFiles
                        .stream()
                        .filter(s -> s.contains(fileName)).findFirst().orElse("there is no such file") :
                secsumClassFiles
                        .stream()
                        .filter(s -> s.contains(fileName)).findFirst().orElse("there is no such file");

        String line;
        StringBuilder builder = new StringBuilder();

        try {
            FileReader fr = new FileReader(fullPathFile);
            BufferedReader br = new BufferedReader(fr);

            while ((line = br.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return builder;
    }

    /**
     * Distinguish the method and class names from the full path of the file
     * @return a list of strings representing the method and class names
     */
    public List<String> getMethodAndClassNames() {
        return secsumFiles.stream()
                .map(str -> str.split("\\.")[str.split("\\.").length - 2]).toList();

    }

    /**
     * Distinguish the class and package names from the full path of the file
     * @return a list of strings representing the class and package names
     */
    public List<String> getClassAndPackageNames() {
        List<String> reducedResult = new ArrayList<>();

        for (String s : secsumFiles) {
            reducedResult.add(s.split("\\.")[s.split("\\.").length - 3] + "."
                    + s.split("\\.")[s.split("\\.").length - 2]);
        }

        return reducedResult
                .stream()
                .filter(s -> !s.contains("_")).toList();
    }

    /**
     * Distinguish the package names from the full path of the file
     * @return a list of strings package names
     */
    public List<String> getPackageNames() {

        List<String> newResults = secsumFiles.stream()
                .map(str -> str.split("\\.")[str.split("\\.").length - 3]).toList();

        Set<String> uniqueNames = new HashSet<>(newResults);
        return new ArrayList<>(uniqueNames);
    }
}
