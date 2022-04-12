/**
 * 
 */
package logic.tool;

import logic.general.Utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author nakhaa
 *
 */
public class JARApplicationHelper extends JavaApplicationHelper {
	// private String jarFilePath;

	/**
	 * @param exConifidentiality
	 * @param imConifidentiality
	 * @param integrity
	 * @param isModelRunFreeVersion
	 */
	public JARApplicationHelper(boolean exConifidentiality, boolean imConifidentiality, boolean integrity,
			boolean isModelRunFreeVersion) {
		super(exConifidentiality, imConifidentiality, integrity, isModelRunFreeVersion);
	}

	/**
	 * @param inputpath
	 * @param outputpath
	 * @param inputFolderpath
	 */
	public JARApplicationHelper(String inputpath, String outputpath, String inputFolderpath) {
		super(inputpath, outputpath, inputFolderpath);
	}

	public JARApplicationHelper() {
		super();
	}



	/**
	 * @param jarRootDirectory
	 * @param excludedApplications
	 * @param exprType
	 * @param secsumfilePath
	 * @param classesFolderMap
	 * @param generateSCGSInputs 
	 * @param scgscommandFilePath 
	 * @param taintChecking 
	 */
	public void checkJARsInDirectory(File jarRootDirectory, ArrayList<String> excludedApplications, String exprType,
			String secsumfilePath,
			HashMap<String,String> classesFolderMap, boolean exceptionEnabeled, boolean generateSCGSInputs, 
			String symmariesPth, String scgscommandFilePath,int methodSkipParameter, boolean taintChecking) {
		if (jarRootDirectory.exists()) {
			for (File jarfile : Utils.getFilesOfTypes(jarRootDirectory.getAbsolutePath(), new String[] {".jar",".war"}))
				if (!excludedApplications.contains(jarfile.getAbsolutePath())) {
					String outputSubdirectory = jarfile.getAbsolutePath().replaceAll(this.getInputPath(), "");
					String jarContentPath = this.getOutputPath()
							+ outputSubdirectory.substring(0, outputSubdirectory.lastIndexOf("."))
							+ File.separator + "JarContent";
					if (processSingleJARFile(jarfile, jarContentPath,
							(new File(jarContentPath).getParent()) + File.separator + exprType,
							secsumfilePath,
							((jarfile).getParentFile()).getParent() + File.separator + "rifl.xml",
							classesFolderMap,
							exceptionEnabeled, 
							generateSCGSInputs,
							symmariesPth,
							scgscommandFilePath, 
							methodSkipParameter,
							taintChecking))
						;
					processedApplications++;
				}
			Utils.log(this.getClass(), processedApplications + " applications have been processed!");
		} else
			Utils.log(this.getClass(), "Could not find the directory " + jarRootDirectory.getAbsolutePath());
	}

	public void checkApplicationsInDirectory(File directory, ArrayList<String> excludedApplications,
			String xmlSrcSinkFileName,
			String secsumfilePath,
			HashMap<String,String> classesFolderMap, boolean exceptionEnabeled, boolean generateSCGSInputs, String symmariesPath,
			String commadFilePath, int methodSkipParameter, boolean taintChecking) {
		if (directory.exists()) {
			for (String application : Utils.getDirectories(directory.getAbsolutePath()))
				if (!excludedApplications.contains(application)) {
					File file = new File(directory + File.separator + application);
					String jarContentPath = directory + File.separator + application + File.separator + "JarContent";
					boolean hasWebInf = new File(jarContentPath + File.separator + "/WEB-INF/classes/").exists();				
					String classesFilePath = jarContentPath + File.separator + (hasWebInf?"/WEB-INF/classes/":
						(classesFolderMap!=null && classesFolderMap.get(file.getName())!=null?
								classesFolderMap.get(file.getName()):""));

					if (processSingleJavaApplication(classesFilePath, (new File(jarContentPath).getParent()) + "/syrsInput",
							directory.getAbsolutePath() + File.separator + xmlSrcSinkFileName, secsumfilePath, exceptionEnabeled,
							generateSCGSInputs, symmariesPath, commadFilePath, methodSkipParameter, taintChecking));
					processedApplications++;
					Utils.log(this.getClass(), processedApplications + " applications have been processed!");
				}
		} else
			Utils.log(this.getClass(), "Could not find the directory " + directory.getAbsolutePath());
	}


	/**
	 * @param jarFilePath
	 * @param jarContentPath
	 * @param targetPath
	 * @param secsumfilePath
	 * @param classesFolderMap
	 * @param symmariesPath 
	 * @param taintChecking 
	 * @return
	 */
	public boolean processSingleJARFile(File jarFilePath, String jarContentPath, String targetPath,
			String secsumfilePath, String xmlSrcSinkPath, HashMap<String, String> classesFolderMap, boolean exceptionEnabeled, 
			boolean generateSCGSInputs, String symmariesPath, String commadFilePath, int methodSkipParameter, boolean taintChecking) {

		Utils.log(getClass(), "Started Processing " + jarFilePath);
		if (Utils.uncompressZipFile(jarFilePath.getAbsolutePath(), jarContentPath, ".class")) {
			//String classesFilePath = this.findClassesFolder(jarContentPath);
			//System.err.println(jarFilePath + "," + classesFilePath);
			boolean hasWebInf = new File(jarContentPath + File.separator + "/WEB-INF/classes/").exists();				
			String classesFilePath = jarContentPath + File.separator + (hasWebInf?"/WEB-INF/classes/":
				(classesFolderMap!=null && classesFolderMap.get(jarFilePath.getName())!=null?
						classesFolderMap.get(jarFilePath.getName()):""));
			if(processSingleJavaApplication(classesFilePath, targetPath, 
					xmlSrcSinkPath, secsumfilePath, exceptionEnabeled,generateSCGSInputs, 
					symmariesPath, commadFilePath, methodSkipParameter, taintChecking))
					return true;
			else
				return false;
		}
		Utils.logErr(this.getClass(),
				"Could not uncompress the jar file " + jarFilePath);
		return false;
	}


	public String findClassesFolder(String jarContentPath) {
		if(Utils.getFilesOfTypes(jarContentPath, new String[] {".class"}).size()==0)
			return null;
		String[] directories = new File(jarContentPath).list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		if(directories!=null) {
			boolean[] foldersWithClasses = new boolean[directories.length];
			int numberOfFoldersWithClasses = 0;
			for(int index=0; index<directories.length;index++) {
				ArrayList<File> temp = Utils.getFilesOfTypes(jarContentPath + File.separator + directories[index], new String[] {".class"});
				if(temp!=null && temp.size()>0)
					numberOfFoldersWithClasses++;
				foldersWithClasses[index] = temp!=null && temp.size()>0;
			}
			if(numberOfFoldersWithClasses==1) {
				for(int index=0; index<foldersWithClasses.length;index++) 
					if(foldersWithClasses[index])
						return jarContentPath + File.separator + directories[index];
			}
			else
				if(numberOfFoldersWithClasses==0)
					return jarContentPath;
		}
		return null;

	}
	
	public boolean loadSingleJARFileIntoSoot(File jarFilePath, String jarContentPath, String targetPath) {
		HashMap<String, String> classesFolderMap = null;
		Utils.log(JARApplicationHelper.class,"Started Processing " + jarFilePath);
		if (Utils.uncompressZipFile(jarFilePath.getAbsolutePath(), jarContentPath, ".class")) {
			//String classesFilePath = this.findClassesFolder(jarContentPath);
			//System.err.println(jarFilePath + "," + classesFilePath);
			boolean hasWebInf = new File(jarContentPath + File.separator + "/WEB-INF/classes/").exists();
			String classesFilePath = jarContentPath + (hasWebInf? File.separator + "/WEB-INF/classes/":
				(classesFolderMap!=null && classesFolderMap.get(jarFilePath.getName())!=null?
						classesFolderMap.get(jarFilePath.getName()):""));
			loadSingleJavaApplicationIntoSoot(classesFilePath, targetPath);
			return true;
		}
		Utils.logErr(this.getClass(),
				"Could not uncompress the jar file from " + jarFilePath);
		return false;
	}
}
