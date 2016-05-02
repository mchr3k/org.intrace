package org.intrace.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.intrace.output.trace.TraceHandler;

/**
 * Contains criteria that the 'user' specifies to request certain classes to
 * be instrumented.
 * @author erikostermueller
 *
 */
public class InstrCriteria {

	public VerboseLogger verboseLogger = null;
	/**
	 * Each item in this Map is a classname that has methods.
	 */
	final Map<String,List<SimpleMethod> > myInstrCriteria = new Hashtable<String,List<SimpleMethod>>();
	static final String CLASS_METHOD_DELIMITER = "#";
	private static final String CRITERIA_DELIM = "|";
	private static final String REGEX_CRITERIA_DELIM = "\\"+CRITERIA_DELIM;
	private static final String INSTRUMENT_ALL_METHODS = "INSTR_ALL_METHODS";
	private String[] classNamesOnly = null;
	private List<String> classNamesOnlyList = new ArrayList<String>();
	private String originalCriteria = null;
	public InstrCriteria(String criteria) {
		this.originalCriteria = criteria;
		criteria = criteria.replace('{', '[');
		String[] tmp = criteria.split(this.REGEX_CRITERIA_DELIM);
		
		for (String s : tmp)
			addClassOrMethod(s);
		
	}
	/*
	 * Example:  org.intracetest.agent.ArgumentTypes#charArrayArg([C)V
	 * 
	 */
	static class SimpleMethod {
		boolean ynAllMethods = false;
		/*
		 * example: charArrayArg
		 */
		private String name;
		/*
		 * example: ([C)V
		 * 
		 */
		private String args;
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getArgs() {
			String rc = "";
			if (args!=null)
				rc = this.args.replace('[', '{');
			return rc;
		}

		public void setArgs(String args) {
			this.args = args;
		}

		public String toString() {
			return name+getArgs();
		}

		public void setNameAndArgs(String methodNameAndArgs) {
			int firstLeftParen = methodNameAndArgs.indexOf("(");
			if (firstLeftParen >= 0) {
				name = methodNameAndArgs.substring(0, firstLeftParen);
				args = methodNameAndArgs.substring(firstLeftParen);
				//System.out.println("in setNameAndArgs just added [" + methodNameAndArgs  + "] as [" + this.toString() + "]");
				
			} else 
				name = methodNameAndArgs;
		}
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for(String className : this.getClassRegex()) {
			
			List<SimpleMethod> methods = this.myInstrCriteria.get(className);
			for(SimpleMethod myMethod : methods) {
				if (++count > 1) sb.append(CRITERIA_DELIM); //This line add this | to make crit1|crit2|crit3
				if (myMethod.ynAllMethods)
					sb.append(className);
				else {
					sb.append(className);
					sb.append(CLASS_METHOD_DELIMITER);
					sb.append( myMethod.toString() );
				}
			}
		}
		return sb.toString();
	}
	public String[] getClassRegex() {
		if (classNamesOnly==null) {
			Set<String> keys = this.myInstrCriteria.keySet();
			classNamesOnly = keys.toArray( new String[0] );
		}
		return classNamesOnly;
		
	}
	/**
	 * Example:  org.intracetest.agent.ArgumentTypes#charArrayArg([C)V
	 * 
	 * @param myclass:  example: org.intracetest.agent.ArgumentTypes
	 * @param method: example: charArrayArg
	 * @param arguments: example: ([C)V
	 * @return
	 */
	public boolean thisMethodSpecified(String myClass, String method, String arguments) {
		List<SimpleMethod> allMethods = null;
		arguments = arguments.replace('[', '{');
		boolean rc = false;
		if (this.allMethodsSpecified(myClass)) {
			rc = true;
		}else {
			allMethods = this.myInstrCriteria.get(myClass);
			if (findMethod(allMethods, method,arguments)!=null)
				rc = true;
		}
		logVerbose("instrument method? [" + rc + 
				"] class[" + myClass + 
				"] method[" + method + 
				"] + method args [" + arguments + 
				"] count of methods instrumented [" + ( (allMethods!=null) ? allMethods.size() : "zero" )+ 
				"]");
		return rc;
			
	}
	public int methodCountPerClass(String myClass) {
		int count = 0;
		List<SimpleMethod> allMethods = this.myInstrCriteria.get(myClass);
		if (allMethods!=null) {
			count = allMethods.size();
		}
		return count;

	}
	private void logVerbose(String s) {
		if (this.verboseLogger!=null) {
			this.verboseLogger.logVerbose(s);
		}
	}
	public boolean allMethodsSpecified(String myClass) {
		boolean rc = false;
		List<SimpleMethod> allMethods = this.myInstrCriteria.get(myClass);
		if (allMethods !=null)
			for(SimpleMethod method : allMethods) {
				if (method.ynAllMethods)
					rc = true;
		}
		//logVerbose("instrument all methods for this class? [" + rc + "] class[" + myClass + "]");
		return rc;
	}
	/**
	 * 
	 * The bug lies here:
	 * Comparing fullMethodNameCriteria [byteArrayArg({B)V] to [byteArrayArg([B)V]
	 * 
	 * @param methods
	 * @param nameCriteria
	 * @param argsCriteria
	 * @return
	 */
	private SimpleMethod findMethod(List<SimpleMethod> methods, String nameCriteria, String argsCriteria){
		SimpleMethod rc = null;
		if (nameCriteria !=null && argsCriteria !=null) {
			String fullMethodNameCriteria = nameCriteria + argsCriteria;
			if (methods != null)
				for(SimpleMethod sm : methods) {
					//logVerbose("Comparing fullMethodNameCriteria [" + fullMethodNameCriteria + "] to [" + sm.toString() + "]");
					if (fullMethodNameCriteria.equals(sm.toString())) {
						rc = sm;
						break;
					}
				}
		}
		return rc;
	}
	/**
	 *  trying to split this into two: MyClass#myMethod(D)V, but MyClass is also allowable here
	 * @param methodOrClass
	 */
	private void addClassOrMethod(String methodOrClass) {
		//System.out.println("###addClassOrMethod [" + methodOrClass + "]");
		SimpleMethod method = new SimpleMethod();
		
		List<SimpleMethod> allMethods = null;
		
		String[] parts = methodOrClass.split(CLASS_METHOD_DELIMITER);
		if (parts.length >= 1 && parts[0]!=null) {
			allMethods = this.myInstrCriteria.get(parts[0]);
			if (allMethods == null) {
				allMethods = new ArrayList<SimpleMethod>();
				//this.logVerbose("in addClassOrMethod just created allMethods hash[" + allMethods.hashCode() + "]myInstrCriteriaHash[" + myInstrCriteria.hashCode() + "]");
				this.myInstrCriteria.put(parts[0], allMethods);
			}
			
			switch (parts.length) {
				case 1: //just the package and class name were specified, meaning that all methods should be instrumented.
					method.ynAllMethods = true;
					break;
				case 2: //a method was specified after a # sign...only instrument this specific method.
					method.setNameAndArgs(parts[1]);
					break;
				default:
					throw new RuntimeException("Was expecting to find either zero or one of the [" + this.CLASS_METHOD_DELIMITER + "] character inside of [" + methodOrClass + "]");
			}
		} else {
			throw new RuntimeException("Error adding this method/class [" + methodOrClass + "]");
		}
		allMethods.add(method);
//		System.out.println("## Just added methodName[" + method.name + "] methodArg [" + method.args + "]methodArgGetter [" + method.getArgs() + "]");
//		System.out.println("@@@@@@@Class count [" + myInstrCriteria.size() + "] for class[" + parts[0] + "] count is [" + allMethods.size() + "]");
	}
}
