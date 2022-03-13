package logic.Benchmarks;

import logic.general.Constants;
import logic.tool.APKApplicationHelper;
import logic.tool.JavaApplicationHelper;

import java.io.File;
import java.util.ArrayList;

public class MainNormal {
	static boolean ignoreCheckPoints = false;
	static boolean fixCodeEnabled = false;
	private static final String inputFolderPath = "/Volumes/Academics/Workspaces/SymmariesExperiments/input/";
	private static final String outputFolderPath = "/Volumes/Academics/Workspaces/SymmariesExperiments/output/";
	static boolean exceptionEnabeled = false;

	public static void main(String[] args) throws Exception {

		new JavaApplicationHelper().processSingleJavaApplication(inputFolderPath + "/academy-web-target/classes/", 
				outputFolderPath + "/academy-web-target/", "", 
				Constants.secsumPath, exceptionEnabeled, 
				true, 
				Constants.symmariesPath, 
				outputFolderPath + "/Misc/CommandFiles/omegapointCommand.command",
				120,
				true);
		
		//processJavaApplicationsFromClassFiles();
		//processJavaApplications();
		//processAndroidApplications(false);
	}

	private static void processAndroidApplications(boolean generateSCGSInputs) {
		ArrayList<String> excludedApplications = new ArrayList<String>();
		String targetSubdirectory = "/Android/FDroid/";
		// targetSubdirectory = "IFSpec";//"/Android";///ICC-Bench-master";
		File apkDirectory = new File(inputFolderPath + targetSubdirectory);
		APKApplicationHelper tool = new APKApplicationHelper(inputFolderPath + targetSubdirectory, outputFolderPath,
				inputFolderPath);
		try {
			tool.checkAPKsInDirectory(apkDirectory, outputFolderPath, excludedApplications, targetSubdirectory, "nice", exceptionEnabeled,
					outputFolderPath + File.separator + "Misc/CommandFiles/" + new File(targetSubdirectory).getName() + ".command", generateSCGSInputs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// inputSubdirectory =
		// "/Volumes/Academics/Dropbox/Workspaces/SCGSWorkspace/ScgsInputGenertaor/input/Android/FDroid/NotWorkingSCGS";
		// processSingleAPKFile("me.kuehle.carreport_79.apk",inputPath,"FDroid",
		// "bad");// line 12, character 11: syntax error at token `.'

	}

}
