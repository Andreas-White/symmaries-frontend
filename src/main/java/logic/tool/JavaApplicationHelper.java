package logic.tool;

import logic.general.Utils;

public class JavaApplicationHelper extends Tool {

    public JavaApplicationHelper(boolean exConifidentiality, boolean imConifidentiality, boolean integrity,
                                 boolean isModelRunFreeVersion) {
        super(exConifidentiality, imConifidentiality, integrity, isModelRunFreeVersion);
        // TODO Auto-generated constructor stub
    }

    public JavaApplicationHelper(String inputpath, String outputpath, String inputFolderpath) {
        super(inputpath, outputpath, inputFolderpath);
        // TODO Auto-generated constructor stub
    }

    public JavaApplicationHelper() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param appPath
     * @param targetFolder
     * @param requiredClassesPaths
     * @param secsumfilePath
     * @param xmlScrSinkPath
     * @param commadFilePath
     * @param methodSkipParameter
     * @param taintChecking
     */
    void buildConfigurationForJavaApplication(String appPath,
                                              String targetFolder,
                                              String[] requiredClassesPaths,
                                              String secsumfilePath,
                                              String xmlScrSinkPath,
                                              boolean exceptionEnabeled,
                                              String symmariesPath,
                                              String commadFilePath,
                                              int methodSkipParameter,
                                              boolean taintChecking) {
        configurations.inputPath = appPath;
        configurations.targetPath = targetFolder;
        configurations.requiredClassesPaths = requiredClassesPaths;
        configurations.assumedSecSigsFilePath = secsumfilePath;
        configurations.exceptionsEnabled = exceptionEnabeled;
        configurations.symmariesPath = symmariesPath;
        configurations.SymmariesCommandFile = commadFilePath;
        configurations.methodSkipParameter = methodSkipParameter;
        configurations.loadFromApk = false;
        configurations.taintCheckingEnabled = taintChecking;
        configurations.explicitConfEnabeled = (!taintChecking);
        configurations.implicitConfEnabeled = (!taintChecking);
        configurations.xmlSourcesAndSinks = xmlScrSinkPath;
    }

    /**
     * @param appPath
     * @param targetFolder
     * @param secsumfilePath
     * @param generateSCGSInputs
     * @param symmariesPath
     * @param methodSkipParameter
     * @param taintChecking
     * @return
     * @throws Exception
     */
    public boolean processSingleJavaApplication(String appPath,
                                                String targetFolder,
                                                String xmlScrSinkPath,
                                                String secsumfilePath,
                                                boolean exceptionEnabeled,
                                                boolean generateSCGSInputs,
                                                String symmariesPath,
                                                String commadFilePath,
                                                int methodSkipParameter,
                                                boolean taintChecking) {
        if (Utils.getFilesOfTypes(appPath, new String[]{".class"}).size() == 0) {
            Utils.log(this.getClass(), "The application has no class files. \n Aborted!");
            return false;
        }

        buildConfigurationForJavaApplication(
                appPath,
                targetFolder,
                new String[]{appPath},
                secsumfilePath,
                xmlScrSinkPath,
                exceptionEnabeled,
                symmariesPath,
                commadFilePath,
                methodSkipParameter,
                taintChecking);
        if (generateSCGSInputs) {
            Utils.log(this.getClass(), "Loading the tool with:" + configurations.inputPath);
            if (start("", null)) {
                Utils.log(this.getClass(), "Finished processing the application " + appPath
                        //+ ".\n----------------------------------------------------------------\n\n\n"
                );
                //				writeSCGSFile(configurations);
                //				return true;
            } else
                return false;
        }
        writeSCGSFile(configurations);
        return true;
    }

    public void loadSingleJavaApplicationIntoSoot(String appPath, String targetFolder) {
        if (Utils.getFilesOfTypes(appPath, new String[]{".class"}).size() == 0) {
            Utils.log(this.getClass(), "The application has no class files. \n Aborted!");
            return;
        }
        buildConfigurationForJavaApplicationBeforeLoadingIntoSoot(appPath, targetFolder, new String[]{appPath});
        Utils.log(this.getClass(), "Loading tool with: " + configurations.inputPath);
        loadApplicationIntoSoot("", null);
    }

    void buildConfigurationForJavaApplicationBeforeLoadingIntoSoot(String appPath,
                                                                   String targetFolder,
                                                                   String[] requiredClassesPaths) {
        configurations.inputPath = appPath;
        configurations.targetPath = targetFolder;
        configurations.requiredClassesPaths = requiredClassesPaths;
        configurations.loadFromApk = false;
    }

    void buildConfigurationForJavaApplicationBeforeRunningInputGenerator(String secsumFilePath,
                                                                         boolean exceptionEnabled,
                                                                         boolean taintChecking,
                                                                         boolean explicitConfEnabled,
                                                                         boolean implicitConfEnabled,
                                                                         String xmlScrSinkPath,
                                                                         String symmariesPath,
                                                                         String commadFilePath,
                                                                         int methodSkipParameter) {
        configurations.assumedSecSigsFilePath = secsumFilePath;
        configurations.exceptionsEnabled = exceptionEnabled;
        configurations.taintCheckingEnabled = taintChecking;
//        configurations.explicitConfEnabeled = (!taintChecking);
//        configurations.implicitConfEnabeled = (!taintChecking);
        configurations.explicitConfEnabeled = explicitConfEnabled;
        configurations.implicitConfEnabeled = implicitConfEnabled;
        configurations.symmariesPath = symmariesPath;
        configurations.SymmariesCommandFile = commadFilePath;
        configurations.methodSkipParameter = methodSkipParameter;
        configurations.xmlSourcesAndSinks = xmlScrSinkPath;
    }

    void runInputGenerator() {
        startInputGenerator("", null);
        Utils.log(this.getClass(), "Finished processing the application.");
    }

    public Object[] fetchApplicationThirdPartyMethodsFromSoot() {
        return getApplicationThirdPartyMethodsFromSoot();
    }

    void writeSymmariesCommand() {
        writeSCGSFile(configurations);
    }
}
