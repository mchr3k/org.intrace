package org.intrace.agent;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

import java.util.Set;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * ASM2 MethodVisitor used to instrument methods.
 */
public class InstrumentedMethodWriter extends MethodAdapter
{
  private static final String HELPER_CLASS = "org/intrace/output/AgentHelper";
  private final String className;
  private final String methodName;
  private final String methodDescriptor;
  private int lineNumber = -1;

  private final Set<Integer> branchTraceLines;
  private final Integer entryLine;

  private boolean writeTraceLine = false;

  /**
   * cTor
   * 
   * @param xiMethodVisitor
   * @param xiClassName
   * @param xiMethodName
   * @param xiDesc
   * @param xiBranchTraceLines
   * @param entryLine
   */
  public InstrumentedMethodWriter(MethodVisitor xiMethodVisitor,
      String xiClassName, String xiMethodName, String xiDesc,
      Set<Integer> xiBranchTraceLines, Integer xiEntryLine)
  {
    super(xiMethodVisitor);
    className = xiClassName;
    methodName = xiMethodName;
    methodDescriptor = xiDesc;
    branchTraceLines = xiBranchTraceLines;
    entryLine = xiEntryLine;
  }

  @Override
  public void visitCode()
  {
    generateCallToWriteTrace(TraceType.BEGIN, ((entryLine != null ? entryLine
                                                                 : -1)));
    traceMethodArgs();
    super.visitCode();
  }

  private void traceMethodArgs()
  {
    Type[] argTypes = Type.getArgumentTypes(methodDescriptor);
    for (int ii = 0; ii < argTypes.length; ii++)
    {
      String typeDescriptor = argTypes[ii].getDescriptor();

      if (argTypes[ii].getSort() == Type.OBJECT)
      {
        typeDescriptor = "Ljava/lang/Object;";
      }
      else if ((argTypes[ii].getSort() == Type.ARRAY)
               && (argTypes[ii].getDescriptor().startsWith("[L")))
      {
        typeDescriptor = "[Ljava/lang/Object;";
      }

      mv.visitLdcInsn(className);
      mv.visitLdcInsn(methodName);
      mv.visitVarInsn(Opcodes.ALOAD, ii);
      mv.visitMethodInsn(INVOKESTATIC, HELPER_CLASS, "arg",
                         "(Ljava/lang/String;Ljava/lang/String;"
                             + typeDescriptor + ")V");
    }
  }

  @Override
  public void visitLineNumber(int xiLineNumber, Label label)
  {
    lineNumber = xiLineNumber;
    if (writeTraceLine || branchTraceLines.contains(xiLineNumber))
    {
      generateCallToWriteTrace(TraceType.BRANCH, lineNumber);
      writeTraceLine = false;
    }
    super.visitLineNumber(xiLineNumber, label);
  }

  @Override
  public void visitInsn(int xiOpCode)
  {
    if (xiOpCode == RETURN)
    {
      generateCallToWriteTrace(TraceType.END, lineNumber);
    }
    super.visitInsn(xiOpCode);
  }

  @Override
  public void visitJumpInsn(int xiOpCode, Label xiBranchLabel)
  {
    writeTraceLine = true;
    super.visitJumpInsn(xiOpCode, xiBranchLabel);
  }

  private void generateCallToWriteTrace(TraceType traceType, int lineNumber)
  {
    mv.visitLdcInsn(className);
    mv.visitLdcInsn(methodName);
    mv.visitIntInsn(Opcodes.BIPUSH, lineNumber);
    switch (traceType)
    {
    case BEGIN:
    {
      mv.visitMethodInsn(INVOKESTATIC, HELPER_CLASS, "enter",
                         "(Ljava/lang/String;Ljava/lang/String;I)V");
    }
      break;

    case BRANCH:
    {
      mv.visitMethodInsn(INVOKESTATIC, HELPER_CLASS, "branch",
                         "(Ljava/lang/String;Ljava/lang/String;I)V");
    }
      break;

    case END:
    {
      mv.visitMethodInsn(INVOKESTATIC, HELPER_CLASS, "exit",
                         "(Ljava/lang/String;Ljava/lang/String;I)V");
    }
      break;
    }
  }

  private enum TraceType
  {
    BEGIN, BRANCH, END
  }
}
