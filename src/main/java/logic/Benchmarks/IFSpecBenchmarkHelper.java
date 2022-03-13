package logic.Benchmarks;

import logic.Statistics.SymmariesResultsHelper;
import logic.general.Constants;
import logic.general.Utils;
import logic.tool.JARApplicationHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class IFSpecBenchmarkHelper extends SymmariesResultsHelper {

	// private HashMap<String, Boolean> groundTruthList = new HashMap<String,
	// Boolean>();
	static String becnhmarkRelativePath = "IFSpec/library/JavaBytecode/";
	static boolean exceptionEnabeled = true;
	public static void main(String[] args) throws Exception {
		IFSpecBenchmarkHelper iFSpecBenchmarkHelper = new IFSpecBenchmarkHelper();
		int methodSkipParameter = 120;
		iFSpecBenchmarkHelper.checkJARsInDirectory(true, methodSkipParameter);
		String ReportName = "IFSpec" + new File(becnhmarkRelativePath).getName();
		iFSpecBenchmarkHelper.checkTheResults( "Misc/StatisticsReports/statistics" + ReportName + ".txt", 
				"Misc/AnalysisReports/scgsResults" + ReportName + ".txt",
				"Misc/Datasets/" + ReportName + ".rtff",
				"Misc/AnalysisReports/" + ReportName + ".csv"
				);

	}

	public void checkJARsInDirectory(boolean generateSCGSInputs, int methodSkipParameter) throws Exception {
		JARApplicationHelper tool = new JARApplicationHelper(inputPath + becnhmarkRelativePath,
				outputPath, inputPath);
		try {
			//String outputSubdirectory = jarfile.getAbsolutePath().replaceAll(this.inputPath, "");

			tool.checkJARsInDirectory(new File(tool.getInputPath() + File.separator + becnhmarkRelativePath),
					// applicationSubdirectory,
					new ArrayList<String>(
							Arrays.asList(
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/Deepcall1/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/Deepcall2/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/Deepalias1/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/Deepalias2/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/simpleClassLoading/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/ReviewerAnonymity-Leak/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/SecuriBench-Arrays10/program/program.jar", // multi-arrays
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/SecuriBench-Arrays10-secure/program/program.jar", // multi-arrays
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/SecuriBench-Arrays9/program/program.jar", // multi-arrays
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/Webstore2/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/SecuriBench-Basic17/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/SecuriBench-Basic17-secure/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/SecuriBench-Collections7/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/SecuriBench-Datastructures2/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/SecuriBench-Factories3/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/SecuriBench-Factories3-secure/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/SecuriBench-Inter12/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/SecuriBench-Inter12-secure/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/SecuriBench-Datastructures2-secure/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/SecuriBench-Collections2-secure/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/SecuriBench-Refl1/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/SecuriBench-Refl2/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/SecuriBench-Refl3/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/SecuriBench-Refl4/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/simpleReflectionAccessPrivateField/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/simpleReflectionAccessPrivateField-secure/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/ReflectionSetSecretPrivateField-secure/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/ReflectionSetSecretPrivateField-Insecure/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/Reflection-Accessibility-Modification-Secure/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/Reflection-Accessibility-Modification/program/program.jar",
									"/Volumes/Academics/Workspaces/SymmariesExperiments/input/IFSpec/library/JavaBytecode/Webstore4/program/program.jar"
									))
					,
					"nice", "", null,
					exceptionEnabeled,
					generateSCGSInputs,
					Constants.symmariesPath,
					outputPath + File.separator + "Misc/CommandFiles/IFSpec.command",
					methodSkipParameter,
					false
					);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// new IFSpecBenchmarkHelper().checkTheResults();
	}

	/*	void checkTheResults(String statisticsReportName, String scgsResultsReportName) {
		extractSCGSStatistics(outputFolderPath + becnhmarkRelativePath, outputFolderPath);
		exportApplicationsStatistics(outputFolderPath, outputFolderPath + "Misc/" + statisticsReportName);
		setExpectedResult(inputFolderPath + becnhmarkRelativePath);
		processSCGSResults(inputFolderPath, outputFolderPath + "Misc/" + scgsResultsReportName, outputFolderPath);
	}*/

	private String getApplicationName(String parent) {
		for (String applicationName : ApplicationsAnalysisResultsMap.keySet())
			if (applicationName.contains(parent))
				return applicationName;
		return parent;
	}

	private void setExpectedResult(String directoryPath) {
		for (File gtFile : Utils.getFilesOfTypes(directoryPath, new String[] {Constants.GroundTruthFile})) {
			try {
				@SuppressWarnings("resource")
				String line = new Scanner(new File(gtFile.getAbsolutePath())).next();
				String applicationName = getApplicationName(gtFile.getParent().replaceAll(inputPath, ""));

				if (line.trim().toUpperCase().equals("SECURE"))
					groundTruthResultsMap.put(applicationName, true);
				else if (line.trim().toUpperCase().equals("INSECURE"))
					groundTruthResultsMap.put(applicationName, false);
				else
					Utils.logErr(this.getClass(), "Cannot read the ground truth result of " + applicationName);
			} catch (FileNotFoundException e) {
				Utils.logErr(this.getClass(), "Could not load the ground truth file: " + gtFile.getAbsolutePath());
			} catch (NumberFormatException e) {
				Utils.logErr(this.getClass(), "Bad statistics file format!");
			}
		}
	}

	void checkTheResults(String statisticsReportName, String symmariesResultsReportName, String datasetFilePath, String sheetPath) {
		extractSymmariesStatistics(outputPath + becnhmarkRelativePath, outputPath);
		exportApplicationsStatistics(outputPath, outputPath + statisticsReportName);
		setExpectedResult(inputPath + becnhmarkRelativePath);
		processSymmariesResults(inputPath, outputPath + symmariesResultsReportName, outputPath);
		//WekaDatasetConstrcutor wekaDatasetConstrcutor = new WekaDatasetConstrcutor(this.ApplicationsAnalysisResultsMap);
		//wekaDatasetConstrcutor.constructDataSet(outputFolderPath + datasetFilePath);
		this.exportMethodStatisticsToSheet(outputPath + sheetPath);
	}

}
