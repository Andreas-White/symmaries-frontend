package logic.tool;

import logic.general.Utils;

import java.io.File;

public class JisymCompiler {

	private static String toolGuide;

	public JisymCompiler() {

	}

	public static void main(String[] args) throws Exception {
		setToolGuide();
		String jarFilePath = null, targetPath = null, secsumfilePath = null;
		//boolean exceptionEnabeled = false;
		boolean executeSCGS = false;
		if(args.length==0)
			Utils.log(JisymCompiler.class,"Please enter a JAR file for analysis." );
		else {			
			if(args[0].equals("--help") || args[0].equals("-h")) {
				System.out.println(toolGuide);
				System.exit(0);
			}
			jarFilePath = args[0];
			String xmlFilePath = "";
			String symmariesPath = "";
			boolean taintChecking = false;
			if(!new File(jarFilePath).exists()) {
				Utils.log(JisymCompiler.class,"The file " +jarFilePath + " does not exist" );
				System.exit(0);
			}
			else {
				if(!jarFilePath.endsWith(".jar")){
					Utils.log(JisymCompiler.class,"Please enter a valid JAR file" );
					System.exit(0);
				} else
					for(int index=1; index < args.length; index ++) {
						if(args[index].equals("-o")) {
							targetPath = args[index+1];
							if(new File(targetPath).exists() && !new File(targetPath).isDirectory() || 
									!new File(targetPath).exists()) {
								Utils.log(JisymCompiler.class,"The output folder " +targetPath + " does not exist." );
								Utils.createDirectory(targetPath);
								Utils.log(JisymCompiler.class,"Created the directory " +targetPath + "." );
							}
							index++;
						}
						else if(args[index].equals("-ss")) {
							xmlFilePath  = args[index+1];
							if(!new File(xmlFilePath).exists()) {
								Utils.log(JisymCompiler.class,"The source/sink file " +xmlFilePath + " does not exist." );
								System.exit(0);
							}
							index++;
						}else if(args[index].equals("-sp")) {
							symmariesPath   = args[index+1];
							if(!new File(symmariesPath).exists()) {
								Utils.log(JisymCompiler.class,"Symmaries path " +symmariesPath + " may not exist." );
							}
							index++;
						}
						else if(args[index].equals("-tf")) {
							secsumfilePath = args[index+1];
							if(!new File(secsumfilePath).exists()) {
								Utils.log(JisymCompiler.class,"Security summery file " +secsumfilePath + " does not exist." );	
								System.exit(0);
							}
							index ++;
						}
						else if(args[index].equals("+taints")) {
							taintChecking  = true;
						}
						else if(args[index].equals("+executeSyrs"))
							executeSCGS  = true;
						else if(args[index].equals("--help") || args[index].equals("-h")) {
							System.out.println(toolGuide);
							System.exit(0);
						}
						else
							Utils.log(JisymCompiler.class,"Unknown argument " +args[index] + ". Skipped.");	
					}
			}

			if(jarFilePath==null){
				Utils.log(JisymCompiler.class,"Enter a JAR file for analysis." );	
				System.exit(0);
			}
			if(xmlFilePath.equals("")){
				Utils.log(JisymCompiler.class,"The source/sink file is missing." );	
			}
			if(symmariesPath.equals("")){
				Utils.log(JisymCompiler.class,"Symmaries path is missing. Considered the default path." );
				symmariesPath = "syrs ";
			}
			if(targetPath==null){
				Utils.log(JisymCompiler.class,"The target folder is missing." );	
				System.exit(0);
			}

			String jarContentPath = targetPath + File.separator + "jarContent";
			int methodSkipParameter = 100;
			JARApplicationHelper jARApplicationHelper = new JARApplicationHelper();
			jARApplicationHelper.processSingleJARFile(new File(jarFilePath), jarContentPath, targetPath, 
					secsumfilePath, xmlFilePath, 
					null, false, true, 
					symmariesPath,
					targetPath + File.separator + "symmariesCommand.command",
					methodSkipParameter,
					taintChecking);
			Utils.deleteDirectory(new File(jarContentPath));
			if(executeSCGS) {
				if (jARApplicationHelper.configurations.symmariesPath.equals("")) {
					Utils.log(JisymCompiler.class,"Symmaries path is missing.\n");
					System.exit(0);
				}
				if(!Utils.runSCGSv2(jARApplicationHelper.configurations.SymmariesCommandFile, jARApplicationHelper.getScgsCommand()))
					Utils.log(JisymCompiler.class, "You may check the generated command file " + targetPath + "/symmariesCommand.command.");
			}
			//Utils.log(JisymCompiler.class, "Finished processing the application!");
		}
	}

	private static void setToolGuide() {
		toolGuide = "JisymCompiler takes a Jar file and generates the input files required to run Symmaries."
				+ "\nYou can find the command to run Symmaries in the target folder (symmariesCommand.command). "
				+ "\n\nUsage: java -jar PathToJisymCompiler.jar jarFileName  [options]\n" + 
				"             \n" + 
				"Options:      \n" + 
				"-o <folder> The output folder\n" + 
				"-ss <file> The source/sink file (xml format)\n" + 
				"-sp <file> The path to the Symmaries executable file\n" + 
				"-tf <file> The security summary file \n" + 
				"+taints To perform taint analysis (Integrity checking)\n" + 
				"+executeSyrs Enables executing Syammries (after generating the Symmaries's input files)\n";
	}
}
