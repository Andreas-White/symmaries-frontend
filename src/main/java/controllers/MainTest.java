package controllers;

import controllers.helper_methods.ClassFileDecompiler;
import controllers.helper_methods.DirectoryReader;
import controllers.helper_methods.JarExtractor;

import java.io.IOException;
import java.util.List;

public class MainTest {

    private static final String JAR_WAR_FILE_PATH = "C:\\Users\\PC\\Downloads\\testjar\\RadioCRM.war";
    private static final String EXTRACTED_JAVA_FILES_PATH = "C:\\Users\\PC\\Downloads\\testjar\\test";

    public static void main(String[] args) throws IOException {

        long start = System.currentTimeMillis();

        JarExtractor jarExtractor = new JarExtractor();
        jarExtractor.extractClassFiles(JAR_WAR_FILE_PATH, EXTRACTED_JAVA_FILES_PATH);

        DirectoryReader directoryReader = new DirectoryReader();
        List<String> classes = directoryReader.displayFilesInDirectory(".class", EXTRACTED_JAVA_FILES_PATH);

        ClassFileDecompiler classFileDecompiler = new ClassFileDecompiler();

        classes.forEach(classFile -> {
            String javaFile = classFile.substring(0, classFile.length() - 5) + "java";
            try {
                classFileDecompiler.decompile(javaFile, classFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        long end = System.currentTimeMillis();

        System.out.println("Total time: " + (end - start));
    }
}
