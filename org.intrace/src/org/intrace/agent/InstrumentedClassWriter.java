package org.intrace.agent;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

import java.util.HashSet;
import java.util.Set;

import org.intrace.output.AgentHelper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * ASM2 ClassWriter used to transform class files to instrument methods to add
 * calls into {@link AgentHelper}.
 */
public class InstrumentedClassWriter extends ClassWriter
{
  private final String mClassName;
  private final ClassAnalysis analysis;

  /**
   * cTor
   * 
   * @param xiClassName
   * @param xiReader
   * @param analysis
   */
  public InstrumentedClassWriter(String xiClassName, ClassReader xiReader,
      ClassAnalysis xiAnalysis)
  {
    super(xiReader, true);
    mClassName = xiClassName;
    analysis = xiAnalysis;
  }

  /**
   * Instrument a particular method.
   */
  @Override
  public MethodVisitor visitMethod(int access, String name, String desc,
                                   String signature, String[] exceptions)
  {
    MethodVisitor mv = super.visitMethod(access, name, desc, signature,
                                         exceptions);

    // Extract analysis results for this method
    Set<Integer> branchTraceLines = analysis.methodReverseGOTOLines.get(name
                                                                        + desc);
    Integer entryLine = analysis.methodEntryLines.get(name + desc);
    if (branchTraceLines == null)
    {
      branchTraceLines = new HashSet<Integer>();
    }

    // Transform the method
    return new InstrumentedMethodWriter(mv, name, desc, branchTraceLines,
                                        entryLine);
  }

  /**
   * ASM2 MethodVisitor used to instrument methods.
   */
  private class InstrumentedMethodWriter extends MethodAdapter
  {
    private static final String HELPER_CLASS = "org/intrace/output/AgentHelper";

    // Final method fields
    private final String methodName;
    private final String methodDescriptor;

    // Analysis data
    private final Set<Integer> reverseGOTOLines;
    private final Integer entryLine;

    // State
    private boolean writeTraceLine = false;
    private int lineNumber = -1;

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
        String xiMethodName, String xiDesc, Set<Integer> xiBranchTraceLines,
        Integer xiEntryLine)
    {
      super(xiMethodVisitor);
      methodName = xiMethodName;
      methodDescriptor = xiDesc;
      reverseGOTOLines = xiBranchTraceLines;
      entryLine = xiEntryLine;
    }

    /**
     * Initial entry point - generate ENTRY call.
     */
    @Override
    public void visitCode()
    {
      generateCallToAgentHelper(InstrumentationType.ENTER,
                                ((entryLine != null ? entryLine : -1)));
      traceMethodArgs();
      super.visitCode();
    }

    /**
     * Pass the args of this method out to the {@link AgentHelper}
     */
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

        mv.visitLdcInsn(mClassName);
        mv.visitLdcInsn(methodName);
        mv.visitVarInsn(Opcodes.ALOAD, ii);
        mv.visitMethodInsn(INVOKESTATIC, HELPER_CLASS, "arg",
                           "(Ljava/lang/String;Ljava/lang/String;"
                               + typeDescriptor + ")V");
      }
    }

    /**
     * Save the current line number and generate a call to {@link AgentHelper}
     * if either writeTraceLine is set to true or the current line was marked by
     * the Reverse GOTO Analysis {@link ClassAnalysis}.
     */
    @Override
    public void visitLineNumber(int xiLineNumber, Label label)
    {
      lineNumber = xiLineNumber;
      if (writeTraceLine || reverseGOTOLines.contains(xiLineNumber))
      {
        generateCallToAgentHelper(InstrumentationType.BRANCH, lineNumber);
        writeTraceLine = false;
      }
      super.visitLineNumber(xiLineNumber, label);
    }

    /**
     * When we see a return instruction we must immediately generate an EXIT
     * call to the Agent Helper.
     */
    @Override
    public void visitInsn(int xiOpCode)
    {
      if (xiOpCode == RETURN)
      {
        generateCallToAgentHelper(InstrumentationType.EXIT, lineNumber);
      }
      super.visitInsn(xiOpCode);
    }

    /**
     * A jump instruction indicates that we are about to enter an optional block
     * of code. We set "writeTraceLine = true;" to ensure that we generate a
     * trace call once we know the line number.
     */
    @Override
    public void visitJumpInsn(int xiOpCode, Label xiBranchLabel)
    {
      writeTraceLine = true;
      super.visitJumpInsn(xiOpCode, xiBranchLabel);
    }

    /**
     * Generate an ENTER/BRANCH/EXIT instrumentation call.
     * 
     * @param traceType
     * @param lineNumber
     */
    private void generateCallToAgentHelper(InstrumentationType traceType,
                                           int lineNumber)
    {
      mv.visitLdcInsn(mClassName);
      mv.visitLdcInsn(methodName);
      mv.visitIntInsn(Opcodes.BIPUSH, lineNumber);
      switch (traceType)
      {
      case ENTER:
      {
        mv.visitMethodInsn(INVOKESTATIC, HELPER_CLASS, "enter",
                           "(Ljava/lang/String;Ljava/lang/String;I)V");
        break;
      }

      case BRANCH:
      {
        mv.visitMethodInsn(INVOKESTATIC, HELPER_CLASS, "branch",
                           "(Ljava/lang/String;Ljava/lang/String;I)V");
        break;
      }

      case EXIT:
      {
        mv.visitMethodInsn(INVOKESTATIC, HELPER_CLASS, "exit",
                           "(Ljava/lang/String;Ljava/lang/String;I)V");
        break;
      }
      }
    }
  }

  /**
   * Three way enum to signal the difference between ENTER/BRANCH/EXIT trace.
   */
  private enum InstrumentationType
  {
    ENTER, BRANCH, EXIT
  }
}
