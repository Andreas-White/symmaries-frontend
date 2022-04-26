package logic.tool;

import logic.general.SynthesisConfiguratons;
import logic.general.Utils;
import logic.jimpleProcessor.JimpleProjectHelper;
import soot.SootMethod;

import java.io.File;
import java.util.List;

public class Tool {
    protected final String symmariesPath = System.getProperty("user.home") + "/.opam/4.04.2/bin/reax.opt";
    protected int processedApplications = 0;
    protected boolean ignoreCheckPoints;
    private String scgsCommand = "";
    public SynthesisConfiguratons configurations = new SynthesisConfiguratons(false, true, false, true);
    public JimpleProjectHelper project;

    private String inputPath;
    private String outputPath;
    private String outputSecuritySummariesResultFile;

    protected void buildSymmariesCommand(SynthesisConfiguratons configurations) {
        int index = 0;
        String[] scgsAlgorithms = new String[]{
                "\'sS:d={P:m},split_outer=false\'",
                "\'sS:d={P:m},split_outer=true\'",
                "\'sS\'",
                "\'sB:rp={stop_dri=1,stop_ri=100,log}\'"};

        //for(String synthAlgorithm:scgsAlgorithms) {
        // for (boolean tflags: new boolean[] {true,false})
        {
            String outputFileName = configurations.targetPath + "/results" + index + "_" + configurations.methodSkipParameter;
            index++;
            String options = " -of secsum+scfg" // +scfg"
                    + " --methskip-cond " + configurations.methodSkipParameter
                    + " --log-level i "
                    + " -tf " + (configurations.exceptionsEnabled ? " +exceptions " : " -exceptions ")
                    + (configurations.taintCheckingEnabled ? " -tf +taints+enforce_integrity-levels " : "")
                    // + " -s " + synthAlgorithm + " "
                    // + " -tf " + (tflags? "'semloc=explicit' ":"'semloc=symbolic' ")
                    ;
            setScgsCommand(getScgsCommand() + configurations.symmariesPath + " " + options + configurations.targetPath + "/types.classes "
                    + configurations.targetPath + "/Meth/all.meth_files " + configurations.targetPath
                    + "/Meth/all.secstubs " + " --full-walk " +
                    " --output " + outputFileName + ".secsums " +
                    " --output " + outputFileName + ".meth_stats \n");
            outputSecuritySummariesResultFile = outputFileName + ".secsums";
        }
        // }
    }

    public Tool(boolean exConifidentiality,
                boolean imConifidentiality,
                boolean integrity,
                boolean isModelRunFreeVersion) {
        configurations = new SynthesisConfiguratons(exConifidentiality, imConifidentiality, integrity,
                isModelRunFreeVersion);
        configurations.monitorHelperPath = "se.lnu.prosses.monitor";
    }

    public Tool(String inputpath,
				String outputpath,
				String inputFolderpath) {
        this.setInputPath(inputFolderpath);
        this.setOutputPath(outputpath);
        // experimentsPath = inputFolderpath + java.io.File.separator + inputpath;
        project = new JimpleProjectHelper(configurations);
    }

    public Tool() {
        project = new JimpleProjectHelper(configurations);
    }


    public boolean start(String fullClassName, String methodName) {
        //project.extractSinkAndSources();
        project.extractSourceSinkFromXML();
        project.loadAssumedSecuritySignatures();
        return startSCGS(fullClassName, methodName);

    }


    public void writeSCGSFile(SynthesisConfiguratons configurations2) {
        buildSymmariesCommand(configurations);
        if (!new File(configurations.SymmariesCommandFile).exists() && !Utils.createFile(configurations.SymmariesCommandFile))
            ;
        else if (!Utils.writeTextFile(configurations.SymmariesCommandFile, "#! /bin/bash\n" + getScgsCommand()))
            Utils.log(getClass(), "Could not write the scgs command file " + configurations.SymmariesCommandFile);

    }

    public boolean startSCGS(String fileOrClassName, String entryPointMethod) {
        if (configurations.loadFromApk) {
            return project.constructSymmariesInputFromAPKFiles(fileOrClassName);

        } else
            return project.constructSymmariesInputFromClassFiles(fileOrClassName, entryPointMethod);

    }

    public String getScgsCommand() {
        return scgsCommand;
    }

    public void setScgsCommand(String scgsCommand) {
        this.scgsCommand = scgsCommand;
    }

    public String getInputPath() {
        return inputPath;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void loadApplicationIntoSoot(String fileOrClassName, String entryPointMethod) {
        if (configurations.loadFromApk)
            project.constructSymmariesInputFromAPKFiles(fileOrClassName);
        else
            project.loadJavaApplicationIntoSoot(fileOrClassName, entryPointMethod);
    }

    public Object[] fetchtApplicationMethodsFromSoot() {
        return project.getApplicationMethods();
    }

    public void startInputGenerator(String fullClassName, String methodName) {
        //project.extractSinkAndSources();
        project.extractSourceSinkFromXML();
        project.loadAssumedSecuritySignatures();
        constructSymmariesInput(fullClassName, methodName);
        return;
    }

    public void constructSymmariesInput(String fileOrClassName, String entryPointMethod) {
        if (configurations.loadFromApk)
            project.constructSymmariesInputFromAPKFiles(fileOrClassName);
        else
            project.constructSymmariesInputFromInputGenerator(fileOrClassName, entryPointMethod);
    }

    public Object[] getApplicationThirdPartyMethodsFromSoot() {
        List<SootMethod> list = project.getSortedLibrarySecurityAssumptions();
        Object[] listOfMethods = new Object[list.size()];
        for (int counter = 0; counter < list.size(); counter++) {
            listOfMethods[counter] = list.get(counter);
        }
        return listOfMethods;
    }

    public String getSymmariesSecuritySummariesOutputFileName() {
        return outputSecuritySummariesResultFile;
    }

}
