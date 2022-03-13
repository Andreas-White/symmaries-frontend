package logic.Statistics;

import logic.general.Constants;
import logic.general.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class SymmariesResultsHelper {
	protected static final String inputPath = "/Volumes/Academics/Workspaces/SymmariesExperiments/input/";
	protected static final String outputPath = "/Volumes/Academics/Workspaces/SymmariesExperiments/output/";

	private static ArrayList<String> usedPackages = new ArrayList<String>();
	private static ArrayList<String> usedMethods = new ArrayList<String>();
	static FilenameFilter secsumsFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.toLowerCase().endsWith(Constants.secsumFileExtension);
		}
	};
	static FilenameFilter methFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.toLowerCase().endsWith(Constants.meth_statsFileExtension);
		}
	};

	public HashMap<String, ApplicationStatsHelper> ApplicationsAnalysisResultsMap = new HashMap<String, ApplicationStatsHelper>();

	protected HashMap<String, Boolean> groundTruthResultsMap = new HashMap<String, Boolean>();

	protected void exportApplicationsStatistics(String outputPath, String reportPath) {
		String out = "";// generateRtffFileHeader();
		for (ApplicationStatsHelper application : ApplicationsAnalysisResultsMap.values()) {
			out += application.exportApplicationResults(outputPath, reportPath);
		}
		if(Utils.writeTextFile(reportPath, out))
			Utils.log(this.getClass(), "Exported applications statistics to " + reportPath);
	}

	public void exportListOfUsedJavaLibrariesMethods(File directory, String targetFileName) {
		try {
			extractAllJavaLibraries(directory);
			String output = "";
			usedPackages.sort(null);
			for (String pack : usedPackages)
				output += pack + "\n";
			// Utils.log(this.getClass(),output);
			Utils.writeTextFile(directory + targetFileName + "Packages.txt", output);
			output = "";
			usedMethods.sort(null);
			for (String method : usedMethods)
				output += method + "\n";
			// Utils.log(this.getClass(),output);
			Utils.writeTextFile(directory + targetFileName + "Methods.txt", output);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void extractAllJavaLibraries(File secstubsDirectory) throws Exception {
		for (File file : Utils.getFilesOfTypes(secstubsDirectory.getAbsolutePath(), new String[] {Constants.secstubsFileExtension}))
			processSecstubFile(file);
	}

	protected void extractSymmariesStatistics(String directoryPath, String rootDirtectory) {
		File file = new File(directoryPath);
		if (file.exists())
			for (File meth_statsFile : Utils.getFilesOfTypes(directoryPath, new String[] {Constants.meth_statsFileExtension})) {
				try {
					String[] fileName = meth_statsFile.getAbsolutePath().split(File.separator);
					String algorithmIndex = //Integer.parseInt
							(fileName[fileName.length - 1].replaceAll(Constants.statsFilePrefix, "")
							.replaceAll(Constants.meth_statsFileExtension, ""));
					String applicationName = meth_statsFile.getParent().replaceAll(rootDirtectory, "");
					if (!ApplicationsAnalysisResultsMap.containsKey(applicationName + algorithmIndex)) {
						File secsumFile = new File(meth_statsFile.getAbsolutePath().replaceAll(Constants.meth_statsFileExtension,Constants.secsumFileExtension));
						ApplicationStatsHelper application = new ApplicationStatsHelper(applicationName, directoryPath,algorithmIndex,meth_statsFile, secsumFile);
						ApplicationsAnalysisResultsMap.put(applicationName + algorithmIndex, application);
					}
					Utils.log(this.getClass(),
							"Procesed the statistics file of application " + meth_statsFile.getAbsolutePath());
				} catch (FileNotFoundException e) {
					System.err.println(
							"Could not load the experiments results. Maybe SCGS has failed to run or has not been called!");
				} catch (NumberFormatException e) {
					System.err.println("Bad-formatted statistics file:" + meth_statsFile.getAbsolutePath());
				}
			}
		else
			System.err.println("The path  " + directoryPath + " does not exist!");
	}

	protected void processSymmariesResults(String inputpath, String reportPath, String outFolderPath) {
		int totalNumberOfAnalyzedResults = 0, insecureConsistents = 0, secureConsisstents = 0, unsounds = 0,
				restrictives = 0;
		String out = "";
		Utils.log(this.getClass(),
				"\n\n------------------------------------\n" + "Comparing the results to the benachmark\n ");
		for (String applicationName : ApplicationsAnalysisResultsMap.keySet()) {
			String tmp = ApplicationsAnalysisResultsMap.get(applicationName).exportApplicationResults(outFolderPath,
					reportPath);
			String comaprisonResult = "";

			Utils.log(this.getClass(),
					"\n------------------------------------\n" + "Processing the Symmaries analysis results of " + applicationName);
			// ArrayList<File> javaFiles = Utils.getFilesOfType(inputpath +
			// applicationName.substring(0,applicationName.lastIndexOf(File.separator)) +
			// "/src",".java");
			// System.out.print(ApplicationsAnalysisResultsMap.get(applicationName).exportApplicationResults());
			if (groundTruthResultsMap.get(applicationName) == null) {
				System.err.println("Could not find the ground truth results of " + applicationName);
			} else if (ApplicationsAnalysisResultsMap.get(applicationName).isSecureApplicationBasedonSCGS())
				if (groundTruthResultsMap.get(applicationName)) {
					secureConsisstents++;
					comaprisonResult = "Consistent secure results for " + applicationName;
					Utils.log(this.getClass(), tmp);
				} else {
					unsounds++;
					comaprisonResult = "Unsound results for " + applicationName;
					Utils.logErr(this.getClass(), tmp);
				}
			else if (groundTruthResultsMap.get(applicationName)) {
				restrictives++;
				comaprisonResult = "Restrictive results  for " + applicationName;
				Utils.logErr(this.getClass(), tmp);

			} else {
				insecureConsistents++;
				comaprisonResult = "Consistent insecure results for " + applicationName;
				Utils.log(this.getClass(), tmp);
			}
			out += tmp + "\n" + comaprisonResult  + "\n-------------------------------------\n";
			for (File file : Utils.getFilesOfTypes(
					inputpath + new File(applicationName).getParentFile().getParent(),
					new String[] {".java"}))
				out += Utils.readTextFile(file.getAbsolutePath());
		}
		Utils.writeTextFile(reportPath, out);
		Utils.log(this.getClass(), "Exported applications statistics to " + reportPath);
		Utils.log(this.getClass(), "\n**************************************************************\n" + "The number of applications processed by SCGS is: " + ApplicationsAnalysisResultsMap.size());
		Utils.log(this.getClass(),"The total number of appliactions whose results are compared: " + totalNumberOfAnalyzedResults);
		Utils.log(this.getClass(), "The number of restrictive results: " + restrictives);
		Utils.log(this.getClass(), "The number of insecureConsistents: " + insecureConsistents);
		Utils.log(this.getClass(), "The number of secureConsisstents: " + secureConsisstents);
		Utils.log(this.getClass(), "The number of unsounds: " + unsounds);
	}

	private void processSecstubFile(File file) {
		try {
			Utils.log(this.getClass(), "Processing " + file.getCanonicalPath());
			Scanner sc = new Scanner(file);
			while (sc.hasNextLine()) {
				String line = sc.nextLine().replaceAll("static", "").trim();
				if (line.contains(":")) {
					String curPackage = (line.contains(":") ? line.substring(line.indexOf(' ') + 1, line.indexOf(':'))
							: line.substring(line.indexOf('('))),
							curMethod = (line.contains(":") ? line.substring(line.indexOf(' ') + 1, line.indexOf('('))
									: line.substring(line.indexOf('(')));

					if (!usedPackages.contains(curPackage))// ) && (curPackage.contains("java") ||
						// curPackage.contains("android")))
						usedPackages.add(curPackage);
					if (usedPackages.contains(curPackage) && !usedMethods.contains(curMethod))
						usedMethods.add(curMethod);

				}
			}
			sc.close();
		} catch (FileNotFoundException e) {
			System.err.println("Could not open the secstub file!");
		} catch (IOException e) {
			System.err.println("Could not find the path!");
		}
	}

	public void exportMethodStatisticsToSheet(String reportPath){
		String out = MethodStatsHelper.getSheetHeader() + "\n";
		for (ApplicationStatsHelper applicationName : this.ApplicationsAnalysisResultsMap.values()) 
			for (MethodStatsHelper methodStats : applicationName.methodsStatsMap.values()) 
				out += methodStats.exportStats() + '\n';
		if(Utils.writeTextFile(reportPath, out))
			Utils.log(getClass(), "Exported the sheet file to " + reportPath);
	}

}
