/**
 * 
 */
package logic.tool;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * @author nakhaa
 *
 */
public class APKApplicationHelper extends Tool {

	/**
	 * @param exConifidentiality
	 * @param imConifidentiality
	 * @param integrity
	 * @param isModelRunFreeVersion
	 */
	public APKApplicationHelper(boolean exConifidentiality, boolean imConifidentiality, boolean integrity,
			boolean isModelRunFreeVersion) {
		super(exConifidentiality, imConifidentiality, integrity, isModelRunFreeVersion);
	}

	/**
	 * @param inputpath
	 * @param outputpath
	 * @param inputFolderpath
	 */
	public APKApplicationHelper(String inputpath, String outputpath, String inputFolderpath) {
		super(inputpath, outputpath, inputFolderpath);
	}

	void buildConfigurationForAPK(String applicationName, String appPath, String targetFolder, String exprType, boolean exceptionEnabeled, String scgsCommandFile) {
		String androidJDKPath = "/Volumes/Applications/android-sdk-macosx/platforms/android-21/android.jar";
		String[] requiredClassesPaths = new String[] { androidJDKPath + "/platforms/android-21/android.jar", };
		configurations.targetPath = getOutputPath() + File.separator + targetFolder + File.separator
				+ applicationName + File.separator + exprType + File.separator;
		configurations.inputPath = appPath + File.separator + applicationName;
		configurations.requiredClassesPaths = requiredClassesPaths;
		configurations.symmariesPath = symmariesPath;
		configurations.monitorHelperPath = "se.lnu.prosses.monitor";
		configurations.xmlSourcesAndSinks = getInputPath() + "/Config/SourcesAndSinks.txt";
		configurations.assumedSecSigsFilePath = getInputPath() + "/Config/assumedSecSigsNoSinks.presecsig";
		configurations.callbacks = getInputPath() + "/Config/AndriodCallbacks.txt";
		configurations.loadFromApk = true;
		configurations.exceptionsEnabled = exceptionEnabeled;
		configurations.SymmariesCommandFile = scgsCommandFile;
	}

	/**
	 * @param apkRootDirectory
	 * @param outputpath
	 * @param excludedApplications
	 * @param targetSubpath
	 * @param exprType
	 * @param generateSCGSInputs 
	 * @throws Exception
	 */

	public void checkAPKsInDirectory(File apkRootDirectory, String outputpath, ArrayList<String> excludedApplications,
			String targetSubpath, String exprType, boolean exceptionEnabeled, String scgsCommandFile, boolean generateSCGSInputs) throws Exception {
		FilenameFilter apkFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".apk");
			}
		};
		if (apkRootDirectory.exists()) {
			File[] apkFiles = apkRootDirectory.listFiles(apkFilter);
			for (File apkfile : apkFiles)
				if (!excludedApplications.contains(apkfile.getName())) {
					processSingleAPKFile(apkfile.getName(), apkRootDirectory.getAbsolutePath(),
							targetSubpath + File.separator + apkRootDirectory.getName(), exprType, exceptionEnabeled, scgsCommandFile, generateSCGSInputs);
					processedApplications++;
				}

			String[] directories = apkRootDirectory.list(new FilenameFilter() {
				@Override
				public boolean accept(File current, String name) {
					return new File(current, name).isDirectory();
				}
			});

			for (String directory : directories) {
				File subDirectory = new File(apkRootDirectory + File.separator + directory);
				checkAPKsInDirectory(subDirectory, outputpath, excludedApplications, targetSubpath, exprType, exceptionEnabeled, scgsCommandFile, generateSCGSInputs);
			}
		} else
			System.out.println("Could not find the directory " + apkRootDirectory.getAbsolutePath());
	}

	void processSingleAPKFile(String applicationName, String appPath, String targetFolder, String exprType, boolean exceptionEnabeled, String scgsCommandFile, boolean generateSCGSInputs)
			throws Exception {
		System.out.println("\n\nprocessSingleAPKFile(" + applicationName + "," + appPath + "," + targetFolder);
		String methodName = "";
		buildConfigurationForAPK(applicationName, appPath, targetFolder, exprType, exceptionEnabeled, scgsCommandFile);
		System.out.println("Starting tool with:" + configurations.inputPath);
		if(generateSCGSInputs)
			start(appPath + File.separator + applicationName, methodName);
		/*
		 * System.out.println("Running SCGS"); runSCGSv2();
		 * System.out.println("Extracting the analysis results ");
		 * extractSCGSResults(applicationName);
		 */
		System.out.println("Finished processing the application " + applicationName
				+ ".\n----------------------------------------------------------------\n\n\n");
		System.out.println("Writting the command file");
		writeSCGSFile(configurations);
	}

}
