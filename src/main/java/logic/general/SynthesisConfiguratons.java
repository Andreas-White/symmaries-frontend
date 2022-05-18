package logic.general;

import java.util.ArrayList;
import java.util.List;

public class SynthesisConfiguratons {
	//public String androidJarPath;
	public String callbacks;
	public boolean explicitConfEnabeled; //
	public boolean implicitConfEnabeled; //
	public boolean taintCheckingEnabled; //
	public String assumedSecSigsFilePath; //
	public String[] requiredClassesPaths;
	public boolean ignoreCheckPoints;
	public boolean monitorType = true;
	public String xmlSourcesAndSinks;
	public boolean loadFromApk;
	public int methodSkipParameter = 0;
	//public String scgsPath;
	public boolean exceptionsEnabled = false;
	public boolean clearOldGeneratedfiles = false;
	public List<String> excludedMathods = new ArrayList<String>();
	public List<String> excludedClasses = new ArrayList<String>();
	public String targetPath;
	public String inputPath;
	public String defaultSecuritySummary = "-<~;";

	public String monitorHelperPath;
	public List<String> thirdPartyMethods = new ArrayList<String>();
	public String SymmariesCommandFile = "";
	public String symmariesPath = "";

	public SynthesisConfiguratons() {
		// TODO Auto-generated constructor stub
	}

	public SynthesisConfiguratons(boolean b, boolean c, boolean d, boolean e) {
		setChecksExplicitConifidentiality(b);
		implicitConfEnabeled = c;
		taintCheckingEnabled = d;
		ignoreCheckPoints = false;
	}

	/*	public boolean isLocalReax() {
		if (reaxPath == null || isValidIP(reaxPath))
			return false;
		return true;
	}*/

	// Check if the IP is valid
	/*	private boolean isValidIP(String IP) {
		String[] IpParts = IP.split("\\.");
		if (IpParts.length == 4) {
			for (int i = 0; i < 4; i++) {
				try {
					int intIpParts = Integer.parseInt(IpParts[i]);
					if (intIpParts < 0 || intIpParts > 255) {
						return false;
					}

				} catch (NumberFormatException e) {
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}*/

	public void setCheckIntegrity(boolean checkIntegrity1) {
		this.taintCheckingEnabled = checkIntegrity1;
	}

	public void setChecksExplicitConifidentiality(boolean checksExplicitConifidentiality) {
		this.explicitConfEnabeled = checksExplicitConifidentiality;
	}

}
