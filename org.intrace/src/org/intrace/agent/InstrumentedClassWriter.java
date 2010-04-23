package org.intrace.agent;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
  private final String className;
  private final ClassAnalysis analysis;
  private int methodNum = 1;

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
    super(xiReader, COMPUTE_MAXS);
    className = xiClassName;
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

    // Transform the method
    return new InstrumentedMethodWriter(mv, access, name, desc,
                                        branchTraceLines, entryLine);
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
    private final int methodAccess;
    private final Map<Label, Integer> labelLineNos = new HashMap<Label, Integer>();
    private final Set<Label> traceLabels = new HashSet<Label>();

    // Analysis data
    private final Set<Integer> reverseGOTOLines;
    private final Integer entryLine;

    // State
    private boolean writeTraceLine = false;
    private CTorEntryState ctorEntryState = CTorEntryState.NORMALMETHOD;
    private int lineNumber = -1;
    private int numBranchesOnLine = 0;
    private TernaryState ternState = TernaryState.BASE;

    /**
     * cTor
     * 
     * @param xiMethodVisitor
     * @param access
     * @param xiClassName
     * @param xiMethodName
     * @param xiDesc
     * @param xiBranchTraceLines
     * @param entryLine
     */
    public InstrumentedMethodWriter(MethodVisitor xiMethodVisitor, int access,
        String xiMethodName, String xiDesc, Set<Integer> xiBranchTraceLines,
        Integer xiEntryLine)
    {
      super(xiMethodVisitor);
      methodAccess = access;
      methodName = xiMethodName;
      methodDescriptor = xiDesc;
      reverseGOTOLines = xiBranchTraceLines;
      entryLine = xiEntryLine;
      if (methodName.equals("<init>"))
      {
        ctorEntryState = CTorEntryState.ISCTOR;
      }
    }

    /**
     * Initial entry point - generate ENTRY call.
     */
    @Override
    public void visitCode()
    {
      methodNum++;
      if (ctorEntryState != CTorEntryState.ISCTOR)
      {
        addEntryCalls();
      }
      // For Constructors we add the entry calls after the first invokeSpecial
      // which calls into the superclass constructor.
      super.visitCode();
    }

    private void addEntryCalls()
    {
      generateCallToAgentHelper(InstrumentationType.ENTER,
                                ((entryLine != null ? entryLine : -1)));
      traceMethodArgs();
    }

    /**
     * Pass the args of this method out to the {@link AgentHelper}
     */
    private void traceMethodArgs()
    {
      Type[] argTypes = Type.getArgumentTypes(methodDescriptor);
      boolean isStaticAccess = ((methodAccess & Opcodes.ACC_STATIC) > 0);
      int offset = (isStaticAccess ? 0 : 1);

      for (int ii = 0; ii < argTypes.length; ii++)
      {
        String typeDescriptor = argTypes[ii].getDescriptor();

        int varslot = ii + offset;
        int opcode = Opcodes.ILOAD;
        if (argTypes[ii].getSort() == Type.OBJECT)
        {
          typeDescriptor = "Ljava/lang/Object;";
          opcode = Opcodes.ALOAD;
        }
        else if ((argTypes[ii].getSort() == Type.ARRAY)
                 && (argTypes[ii].getDescriptor().startsWith("[[")))
        {
          // All multidimensional arrays are handled by the object array
          // function
          typeDescriptor = "[Ljava/lang/Object;";
        }
        else if ((argTypes[ii].getSort() == Type.ARRAY)
                 && (argTypes[ii].getDescriptor().startsWith("[L")))
        {
          // All object arrays are cast to the object supertype
          typeDescriptor = "[Ljava/lang/Object;";
        }
        else if (argTypes[ii].getSort() == Type.LONG)
        {
          opcode = Opcodes.LLOAD;
          offset++;
        }
        else if (argTypes[ii].getSort() == Type.FLOAT)
        {
          opcode = Opcodes.FLOAD;
        }
        else if (argTypes[ii].getSort() == Type.DOUBLE)
        {
          opcode = Opcodes.DLOAD;
          offset++;
        }

        if (argTypes[ii].getSort() == Type.ARRAY)
        {
          opcode = Opcodes.ALOAD;
        }

        mv.visitLdcInsn(className);
        mv.visitLdcInsn(methodName);
        mv.visitVarInsn(opcode, varslot);
        mv.visitMethodInsn(INVOKESTATIC, HELPER_CLASS, "arg",
                           "(Ljava/lang/String;Ljava/lang/String;"
                               + typeDescriptor + ")V");
      }
    }

    /**
     * Guard against a label which hasn't been resolved. This happens sometimes,
     * I assume due to a bug in ASM.
     */
    @Override
    public void visitLocalVariable(String name, String desc, String signature,
                                   Label start, Label end, int index)
    {
      try
      {
        end.getOffset();
      }
      catch (IllegalStateException ex)
      {
        mv.visitLabel(end);
      }
      super.visitLocalVariable(name, desc, signature, start, end, index);
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
      labelLineNos.put(label, xiLineNumber);

      if (writeTraceLine || reverseGOTOLines.contains(xiLineNumber)
          || traceLabels.contains(label))
      {
        // This check excludes ternary statements where we can't add trace
        if (numBranchesOnLine < 2)
        {
          if (ctorEntryState == CTorEntryState.SEEN_SPECIAL)
          {
            addEntryCalls();
            ctorEntryState = CTorEntryState.ENTRY_WRITTEN;
          }
          else
          {
            generateCallToAgentHelper(InstrumentationType.BRANCH, lineNumber);
          }
          writeTraceLine = false;
        }
        else
        {
          writeTraceLine = false;
        }
      }

      numBranchesOnLine = 0;
      super.visitLineNumber(xiLineNumber, label);
    }

    /**
     * When we see a return instruction we must immediately generate an EXIT
     * call to the Agent Helper.
     */
    @Override
    public void visitInsn(int xiOpCode)
    {
      if ((ternState == TernaryState.BASE) && (xiOpCode == Opcodes.DUP))
      {
        ternState = TernaryState.SEEN_DUP;
      }
      else if ((ternState == TernaryState.SEEN_BRANCH)
               && (xiOpCode == Opcodes.POP))
      {
        ternState = TernaryState.BASE;
        writeTraceLine = false;
      }
      else
      {
        ternState = TernaryState.BASE;
      }

      if (xiOpCode == Opcodes.RETURN)
      {
        // Ensure that cTor entry call gets written even if the cTor is
        // implicit and therefore has only a single line number.
        if (ctorEntryState == CTorEntryState.SEEN_SPECIAL) 
        {
          addEntryCalls();
          ctorEntryState = CTorEntryState.ENTRY_WRITTEN;
        }
        generateCallToAgentHelper(InstrumentationType.EXIT, lineNumber);
      }
      super.visitInsn(xiOpCode);
    }

    /**
     * Handle cTor case where we write entry trace later.
     */
    @Override
    public void visitMethodInsn(int xiOpCode, String owner, String name,
                                String desc)
    {
      if ((ctorEntryState == CTorEntryState.ISCTOR)
          && (xiOpCode == Opcodes.INVOKESPECIAL))
      {
        ctorEntryState = CTorEntryState.SEEN_SPECIAL;
        writeTraceLine = true;
      }

      super.visitMethodInsn(xiOpCode, owner, name, desc);
    }

    /**
     * A jump instruction indicates that we are about to enter an optional block
     * of code. We set "writeTraceLine = true;" to ensure that we generate a
     * trace call once we know the line number.
     */
    @Override
    public void visitJumpInsn(int xiOpCode, Label xiBranchLabel)
    {
      if (ternState == TernaryState.SEEN_DUP)
      {
        ternState = TernaryState.SEEN_BRANCH;
      }

      numBranchesOnLine++;
      Integer lineNo = labelLineNos.get(xiBranchLabel);
      if (lineNo == null)
      {
        // This is a forward jump
        writeTraceLine = true;
      }
      super.visitJumpInsn(xiOpCode, xiBranchLabel);
    }

    /**
     * Try catch handler
     */
    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler,
                                   String type)
    {
      traceLabels.add(handler);
      super.visitTryCatchBlock(start, end, handler, type);
    }

    /**
     * Switch block
     */
    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt,
                                     Label[] labels)
    {
      traceLabels.add(dflt);
      for (Label label : labels)
      {
        traceLabels.add(label);
      }
      super.visitTableSwitchInsn(min, max, dflt, labels);
    }
    
    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) 
    {
      traceLabels.add(dflt);
      for (Label label : labels)
      {
        traceLabels.add(label);
      }
      super.visitLookupSwitchInsn(dflt, keys, labels);
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
      mv.visitLdcInsn(className);
      mv.visitLdcInsn(methodName);
      mv.visitLdcInsn(lineNumber);
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

  private enum TernaryState
  {
    BASE, SEEN_DUP, SEEN_BRANCH
    /* SEEN_POP */
  }

  private enum CTorEntryState
  {
    NORMALMETHOD, ISCTOR, SEEN_SPECIAL, ENTRY_WRITTEN
  }
}
