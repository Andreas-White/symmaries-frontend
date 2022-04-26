package logic.tool;

import logic.general.Utils;

import java.io.File;
import java.io.IOException;

public class GraphicalUIHelper {
    private boolean jarApplication;
    private boolean javaProject;
    private boolean androidApplication;

    private JARApplicationHelper jarApplicationHelper;
    private JavaApplicationHelper javaApplicationHelper;
    private APKApplicationHelper apkApplicationHelper;

    private String inputApplicationOrDirectoryPath;
    private String targetPath;

    private String sourcesAndSinksFilePath;
    private String secsumFilePath;
    private boolean exceptionEnabeled;
    private boolean taintCheckingEnabled;
    private boolean explicitConfEnabled;
    private boolean implicitConfEnabled;
    private String symmariesPath;
    private String commandFilePath;
    private int methodSkipParameter;

    public GraphicalUIHelper(boolean jarApplication, boolean javaProject, boolean androidApplication) {
        this.jarApplication = jarApplication;
        this.javaProject = javaProject;
        this.androidApplication = androidApplication;
    }

    public void loadApplicationIntoSoot() {
        if (jarApplication) {
            try {
                String jarContentPath = targetPath + File.separator + "jarContent";
                jarApplicationHelper = new JARApplicationHelper();
                jarApplicationHelper.loadSingleJARFileIntoSoot(new File(inputApplicationOrDirectoryPath), jarContentPath, targetPath);
                Utils.deleteDirectory(new File(jarContentPath));
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        } else if (javaProject) {
            javaApplicationHelper = new JavaApplicationHelper();
            javaApplicationHelper.loadSingleJavaApplicationIntoSoot(inputApplicationOrDirectoryPath, targetPath);
        } else if (androidApplication) {
            //apkApplicationHelper = new APKApplicationHelper();
        }
    }

    public Object[] getAllApplicationMethods() {
        return jarApplicationHelper.fetchtApplicationMethodsFromSoot();
    }

    public Object[] getAllThirdPartyMethods() {
        return jarApplicationHelper.fetchApplicationThirdPartyMethodsFromSoot();
    }

    public void runInputGenerator() {
        configureCommandFilePath();
        if (jarApplication) {
            jarApplicationHelper
					.buildConfigurationForJavaApplicationBeforeRunningInputGenerator(
							secsumFilePath,
							exceptionEnabeled,
							taintCheckingEnabled,
                            explicitConfEnabled,
                            implicitConfEnabled,
							sourcesAndSinksFilePath,
							symmariesPath,
							commandFilePath,
							methodSkipParameter
					);
            jarApplicationHelper.runInputGenerator();
        } else if (javaProject) {
            javaApplicationHelper
					.buildConfigurationForJavaApplicationBeforeRunningInputGenerator(
							secsumFilePath,
							exceptionEnabeled,
							taintCheckingEnabled,
                            explicitConfEnabled,
                            implicitConfEnabled,
							sourcesAndSinksFilePath,
							symmariesPath,
							commandFilePath,
							methodSkipParameter
					);
            javaApplicationHelper.runInputGenerator();
        } else if (androidApplication) {

        }
    }

    public void runSymmaries() {
        if (jarApplication) {
            try {
                jarApplicationHelper.writeSymmariesCommand();
                if (!Utils.runSCGSv2(targetPath, jarApplicationHelper.getScgsCommand()))
                    Utils.log(JisymCompiler.class, "You may check the generated command file " + targetPath + "/symmariesCommand.command.");
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        } else if (javaProject) {
            try {
                javaApplicationHelper.writeSymmariesCommand();
                if (!Utils.runSCGSv2(targetPath, javaApplicationHelper.getScgsCommand()))
                    Utils.log(JisymCompiler.class, "You may check the generated command file " + targetPath + "/symmariesCommand.command.");
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        } else if (androidApplication) {

        }
    }

    public void setInputPath(String inputApplicationOrDirectoryPath) {
        this.inputApplicationOrDirectoryPath = inputApplicationOrDirectoryPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public void setSourcesAndSinksPath(String sourcesAndSinksFilePath) {
        this.sourcesAndSinksFilePath = sourcesAndSinksFilePath;
    }

    public void setSecsumFilePath(String secsumFilePath) {
        this.secsumFilePath = secsumFilePath;
    }

    public void setExceptionEnabeled(boolean exceptionEnabeled) {
        this.exceptionEnabeled = exceptionEnabeled;
    }

    public void setTaintCheckingEnabeled(boolean taintCheckingEnabled) {
        this.taintCheckingEnabled = taintCheckingEnabled;
    }

    public void setSymmariesPath(String symmariesPath) {
        this.symmariesPath = symmariesPath;
    }

    public void setExplicitConfEnabled(boolean explicitConfEnabled) {
        this.explicitConfEnabled = explicitConfEnabled;
    }

    public void setImplicitConfEnabled(boolean implicitConfEnabled) {
        this.implicitConfEnabled = implicitConfEnabled;
    }

    private void configureCommandFilePath() {
        commandFilePath = targetPath + File.separator + "symmariesCommand.command";
    }

    public void setMethodSkipParameter(int methodSkipParameter) {
        this.methodSkipParameter = methodSkipParameter;
    }

    public String getSymmariesSecuritySummariesOutputFileName() {
        if (jarApplication) {
            return jarApplicationHelper.getSymmariesSecuritySummariesOutputFileName();
        } else if (javaProject) {
            return javaApplicationHelper.getSymmariesSecuritySummariesOutputFileName();
        } else if (androidApplication) {

        }
        return null;
    }

}