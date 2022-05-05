package controllers.helper_methods;

import controllers.models.ClassObject;
import controllers.models.MethodObject;
import controllers.models.PackageObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SecsumFilesParser {

    private final DirectoryReader reader = new DirectoryReader();
    private final String DEFAULT_PATH = "C:\\Users\\PC\\Desktop\\Project_Degree\\ova_files\\symmaries-ant-pr\\Examples\\RadioCRM\\Meth";

    private final List<String> secsumFiles = reader.displayFilesInDirectory("-secsum", DEFAULT_PATH);
    private final List<String> secsumClassFiles = reader.displayFilesInDirectory(".secsum", DEFAULT_PATH);

    public SecsumFilesParser() throws IOException {
    }

    /**
     * Reads the context of the file, so it can be displayed
     * @param fileName a string representing the name of the file
     * @param isMethod a boolean value to check if it is a method or class
     * @return a String object containing the context of the file
     */
    private String readSecsumFile(String fileName, boolean isMethod) {

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

        return builder.toString();
    }

    /**
     * Distinguish the method and class names from the full path of the file
     * @return a list of strings representing the method and class names
     */
    private List<String> getMethodAndClassNames() {
        return secsumFiles.stream()
                .map(str -> str.split("\\.")[str.split("\\.").length - 2]).toList();
    }

    /**
     * Distinguish the class and package names from the full path of the file
     * @return a list of strings representing the class and package names
     */
    private List<String> getClassAndPackageNames() {
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
    private List<String> getPackageNames() {

        List<String> newResults = secsumFiles.stream()
                .map(str -> str.split("\\.")[str.split("\\.").length - 3]).toList();

        Set<String> uniqueNames = new HashSet<>(newResults);
        return new ArrayList<>(uniqueNames);
    }

    public List<PackageObject> getAllPackages() {
        List<PackageObject> packageObjectList = new ArrayList<>();

        List<String> packageNames = getPackageNames();
        List<String> classNames = getClassAndPackageNames();
        List<String> methodNames = getMethodAndClassNames();

        for (String packageName : packageNames) { // Create package object
            PackageObject packageObject = new PackageObject();
            packageObject.setName(packageName);

            List<ClassObject> classObjectList = new ArrayList<>();

            for (String classAndPackageName : classNames) { // Create class object
                String className = classAndPackageName.split("\\.")[1];
                if (packageName.equals(classAndPackageName.split("\\.")[0])) {

                    ClassObject classObject = new ClassObject();
                    classObject.setName(className);
                    classObject.setContent(
                            readSecsumFile(
                                    packageObject.getName() +
                                            "." +
                                            classObject.getName() +
                                            ".secsum",
                                    false));

                    List<MethodObject> methodObjectList = new ArrayList<>();

                    for (String methodName : methodNames) { // Create method object
                        if (className.equals(methodName.split("_")[0]) && methodName.contains("_")) {

                            MethodObject methodObject = new MethodObject();
                            methodObject.setName(methodName.substring(className.length() + 1));
                            methodObject.setContent(
                                    readSecsumFile(
                                            packageObject.getName() +
                                                    '.' + classObject.getName() +
                                                    '_' + methodObject.getName(),
                                            true)
                            );

                            methodObjectList.add(methodObject);
                        }
                    }
                    classObject.setMethodsList(methodObjectList);
                    classObjectList.add(classObject);
                }
            }
            packageObject.setClassesList(classObjectList);
            packageObjectList.add(packageObject);
        }
        return packageObjectList;
    }
}
