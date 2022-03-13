package logic.Statistics;

import logic.general.Constants;
import logic.general.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class ApplicationStatsHelper {
	String applicationName;
	//public boolean isSecure;
	ArrayList<String> skippedMethodsList = new ArrayList<String>();
	ArrayList<String> processedMethodsList = new ArrayList<String>();
	ArrayList<String> insecureMethodsList = new ArrayList<String>();
	HashMap<String, MethodStatsHelper> methodsStatsMap = new HashMap<String, MethodStatsHelper>();
	private HashMap<String, String> methodSignatureToMethFileMap = new HashMap<String, String>();
	private ArrayList<String> sourcesList = new ArrayList<String>();

	public ApplicationStatsHelper(String applicationName, String directoryPath, String usedAlgorithm, File meth_statsFile,
			File secsumsFile) throws FileNotFoundException {
		this.applicationName = applicationName;
		extractStatsFromMeth_StatsFile(meth_statsFile, usedAlgorithm);
		for (File methFile : Utils.getFilesOfTypes(meth_statsFile.getParent(), new String[] {Constants.methExtension})) {
			Scanner sc = new Scanner(methFile);
			methodSignatureToMethFileMap.put(this.getMethodSignatureFromMethPath(sc.nextLine()),
					methFile.getAbsolutePath());
			String methodName = getMethodNameFromMethPath(methFile.getAbsolutePath());
			if (!processedMethodsList.contains(methodName))
				skippedMethodsList.add(methodName);
			sc.close();
		}
		for (File srcsFile : Utils.getFilesOfTypes(meth_statsFile.getParent(), new String[] {Constants.srcsExtension})) {
			for(String source: Utils.readStringTextFile(srcsFile.getAbsolutePath()).split("\n"))
				sourcesList.add(source);
		}
		extractSecurityGuardsFromFile(secsumsFile);
		// numberOfProcessedMethods = processedMethodsList.size();
		setInsecureMethodList();
	}

	public String exportApplicationResults(String outputPath, String targetPath) {
		String out = "\n\n\n\n\n\n\n\n\n\n*******************************\n" + "Application " + applicationName + "\n";
		out += "This application is " + (isSecureApplicationBasedonSCGS()? "SECURE" : "INSECURE") + " according to SCGS\n";
		out += "The Total Number of Methods: " + "\n";
		out += "Number of Processed Methods: " + this.processedMethodsList.size() + "\n";
		out += "Number of Skipped Methods: " + this.skippedMethodsList.size() + "\n";
		if (skippedMethodsList.size() > 0) {
			out += "List of Skipped Methods:\n ";
			for (String method : skippedMethodsList)
				out += method + "\n ";
		} else
			out += "All the methods have been processed!";
		out += "\nList of processed Methods:\n ";
		for (String methodName : this.processedMethodsList) {
			for (String checkpoint : methodsStatsMap.get(methodName).checkpointGuardMaps.keySet())
				out += checkpoint + " : " + methodsStatsMap.get(methodName).checkpointGuardMaps.get(checkpoint)
				+ "\n";
		}

		out += "\nList of entry points or source methods:\n ";
		for (String methodName : processedMethodsList) {
			String escapedMethodName = methodName.substring(methodName.lastIndexOf(File.separator)+1, methodName.lastIndexOf("."));
			if(sourcesList.contains(escapedMethodName) || escapedMethodName.contains("doGet"))
				for (String checkpoint : methodsStatsMap.get(methodName).checkpointGuardMaps.keySet())
					out += checkpoint + " : " + methodsStatsMap.get(methodName).checkpointGuardMaps.get(checkpoint)
					+ "\n";
		}

		return out;
	}

	void extractSecurityGuardsFromFile(File secsumFile) {
		String fileContent = Utils.readTextFile(secsumFile.getAbsolutePath()).toString();
		try {
			if(!fileContent.trim().equals("")) {
				String[] applicationResults = fileContent.trim().split("\\}");
				for (String method_result : applicationResults) {
					HashMap<String, String> methodsResults = new HashMap<String, String>();
					String[] elements = method_result.split("\\{")[0].split("\\(")[0].split(" ");
					String method_name = elements[elements.length - 1];
					for (String element : method_result.split("\\{")[1].split(";")) {
						String lhs = element.split("=")[0].trim();
						if (lhs.matches("(guard)") || lhs.startsWith("ok(")) {
							String rhs = element.substring(element.indexOf("=") + 1);
							methodsResults.put(method_name + "%" + lhs, rhs);
						}
					}
					String methodSignature = getMethodSignatureFromMethPath(method_result);
					if (this.methodSignatureToMethFileMap.get(methodSignature) != null) // if this methods is not a third=part
						// method
						if (this.methodsStatsMap
								.get(this.getMethodNameFromMethPath(methodSignatureToMethFileMap.get(methodSignature))) != null)
							this.methodsStatsMap.get(this.getMethodNameFromMethPath(
									methodSignatureToMethFileMap.get(methodSignature))).checkpointGuardMaps = methodsResults;
						else
							Utils.logErr(this.getClass(), "Could not fidn the entry of the method "
									+ this.getMethodNameFromMethPath(methodSignatureToMethFileMap.get(methodSignature)));

				}
			}
		}
		catch (RuntimeException ex){
			Utils.logErr(this.getClass(), "Could  not extract the security summaries from " + secsumFile.getAbsolutePath() +
					"It's possibly empty or does not follow the secsum files syntax");
		}
	}

	private void extractStatsFromMeth_StatsFile(File meth_statsFile, String usedAlgorithm) {
		String fileContent = Utils.readStringTextFile(meth_statsFile.getAbsolutePath());
		String[] applicationResults = fileContent.replaceAll(" ", "").replaceAll("methods:\\{", "").split("stats")[0].split("\\}\\,");
		// List<String> processedMethodsList = new ArrayList<String>();
		for (int index=0; index < applicationResults.length -1; index++) {
			String method_result = applicationResults[index].trim();
			MethodStatsHelper scgsMethodStats = new MethodStatsHelper();
			scgsMethodStats.usedAlgorithm = usedAlgorithm;
			scgsMethodStats.methodID = getMethodNameFromMethPath(method_result.split(":")[0]);
			methodsStatsMap.put(scgsMethodStats.methodID, scgsMethodStats);
			processedMethodsList.add(scgsMethodStats.methodID);
			String[] elements = method_result.split("infos:")[1].split("stats:")[0].split(",");
			for (String element : elements) {
				String lhs = element.split(":")[0].trim();
				String rhs = element.substring(element.indexOf(":") + 1).replaceAll("\\}", "").trim();
				switch (lhs) {
				case "scfg_generation_time":
					scgsMethodStats.scfg_generation_time = (long) (1000000 * Float.parseFloat(rhs));
					break;
				case "nb_scfg_locations":
					scgsMethodStats.nb_scfg_locations = Integer.parseInt(rhs);
					break;
				case "nb_scfg_transitions":
					scgsMethodStats.nb_scfg_transitions = Integer.parseInt(rhs);
					break;
				case "nb_scfg_variables":
					scgsMethodStats.nb_scfg_variables = Integer.parseInt(rhs);
					break;
				case "nb_scfg_state_variables":
					scgsMethodStats.nb_scfg_state_variables = Integer.parseInt(rhs);
					break;
				case "nb_scfg_input_variables":
					scgsMethodStats.nb_scfg_input_variables = Integer.parseInt(rhs);
					break;
				case "nb_scfg_contr_variables":
					scgsMethodStats.nb_scfg_contr_variables = Integer.parseInt(rhs);
					break;
				case "max_scfg_in_degree":
					scgsMethodStats.max_scfg_in_degree = Integer.parseInt(rhs);
					break;
				case "max_scfg_out_degree":
					scgsMethodStats.max_scfg_out_degree = Integer.parseInt(rhs);
					break;
				case "summarization":
					scgsMethodStats.summarization = rhs;
					break;
				case "model_instantiation_time":
					scgsMethodStats.model_instantiation_time = (long) (1000000 * Float.parseFloat(rhs));
					break;
				case "synthesis_time":
					scgsMethodStats.synthesis_time = (long) (1000000 * Float.parseFloat(rhs));
					break;
				case "triangularization_time":
					scgsMethodStats.triangularization_time = (long) (1000000 * Float.parseFloat(rhs));
					break;
				case "unsat_guard":
					scgsMethodStats.unsat_guard = Boolean.parseBoolean(rhs);
					break;
				}
			}

		}
	}

	private String getMethodNameFromMethPath(String methodPath) {
		// Utils.log(this.getClass(),methodPath + " " + applicationName);
		while(methodPath.contains("//")||methodPath.contains("\""))
			methodPath = methodPath.trim().replaceAll("\"", "").replaceAll("//", File.separator);
		if(methodPath.contains(applicationName))
			return methodPath.substring(methodPath.indexOf(applicationName));
		else
			return methodPath;
	}

	private String getMethodSignatureFromMethPath(String method_result) {
		return method_result.split("\\{")[0].replaceAll(" ", "").replaceAll("\n", "").trim();
	}

	public boolean isSecureApplicationBasedonSCGS() {
		return insecureMethodsList.size() == 0;
	}

	void setInsecureMethodList() {
		for (MethodStatsHelper methodPoint : this.methodsStatsMap.values())
			if (!methodPoint.isSecure())
				insecureMethodsList.add(methodPoint.methodID);
	}

}
