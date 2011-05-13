package org.intrace.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;

/**
 * InTrace uses ASM to instrument class files. However, ASM traverses bytecode
 * linearly which means some information is not available when we need it. This
 * class implements a first pass analysis phase to collect information which we
 * can't collect during the transformation phase.
 * <p>
 * This analysis collects two sets of data.
 * <ul>
 * <li>Reverse GOTO Lines
 * <li>Method Entry Line
 * </ul>
 * <h1>Reverse GOTO Lines</h1> This analysis records the target line number of
 * GOTOs that jump backwards in the code.
 * 
 * <h2>Example:</h2>
 * 
 * <pre>
 *  1. public void method()
 *  2. {
 *  3.   do
 *  4.   {
 *  5.     // Branch C
 *  6.     // Branch C
 *  7.   }
 *  8.   while (condition)
 *  9. }
 * </pre>
 * 
 * We want to capture line 5 and line 9. Example 1 gets transformed into the
 * following bytecode:
 * 
 * <pre>
 *  5. label1:
 *  5. // Branch C
 *  6. // Branch C
 *  8. if (condition) goto label1
 * </pre>
 * 
 * <h3>Line 5:</h3>
 * <ul>
 * <li>{@link ClassAnalysis#visitLineNumber(int, Label)} is called and we record
 * a mapping from label1 to line 5.
 * </ul>
 * 
 * <h3>Line 8:</h3>
 * <ul>
 * <li>{@link ClassAnalysis#visitJumpInsn(int, Label)} is called with a GOTO
 * instruction so we check whether we have already seen the target label. In
 * this case, we have so we mark the target line number as a reverse GOTO.
 * </ul>
 * 
 * <h1>Method Entry Line</h1> This analysis records the first source line of a
 * method. This is necessary so that the transformation phase can add an entry
 * trace line in the call to {@link InstrumentedMethodWriter#visitCode()} and
 * know the source line.
 */
public class ClassAnalysis extends EmptyVisitor
{
  // Output of this analysis
  public final Map<String, Set<Integer>> methodReverseGOTOLines = new HashMap<String, Set<Integer>>(1);
  public final Map<String, Integer> methodEntryLines = new HashMap<String, Integer>(1);
  public final Map<String, List<String>> methodArgNames = new HashMap<String, List<String>>(1);

  // Intermediate fields
  private Set<Integer> currentMethod_reverseGOTOLines = new HashSet<Integer>(1);
  private final Map<Label, Integer> currentMethod_labelLineNos = new HashMap<Label, Integer>(1);
  private String currentMethod_sig;
  private boolean currentMethod_recordedEntryLine = false;
  private int currentMethod_numArgs = 0;
  private boolean currentMethod_skipArg; 

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc,
                                   String signature, String[] exceptions)
  {    
    currentMethod_sig = name + desc;
    currentMethod_recordedEntryLine = false;
    methodArgNames.put(currentMethod_sig, new ArrayList<String>(1));
    Type[] argTypes = Type.getArgumentTypes(desc);
    currentMethod_numArgs = argTypes.length;
    currentMethod_skipArg = ((access & Opcodes.ACC_STATIC) == 0);
    if (currentMethod_skipArg)
    {
      currentMethod_numArgs++;
    }
    
    currentMethod_reverseGOTOLines = new HashSet<Integer>(1);
    currentMethod_labelLineNos.clear();
    return this;
  }

  @Override
  public void visitLocalVariable(String name, String desc, String signature,
                                 Label start, Label end, int index)
  {
    if (currentMethod_skipArg)
    {
      currentMethod_skipArg = false;
      currentMethod_numArgs--;
    }
    else
    {    
      if (currentMethod_numArgs > 0)
      {
        methodArgNames.get(currentMethod_sig).add(name);
        currentMethod_numArgs--;
      }
    }
  }
  
  @Override
  public void visitLineNumber(int xiLineNo, Label xiLabel)
  {
    if (!currentMethod_recordedEntryLine)
    {
      methodEntryLines.put(currentMethod_sig, xiLineNo);
      currentMethod_recordedEntryLine = true;
    }

    currentMethod_labelLineNos.put(xiLabel, xiLineNo);
  }

  @Override
  public void visitJumpInsn(int xiOpCode, Label xiBranchLabel)
  {
    Integer lineNo = currentMethod_labelLineNos.get(xiBranchLabel);
    if (lineNo != null)
    {
      currentMethod_reverseGOTOLines.add(lineNo);
    }
  }

  @Override
  public void visitEnd()
  {        
    List<String> methodLocalVars = methodArgNames.get(currentMethod_sig);
    if ((methodLocalVars != null) && (methodLocalVars.size() == 0))
    {
      methodArgNames.remove(currentMethod_sig);
    }
    
    methodReverseGOTOLines.put(currentMethod_sig,
                               currentMethod_reverseGOTOLines);
    currentMethod_sig = null;
    currentMethod_labelLineNos.clear();
  }
}
