package logic.jimpleProcessor;

import logic.general.Constants;
import logic.general.SynthesisConfiguratons;
import logic.general.Utils;
import logic.general.XMLParser;
import org.xmlpull.v1.XmlPullParserException;
import soot.*;
import soot.jimple.ClassConstant;
import soot.jimple.FieldRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.internal.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JimpleProjectHelper {
	public SourceSinkHelper sourceSinkHelper= new SourceSinkHelper();
	Hashtable<String, ArrayList<SecuritySignature>> securitySignatures = new Hashtable<String, ArrayList<SecuritySignature>>();
	public SynthesisConfiguratons configurations;
	private LinkedHashMap<SootMethod, String> librarySecurityAssumptions = new LinkedHashMap<SootMethod, String> ();
	public String staticFileds = "";

	public class SecuritySignature {
		public String className;
		public String method;
		public String returnType;
		public String[] argsType = new String[] {};
		public String secSignature;
		public boolean isStatic = false;


		public boolean extractSecuritySigntaure(String line) {
			String[] elements = line.split("\\{");
			Matcher m = Pattern.compile("\\S+:\\S+\\(").matcher(elements[0]);
			try {
				if (m.find()) {
					String packAndMethodName = m.group().substring(0, m.group().length() - 1);
					className = packAndMethodName.split(":")[0];
					method = packAndMethodName.split(":")[1];
				} else {
					className = elements[0].substring(0, elements[0].indexOf('('));
					method = "";
				}

				m = Pattern.compile("\\(.*\\)").matcher(elements[0]);
				if (m.find()) {
					String tmp = m.group();
					tmp = tmp.substring(1, tmp.length() - 1);
					if (!tmp.equals(""))
						argsType = tmp.replaceAll(" ", "").split(",");
				}

				if (elements[0].contains(":")) {
					m = Pattern.compile("(\\S)*\\s*").matcher(elements[0].replaceAll("static", "").trim());
					if (m.find())
						returnType = m.group().trim();
				}
				secSignature = elements[1].split("\\}")[0].replaceAll("\\{", "").replaceAll("\\}", "");
				isStatic = line.trim().startsWith("static");
			} catch (StringIndexOutOfBoundsException ex) {
				Utils.log(this.getClass(),
						"This line of secsum file does not follow a correct method signature: \n 		" + line);
				return false;
			} catch (ArrayIndexOutOfBoundsException ex) {
				Utils.log(this.getClass(),
						" This line of secsum file does not follow a correct method signature: \n 		" + line);
				return false;

			}
			return true;
		}

		public boolean matches(SootMethod method2) throws TransformationException {
			if (method2.getParameterCount() != argsType.length)
				return false;
			TypeProcessor typeProcessor = new TypeProcessor();
			if (!(method2.getDeclaringClass().toString().equals(className) || typeProcessor
					.isSubTypeSuperType(method2.getDeclaringClass(), Scene.v().getSootClass(className)))) {
				return false;
			}
			if (argsType != null)
				for (int i = 0; i < argsType.length; i++)
					if (!(argsType[i].equals(method2.getParameterType(i).toString()) || typeProcessor
							.isSubTypeSuperType(Scene.v().getSootClass(method2.getParameterType(i).toString()),
									Scene.v().getSootClass(argsType[i])))) {
						return false;
					}
			if (returnType!=null && (!(this.returnType.equals(method2.getReturnType().toString()) || typeProcessor.isSubTypeSuperType(
					Scene.v().getSootClass(method2.getReturnType().toString()), Scene.v().getSootClass(returnType)))) ) {
				return false;
			}
			if (returnType==null &&!method2.isConstructor()) {
				return false;
			}
			//Utils.log(this.getClass(), "Security signature of " + method2 + " was found.");
			return true;
		}

		@Override
		public String toString() {
			String argsTypesDesc = "", separator = "";
			if (argsType != null)
				for (String arg : argsType) {
					argsTypesDesc += separator + arg;
					separator = ",";
				}

			return returnType + " " + className + ":" + method + "(" + argsTypesDesc + ")" + "{" + secSignature + "}\n";
		}
	}

	public static String escapeTypeName(String type) {
		return type.toString().replaceAll("-", "");
	}

	public static String escapeTypeName(Type type) {
		return type.toString().replaceAll("-", "");
	}


	public JimpleProjectHelper(SynthesisConfiguratons settings1) {
		configurations = settings1;
	}

	public JimpleProjectHelper() {
		// TODO Auto-generated constructor stub
	}

	public void analyzeEntryPoints() {
		TypeProcessor typeProcessor = new TypeProcessor();
		for (SootMethod entryPoint : Scene.v().getEntryPoints())
			analyzeSingleEntrypoint(SootUtilities.getClassName(entryPoint.getDeclaringClass()), entryPoint,
					typeProcessor, sourceSinkHelper);
		typeProcessor.writeTypesClassesFile(configurations.targetPath);
		typeProcessor.writeTypeHierarchyDotFile(configurations.targetPath);
	}

	public void analyzeSingleEntrypoint(String className, SootMethod entryPoint, TypeProcessor typeProcessor, SourceSinkHelper sourceSinkHelper) {
		SymmariesHelper scsgHelper = new SymmariesHelper(this, configurations, typeProcessor,sourceSinkHelper);
		scsgHelper.processMethod(entryPoint, SootUtilities.getClassName(entryPoint.getDeclaringClass()));
		generateAllMethSecSumsFiles(scsgHelper);
		// return scsgHelper.typeProcessor;
	}

	public boolean constructSymmariesInputFromAPKFiles(String apkFile) {
		try {
			Utils.remakeDirectory(configurations.targetPath + File.separator + "/Jimple");
			Utils.remakeDirectory(configurations.targetPath + File.separator + "/CFG");
			Utils.remakeDirectory(configurations.targetPath + File.separator + "/Meth");
			Utils.log(this.getClass(), "Soot: Loading " + apkFile);
			SootInit.initSootForApk(configurations.requiredClassesPaths, apkFile, configurations.xmlSourcesAndSinks,
					configurations.callbacks);
			analyzeEntryPoints();
			return true;
		} catch (IOException e) {
			Utils.logErr(getClass(), "Could not create a dirctory in " + configurations.targetPath);
			//e.printStackTrace();
		} catch (XmlPullParserException e) {
			Utils.logErr(getClass(), "Ill-formatted xml file ");
			//e.printStackTrace();
		}
		return false;
	}

	public boolean constructSymmariesInputFromClassFiles(String className, String methodName) {
		if(loadApplication()) {
			//		TypeProcessor typeProcessor = ;
			SymmariesHelper symmariesHelper = new SymmariesHelper(this, configurations, new TypeProcessor(), sourceSinkHelper);
			processApplication(className, methodName, symmariesHelper);
			generateAllMethSecSumsFiles(symmariesHelper);
			symmariesHelper.typeProcessor.writeTypesClassesFile(configurations.targetPath);
			symmariesHelper.typeProcessor.writeTypeHierarchyDotFile(configurations.targetPath);
			return true;
		} else
			return false;
	}
	
	public void constructSymmariesInputFromInputGenerator(String className, String methodName) {
		SymmariesHelper symmariesHelper = new SymmariesHelper(this, configurations, new TypeProcessor(), sourceSinkHelper);
		processApplication(className, methodName, symmariesHelper);
		generateAllMethSecSumsFiles(symmariesHelper);
		symmariesHelper.typeProcessor.writeTypesClassesFile(configurations.targetPath);
		symmariesHelper.typeProcessor.writeTypeHierarchyDotFile(configurations.targetPath);
	}

	private void processApplication(String className, String methodName, SymmariesHelper symmariesHelper) {
		if (methodName == null) {
			// it first loads all the calsses again
			for (SootClass sootClass : Scene.v().getApplicationClasses())
				Scene.v().loadClassAndSupport(sootClass.getName());
			Scene.v().loadNecessaryClasses();
			SootClass[] applicationClasses = new SootClass[] {};

			applicationClasses = Scene.v().getApplicationClasses().toArray(applicationClasses);
			for (SootClass sootClass : applicationClasses) {// Scene.v().getApplicationClasses()) {
				for (SootMethod entryPoint : sootClass.getMethods()) {
					if (!symmariesHelper.processedMethods.contains(getMethodUniqueName(entryPoint, sootClass.getName()))) {
						Utils.log(this.getClass(), "Processing the entry point " + entryPoint.getDeclaringClass() + "."
								+ entryPoint.getName());
						symmariesHelper.processMethod(entryPoint,
								SootUtilities.getClassName(entryPoint.getDeclaringClass()));
					}
				}
			}
		} else
			symmariesHelper.processMethod(Scene.v().getMethod(methodName), className);		
	}

	private boolean loadApplication() {
		if (configurations.clearOldGeneratedfiles || !new File(configurations.targetPath+ File.separator + "/CFG").exists()
				|| !new File(configurations.targetPath+ File.separator + "/Jimple").exists()
				|| !new File(configurations.targetPath+ File.separator + "/Meth").exists())
			try {
				Utils.remakeDirectory(configurations.targetPath + File.separator + "/Jimple");
				Utils.remakeDirectory(configurations.targetPath + File.separator + "/CFG");
				Utils.remakeDirectory(configurations.targetPath + File.separator + "/Meth");
			} catch (IOException e) {
				Utils.log(this.getClass(), "The  path " + configurations.targetPath + " does not exist!\n");
				return false;
			}
		System.out.print("Soot: Loading from the path: " + configurations.inputPath);
		try {
			SootInit.initSootForJavaClasses(configurations.inputPath, configurations.requiredClassesPaths);
		}
		catch(RuntimeException ex) {
			Utils.logErr(getClass(), "Soot could not load the application successfully! ");
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	public String escapeMethodName(String name) {
		return name.replaceAll("<", "").replaceAll(">", "").replaceAll("-", "");
	}

	// public String getUniqueName(MethodInvocation methodInvocation) {

	// return
	// methodInvocation.resolveMethodBinding().getDeclaringClass().getQualifiedName()
	// + methodInvocation.getName();
	// }

	public void extractSinkAndSources() {
		/*		if(configurations.sourcesAndSinks==null) {
			Utils.log(getClass(), "The source/sink file (in text format) is missing.");
			return;
		}

		File file = new File(configurations.sourcesAndSinks);
		if(!file.exists()){
			Utils.log(getClass(), "The source/sink file (in text format) " + configurations.sourcesAndSinks + " does not exist!");
			return;
		}
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while (line != null) {
				String[] elements = line.split("->");
				if (!line.equals("")) {
					String methodPackageName = elements[0].split(":")[0].replaceAll("<", "") + "_"
							+ elements[0].split(":")[1].trim().split(" ")[1];
					methodPackageName = methodPackageName.substring(0, methodPackageName.indexOf("("));
					// java.net.URLConnection: java.io.InputStream getInputStream()
					if (elements[1].contains("SINK"))
						sourceSinkHelper.sinkMethods.add(methodPackageName);
					else
						sourceSinkHelper.sourceMethods.add(methodPackageName);
					// Utils.log(this.getClass(),methodPackageName.trim());
				}
				line = br.readLine();
			}
		} catch (IOException e) {
			Utils.logErr(this.getClass(), "Couldn't find the file " + file.getAbsolutePath());
		}*/
	}

	private void generateAllMethSecSumsFiles(SymmariesHelper scsgHelper) {
		String meth_files = "";
		for (String method : scsgHelper.processedMethods)
			if (!configurations.excludedMathods.contains(method)) { // && !method.contains("dummyMainMethod"))
				meth_files += configurations.targetPath + "/Meth/" + method + ".meth\n";
				// Utils.log(this.getClass(),"Exported the file " + "/Meth/" + method +
				// ".meth");
			}
		Utils.writeTextFile(configurations.targetPath + "/Meth/all.meth_files", meth_files);
		Utils.log(this.getClass(), "Created the file /Meth/all.meth_files");
		String secstubs = "";
		for (SootMethod method : getSortedLibrarySecurityAssumptions()) {
			String assumption = scsgHelper.constructHeader(method, true, SootUtilities.getClassName(method.getDeclaringClass()), new ArrayList<Value>());
			secstubs += assumption + "{" + getLibrarySecurityAssumptions().get(method) + "}\n";
		}

		for (Type type : scsgHelper.dummyLowSinkMethodsList) 
			secstubs += "void " + Constants.dummyClass + ":" + Constants.lowSinkMethodName + "(" + type + "){output_⊥;}\n";
		for (Type type : scsgHelper.dummyHighSinkMethodsList) 
			secstubs += "void " + Constants.dummyClass + ":" + Constants.highSinkMethodName + "(" + type + "){@!;}\n";
		for (Type type : scsgHelper.dummyLowSourceMethodsList) 
			secstubs += type + " " + Constants.dummyClass + ":" + Constants.lowSourceMethodName + "(){ -<~; return new_?}\n";
		for (Type type : scsgHelper.dummyHighSourceMethodsList) 
			secstubs += type + " " + Constants.dummyClass + ":" + Constants.highSourceMethodName + "(){ -<~; return .*h }\n";

		String sourcesList = "";
		for(SootMethod method: Scene.v().getEntryPoints()) {
			sourcesList  += getMethodUniqueName(method, method.getDeclaringClass().getName())+ "\n";
		}
		/*for(String sourceMethod: sourceSinkHelper.sourceMethods) {
			sourcesList += sourceMethod + "\n";
		}*/

		Utils.writeTextFile(configurations.targetPath + "/Meth/all." + Constants.srcsExtension, sourcesList);
		Utils.writeTextFile(configurations.targetPath + "/Meth/all.secstubs", secstubs);
		Utils.log(this.getClass(), "Created the file /Meth/all.secstubs");
	}

	public String getMethodUniqueName(SootMethod method, String declaringClass) {
		return (method.isConstructor() ? declaringClass : // SootUtilities.getClassName(method.getDeclaringClass()):
			declaringClass + // SootUtilities.getClassName(method.getDeclaringClass()) +
			// //(method.isConstructor()?"":
			"_" + escapeMethodName(method.getName()) + method.equivHashCode())
				.replaceAll("-", "");
	}

	public String getMethodUniqueNameV2(SootMethod method) {
		return SootUtilities.getClassName(method.getDeclaringClass()) + // (method.isConstructor()?"":
				"_" + escapeMethodName(method.getName());
	}

	public String getSymmariesExpression(Value value, Value renameToThisVariable, TypeProcessor typeProcessor)
			throws TransformationException {
		// Value value = expression.getValue();
		if (SootUtilities.isLocalVariable(value))
			if (renameToThisVariable != null && renameToThisVariable.toString().equals(value.toString()))
				return "this";
			else
				return value.toString();
		if (SootUtilities.isParameterRef(value) || SootUtilities.isConstant(value))
			return value.toString();

		if (SootUtilities.isStaticField(value)) {
			staticFileds += SootUtilities.getClassName(((StaticFieldRef) value).getField().getDeclaringClass())
					.toString() + "\n";
			// throw new JimpleTransformationException (value + " is a static field. This
			// tool does not support static fields") ;
			return SootUtilities.getClassName(((StaticFieldRef) value).getField().getDeclaringClass()) + "."
			+ ((StaticFieldRef) value).getField().getName().toString();
		}
		if (SootUtilities.isFieldRef(value)) {
			return getSymmariesExpression(((FieldRef) value).getUseBoxes().get(0).getValue(), renameToThisVariable,
					typeProcessor)
					+ "."
					+ (((FieldRef) value).getField().getName().matches("[A-Z].*")
							? ((FieldRef) value).getField().getName()
									: ((FieldRef) value).getField().getName());
			// return ((FieldRef)value).getUseBoxes().get(0).getValue().toString() + "." +
			// ((FieldRef)value).getField().getName().toString();
		}
		if (SootUtilities.isCastExpr(value)) {
			JCastExpr expr = (JCastExpr) value;
			return "(" + escapeTypeName(expr.getType()) + ") "
			+ getSymmariesExpression(expr.getOp(), renameToThisVariable, typeProcessor);
		}

		if (value instanceof JNegExpr) {
			JNegExpr expr = (JNegExpr) value;
			return " ! (" + getSymmariesExpression(expr.getOp(), renameToThisVariable, typeProcessor) + ")";
		}

		if (value instanceof JAndExpr || value instanceof JOrExpr || value instanceof JDivExpr
				|| value instanceof JSubExpr || value instanceof JCmplExpr || value instanceof JMulExpr
				|| value instanceof JAddExpr || value instanceof JCmpExpr) {
			String op = (value instanceof JAndExpr ? " & "
					: (value instanceof JOrExpr ? " | "
							: (value instanceof JDivExpr ? "/"
									: (value instanceof JSubExpr ? "-"
											: (value instanceof JCmplExpr ? " cmpl "
													: (value instanceof JMulExpr ? " * "
															: (value instanceof JAddExpr ? "+"
																	: (value instanceof JCmpExpr ? " cmp " : ""))))))));
			typeProcessor.addsubtypeSupertypeRelation(((AbstractBinopExpr) value).getOp1().getType(),
					((AbstractBinopExpr) value).getOp2().getType());
			return "(" + getSymmariesExpression(((AbstractBinopExpr) value).getOp1(), renameToThisVariable, typeProcessor)
			+ op + getSymmariesExpression(((AbstractBinopExpr) value).getOp2(), renameToThisVariable, typeProcessor)
			+ ")";
		}
		if (value instanceof JLtExpr || value instanceof JLeExpr || value instanceof JNeExpr || value instanceof JGtExpr
				|| value instanceof JGeExpr || value instanceof JRemExpr || value instanceof JEqExpr
				|| value instanceof JUshrExpr || value instanceof JShrExpr || value instanceof JShlExpr
				|| value instanceof JCmpgExpr) {
			String op = (value instanceof JLtExpr ? "<"
					: (value instanceof JLeExpr ? "<="
							: (value instanceof JNeExpr ? "!="
									: (value instanceof JGtExpr ? ">"
											: (value instanceof JGeExpr ? ">="
													: (value instanceof JRemExpr ? "%"
															: (value instanceof JEqExpr ? "=="
																	: (value instanceof JUshrExpr ? ">>>"
																			: (value instanceof JShlExpr ? "<<"
																					: (value instanceof JShrExpr ? ">>"
																							: " cmpg "))))))))));
			typeProcessor.addsubtypeSupertypeRelation(((AbstractBinopExpr) value).getOp1().getType(),
					((AbstractBinopExpr) value).getOp2().getType());
			return "(" + getSymmariesExpression(((AbstractBinopExpr) value).getOp1(), renameToThisVariable, typeProcessor)
			+ op + getSymmariesExpression(((AbstractBinopExpr) value).getOp2(), renameToThisVariable, typeProcessor)
			+ ")";
		}
		if (value instanceof JArrayRef) {
			JArrayRef expr = (JArrayRef) value;
			return getSymmariesExpression(expr.getBase(), renameToThisVariable, typeProcessor) + "["
			+ getSymmariesExpression(expr.getIndex(), renameToThisVariable, typeProcessor) + "]";
		}
		if (value instanceof JNewArrayExpr) {
			JNewArrayExpr expr = (JNewArrayExpr) value;
			return "new " + expr.getBaseType() + "[" + expr.getSize() + "]";
		}
		if (value instanceof JLengthExpr)
			return "#" + ((JLengthExpr) value).getOp();
		if (value instanceof JInstanceOfExpr)
			return value.toString();
		if (value instanceof JXorExpr) {
			return "(!" + getSymmariesExpression(((JXorExpr) value).getOp1(), renameToThisVariable, typeProcessor) + " & "
					+ getSymmariesExpression(((JXorExpr) value).getOp2(), renameToThisVariable, typeProcessor) + ") |" + "(!"
					+ getSymmariesExpression(((JXorExpr) value).getOp2(), renameToThisVariable, typeProcessor) + " & "
					+ getSymmariesExpression(((JXorExpr) value).getOp1(), renameToThisVariable, typeProcessor) + ")";
		}

		if (SootUtilities.isConstant(value))
			return Constants.NIL;

		if (SootUtilities.isNullConstant(value))
			return value.toString();
		if (value instanceof ClassConstant) {
			return ((ClassConstant) value).toSootType().toString();// + "\"";
		}
		if (value instanceof JNewMultiArrayExpr) {
			throw new TransformationException("Symmaries does not support mutil arrays");
		}
		throw new TransformationException("Symmaries does not support expressions of type " +  value.getType());

		//		Utils.log(this.getClass(), value.toString());
		//		return "";
	}

	public void loadAssumedSecuritySignatures() {
		if(configurations.assumedSecSigsFilePath==null) {
			Utils.log(getClass(), "The security stubs file is missing." );
			return;
		}
		File file = new File(configurations.assumedSecSigsFilePath);
		if(!file.exists()) {
			Utils.log(getClass(), "The security stubs file " + file.getAbsolutePath() + " does not exist." );
			return;
		}
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while (line != null) {
				if (!line.trim().equals("")) {
					SecuritySignature securitySignature = new SecuritySignature();
					if (securitySignature.extractSecuritySigntaure(line)) {
						if (securitySignatures.get(securitySignature.method) == null)
							securitySignatures.put(securitySignature.method, new ArrayList<SecuritySignature>());
						securitySignatures.get(securitySignature.method).add(securitySignature);
					}
				}
				line = br.readLine();
			}
		} catch (IOException e) {
			Utils.logErr(this.getClass(), "Couldn't find the file " + file.getAbsolutePath());
		}
	}

	public LinkedHashMap<SootMethod, String> getLibrarySecurityAssumptions() {
		return librarySecurityAssumptions;
	}

	public class SootMethodsComparator implements Comparator {
		@Override
		public int compare(Object o1, Object o2) {
			if(((SootMethod)o1).isJavaLibraryMethod() &&
					!((SootMethod)o2).isJavaLibraryMethod())
				return 1;
			else 
				if(((SootMethod)o2).isJavaLibraryMethod() &&
						!((SootMethod)o1).isJavaLibraryMethod())
					return -1;
				else
					return (((SootMethod)o1).getDeclaringClass().getName()+((SootMethod)o1).getName()).compareTo
							(((SootMethod)o2).getDeclaringClass().getName()+((SootMethod)o2).getName());
		}
	}

	public List<SootMethod> getSortedLibrarySecurityAssumptions() {
		List<SootMethod> sortedKeys = new ArrayList<SootMethod>();
		sortedKeys.addAll(librarySecurityAssumptions.keySet());
		Collections.sort(sortedKeys, new SootMethodsComparator());
		return sortedKeys;
	}


	public void setLibrarySecurityAssumptions(LinkedHashMap<SootMethod, String> librarySecurityAssumptions) {
		this.librarySecurityAssumptions = librarySecurityAssumptions;
	}

	public void extractSourceSinkFromXML() {
		sourceSinkHelper= new SourceSinkHelper();
		XMLParser xmlParser = new XMLParser();
		if(xmlParser.readXMLFile(configurations.xmlSourcesAndSinks)) {
			Utils.log(getClass(),"Processing the xml file " + configurations.xmlSourcesAndSinks);
			xmlParser.getAllSinksOrSources(sourceSinkHelper.highSources, true,true);
			xmlParser.getAllSinksOrSources(sourceSinkHelper.highSinks, true,false);
			xmlParser.getAllSinksOrSources(sourceSinkHelper.lowSources, false,true);
			xmlParser.getAllSinksOrSources(sourceSinkHelper.lowSinks, false,false);
		}
		if(sourceSinkHelper.highSources.isEmpty())
			Utils.log(getClass(), "The application has NO high source!");
		if(sourceSinkHelper.lowSources.isEmpty())
			Utils.log(getClass(), "The application has NO low sources!");
		if(sourceSinkHelper.highSinks.isEmpty())
			Utils.log(getClass(), "The application has NO high sinks!");
		if(sourceSinkHelper.lowSinks.isEmpty())
			Utils.log(getClass(), "The application has NO low sink!");
	}
	
	public void loadJavaApplicationIntoSoot(String className, String methodName) {
		loadApplication();
	}
	
	private ArrayList<SootMethod> applicationMethods;

	public Object[] getApplicationMethods() {
		if(applicationMethods == null) {
			applicationMethods = new ArrayList<SootMethod>();
			for (SootClass sootClass : Scene.v().getApplicationClasses())
				Scene.v().loadClassAndSupport(sootClass.getName());
			Scene.v().loadNecessaryClasses();
			SootClass[] applicationClasses = new SootClass[] {};

			applicationClasses = Scene.v().getApplicationClasses().toArray(applicationClasses);
			for (SootClass sootClass : applicationClasses) {// Scene.v().getApplicationClasses()) {
				for (SootMethod entryPoint : sootClass.getMethods()) {
					applicationMethods.add(entryPoint);
				}
			}
		}
		return applicationMethods.toArray();
	}
}
