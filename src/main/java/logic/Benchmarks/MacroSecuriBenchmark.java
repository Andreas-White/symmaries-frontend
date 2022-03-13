package logic.Benchmarks;

import logic.Statistics.SymmariesResultsHelper;
import logic.general.Constants;
import logic.general.Utils;
import logic.tool.JARApplicationHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MacroSecuriBenchmark extends SymmariesResultsHelper {

	static String becnhmarkRelativePath = "/WebApplications/Selected/";
	private static String ReportName;
	boolean exceptionEnabeled = false;

	public static void main(String[] args) throws Exception {
		MacroSecuriBenchmark macroSecuriBenchmark = new MacroSecuriBenchmark();
		ReportName = "WebApplications" + new File(becnhmarkRelativePath).getName();
		int methodSkipParameter = 0;
		//copySelectedApps();
		//macroSecuriBenchmark.unzipAllfiles(new File(inputPath + java.io.File.separator + becnhmarkRelativePath));
		//macroSecuriBenchmark.checkApplicationsInDirectory(true, methodSkipParameter, Constants.secsumPath);
		/*macroSecuriBenchmark.checkTheResults( "Misc/StatisticsReports/statistics" + ReportName + ".txt", 
				"Misc/AnalysisReports/scgsResults" + ReportName + ".txt",
				"Misc/Datasets/" + ReportName + ".rtff",
				"Misc/AnalysisReports/" + ReportName + ".csv"
				);*/
		macroSecuriBenchmark.writeApplicationSizeStatistics(new File(inputPath + File.separator + becnhmarkRelativePath));
	}

	private static void copySelectedApps() {
		String[] lines = Utils.readStringTextFile(outputPath + "/Misc/selectedApps.csv").split("\n");
		for(int index=1; index < lines.length; index++) {
			String fileName = new File(lines[index].split(";")[0]).getParent().replace("output", "input");
			if(new File(fileName+".jar").exists())
				Utils.copy(fileName+".jar", inputPath + "/WebApplications/Selected/" +  new File(fileName).getName() + ".jar");
			else
				Utils.copy(fileName+".war", inputPath + "/WebApplications/Selected/" + new File(fileName).getName() + ".war");

		}
	}

	public void unzipAllfiles(File jarRootDirectory) {
		if (jarRootDirectory.exists()) {
			for (File jarfile : Utils.getFilesOfTypes(jarRootDirectory.getAbsolutePath(), new String[] {".jar",".war"})) {
				String outputSubdirectory = jarfile.getAbsolutePath().replaceAll(inputPath, "");
				String jarContentPath = outputPath
						+ outputSubdirectory.substring(0, outputSubdirectory.lastIndexOf("."))
						+ File.separator + "JarContent";
				Utils.uncompressZipFile(jarfile.getAbsolutePath(), jarContentPath, ".class");				
			}
		}
	}

	public void writeApplicationSizeStatistics(File jarRootDirectory) {
		if (jarRootDirectory.exists()) {
			String applicationSizeInfo = "Application;Size;number Of classes;number Of ThirdParty Methods;Number Of Method\n";
			for (File jarfile : Utils.getFilesOfTypes(jarRootDirectory.getAbsolutePath(), new String[] {".jar",".war"})) {
				String outputSubdirectory = jarfile.getAbsolutePath().replaceAll(inputPath, "");
				String jarContentPath = outputPath
						+ outputSubdirectory.substring(0, outputSubdirectory.lastIndexOf("."))
						+ File.separator + "JarContent";
				if(new File(jarContentPath).exists()) {
					applicationSizeInfo += jarContentPath + "; " + Utils.folderSize(new File(jarContentPath)) + 
							"; " + Utils.numberOfFiles(new File(jarContentPath)) + 
							"; " + Utils.readStringTextFile(new File(jarContentPath).getParent()+ "/syrsInput/Meth/all.secstubs").split("\n").length + 
							"; " + Utils.getFilesOfTypes(new File(jarContentPath).getParent()+ "/syrsInput/Meth/", new String[] {".meth"}).size() + 
							"\n";
				}
			}
			Utils.updateTextFile(outputPath + File.separator + "WebApplications/statistics.csv", applicationSizeInfo);
			System.out.println(applicationSizeInfo);
		}
	}


	public void checkJARsInDirectory(boolean generateSCGSInputs, int methodSkipParameter) throws Exception {

		JARApplicationHelper tool = new JARApplicationHelper(inputPath + becnhmarkRelativePath,
				outputPath, inputPath);
		try {
			tool.checkJARsInDirectory(new File(tool.getInputPath() + File.separator + becnhmarkRelativePath),
					new ArrayList<String>(Arrays.asList("")),
					"nice", "",
					getClassesFolderMap(),
					exceptionEnabeled,
					generateSCGSInputs,
					Constants.symmariesPath,
					outputPath + File.separator + "Misc/CommandFiles/MacroBench" + ReportName + ".command",
					methodSkipParameter,
					true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// new IFSpecBenchmarkHelper().checkTheResults();
	}

	public void checkApplicationsInDirectory(boolean generateSymmariesInputs, int methodSkipParameter, String secstubsPath, boolean taintChecking) throws Exception {

		JARApplicationHelper tool = new JARApplicationHelper(inputPath + becnhmarkRelativePath,
				outputPath, inputPath);
		try {
			tool.checkApplicationsInDirectory(new File(tool.getOutputPath() + File.separator + becnhmarkRelativePath),
					new ArrayList<String>(Arrays.asList("")),
					"",
					secstubsPath,
					getClassesFolderMap(),
					exceptionEnabeled,
					generateSymmariesInputs,
					Constants.symmariesPath,
					outputPath + File.separator + "Misc/CommandFiles/MacroBench" + ReportName + ".command",
					methodSkipParameter,
					taintChecking);

		} catch (Exception e) {
			e.printStackTrace();
		}
		// new IFSpecBenchmarkHelper().checkTheResults();
	}

	private HashMap<String, String> getClassesFolderMap() {
		HashMap<String,String> results = new HashMap<String,String>();
		String[] fileContent = Utils.readStringTextFile(inputPath+ File.separator + "WebApplications" + File.separator + "classesFolderPath.txt").split("\n");
		for(String line: fileContent) {
			results.put(line.split(",")[0].trim(), line.split(",")[1].trim());
		}
		return results;
	}

	void checkTheResults(String statisticsReportName, String symmariesResultsReportName, String datasetFilePath, String sheetPath) {
		extractSymmariesStatistics(outputPath + becnhmarkRelativePath, outputPath);
		exportApplicationsStatistics(outputPath, outputPath + statisticsReportName);
		processSymmariesResults(inputPath, outputPath + symmariesResultsReportName, outputPath);
		//WekaDatasetConstrcutor wekaDatasetConstrcutor = new WekaDatasetConstrcutor(this.ApplicationsAnalysisResultsMap);
		//wekaDatasetConstrcutor.constructDataSet(outputFolderPath + datasetFilePath);
		this.exportMethodStatisticsToSheet(outputPath + sheetPath);
	}

}
