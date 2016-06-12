package org.intrace.agent;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.intrace.output.AgentHelper;
import org.intrace.output.trace.TraceHandler;
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
  private final boolean threadClass;
  private final boolean shouldInstrument;
  private AgentSettings settings = null;

  /**
   * cTor
   *
   * @param xiClassName
   * @param xiReader
   * @param xiShouldInstrument
   * @param analysis
   */
  public InstrumentedClassWriter(String xiClassName, ClassReader xiReader,
      ClassAnalysis xiAnalysis, boolean xiShouldInstrument, AgentSettings settings)
  {
    super(xiReader, COMPUTE_MAXS);
    className = xiClassName;
    analysis = xiAnalysis;
    shouldInstrument = xiShouldInstrument;
    threadClass = xiClassName.equals("java.lang.Thread");
    this.settings = settings;
  }

  /**
   * Instrument a particular method.
   */
  @Override
  public MethodVisitor visitMethod(int access, String name, String desc,
                                   String signature, String[] exceptions)
  {

	    if (settings.isVerboseMode())
	    {
	    	
	    	/**		The following code provides invaluable debug for conjuring right syntax for method parameters.
	    	 * 		http://hsqldb.org/doc/src/org/hsqldb/jdbc/JDBCConnection.html#isWrapperFor(java.lang.Class)
					[14:44:45.065]:[19]:access:1
					[14:44:45.065]:[19]:name:isWrapperFor
					[14:44:45.065]:[19]:desc:(Ljava/lang/Class;)Z   <<<<<==== complicate syntax for method specification
					[14:44:45.065]:[19]:signature:(Ljava/lang/Class<*>;)Z
					
					http://hsqldb.org/doc/src/org/hsqldb/jdbc/JDBCConnection.html#prepareStatement(java.lang.String, java.lang.String[])
					[14:44:45.068]:[19]:access:33
					[14:44:45.068]:[19]:name:prepareStatement
					[14:44:45.069]:[19]:desc:(Ljava/lang/String;[Ljava/lang/String;)Ljava/sql/PreparedStatement;   <<<<<==== complicated syntax for method specification
					[14:44:45.069]:[19]:signature:null
					
	    	 */
	  	  StringBuilder sb = new StringBuilder();
	  	  sb.append(this.className);
	  	  sb.append(InstrCriteria.CLASS_METHOD_DELIMITER);
	  	  sb.append(name);
	  	  sb.append(desc);
		//System.out.println("@#%:" + sb.toString() );
	      TraceHandler.INSTANCE.writeTraceOutput("DEBUG: method signature: " + sb.toString());
	    }	  
    MethodVisitor mv = super.visitMethod(access, name, desc, signature,
                                         exceptions);

    // If this class/method is not excluded, but then consider including it.
    // If all methods are allowed (by not specifying any methods) or this specific method is defined, then instrument it.

    if (this.settings.getClassesToExclude()==null
    		||!this.settings.getClassesToExclude().thisMethodSpecified(this.className, name, desc)) {
    	if (this.settings.getClassesToInclude().thisMethodSpecified(this.className, name, desc)  ) {
            // Extract analysis results for this method
            Set<Integer> branchTraceLines = analysis.methodReverseGOTOLines.get(name
                                                                                + desc);
            Integer entryLine = analysis.methodEntryLines.get(name + desc);

            if (!threadClass || !name.equals("getUncaughtExceptionHandler"))
            {
            	//System.out.println("Instrumenting class [" + this.className + "] method [" + name + "] args [" + desc + "]");
              mv = new InstrumentedMethodWriter(mv, access, name, desc,
                                                     branchTraceLines, entryLine);
            }
    	}
    }
    // Transform the method
    return mv;
  }

  /**
   * ASM2 MethodVisitor used to instrument methods.
   */
  private class InstrumentedMethodWriter extends MethodAdapter
  {
    private static final String HELPER_CLASS = "org/intrace/output/AgentHelper";
    private static final String CRITICAL_BLOCK = "INSTRU_CRITICAL_BLOCK";

    // Final method fields
    private final String methodName;
    private final String methodDescriptor;
    private final int methodAccess;

    // Analysis data
    private final Set<Integer> reverseGOTOLines;
    private final Integer entryLine;
    private final Map<Label, Integer> labelLineNos = new HashMap<Label, Integer>();
    private final Set<Label> traceLabels = new HashSet<Label>();
    private final Set<Label> exceptionHandlerLabels = new HashSet<Label>();

    // State
    private boolean writeTraceLine = false;
    private CTorEntryState ctorEntryState = CTorEntryState.NORMALMETHOD;
    private int lineNumber = -1;
    private int numBranchesOnLine = 0;
    private TernaryState ternState = TernaryState.BASE;
    private Label threadLabel;

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
     * Add the special preable for the Thread.setUncaughtExceptionHandler(...)
     */
    private void addThreadSetUCEHPreamble()
    {
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitVarInsn(Opcodes.ALOAD, 1);
      mv.visitFieldInsn(Opcodes.GETSTATIC, HELPER_CLASS, CRITICAL_BLOCK,
                        "L" + HELPER_CLASS + "$CriticalBlock;");
      Label l1 = new Label();
      Label l2 = new Label();
      Label l3 = new Label();
      mv.visitJumpInsn(Opcodes.IF_ACMPEQ, l3);
      mv.visitLabel(l2);
      mv.visitVarInsn(Opcodes.ALOAD, 0);
      mv.visitFieldInsn(Opcodes.GETFIELD, "java/lang/Thread", "uncaughtExceptionHandler",
                        "Ljava/lang/Thread$UncaughtExceptionHandler;");
      mv.visitFieldInsn(Opcodes.GETSTATIC, HELPER_CLASS, CRITICAL_BLOCK,
                        "L" + HELPER_CLASS + "$CriticalBlock;");
      mv.visitJumpInsn(Opcodes.IF_ACMPNE, l1);
      mv.visitLabel(l3);
      mv.visitVarInsn(Opcodes.ALOAD, 0);
      mv.visitVarInsn(Opcodes.ALOAD, 1);
      mv.visitFieldInsn(Opcodes.PUTFIELD, "java/lang/Thread", "uncaughtExceptionHandler",
                        "Ljava/lang/Thread$UncaughtExceptionHandler;");
      threadLabel = new Label();
      mv.visitJumpInsn(Opcodes.GOTO, threadLabel);
      mv.visitLabel(l1);
      mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
    }

    /**
     * Initial entry point - generate ENTRY call.
     */
    @Override
    public void visitCode()
    {
      if (threadClass && methodName.equals("setUncaughtExceptionHandler"))
      {
        addThreadSetUCEHPreamble();
      }

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
      if (!shouldInstrument) return;

      generateCallToAgentHelper(InstrumentationType.ENTER,
                                ((entryLine != null ? entryLine
                                                   : -1)));
      traceMethodArgs();
    }

    /**
     * Pass the args of this method out to the {@link AgentHelper}
     */
    private void traceMethodArgs()
    {
      Type[] argTypes = Type.getArgumentTypes(methodDescriptor);
      boolean isStaticAccess = ((methodAccess & Opcodes.ACC_STATIC) > 0);
      int offset = (isStaticAccess ? 0
                                  : 1);
      List<String> argNames = analysis.methodArgNames.get(methodName + methodDescriptor);

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

        if ((argNames != null) && (argNames.size() > ii))
        {
          mv.visitLdcInsn("Arg (" + argNames.get(ii) + ")");
        }
        else
        {
          mv.visitLdcInsn("Arg");
        }
        mv.visitLdcInsn(className);
        mv.visitLdcInsn(methodName);
        mv.visitVarInsn(opcode, varslot);
        mv.visitMethodInsn(INVOKESTATIC, HELPER_CLASS, "val",
                           "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;"
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
     * Write branch trace.
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
            if (shouldInstrument &&
                exceptionHandlerLabels.contains(label))
            {
              // Top of the stack contains an exception - generate code to trace
              // it
              // Duplicate the exception
              mv.visitInsn(Opcodes.DUP);

              // Load args
              mv.visitLdcInsn("Caught");
              mv.visitInsn(Opcodes.SWAP);
              mv.visitLdcInsn(className);
              mv.visitInsn(Opcodes.SWAP);
              mv.visitLdcInsn(methodName);
              mv.visitInsn(Opcodes.SWAP);
              mv.visitLdcInsn(lineNumber);
              mv.visitInsn(Opcodes.SWAP);

              // Generate call to trace exception
              mv
                .visitMethodInsn(
                                 INVOKESTATIC,
                                 HELPER_CLASS,
                                 "val",
                                 "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/Throwable;)V");
            }
            else if (shouldInstrument)
            {
              generateCallToAgentHelper(InstrumentationType.BRANCH, lineNumber);
            }
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
     * Handle return instructions by writing exit trace.
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

      if (!shouldInstrument)
      {
        // Don't instrument - ignore
      }
      else if (xiOpCode == Opcodes.RETURN)
      {
        // Ensure that cTor entry call gets written even if the cTor is
        // implicit and therefore has only a single line number.
        if ((ctorEntryState == CTorEntryState.SEEN_SPECIAL) ||
            ("java.lang.Object".equals(className) && (ctorEntryState == CTorEntryState.ISCTOR)))
        {
          addEntryCalls();
          ctorEntryState = CTorEntryState.ENTRY_WRITTEN;
        }

        generateCallToAgentHelper(InstrumentationType.EXIT, lineNumber);
      }
      else if ((xiOpCode == Opcodes.IRETURN) || (xiOpCode == Opcodes.FRETURN)
               || (xiOpCode == Opcodes.ARETURN))
      {
        // Duplicate the return value
        mv.visitInsn(Opcodes.DUP);

        // Push the callname and methodname while keeping the return value#
        // at the top of the stack
        mv.visitLdcInsn("Return");
        mv.visitInsn(Opcodes.SWAP);
        mv.visitLdcInsn(className);
        mv.visitInsn(Opcodes.SWAP);
        mv.visitLdcInsn(methodName);
        mv.visitInsn(Opcodes.SWAP);

        if (xiOpCode == Opcodes.IRETURN)
        {
          mv
            .visitMethodInsn(INVOKESTATIC, HELPER_CLASS, "val",
                             "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V");
        }
        else if (xiOpCode == Opcodes.FRETURN)
        {
          mv
            .visitMethodInsn(INVOKESTATIC, HELPER_CLASS, "val",
                             "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;F)V");
        }
        else if (xiOpCode == Opcodes.ARETURN)
        {
          mv
            .visitMethodInsn(INVOKESTATIC, HELPER_CLASS, "val",
                             "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V");
        }
        generateCallToAgentHelper(InstrumentationType.EXIT, lineNumber);
      }
      else if ((xiOpCode == Opcodes.LRETURN) || (xiOpCode == Opcodes.DRETURN))
      {
        // Duplicate the return value
        mv.visitInsn(Opcodes.DUP2);

        // Push the callname and methodname while keeping the return value
        // at the top of the stack
        mv.visitLdcInsn("Return");
        mv.visitInsn(Opcodes.DUP_X2);
        mv.visitInsn(Opcodes.POP);
        mv.visitLdcInsn(className);
        mv.visitInsn(Opcodes.DUP_X2);
        mv.visitInsn(Opcodes.POP);
        mv.visitLdcInsn(methodName);
        mv.visitInsn(Opcodes.DUP_X2);
        mv.visitInsn(Opcodes.POP);

        if (xiOpCode == Opcodes.LRETURN)
        {
          mv
            .visitMethodInsn(INVOKESTATIC, HELPER_CLASS, "val",
                             "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;J)V");
        }
        else if (xiOpCode == Opcodes.DRETURN)
        {
          mv
            .visitMethodInsn(INVOKESTATIC, HELPER_CLASS, "val",
                             "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;D)V");
        }
        generateCallToAgentHelper(InstrumentationType.EXIT, lineNumber);
      }
      else if (xiOpCode == Opcodes.ATHROW)
      {
        // Top of the stack contains an exception - generate code to trace
        // it - Duplicate the exception
        mv.visitInsn(Opcodes.DUP);

        // Load args
        mv.visitLdcInsn("Throw");
        mv.visitInsn(Opcodes.SWAP);
        mv.visitLdcInsn(className);
        mv.visitInsn(Opcodes.SWAP);
        mv.visitLdcInsn(methodName);
        mv.visitInsn(Opcodes.SWAP);
        mv.visitLdcInsn(lineNumber);
        mv.visitInsn(Opcodes.SWAP);

        // Generate call to trace exception
        mv
          .visitMethodInsn(
                           INVOKESTATIC,
                           HELPER_CLASS,
                           "val",
                           "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/Throwable;)V");

        // Also write exit trace
        generateCallToAgentHelper(InstrumentationType.EXIT, lineNumber);
      }

      if (xiOpCode == Opcodes.RETURN)
      {
        if (threadClass && methodName.equals("setUncaughtExceptionHandler"))
        {
          mv.visitLabel(threadLabel);
          mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        }
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
     * For all forward branch instructions we trace the next line we see as we
     * know it is optional.
     * <p>
     * We don't mark the target label as we don't know whether it is optional
     * code.
     */
    @Override
    public void visitJumpInsn(int xiOpCode, Label xiBranchLabel)
    {
      if (ternState == TernaryState.SEEN_DUP)
      {
        ternState = TernaryState.SEEN_BRANCH;
      }
      else
      {
        ternState = TernaryState.BASE;
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
     * Try catch block - mark all labels for tracing
     */
    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler,
                                   String type)
    {
      // Null type means that handler is a finally block which will always be
      // executed
      if (type != null)
      {
        traceLabels.add(handler);
        exceptionHandlerLabels.add(handler);
      }
      super.visitTryCatchBlock(start, end, handler, type);
    }

    /**
     * Table switch block - mark all labels for tracing
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

    /**
     * Lookup switch block - mark all labels for tracing
     */
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
    ENTER, BRANCH, EXIT;
  }

  /**
   * Type used to suppress trace in the case where the following instruction
   * sequence is seen.
   * <ul>
   * <li>...
   * <li>DUP
   * <li>BRANCH
   * <li>POP
   * <li>...
   * </ul>
   * This sequence is used in ternary if statements of the form (cond(x) ? x :
   * Y). This agent currently does not support adding trace into these
   * constructs.
   * <p>
   * MCHR: This should either be fixed or strengthened to ensure it covers all
   * ternary statements. Does numBranchesOnLine already strengthen this?
   */
  private enum TernaryState
  {
    BASE, SEEN_DUP, SEEN_BRANCH
    /* SEEN_POP */
  }

  /**
   * cTor related state.
   * <p>
   * Normal methods are assigned NORMALMETHOD.
   * <p>
   * cTors are assigned ISCTOR and then SEEN_SPECIAL once the superclass
   * constructor call is issued. This allows us to avoid writing entry trace
   * before the superclass constructor call.
   * <p>
   * ENTRY_WRITTEN is set if a line number is processed such that we write entry
   * trace. Otherwise we know to write entry trace before exit trace when we
   * visit a return instruction. This is mostly only useful for processing
   * implicit constructors.
   * <p>
   * MCHR: Ensure this all works in the case of a constructor calling through to
   * another constructor in the same class.
   */
  private enum CTorEntryState
  {
    NORMALMETHOD, ISCTOR, SEEN_SPECIAL, ENTRY_WRITTEN
  }
}
