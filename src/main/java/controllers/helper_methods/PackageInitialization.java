package controllers.helper_methods;

import controllers.models.ClassObject;
import controllers.models.ClassObjectDTO;
import controllers.models.MethodObject;
import controllers.models.PackageObject;

import java.io.IOException;
import java.util.List;

public class PackageInitialization {

    private SecsumFilesParser secsumFilesParser = null;
    private MethodExtractor methodExtractor = null;

    public List<PackageObject> getFinalStateOfPackages(boolean isJarFile,
                                                       boolean isJavaProject,
                                                       boolean isApkFile) throws IOException {
        long start = System.currentTimeMillis();
        try {
            secsumFilesParser = new SecsumFilesParser();
            methodExtractor = new MethodExtractor();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<PackageObject> packageObjectList = secsumFilesParser.getAllPackages();

        List<ClassObjectDTO> classObjectDTOS = null;

        if (isJarFile) {
            classObjectDTOS = methodExtractor.getAllMethodsFromJarFile();
            System.out.println("It's a jar");
        }
        if (isJavaProject) {
            classObjectDTOS = methodExtractor.getAllMethodsFromJavaProject();
            System.out.println("it's a project");
        }
        if (isApkFile)
            System.out.println("Implement for Apk file");

        for(PackageObject packageObject : packageObjectList) {
            for (ClassObject classObject : packageObject.getClassesList()) {
                for (int i = 0; i < classObjectDTOS.size(); i++) {
                    if (classObject.getName().equals(classObjectDTOS.get(i).getClassName())) {
                        for (MethodObject methodObject: classObject.getMethodsList()) {
                            for (int j = 0; j < classObjectDTOS.get(i).getMethodNames().size(); j++) {
                                if (methodObject.getName().startsWith(classObjectDTOS.get(i).getMethodNames().get(j))) {
                                    methodObject.setJavaFileContent(classObjectDTOS.get(i).getMethodContext().get(j));
                                    methodObject.setName(classObjectDTOS.get(i).getMethodNames().get(j));

                                    classObjectDTOS.get(i).getMethodNames()
                                            .remove(classObjectDTOS.get(i).getMethodNames().get(j));
                                    classObjectDTOS.get(i).getMethodContext()
                                            .remove(classObjectDTOS.get(i).getMethodContext().get(j));
                                    break;
                                }
                            }
                        }
                        classObjectDTOS.remove(classObjectDTOS.get(i));
                        break;
                    }
                }
            }
        }

        System.out.println("Total initialization time: " + (System.currentTimeMillis() - start));
        return packageObjectList;
    }
}
