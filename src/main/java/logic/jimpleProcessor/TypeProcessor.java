package logic.jimpleProcessor;

import logic.general.Utils;
import soot.*;
import soot.jimple.StaticFieldRef;
import soot.jimple.internal.*;

import java.util.*;

public class TypeProcessor {

	public Hashtable<String, Set<String>> subtypeSupertypesMap = new Hashtable<String, Set<String>>();
	private List<Set<Type>> castableTypeGroups = new ArrayList<Set<Type>>();
	private ClassHierarchy typeHierarchy = new ClassHierarchy(null);
	public List<SootField> allAccessedFields = new ArrayList<SootField>();

	private String addMissingTypesOfAccessedFields(List<SootField> accessedFieldTypes, List<String> leafTypes) {
		String typesDescriptions = "";
		ArrayList<String> missingTypes = new ArrayList<String>();
		for (SootField field : accessedFieldTypes)
			allAccessedFields.remove(field);
		for (SootField field : allAccessedFields)
			if (!missingTypes.contains(SootUtilities.getClassName(field.getDeclaringClass())))
				missingTypes.add(SootUtilities.getClassName(field.getDeclaringClass()));
		for (String type : missingTypes)
			typesDescriptions += getSingleTypeDescription(type, accessedFieldTypes, leafTypes);
		return typesDescriptions;
	}

	public void addNewType(final Type type2) {
		for (Set<Type> group : castableTypeGroups)
			if (group.contains(type2))
				return;
		this.castableTypeGroups.add(new HashSet<Type>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				add(type2);
			}
		});
		// Utils.log(this.getClass(),"New type" + type2);
	}

	public void addsubtypeSupertypeRelation(Type type1, Type type2) throws TransformationException {
		//System.out.println(type1 + " : " + type2);
		if (type2 == null || type2.toString().equals("null_type"))
			return;
		while (type1 instanceof ArrayType)
			type1 = ((ArrayType) type1).baseType;
		while (type2 instanceof ArrayType)
			type2 = ((ArrayType) type2).baseType;

		if (SootUtilities.isPrimType(type1) && SootUtilities.isPrimType(type2))
			return;
		if (!SootUtilities.isPrimType(type1) && !SootUtilities.isPrimType(type2)) {
			boolean compatbileTypes = false;
			if (!type1.equals(type2)) {
				if (this.isSubTypeSuperType(Scene.v().getSootClass(type1.toString()),
						Scene.v().getSootClass(type2.toString()))) {
					if (subtypeSupertypesMap.get(type1.toString()) == null)
						this.subtypeSupertypesMap.put(type1.toString(), new HashSet<String>());
					this.subtypeSupertypesMap.get(type1.toString()).add(type2.toString());
					compatbileTypes = true;
				}
				if (this.isSubTypeSuperType(Scene.v().getSootClass(type2.toString()),
						Scene.v().getSootClass(type1.toString()))) {
					if(compatbileTypes)
						Utils.logErr(this.getClass(), type1 + " and " + type2 + " cannot be a superclass and subclass of each other at the same time!");
					else {
						if (subtypeSupertypesMap.get(type2.toString()) == null)
							this.subtypeSupertypesMap.put(type2.toString(), new HashSet<String>());
						this.subtypeSupertypesMap.get(type2.toString()).add(type1.toString());
						compatbileTypes = true;
					}
				}
				if (!compatbileTypes) {
					if(type1.toString().equals("java.lang.Object")) {
						if (subtypeSupertypesMap.get(type2.toString()) == null)
							this.subtypeSupertypesMap.put(type2.toString(), new HashSet<String>());
						this.subtypeSupertypesMap.get(type2.toString()).add(type1.toString());
					} else
						if(type2.toString().equals("java.lang.Object")) {
							if (subtypeSupertypesMap.get(type1.toString()) == null)
								this.subtypeSupertypesMap.put(type1.toString(), new HashSet<String>());
							this.subtypeSupertypesMap.get(type1.toString()).add(type2.toString());
						} else
							throw new TransformationException(type1 + " and " + type2 + " should be compatible while "
									+ "it's not recongized by Soot! Skiped processing the current method" );
				}
			} else if (!subtypeSupertypesMap.keySet().contains(type1.toString()))
				subtypeSupertypesMap.put(type1.toString(), new HashSet<String>());
		}
		else {
			throw new TransformationException(type1 + " and " + type2 + " cannot be compatible! Skiped processing the method." );
		}
	}

	String constructTypeDescriptions() {

		ArrayList<String> leafTypes = new ArrayList<String>();
		// typeHierarchy.buildTypesHierarchy(castableTypeGroups);

		// mapTypesToSuperTypes();
		List<SootField> accessedFieldTypes = new ArrayList<SootField>();
		String typesDescriptions = "";
		for (String type : subtypeSupertypesMap.keySet()) {
			typesDescriptions += getSingleTypeDescription(type, accessedFieldTypes, leafTypes);
		}
		if (!subtypeSupertypesMap.keySet().contains("android.os.IBinder")
				&& subtypeSupertypesMap.keySet().contains("android.content.Intent"))
			typesDescriptions += "android.os.IBinder {}\n";

		typesDescriptions += addMissingTypesOfAccessedFields(accessedFieldTypes, leafTypes);
		for (String type : leafTypes)
			typesDescriptions += type + "{}\n";

		return typesDescriptions;
	}

	void constructTypeHierarchyFromClassDef(SootClass declaringClass) {
		if (!subtypeSupertypesMap.containsKey(SootUtilities.getClassName(declaringClass))) {
			Set<String> out = new HashSet<String>();
			subtypeSupertypesMap.put(SootUtilities.getClassName(declaringClass), out);
			for (SootField field : declaringClass.getFields()) {
				constructTypeHierarchyFromTypeDef(field.getType(), declaringClass);
			}
			if (!declaringClass.isInterface()
					&& Scene.v().getActiveHierarchy().getSuperclassesOf(declaringClass).isEmpty()
					|| declaringClass.isInterface()
					&& Scene.v().getActiveHierarchy().getSuperinterfacesOf(declaringClass).isEmpty()
					&& Scene.v().getActiveHierarchy().getImplementersOf(declaringClass).isEmpty())
				;
			else {
				Set<SootClass> superClassesOrInterfaces = getSuperInterfaces(declaringClass);
				for (SootClass superClasss : superClassesOrInterfaces) {
					constructTypeHierarchyFromClassDef(superClasss);
					out.add(SootUtilities.getClassName(superClasss));
				}
			}
		}
	}

	void constructTypeHierarchyFromTypeDef(Type type, SootClass declaringClass) {
		while (type instanceof ArrayType)
			type = ((ArrayType) type).baseType;
		if (Scene.v().getSootClass(type.toString()) != declaringClass && !(type instanceof PrimType)) {
			constructTypeHierarchyFromClassDef(Scene.v().getSootClass(type.toString()));
			if (!SootUtilities.isPrimType(declaringClass.getType()))
				constructTypeHierarchyFromClassDef(declaringClass);
		}
	}

	public String escapeTypeName(String type) {
		// TODO Auto-generated method stub
		return type.replaceAll("-", "");
	}

	String getSingleTypeDescription(String type, List<SootField> missingFieldTypes, List<String> leafTypes) {

		SootClass declaringClass = Scene.v().getSootClass(type);
		String fields = " {";
		for (SootField field : allAccessedFields)
			if (SootUtilities.getClassName(field.getDeclaringClass())
					.equals(SootUtilities.getClassName(declaringClass))) {
				fields += escapeTypeName(field.getType().toString()) + "  " + field.getName() + "; ";
				missingFieldTypes.add(field);
			}
		fields += "}";
		String superClasses = "", separator = "";
		if (subtypeSupertypesMap.get(type) != null)
			for (String str : subtypeSupertypesMap.get(type)) {
				superClasses += separator + this.escapeTypeName(str);
				separator = ",";
				if (!subtypeSupertypesMap.keySet().contains(str) && !leafTypes.contains(str))
					leafTypes.add(str);
			}
		if (type.trim().equals("android.content.Intent")) {
			superClasses += separator + "android.os.IBinder";

		}
		return SootUtilities.getClassName(declaringClass) + (superClasses.equals("") ? "" : " <: " + superClasses)
				+ fields + "\n";
	}

	Set<SootClass> getSuperInterfaces(SootClass declaringClass) {
		if (!declaringClass.isInterface())
			return new HashSet<SootClass>(Scene.v().getActiveHierarchy().getSuperclassesOf(declaringClass));
		Set<SootClass> res = new HashSet<SootClass>();
		res.addAll(Scene.v().getActiveHierarchy().getSuperinterfacesOf(declaringClass));
		return res;
	}

	boolean isSubTypeSuperType(SootClass subClass, SootClass superClasss) throws TransformationException {
		try {
			if (subClass.isInterface())
				if (superClasss.isInterface())
					return Scene.v().getActiveHierarchy().isInterfaceSubinterfaceOf(subClass, superClasss);
				else {
					if (Scene.v().getActiveHierarchy().getImplementersOf(subClass) != null
							&& Scene.v().getActiveHierarchy().getImplementersOf(subClass).contains(superClasss))
						return false;
					for (SootClass implementingClass : Scene.v().getActiveHierarchy().getImplementersOf(subClass)) {
						if (isSubTypeSuperType(implementingClass, superClasss))
							return true;
					}
					return false;
				}
			else if (superClasss.isInterface())
				return Scene.v().getActiveHierarchy().getImplementersOf(superClasss).contains(subClass);
			else if (superClasss.getName().equals("java.lang.Object"))
				return true;
			else
				return Scene.v().getActiveHierarchy().isClassSubclassOf(subClass, superClasss);
		}
		catch(RuntimeException ex) {
			throw new TransformationException("Runtime error with checking the inheritance relations between " + subClass.getName() + " and " + superClasss.getName());
		}
	}
	public void joinTypeGroupsOld(Type type1, Type type2) {
		if (type2 == null || type2.toString().equals("null_type"))
			return;
		while (type1 instanceof ArrayType)
			type1 = ((ArrayType) type1).baseType;
		while (type2 instanceof ArrayType)
			type2 = ((ArrayType) type2).baseType;

		if (SootUtilities.isPrimType(type1) || SootUtilities.isPrimType(type2))
			return;

		if (type1.equals(type2) && SootUtilities.isLibraryClass(Scene.v().getSootClass(type2.toString()))) {
			addNewType(type2);
			return;
		}
		Set<Type> type1Group = null;
		Set<Type> type2Group = null;
		for (Set<Type> group : castableTypeGroups) {
			if (group.contains(type1))
				type1Group = group;
			if (group.contains(type2))
				type2Group = group;
			if (type1Group != null && type2Group != null)
				break;
		}
		if (type1Group != null)
			if (type2Group != null) {
				if (type1Group == type2Group)
					return;
				type2Group.addAll(type1Group);
				this.castableTypeGroups.remove(type1Group);
			} else
				type2Group = type1Group;
		if (type2Group == null) {
			type2Group = new HashSet<Type>();
			this.castableTypeGroups.add(type2Group);

		}
		type2Group.add(type1);
		type2Group.add(type2);
	}

	void mapTypesToSuperTypes() {
		Stack<ClassHierarchy> stack = new Stack<ClassHierarchy>();
		for (ClassHierarchy node : typeHierarchy.getLeaves()) {
			stack.add(node);
			Utils.log(this.getClass(), "Leaf: " + node.data);
		}
		List<String> visited = new ArrayList<String>();
		Utils.log(this.getClass(), "Building subtype/supertype relations");
		if (stack.peek().data != null)
			while (!stack.isEmpty()) {
				ClassHierarchy node = stack.pop();
				// Utils.log(this.getClass(),"Processing Type " + node.data);
				String curType = SootUtilities.getClassName(node.data);
				visited.add(curType);
				if (this.subtypeSupertypesMap.get(curType) == null)
					this.subtypeSupertypesMap.put(curType, new HashSet<String>());
				for (ClassHierarchy parent : node.parents) {
					Utils.log(this.getClass(), "parent.data " + parent.data);
					if (parent.data != null) {
						this.subtypeSupertypesMap.get(curType).add(SootUtilities.getClassName(parent.data));
						if (!visited.contains(SootUtilities.getClassName(parent.data)))
							stack.add(parent);
					}
				}
			}
	}

	public void processExpressionTypes(Value value) throws TransformationException {
		// Value value = expression.getValue();
		if (SootUtilities.isLocalVariable(value) || SootUtilities.isParameterRef(value)
				|| SootUtilities.isConstant(value) || SootUtilities.isStaticField(value)
				|| SootUtilities.isFieldRef(value))
			return;

		if (value instanceof JCmpExpr || value instanceof JAddExpr || value instanceof JNeExpr
				|| value instanceof JEqExpr || (value instanceof JMulExpr) || value instanceof JAndExpr
				|| value instanceof JOrExpr || value instanceof JDivExpr
				|| value instanceof JSubExpr || value instanceof JCmplExpr
				|| value instanceof JCmpgExpr || value instanceof JXorExpr || value instanceof JGtExpr
				|| value instanceof JUshrExpr || value instanceof JLtExpr || value instanceof JShlExpr
				|| value instanceof JNeExpr || value instanceof JShrExpr || value instanceof JGeExpr
				|| value instanceof JLeExpr) {

			if (((AbstractBinopExpr) value).getOp1()
					.getType() != ((AbstractBinopExpr) value).getOp2().getType()) {
				this.processTypes(((AbstractBinopExpr) value).getOp1().getType(),
						((AbstractBinopExpr) value).getOp2());
				// this.processTypes(((soot.jimple.internal.AbstractBinopExpr)value).getOp2().getType(),
				// ((soot.jimple.internal.AbstractBinopExpr)value).getOp1());
			}
			if (((AbstractBinopExpr) value).getOp1().getUseBoxes().size() > 1)
				processExpressionTypes(((AbstractBinopExpr) value).getOp1());
			if (((AbstractBinopExpr) value).getOp2().getUseBoxes().size() > 1)
				processExpressionTypes(((AbstractBinopExpr) value).getOp2());
			return;
		}
		if (value instanceof JNegExpr || value instanceof JRemExpr || value instanceof JLengthExpr
				|| value instanceof JInstanceOfExpr) {
			if (((AbstractUnopExpr) value).getOp().getUseBoxes().size() > 1)
				processExpressionTypes(((AbstractUnopExpr) value).getOp());
			return;
		}
		if (SootUtilities.isCastExpr(value)) {
			processExpressionTypes(((JCastExpr) value).getOp());
			return;
		}

	}

	public void processTypes(Type lhsType, Value rhsValue) throws TransformationException {
		while (lhsType instanceof ArrayType)
			lhsType = ((ArrayType) lhsType).baseType;

		if (SootUtilities.isCastExpr(rhsValue)) {
			addsubtypeSupertypeRelation(((JCastExpr) rhsValue).getOp().getType(), ((JCastExpr) rhsValue).getCastType());
			addsubtypeSupertypeRelation(((JCastExpr) rhsValue).getOp().getType(), rhsValue.getType());
		}
		if (SootUtilities.isStaticField(rhsValue)) {
			SootClass rhsClass = ((StaticFieldRef) rhsValue).getField().getDeclaringClass();
			addNewType(rhsClass.getType());
			addsubtypeSupertypeRelation(rhsClass.getType(), null);
		}
		addsubtypeSupertypeRelation(lhsType, rhsValue.getType());
	}

	public void writeTypeHierarchyDotFile(String targetPath) {

		String typesGraph = "";
		String nodes = ""; int index = 0;
		for (String type : this.subtypeSupertypesMap.keySet()) {
			if(subtypeSupertypesMap.get(type).size()==0)
				nodes  += "node" + index++ + "[shape=diamond, label = " + "\"" + type + "\"];\n";
			else
				for (String parentType : subtypeSupertypesMap.get(type))
					typesGraph += "\"" + parentType + "\" -> \"" + type + "\" ;\n		";
		}
		typesGraph ="digraph \"typesDiagram\" {\n" + nodes + typesGraph + "}";
		Utils.writeTextFile(targetPath + "/types.dot", typesGraph);

	}

	public void writeTypesClassesFile(String targetPath) {
		String types = constructTypeDescriptions();
		Utils.writeTextFile(targetPath + "/types.classes", types);
	}

}
