package org.intrace.agent;

import static org.objectweb.asm.Opcodes.GOTO;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

/**
 * First pass analysis phase
 */
public class ClassBranchLineAnalysis extends EmptyVisitor
{
  public final Map<String, Set<Integer>> methodBranchTraceLines = new HashMap<String, Set<Integer>>();
  public final Map<String, Integer> methodEntryLine = new HashMap<String, Integer>();
  private Set<Integer> branchTraceLines = new HashSet<Integer>();
  private final Map<Label,Integer> methodLabelLineNos = new HashMap<Label,Integer>();
  private String methodSig;
  private boolean traceThisLine = false;
  private boolean recordedMethodEntryLine = false;

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
  {
    methodLabelLineNos.clear();
    methodSig = name + desc;
    recordedMethodEntryLine = false;
    return this;
  }

  @Override
  public void visitLineNumber(int xiLineNo, Label xiLabel)
  {
    if (!recordedMethodEntryLine)
    {
      methodEntryLine.put(methodSig, xiLineNo);
      recordedMethodEntryLine = true;
    }
    methodLabelLineNos.put(xiLabel, xiLineNo);
    if (traceThisLine)
    {
      branchTraceLines.add(xiLineNo);
      traceThisLine = false;
    }
  }

  @Override
  public void visitJumpInsn(int xiOpCode, Label xiBranchLabel)
  {
    if (xiOpCode != GOTO)
    {
      traceThisLine= true;
    }
    else
    {
      Integer lineNo = methodLabelLineNos.get(xiBranchLabel);
      if (lineNo != null)
      {
        branchTraceLines.add(lineNo);
      }
    }
  }

  @Override
  public void visitEnd()
  {
    if (methodSig != null)
    {
      methodBranchTraceLines.put(methodSig, branchTraceLines);
      methodSig = null;
      branchTraceLines = new HashSet<Integer>();
    }
  }
}
