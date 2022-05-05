package controllers.helper_methods;

import controllers.models.ClassObjectDTO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class MethodExtractor {

    // To be replaced with applicationPath
    private static final String JAR_WAR_FILE_PATH = "C:\\Users\\PC\\Downloads\\testjar\\RadioCRM.war";
    // To be replaced with applicationPath
    private static final String JAVA_PROJECT_PATH = "C:\\Users\\PC\\Downloads\\testjar\\RadioCRM";
    // To be replaced with applicationPath + test
    private static final String EXTRACTED_JAVA_FILES_PATH = "C:\\Users\\PC\\Downloads\\testjar\\test";

    public List<ClassObjectDTO> getAllMethodsFromJarFile() throws IOException {

        JarExtractor jarExtractor = new JarExtractor();
        jarExtractor.extractClassFiles(JAR_WAR_FILE_PATH, EXTRACTED_JAVA_FILES_PATH);

        DirectoryReader directoryReader = new DirectoryReader();
        List<String> classes = directoryReader.displayFilesInDirectory(".class", EXTRACTED_JAVA_FILES_PATH);

        ClassFileDecompiler classFileDecompiler = new ClassFileDecompiler();
        MethodParser methodParser = new MethodParser();

        List<ClassObjectDTO> classObjectDTOS = new ArrayList<>();

        classes.forEach(classFile -> {
            String javaFile = classFile.substring(0, classFile.length() - 5) + "java";

            ClassObjectDTO dto = new ClassObjectDTO();
            String pattern = Pattern.quote(System.getProperty("file.separator"));
            dto.setClassName(javaFile.split(pattern)[javaFile.split(pattern).length - 1].replaceAll(".java", ""));
            try {
                classFileDecompiler.decompile(javaFile, classFile);
                methodParser.getMethods(dto.getMethodNames(), dto.getMethodContext(), javaFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            classObjectDTOS.add(dto);
        });
        deleteDirectory();
        return classObjectDTOS;
    }

    public List<ClassObjectDTO> getAllMethodsFromJavaProject() throws IOException {

        DirectoryReader directoryReader = new DirectoryReader();
        List<String> javaFiles = directoryReader.displayFilesInDirectory(".java", JAVA_PROJECT_PATH);

        MethodParser methodParser = new MethodParser();

        List<ClassObjectDTO> classObjectDTOS = new ArrayList<>();

        javaFiles.forEach(javaFile -> {

            ClassObjectDTO dto = new ClassObjectDTO();
            String pattern = Pattern.quote(System.getProperty("file.separator"));
            dto.setClassName(javaFile.split(pattern)[javaFile.split(pattern).length - 1].replaceAll(".java", ""));
            try {
                methodParser.getMethods(dto.getMethodNames(), dto.getMethodContext(), javaFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            classObjectDTOS.add(dto);
        });

        return classObjectDTOS;
    }

//    private void deleteDirectory(File directoryToBeDeleted) {
//        File[] allContents = directoryToBeDeleted.listFiles();
//        assert allContents != null;
//        for (File file : allContents) {
//            file.delete();
//        }
//    }

    public void deleteDirectory() throws IOException {

        Files.walk(Path.of(EXTRACTED_JAVA_FILES_PATH))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
