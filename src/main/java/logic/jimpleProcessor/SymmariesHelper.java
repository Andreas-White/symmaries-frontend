package logic.jimpleProcessor;

import logic.general.Constants;
import logic.general.SynthesisConfiguratons;
import logic.general.Utils;
import logic.jimpleProcessor.JimpleProjectHelper.SecuritySignature;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;
import soot.toolkits.exceptions.ThrowAnalysis;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.util.Chain;
import soot.util.cfgcmd.CFGToDotGraph;

import java.io.File;
import java.util.*;

public class SymmariesHelper {
	JimpleProjectHelper project;
	//public SynthesisConfiguratons configurations;
	public List<String> underProcessingMethods = new ArrayList<String>();
	public List<String> processedMethods = new ArrayList<String>();
	ThrowAnalysis throwAnalysis = soot.toolkits.exceptions.UnitThrowAnalysis.v();
	TypeProcessor typeProcessor;
	private Value renameToThisVariable;
	public List<Type> dummyLowSinkMethodsList = new ArrayList<Type>();
	public List<Type> dummyHighSourceMethodsList = new ArrayList<Type>();
	public List<Type> dummyHighSinkMethodsList = new ArrayList<Type>();
	public List<Type> dummyLowSourceMethodsList = new ArrayList<Type>();


	public SymmariesHelper(JimpleProjectHelper jimpleProjectHelper, SynthesisConfiguratons configurations1,
			TypeProcessor tp, SourceSinkHelper sourceSinkHelper) {
		project = jimpleProjectHelper;
		typeProcessor = tp;
		//configurations = configurations1;

	}

	private void addDummyLowSinkMethod(Type type) {
		if (!dummyLowSinkMethodsList.contains(type))
			dummyLowSinkMethodsList.add(type);
	}

	private void addDummyHighSinkMethod(Type type) {
		if (!dummyHighSinkMethodsList.contains(type))
			dummyHighSinkMethodsList.add(type);
	}

	private void addDummyHighSourceMethod(Type type) {
		if (!dummyHighSourceMethodsList.contains(type))
			dummyHighSourceMethodsList.add(type);
	}

	private void addDummyLowSourceMethod(Type type) {
		if (!dummyLowSourceMethodsList.contains(type))
			dummyLowSourceMethodsList.add(type);
	}

	private void addToTheAccessedFieldsList(SootField field) {
		if (!typeProcessor.allAccessedFields.contains(field))
			typeProcessor.allAccessedFields.add(field);
	}

	// A method to find Object variables that can be rewritten:
	// a sequence `java.lang.Object $r1; $r1 = o.method(); C $r2; $r2 = (C) $r1;'
	// can be
	// rewritten `C $r2; $r2 = (C) o.method();'.

	private void buildLibrarySecurityAssumption(SootMethod method, String update) throws TransformationException {
		if (project.getLibrarySecurityAssumptions().containsKey(method) && update.equals(""))
			return;
		{
			String assumptions =  "";
			if(method.isConstructor()) {
				assumptions += "-<~;.*<~@";
				/*for (SecuritySignature ss : project.securitySignatures.get("")) 
					if (ss.matches(method) && (project.getLibrarySecurityAssumptions().get(method)==null || 
							project.getLibrarySecurityAssumptions().get(method)!=null && !project.getLibrarySecurityAssumptions().get(method).contains(ss.secSignature))&&
							!assumptions.contains(ss.secSignature)) {
						assumptions +=  ss.secSignature;
					}*/
			}
			else if(project.securitySignatures.containsKey(method.getName())) {
				for (SecuritySignature ss : project.securitySignatures.get(method.getName())) {
					if (ss.matches(method) && (project.getLibrarySecurityAssumptions().get(method)==null || 
							project.getLibrarySecurityAssumptions().get(method)!=null && !project.getLibrarySecurityAssumptions().get(method).contains(ss.secSignature)) &&
							!assumptions.contains(ss.secSignature)) {
						assumptions +=  ss.secSignature;
					}
				}
			}
			//if (isSink(method)) {
			//	assumptions += update + (update.equals("")? "":";") + "output_L - <~;";
			//	updatedSecuritySummaries= true;
			//}
			if (project.sourceSinkHelper.isHighSource(method)) {
				assumptions +=  "return .*;";
			}
			if(assumptions.equals("") && !project.getLibrarySecurityAssumptions().containsKey(method))
				assumptions +=  update + (update.equals("")? "":";") + generateSecuritySummary(method) ;
			else
				assumptions +=  update + (update.equals("")? "":";") + (project.getLibrarySecurityAssumptions().containsKey(method)? project.getLibrarySecurityAssumptions().get(method): "");
			project.getLibrarySecurityAssumptions().put(method, assumptions);
		}
	}

	private String generateSecuritySummary(SootMethod method) {
		// getter, setter, to, create, is, has, print, send, close, update
		return project.configurations.defaultSecuritySummary;
	}

	public String constructHeader(SootMethod method, boolean library, String callerClass, List<Value> modifiedArgs) {

		String ret = (method.isStatic() ? "static " : "");
		ret += (method.isConstructor() ? "" : method.getReturnType().toString() + " ");
		ret += callerClass;
		ret += (method.isConstructor() ? "" : ":" + project.escapeMethodName(method.getName())) + "(";
		String separator = "";
		if (!library)
			for (Value par : method.retrieveActiveBody().getParameterLocals()) {
				ret += separator + JimpleProjectHelper.escapeTypeName(par.getType()) + " "
						+ (modifiedArgs.contains(par) ? renameArg(par) : par);
				separator = ",";
			}
		else //if (args != null)
			for (int index = 0; index < method.getParameterCount(); index++) {
				ret += separator + method.getParameterType(index) + " ";
				separator = ",";
			}
		ret += ")";
		return ret;
	}

	private String constructMethodCallExpr(Unit invoke) throws TransformationException {
		InvokeExpr invokeExpression = SootUtilities.getInvokeExpr(invoke);
		SootMethod method = invokeExpression.getMethod();
		boolean isLibrary = SootUtilities.isLibraryMethodCall(method, project.configurations.loadFromApk, project.configurations.thirdPartyMethods,
				project.getMethodUniqueName(method, SootUtilities.getInvokeBaseType(invoke)));
		String base = (SootUtilities.isStaticInvoke(invoke) ? SootUtilities.getClassName(method.getDeclaringClass())
				: project.getSymmariesExpression(SootUtilities.getInstanceInvkBase(invoke), this.renameToThisVariable,
						typeProcessor));
		if(SootUtilities.isStaticInvoke(invoke))
			typeProcessor.addsubtypeSupertypeRelation(method.getDeclaringClass().getType(), method.getDeclaringClass().getType());
		String ret = "", separator = "", sinkDefinition = "", checkpointDefinition = "", sourceDefinition="";
		if (SootUtilities.isSpecialInvoke(invoke))
			ret = base + "#" + SootUtilities.getClassName(method.getDeclaringClass()) + ":"
					+ project.escapeMethodName(invokeExpression.getMethod().getName()) + "(";
		else
			ret = base + "." + project.escapeMethodName(invokeExpression.getMethod().getName()) + "(";

		for (int index = 0; index < invokeExpression.getArgs().size(); index++) {
			Value arg = invokeExpression.getArgs().get(index);
			// if(SootUtilities.isFieldRef(arg))
			// allAccessedFields.add(arg);
			String argName = project.getSymmariesExpression(arg, this.renameToThisVariable, typeProcessor).toString().trim();
			Type castedArgType = (SootUtilities.isNullConstant(arg)
					|| SootUtilities.isPrimType(method.getParameterType(index))
					|| method.getParameterType(index).equals(invokeExpression.getArgs().get(index).getType()) ? null
							: method.getParameterType(index));
			typeProcessor.processTypes(method.getParameterType(index), arg);
			if (castedArgType != null)
				typeProcessor.addsubtypeSupertypeRelation(arg.getType(), castedArgType);
			if (argName.matches("class\\s\".*\"")) {
				Constant cst = ClassConstant.v(argName);
				argName = cst.getType().toString();
				// argName = argName.split("\"")[1];
			}
			ret += separator + (castedArgType == null ? "" : "(" + castedArgType + ")") + argName;
			sinkDefinition += this.getSinkParameterStatments(method, index, arg);
			sourceDefinition += this.getSourceParameterStatments(method, index, arg, isLibrary);

			separator = ",";
		}
		if(!SootUtilities.isStaticInvoke(invoke) ) {
			sinkDefinition += getSinkBaseStatments(method, SootUtilities.getInvokeBase(invoke));
			sourceDefinition += getSourceBaseStatments(method, SootUtilities.getInvokeBase(invoke), isLibrary);
		}

		//if (isSource(method))
		//	checkpointDefinition = "checkpoint cp" + checkpointIndex++ ;
		if(isLibrary)
			updateLibraryMethodSummaries(method);

		ret += ")";

		if (SootUtilities.isAssign(invoke)) {
			JAssignStmt as = (JAssignStmt) invoke;
			ret = project.getSymmariesExpression(as.leftBox.getValue(), renameToThisVariable, typeProcessor) + " = " + ret;
		}
		if(isLibrary)
			return  checkpointDefinition + //(checkpointDefinition.equals("")?"":";\n") + 
					sinkDefinition + //(sinkDefinition.equals("")?"":";\n") +
					sourceDefinition + // (sourceDefinition.equals("")?"":";\n") + 
					ret ;
		else
			return 
					sinkDefinition + //(sinkDefinition.equals("")?"":";\n") +
					checkpointDefinition + //(checkpointDefinition.equals("")?"":";\n") + 
					sourceDefinition + //(sourceDefinition.equals("")?"":";\n") +  
					ret;
	}

	private void updateLibraryMethodSummaries(SootMethod method) throws TransformationException {
		if(project.sourceSinkHelper.returnsHighSource(method)) {
			buildLibrarySecurityAssumption(method, "return .*h");
		}
		if( project.sourceSinkHelper.returnsLowSource(method)) {
			buildLibrarySecurityAssumption(method, "return .*l?");
		}
		
		if( project.sourceSinkHelper.isHighSource(method)) {
			buildLibrarySecurityAssumption(method, "@<~@; *<~@; return @");
		}
		if( project.sourceSinkHelper.isLowSink(method)) {
			buildLibrarySecurityAssumption(method, "output_L");
		}		
	
		if( project.sourceSinkHelper.isHighSink(method)) {
			buildLibrarySecurityAssumption(method, "@!; -<~");
		}
		if( project.sourceSinkHelper.isLowSource(method)) {
			buildLibrarySecurityAssumption(method, "-!; -<~");//@<~@; *<~@;return new_?");
		}		
	}

	private HashMap<String, Unit> getRemovableObjectVariablesFromBody(Body body) {
		HashMap<String, Unit> objectVariables = new HashMap<String, Unit>();
		List<String> toRemoveObjects = new ArrayList<String>();
		// get all java.lang.Objects first
		for (Local local : body.getLocals())
			if (local.getType().toString().equals(Constants.OBJECT))
				objectVariables.put(local.toString(), null);

		if (objectVariables.size() == 0)
			return null;

		// remove those java.lang.Object that are used only once in the left hand side
		for (String local : objectVariables.keySet())
			for (Unit unit : body.getUnits()) {
				boolean isLocalAssigned = false, isLocalUsed = false;
				for (ValueBox useBox : unit.getDefBoxes())
					if (useBox.getValue().toString().equals(local))
						isLocalAssigned = true;
				for (ValueBox useBox : unit.getUseBoxes())
					if (useBox.getValue().toString().equals(local))
						isLocalUsed = true;
				if (isLocalAssigned && objectVariables.get(local) == null)
					objectVariables.put(local, unit);
				else if (isLocalAssigned || isLocalUsed)// && SootUtilities.varUsed(unit,local)))
				{ // this means that the variable is assigned more than once; we support simple
					// cases for now
					toRemoveObjects.add(local);
					break;
				}
			}
		// Utils.log(this.getClass()"test");

		for (String local : toRemoveObjects)
			objectVariables.remove(local);

		return objectVariables;
	}



	private void postProcessMethod(SootMethod method, String methodUniqueName, String output, Body body) {
		// Utils.log(this.getClass() ,"Finished processing " +
		// method.getDeclaringClass() + "." + method.getName());
		Utils.writeTextFile(project.configurations.targetPath + "/Meth/" + methodUniqueName + ".meth", output);
		Utils.writeTextFile(project.configurations.targetPath + "/Jimple/" + methodUniqueName + ".jimple", body.toString());
		processedMethods.add(methodUniqueName);
		// toBeProcessedMethods.remove(methodUniqueName);
	}

	private Body preProcessAndGetMethodBody(SootMethod method, String declaringClass, String methodUniqueName)
			throws TransformationException, RuntimeException {
		for (Type type : method.getParameterTypes())
			//	typeProcessor.constructTypeHierarchyFromTypeDef(type, method.getDeclaringClass());
			typeProcessor.addsubtypeSupertypeRelation(type, type);

		if (SootUtilities.isLibraryMethodCall(method, project.configurations.loadFromApk, project.configurations.thirdPartyMethods,
				project.getMethodUniqueName(method, declaringClass))) {
			buildLibrarySecurityAssumption(method, "");
			return null;
		}
		if (this.processedMethods.contains(methodUniqueName) || this.underProcessingMethods.contains(methodUniqueName))
			return null;

		Body body = null;
		try {
			body = method.retrieveActiveBody();
		} catch (Exception ex) {
			Utils.log(this.getClass(), "The body of " + methodUniqueName + " could not be retrived!");
			return null;
		}
		typeProcessor.addsubtypeSupertypeRelation(method.getDeclaringClass().getType(), method.getDeclaringClass().getType());
		//typeProcessor.constructTypeHierarchyFromClassDef(method.getDeclaringClass());
		for (Local value : method.retrieveActiveBody().getLocals())
			typeProcessor.addsubtypeSupertypeRelation(value.getType(), value.getType());
		//.constructTypeHierarchyFromTypeDef(value.getType(), method.getDeclaringClass());
		if(containRedeclaredVariable(method))
			throw new TransformationException("The method " + project.getMethodUniqueName(method, declaringClass) + " redeclare a variable!");

		underProcessingMethods.add(methodUniqueName);
		// Utils.log(this.getClass()"Extracting CFG for " + method.getDeclaringClass() +
		// "." +
		// method.getName() + " has started.");

		ExceptionalUnitGraph cfg = new ExceptionalUnitGraph(body, throwAnalysis);
		CFGToDotGraph todot = new CFGToDotGraph();
		try{
			todot.drawCFG(cfg, body)
			.plot(project.configurations.targetPath + "/CFG/" + File.separator + method.getName() + "cfg.dot");
		} catch(RuntimeException ex){
			Utils.logErr(getClass(), "Could not export the control flow graph of the method " +  method.getDeclaringClass() + "." + method.getName() 
			//+ ex.getMessage()
					);
		}
		return body;
	}

	private boolean containRedeclaredVariable(SootMethod method) {
		for (Local value1 : method.retrieveActiveBody().getLocals())
			for (Local value2 : method.retrieveActiveBody().getLocals())
				if((value1!=value2) && value1.getName()==value2.getName())
					return true;
		return false;
	}

	private String processAllInvokation(Unit unit) throws TransformationException {
		updateAccessedFields(unit);
		if (SootUtilities.isInitInvokation(unit))
			return processInstanceInvokationAssignment(unit); // constructors
		try {
			return processInvokation(unit);// non constructors
		} catch (TransformationException e) {
			throw new TransformationException(e.getLocalizedMessage());
		}
		catch(RuntimeException e) {
			throw new TransformationException("Could not transform the invocation " + unit);
		}
	}

	private String processAssigment(Unit unit, Body body) throws TransformationException {
		JAssignStmt as = (JAssignStmt) unit;
		typeProcessor.processTypes(as.leftBox.getValue().getType(), as.rightBox.getValue());
		String sourceDefinition = "", sinkDefinition = "";
		if (SootUtilities.isFieldLoad(as)) {
			addToTheAccessedFieldsList(SootUtilities.getField(as.rightBox.getValue()));
			sourceDefinition = getSinkFieldStatment(as); 
		} else if (SootUtilities.isFieldStore(as)) {
			addToTheAccessedFieldsList(SootUtilities.getField(as.leftBox.getValue()));
			sinkDefinition = getSourceFieldStatment(as);
		}
		if (as.rightBox.getValue() instanceof InvokeExpr)
			return processInvokation(as);
		else if (SootUtilities.isNewStmt(unit))
			return project.getSymmariesExpression(as.leftBox.getValue(), renameToThisVariable, typeProcessor) + "="
			+ (as.rightBox.getValue().toString().replaceAll("-", ""));
		else if (as.rightBox.getValue() instanceof JInstanceOfExpr) {
			JInstanceOfExpr expr = (JInstanceOfExpr) as.rightBox.getValue();
			typeProcessor.addsubtypeSupertypeRelation(expr.getOp().getType(), expr.getCheckType());
			return project.getSymmariesExpression(as.leftBox.getValue(), renameToThisVariable, typeProcessor) + "="
			+ project.getSymmariesExpression(expr.getOp(), renameToThisVariable, typeProcessor) + " instanceof "
			+ JimpleProjectHelper.escapeTypeName(expr.getCheckType());
		} else if (as.rightBox.getValue() instanceof ClassConstant) {
			ClassConstant expr = (ClassConstant) as.rightBox.getValue();
			typeProcessor.addsubtypeSupertypeRelation(as.leftBox.getValue().getType(), expr.toSootType());
			return project.getSymmariesExpression(as.leftBox.getValue(), renameToThisVariable, typeProcessor) + "="
			+ project.getSymmariesExpression(as.rightBox.getValue(), renameToThisVariable, typeProcessor);
		}
		return sinkDefinition
				+ sourceDefinition
				+ project.getSymmariesExpression(as.leftBox.getValue(), renameToThisVariable, typeProcessor) + "="
				+ project.getSymmariesExpression(as.rightBox.getValue(), renameToThisVariable, typeProcessor) ;
	}


	private String getSourceFieldStatment(JAssignStmt as) throws TransformationException {
		String sourceDefinition = "";
		if (project.configurations.taintCheckingEnabled && project.sourceSinkHelper.isLowSource(SootUtilities.getField(as.leftBox.getValue()))){
			// The right value will be leaked through the left hand side
			sourceDefinition = Constants.dummyClass + "." + Constants.lowSourceMethodName + "(" +
					project.getSymmariesExpression(as.rightBox.getValue(), renameToThisVariable, typeProcessor) + ");\n";
			this.addDummyLowSourceMethod((as.rightBox.getValue()).getType());
		} 
		if ((project.configurations.implicitConfEnabeled || project.configurations.explicitConfEnabeled) && project.sourceSinkHelper.isHighSource(SootUtilities.getField(as.leftBox.getValue()))){
			sourceDefinition = Constants.dummyClass + "." + Constants.highSourceMethodName + "(" +
					project.getSymmariesExpression(as.rightBox.getValue(), renameToThisVariable, typeProcessor) + ");\n";
			this.addDummyHighSourceMethod((as.rightBox.getValue()).getType());
		}
		return sourceDefinition;
	}

	private String getSinkFieldStatment(JAssignStmt as) throws TransformationException {
		String sinkDefinition = "";
		if (project.configurations.taintCheckingEnabled && project.sourceSinkHelper.isHighSink(SootUtilities.getField(as.rightBox.getValue()))) {
			sinkDefinition = project.getSymmariesExpression(as.leftBox.getValue(), renameToThisVariable, typeProcessor) + "=" +
					Constants.dummyClass + "." + Constants.highSinkMethodName + "();\n";
			this.addDummyHighSinkMethod(as.leftBox.getValue().getType());

		} 
		if ((project.configurations.implicitConfEnabeled || project.configurations.explicitConfEnabeled) &&  project.sourceSinkHelper.isLowSink(SootUtilities.getField(as.rightBox.getValue()))) {
			sinkDefinition += project.getSymmariesExpression(as.leftBox.getValue(), renameToThisVariable, typeProcessor) + "=" +
					Constants.dummyClass + "." + Constants.lowSinkMethodName + "();\n";
			this.addDummyLowSinkMethod(as.leftBox.getValue().getType());
		}
		return sinkDefinition;
	}

	private String processInstanceInvokationAssignment(Unit invoke) throws TransformationException {
		SootMethod method = SootUtilities.getInvokeExpr(invoke).getMethod();
		boolean isLibrary = SootUtilities.isLibraryMethodCall(method, project.configurations.loadFromApk, project.configurations.thirdPartyMethods,
				project.getMethodUniqueName(method, SootUtilities.getInvokeBaseType(invoke)));
		if (isLibrary) {
			buildLibrarySecurityAssumption(method, "");//, SootUtilities.getInvokeArgs(invoke));
		} else {
			String type = "";
			if (invoke instanceof JDynamicInvokeExpr)
				type = JimpleProjectHelper
				.escapeTypeName(((JDynamicInvokeExpr) invoke).getMethod().getDeclaringClass().getType());
			else
				type = JimpleProjectHelper.escapeTypeName(SootUtilities.getInvokeBaseType(invoke));
			processMethod(method, type);
		}
		return constructInstanceInvokationExpr(invoke, method, isLibrary);
	}

	private String constructInstanceInvokationExpr(Unit invoke, SootMethod method, boolean isLibrary) throws TransformationException {
		InvokeExpr invokeExpression = ((JInvokeStmt) invoke).getInvokeExpr();
		Value base = SootUtilities.getInstanceInvkBase(invoke);
		typeProcessor.addsubtypeSupertypeRelation(base.getType(), method.getDeclaringClass().getType());
		String ret = project.getSymmariesExpression(base, this.renameToThisVariable, typeProcessor) + "#"
				+ JimpleProjectHelper.escapeTypeName(method.getDeclaringClass().getType()), separator = "";
		ret += "(";
		String sinkDefinition = "", checkpointDefinition = "", sourceDefinition = "";
		for (int index = 0; index < invokeExpression.getArgs().size(); index++) {
			Value arg = invokeExpression.getArgs().get(index);
			typeProcessor.processTypes(method.getParameterType(index), arg);
			String argName = project.getSymmariesExpression(arg, this.renameToThisVariable, typeProcessor).trim();
			if (argName.matches("class\\s\".*\"")) {
				Constant cst = ClassConstant.v(argName);
				argName = cst.getType().toString();
			}
			ret += separator
					+ (SootUtilities.isNullConstant(arg) || SootUtilities.isPrimType(method.getParameterType(index))
							|| method.getParameterType(index) == (arg.getType()) ? ""
									: "(" + method.getParameterType(index) + ")")
					+ argName;
			sinkDefinition += getSinkParameterStatments(method, index, arg);
			sourceDefinition += this.getSourceParameterStatments(method, index, arg, isLibrary);
			separator = ",";
		}
		sinkDefinition += getSinkBaseStatments(method, base);
		sourceDefinition += getSourceBaseStatments(method, base, isLibrary);
		if(isLibrary)
			updateLibraryMethodSummaries(method);
		//if (isSource(method))
		//checkpointDefinition = "\n checkpoint cp" + checkpointIndex++;
		ret += ")";
		if(isLibrary)
			return  checkpointDefinition + (checkpointDefinition.equals("")?"":";\n") + 
					sinkDefinition + (sinkDefinition.equals("")?"":";\n") +
					sourceDefinition + (sourceDefinition.equals("")?"":";\n") + 
					ret ;
		else
			return 
					sinkDefinition + (sinkDefinition.equals("")?"":";\n") +
					checkpointDefinition + (checkpointDefinition.equals("")?"":";\n") + 
					sourceDefinition +(sourceDefinition.equals("")?"":";\n") +  
					ret;
	}

	private String getSinkBaseStatments(SootMethod method, Value base) {
		String out = "";
		if ((project.configurations.implicitConfEnabeled || project.configurations.explicitConfEnabeled) && project.sourceSinkHelper.hasLowSinkBase(method)) {
			out += Constants.dummyClass + "." + Constants.lowSinkMethodName + "(" + base + ");\n";
			addDummyLowSinkMethod(base.getType());
		}
		if (project.configurations.taintCheckingEnabled && project.sourceSinkHelper.hasHighSinkBase(method)) {
			out += Constants.dummyClass + "." + Constants.highSinkMethodName + "(" + base + ");\n";
			addDummyHighSinkMethod(base.getType());
		}
		return out;
	}

	private String getSourceBaseStatments(SootMethod method, Value base, boolean isLibrary) throws TransformationException {
		String out = "";
		if (project.configurations.taintCheckingEnabled && isLibrary && project.sourceSinkHelper.hasLowSourceBase(method)) {
			out += Constants.dummyClass + "." + Constants.lowSourceMethodName + "(" + base + ");\n";
			addDummyLowSourceMethod(base.getType());
		}
		if ((project.configurations.implicitConfEnabeled || project.configurations.explicitConfEnabeled) && isLibrary && project.sourceSinkHelper.hasHighSourceBase(method)) {
			out += Constants.dummyClass + "." + Constants.highSourceMethodName + "(" + base + ");\n";
			addDummyHighSourceMethod(base.getType());
		}

		return out;
	}

	private String getSinkParameterStatments(SootMethod method, int index, Value arg) throws TransformationException {
		String out = "";
		if ((project.configurations.implicitConfEnabeled || project.configurations.explicitConfEnabeled) && project.sourceSinkHelper.hasLowSinkParameter(method, index)) {
			out += Constants.dummyClass + "." + Constants.lowSinkMethodName + "(" + arg + ");\n";
			addDummyLowSinkMethod(arg.getType());
		}
		if (project.configurations.taintCheckingEnabled && project.sourceSinkHelper.hasHighSinkParameter(method, index)) {
			out += Constants.dummyClass + "." + Constants.highSinkMethodName + "(" + arg + ");\n";
			addDummyHighSinkMethod(arg.getType());
		}
		return out;
	}

	private String getSourceParameterStatments(SootMethod method, int index, Value arg, boolean isLibrary) throws TransformationException {
		String out = "";
		if (isLibrary && project.configurations.taintCheckingEnabled && project.sourceSinkHelper.hasLowSourceParameter(method, index)) {
			out += Constants.dummyClass + "." + Constants.lowSourceMethodName + "(" + arg + ");\n";
			addDummyLowSourceMethod(arg.getType());
		}
		if (isLibrary && (project.configurations.implicitConfEnabeled || project.configurations.explicitConfEnabeled)  && project.sourceSinkHelper.hasHighSourceParameter(method, index)) {
			out += Constants.dummyClass + "." + Constants.highSourceMethodName + "(" + arg + ");\n";
			addDummyHighSourceMethod(arg.getType());
		}

		return out;
	}


	private String processInvokation(Unit invoke) throws TransformationException {
		if (SootUtilities.getInvokeExpr(invoke) instanceof JDynamicInvokeExpr) {
			Utils.log(this.getClass(), "Symmaries does not support handling " + invoke);
			return null;
		}

		try {
			SootMethod method = SootUtilities.getInvokeExpr(invoke).getMethod();
			// Utils.log(this.getClass()invoke);
			if (!SootUtilities.isLibraryMethodCall(method, project.configurations.loadFromApk, project.configurations.thirdPartyMethods,
					project.getMethodUniqueName(method, SootUtilities.getInvokeBaseType(invoke)))
					&& !project.configurations.excludedClasses.contains(method.getDeclaringClass().getName())
					&& !method.isSynchronized())
				if (method.getDeclaringClass().isInterface()) {
					// Utils.log(this.getClass()method.getDeclaringClass().getName());
					for (SootClass classs : Scene.v().getActiveHierarchy()
							.getImplementersOf(method.getDeclaringClass())) {
						if (classs.getMethods().contains(method)) {
							SootMethod Implementedmethod = classs.getMethod(method.getName(),
									method.getParameterTypes(), method.getReturnType());
							processMethod(Implementedmethod, classs.getName());
						}
					}
				} else {
					String type;
					if (invoke instanceof JDynamicInvokeExpr)
						type = JimpleProjectHelper.escapeTypeName(
								((JDynamicInvokeExpr) invoke).getMethod().getDeclaringClass().getType());
					else
						type = JimpleProjectHelper.escapeTypeName(SootUtilities.getInvokeBaseType(invoke));

					processMethod(method, type);
				}
			else
				buildLibrarySecurityAssumption(method, "");//, SootUtilities.getInvokeExpr(invoke).getArgs());
			return constructMethodCallExpr(invoke);
		}
		catch (RuntimeException e) {
			throw new TransformationException("Could not tranform " + invoke);
		}
	}

	private String processLocalVariables(Chain<Local> locals, List<Local> parameterLocals,
			Set<String> removableObjectVariables) throws TransformationException {
		String ret = "", separator = ";\n  ";
		for (Value var : locals) {
			if (removableObjectVariables == null || !removableObjectVariables.contains(var.toString())) {
				if (!parameterLocals.contains(var) && !var.toString().equals(Constants.This)
						&& !var.equals(renameToThisVariable))
					ret += JimpleProjectHelper.escapeTypeName(var.getType()) + " " + var + separator;
				Type varType = var.getType();
				while (varType instanceof ArrayType)
					varType = ((ArrayType) varType).baseType;
				if (!SootUtilities.isPrimType(varType))
					typeProcessor.addNewType(varType);
				if (SootUtilities.isSubtypeOf(Scene.v().getSootClass(varType.toString()),
						Scene.v().getSootClass("java.lang.Throwable")))
					typeProcessor.addsubtypeSupertypeRelation(varType, Constants.throwableType);
			}
		}
		return ret;
	}

	public void processMethod(SootMethod method, String declaringClass) {
		try {
			String methodUniqueName = project.getMethodUniqueName(method, declaringClass);//
			declaringClass = declaringClass.replaceAll("-", "");
			Body body = preProcessAndGetMethodBody(method, declaringClass, methodUniqueName);
			if (body == null) {
				// Utils.log(this.getClass()"The method " + methodUniqueName + "has been already
				// analyzed or is a third-party method");
				return;
			}
			Set<String> removableObjectVariables = tranformCodeByRemovingExtraObjectVariables(body);
			setRenameToThisVariable(body);
			Hashtable<Unit, String> statementLabels = new Hashtable<Unit, String>();
			String output = "{\n";
			output += processLocalVariables(body.getLocals(), body.getParameterLocals(), removableObjectVariables);
			List<Value> modifiedArgs = new ArrayList<Value>();
			String methodBody = processMethodBody(body, statementLabels, method, modifiedArgs);
			if (methodBody == null)
				return;
			output += methodBody;
			output += processTraps(body, statementLabels);
			output += "} \n";
			String header = constructHeader(method, false,declaringClass, modifiedArgs) + "\n";
			output = header + output;
			postProcessMethod(method, methodUniqueName, output, body);
			underProcessingMethods.remove(methodUniqueName);
		} 
		catch (TransformationException e) {
			Utils.logErr(this.getClass(), "Could not transform the method " + method.getDeclaringClass() + "."
					+ method.getName() 
					//+ "\n The method body is " + method.getActiveBody() 
					//+ e.getMessage() 
					);
			return;
		}
		return;
	}

	private String processMethodBody(Body body, Hashtable<Unit, String> statementLabels, SootMethod method,
			List<Value> modifiedArgs) throws TransformationException {
		String output = "", separator = ";\n ";
		int labelID = 1;

		for (Unit unit : body.getUnits()) {
			/*
			 * ThrowableSet exceptionsCausedByUnit = throwAnalysis.mightThrow(unit);
			 * Utils.log(this.getClass()"\nALL EXCEPTIONS THAT CAN BE RAISED BY " + unit +
			 * " ARE: \n            " + exceptionsCausedByUnit.toAbbreviatedString()); for
			 * (final Trap t : body.getTraps())
			 * if(exceptionsCausedByUnit.catchableAs(t.getException().getType()))
			 * Utils.log(this.getClass()"NON- GENERIC OR USER-RAISED ONES ARE:\n           "
			 * + t.getException().getType() ); for(Tag tag: unit.getTags())
			 * Utils.log(this.getClass()"TAGE: " + tag.getName() + " " + tag.getValue());
			 */
			String label = "", statement = null;
			if (statementLabels.containsKey(unit))
				label = statementLabels.get(unit) + ":";
			if (!unit.getBoxesPointingToThis().isEmpty() && !statementLabels.containsKey(unit)) {
				label = "label" + labelID + ":";
				statementLabels.put(unit, "label" + labelID++);
			}
			if (SootUtilities.isIdentityStmt(unit)) {
				if ((((JIdentityStmt) unit).rightBox.getValue() instanceof ThisRef)) {
					String lhs = project.getSymmariesExpression(((JIdentityStmt) unit).leftBox.getValue(),
							renameToThisVariable, typeProcessor);
					if (!lhs.equals("this"))
						statement = lhs + "= this";
					else
						statement = "";
				} else if ((((JIdentityStmt) unit).rightBox.getValue() instanceof ParameterRef)) {
					int parameterIndex = ((ParameterRef)((JIdentityStmt) unit).rightBox.getValue()).getIndex();
					if(project.sourceSinkHelper.hasHighSourceParameter(method, parameterIndex)) {
						statement = (((JIdentityStmt) unit).leftBox.getValue()) + "=" + Constants.dummyClass + "." + Constants.highSourceMethodName + "()";
						this.addDummyHighSourceMethod(method.getParameterType(parameterIndex));
					}
					else if(project.sourceSinkHelper.hasLowSourceParameter(method, parameterIndex)) {
						statement = (((JIdentityStmt) unit).leftBox.getValue()) + "=" + Constants.dummyClass + "." + Constants.lowSourceMethodName + "()";
						this.addDummyLowSourceMethod(method.getParameterType(parameterIndex));
					}
					else
						statement = "";
				}
				else if ((((JIdentityStmt) unit).rightBox.getValue() instanceof JCaughtExceptionRef))
					statement = ((JIdentityStmt) unit).leftBox.getValue() + " = catch";
				else
					throw new TransformationException("An error in translating " + unit.toString());
			} else if (SootUtilities.isAssign(unit)) {
				statement = processAssigment(unit, body);
				if (statement == null) {
					Utils.logErr(this.getClass(), "\n The statement " + unit
							+ " cannot be tranformed.\nIgnored and skipped the method " + method.getSignature());
					return null;
				}

				if (!statement.equals(""))
					for (Local args : body.getParameterLocals())
						if (!SootUtilities.isPrimType(args.getType())
								&& ((JAssignStmt) unit).leftBox.getValue().toString().equals(args.getName().toString())
								&& !modifiedArgs.contains(args)) {
							modifiedArgs.add(args);
							break;
						}
			} else if (SootUtilities.isInvoke(unit)) {
				String methodDescr = processAllInvokation(unit);
				if (methodDescr == null)
					return null;
				SootMethod method1 = SootUtilities.getInvokeExpr(unit).getMethod();
				if (!underProcessingMethods.contains(project.getMethodUniqueName(method1, SootUtilities.getInvokeBaseType(unit))))
					statement = methodDescr;
				else
					if(SootUtilities.isInstanceInvoke(unit)) {
						boolean isLibrary = SootUtilities.isLibraryMethodCall(method, project.configurations.loadFromApk, project.configurations.thirdPartyMethods,project.getMethodUniqueName(method, SootUtilities.getInvokeBaseType(unit)));
						statement = constructInstanceInvokationExpr(unit, method1, isLibrary);
					} else
						statement = this.constructMethodCallExpr(unit);

			} else if (SootUtilities.isGotoStatment(unit)) {
				statementLabels.putIfAbsent(((JGotoStmt) unit).getTargetBox().getUnit(), "label" + labelID++);
				statement = "goto " + statementLabels.get(((JGotoStmt) unit).getTargetBox().getUnit());
			} else if (SootUtilities.isConditional(unit)) {
				statementLabels.putIfAbsent(((IfStmt) unit).getTarget(), "label" + labelID++);
				statement = "if " + project.getSymmariesExpression(((IfStmt) unit).getConditionBox().getValue(),
						renameToThisVariable, typeProcessor) + " goto "
						+ statementLabels.get(((IfStmt) unit).getTarget());
				if (((IfStmt) unit).getConditionBox().getValue().getUseBoxes().size() > 1)
					typeProcessor.processExpressionTypes(((IfStmt) unit).getConditionBox().getValue());
			} else if (SootUtilities.isReturn(unit)) {
				statement = processReturn(unit, method);
			} else if (SootUtilities.isNOP(unit)) {
				statement = ":;";
			} else if (SootUtilities.isVoidReturn(unit) || SootUtilities.isReturn(unit) || unit instanceof JThrowStmt) {
				statement = unit.toString();
			} else {
				Utils.logErr(this.getClass(), "\n The statement " + unit
						+ " cannot be tranformed.\nIgnored and excluded the method " + method.getSignature());
				return null;
			}
			if (statement == null)
				return null;
			if (!(label + statement).equals(""))
				output += label + statement + separator;
			// sourceLocation = processInvokation((JInvokeStmt) unit, sourceLocation);
		}
		// To rename the argument and define a new local variable with the arg's
		// original name
		String renamedArgsDef = "";
		for (Value arg : modifiedArgs)
			renamedArgsDef += arg.getType() + " " + arg + ";\n";
		for (Value arg : modifiedArgs)
			output = arg + "=" + renameArg(arg) + ";\n" + output;
		return renamedArgsDef + output;
		// return output;
	}

	private String processReturn(Unit unit, SootMethod method) throws TransformationException {
		this.typeProcessor.processTypes(method.getReturnType(), ((JReturnStmt) unit).getOp());
		String returnValue = "";
		String sink = ""; 
		String source = "";
		if (project.sourceSinkHelper.isLowSink(method)) {
			sink = Constants.dummyClass + "." + Constants.lowSinkMethodName + "(" + returnValue + ");\n";
			addDummyLowSinkMethod(method.getReturnType());
		}
		if (project.sourceSinkHelper.isHighSink(method)) {
			sink += Constants.dummyClass + "." + Constants.highSinkMethodName + "(" + returnValue + ");\n";
			addDummyHighSinkMethod(method.getReturnType());
		}
		if (project.sourceSinkHelper.returnsHighSource(method)) {
			source = returnValue + "=" + Constants.dummyClass + "." + Constants.highSourceMethodName + "();\n";
			this.addDummyHighSourceMethod(method.getReturnType());
		}
		if (project.sourceSinkHelper.returnsLowSource(method)) {
			source += returnValue + "=" + Constants.dummyClass + "." + Constants.lowSourceMethodName + "();\n";
			addDummyLowSourceMethod(method.getReturnType());
		}
		if (((JReturnStmt) unit).getOp() instanceof ClassConstant) {
			ClassConstant expr = (ClassConstant) ((JReturnStmt) unit).getOp();
			typeProcessor.addsubtypeSupertypeRelation(method.getReturnType(), expr.toSootType());
			returnValue = JimpleProjectHelper.escapeTypeName(expr.toSootType());
		} else
			returnValue = ((JReturnStmt) unit).getOp().toString();
		return sink + source +
				//(sink ? Constants.dummyClass + "." + Constants.lowSinkMethodName + "(" + returnValue + ");\n" : "") +
				//(source ? returnValue + "=" + Constants.dummyClass + "." + Constants.highSourceMethodName + "();\n" : "") + 
				"return  " + returnValue;
	}


	private String processTraps(Body body, Hashtable<Unit, String> statementLabels) throws TransformationException {
		String output = "";
		//if (body.getTraps().size() > 0)
		//	project.configurations.exceptionsEnabled = true;

		for (Trap trap : body.getTraps()) {
			output += "catch " + trap.getException() + ": " + statementLabels.get(trap.getBeginUnit()) + " - "
					+ statementLabels.get(trap.getEndUnit()) + " : " + statementLabels.get(trap.getHandlerUnit())
					+ ";\n";
			this.typeProcessor.addsubtypeSupertypeRelation(trap.getException().getType(), Constants.throwableType);
		}
		return output;
	}

	private String renameArg(Value arg) {
		return arg.toString().replace('$', '@') + "SCGS";
	}

	private void setRenameToThisVariable(Body body) {
		renameToThisVariable = null;
		for (Unit unit : body.getUnits())
			if (SootUtilities.isIdentityStmt(unit) && (((JIdentityStmt) unit).rightBox.getValue() instanceof ThisRef))
				renameToThisVariable = ((JIdentityStmt) unit).leftBox.getValue();
		for (Unit unit : body.getUnits())
			for (ValueBox valuBox : unit.getDefBoxes())
				if (valuBox.getValue().equals(renameToThisVariable)) {
					renameToThisVariable = null;
					return;
				}
	}

	private boolean tranformCode(Body body, HashMap<String, Unit> objectVariables) {
		Hashtable<Unit, Unit> toAddUnits = new Hashtable<Unit, Unit>();
		List<Unit> toRemoveUnits = new ArrayList<Unit>();

		boolean transformed = false;
		for (Unit unit : body.getUnits())
			if (SootUtilities.isAssign(unit)) {
				JAssignStmt as = (JAssignStmt) unit;
				if (SootUtilities.isCastExpr(as.rightBox.getValue())
						&& objectVariables.containsKey(((JCastExpr) (as.rightBox.getValue())).getOp().toString())) {
					Unit oldUnit = objectVariables.get(((JCastExpr) (as.rightBox.getValue())).getOp().toString());
					Value rhsValue;
					if (SootUtilities.isAssign(oldUnit))
						rhsValue = ((JAssignStmt) oldUnit).rightBox.getValue();
					else
						rhsValue = ((JIdentityStmt) oldUnit).rightBox.getValue();
					Unit toAdd1 = Jimple.v().newAssignStmt(as.leftBox.getValue(), rhsValue);
					toAddUnits.put(unit, toAdd1);
					toRemoveUnits.add(oldUnit);
				}
			}

		for (Unit unit : toAddUnits.keySet()) {
			body.getUnits().insertBefore(toAddUnits.get(unit), unit);
			body.getUnits().remove(unit);
			transformed = true;
		}

		for (Unit unit : toRemoveUnits) {
			body.getUnits().remove(unit);
			transformed = true;
		}

		return transformed;
	}

	private Set<String> tranformCodeByRemovingExtraObjectVariables(Body body) {
		HashMap<String, Unit> removableObjectVariables = getRemovableObjectVariablesFromBody(body);
		boolean transformed = false;
		// Utils.log(this.getClass()"test");
		if (removableObjectVariables == null)
			return null;

		if (removableObjectVariables.size() > 0) {
			transformed = tranformCode(body, removableObjectVariables);
		}

		if (transformed)
			return removableObjectVariables.keySet();
		return null;
	}

	private void updateAccessedFields(Unit unit) {
		InvokeExpr invokeExpression = SootUtilities.getInvokeExpr(unit);
		for (int index = 0; index < invokeExpression.getArgs().size(); index++) {
			Value arg = invokeExpression.getArgs().get(index);
			if (SootUtilities.isFieldRef(arg))
				addToTheAccessedFieldsList(SootUtilities.getField(arg));
		}
	}

}
