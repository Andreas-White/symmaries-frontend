package controllers.helpers;

import java.io.BufferedReader;
import java.io.File;
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

    public List<String> displaySecsumFiles(String fileExt)
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
                    .filter(f -> f.endsWith(fileExt))
                    .collect(Collectors.toList());
        }

        return result;
    }

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

    public List<String> getMethodAndClassNames() throws IOException {
        return secsumFiles.stream()
                .map(str -> str.split("\\.")[str.split("\\.").length - 2]).toList();
    }

    public List<String> getClassAndPackageNames() throws IOException {
        List<String> reducedResult = new ArrayList<>();

        for (String s : secsumFiles) {
            reducedResult.add(s.split("\\.")[s.split("\\.").length - 3] + "."
                    + s.split("\\.")[s.split("\\.").length - 2]);
        }

        return reducedResult
                .stream()
                .filter(s -> !s.contains("_")).toList();
    }

    public List<String> getPackageNames() throws IOException {

        List<String> newResults = secsumFiles.stream()
                .map(str -> str.split("\\.")[str.split("\\.").length - 3]).toList();

        Set<String> uniqueNames = new HashSet<>(newResults);
        return new ArrayList<>(uniqueNames);
    }
}
