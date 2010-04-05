package gb.instrument.agent;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

import java.util.Set;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * ASM2 MethodVisitor used to add trace to methods.
 */
public class InstrumentedMethodWriter extends MethodAdapter
{
  private static final String HELPER_CLASS = "gb/instrument/agent/AgentHelper";
  private final String className;
  private final String methodName;
  private final String methodDescriptor;
  private int lineNumber = -1;

  private final Set<Integer> branchTraceLines;

  private boolean writeTraceLine = false;

  /**
   * cTor
   * @param xiMethodVisitor
   * @param xiClassName
   * @param xiMethodName
   * @param xiDesc 
   * @param xiBranchTraceLines
   */
  public InstrumentedMethodWriter(MethodVisitor xiMethodVisitor,
                           String xiClassName,
                           String xiMethodName,
                           String xiDesc, 
                           Set<Integer> xiBranchTraceLines)
  {
    super(xiMethodVisitor);
    className = xiClassName;
    methodName = xiMethodName;
    methodDescriptor = xiDesc;
    branchTraceLines = xiBranchTraceLines;
  }

  @Override
  public void visitCode()
  {
    generateCallToWriteBranchTrace(TraceType.BEGIN,-1);
    traceArgs();
    super.visitCode();
  }
  
  private void traceArgs()
  {
    Type[] argTypes = Type.getArgumentTypes(methodDescriptor);
    for (int ii = 0; ii < argTypes.length; ii++)
    {
      String typeDescriptor = argTypes[ii].getDescriptor();     
      
      if (argTypes[ii].getSort() == Type.OBJECT)
      {
        typeDescriptor = "Ljava/lang/Object;";
      }
      else if ((argTypes[ii].getSort() == Type.ARRAY) &&
               (argTypes[ii].getDescriptor().startsWith("[L")))
      {
        typeDescriptor = "[Ljava/lang/Object;";
      }
              
      mv.visitLdcInsn(className);
      mv.visitLdcInsn(methodName);
      mv.visitVarInsn(Opcodes.ALOAD, ii);
      mv.visitMethodInsn(INVOKESTATIC,
                         HELPER_CLASS,
                         "arg",
                         "(Ljava/lang/String;Ljava/lang/String;" + typeDescriptor + ")V");
    } 
  }

  @Override
  public void visitLineNumber(int xiLineNumber, Label label)
  {    
    lineNumber = xiLineNumber;
    if (writeTraceLine ||
        branchTraceLines.contains(xiLineNumber))
    {
      generateCallToWriteBranchTrace(TraceType.BRANCH, lineNumber);
      if (writeTraceLine)
      {
        writeTraceLine = false;
      }
    }
    super.visitLineNumber(xiLineNumber, label);
  }

  @Override
  public void visitInsn(int xiOpCode)
  {
    if (xiOpCode == RETURN)
    {
      generateCallToWriteBranchTrace(TraceType.END, lineNumber);
    }
    super.visitInsn(xiOpCode);
  }

  @Override
  public void visitJumpInsn(int xiOpCode, Label xiBranchLabel)
  {
    writeTraceLine = true;
    super.visitJumpInsn(xiOpCode, xiBranchLabel);
  }

  private void generateCallToWriteBranchTrace(TraceType traceType,
                                              int lineNumber)
  {
    switch (traceType)
    {
      case BEGIN:
      {
        mv.visitLdcInsn(className);
        mv.visitLdcInsn(methodName);        
        mv.visitMethodInsn(INVOKESTATIC,
                           HELPER_CLASS,
                           "enter",
                           "(Ljava/lang/String;Ljava/lang/String;)V");
      }
      break;
      
      case BRANCH:
      {
        mv.visitLdcInsn(className);
        mv.visitLdcInsn(methodName);
        mv.visitIntInsn(Opcodes.BIPUSH, lineNumber);
        mv.visitMethodInsn(INVOKESTATIC,
                           HELPER_CLASS,
                           "branch",
                           "(Ljava/lang/String;Ljava/lang/String;I)V");
      }
      break;
      
      case END:
      {
        mv.visitLdcInsn(className);
        mv.visitLdcInsn(methodName);
        mv.visitIntInsn(Opcodes.BIPUSH, lineNumber);
        mv.visitMethodInsn(INVOKESTATIC,
                           HELPER_CLASS,
                           "exit",
                           "(Ljava/lang/String;Ljava/lang/String;I)V");
      }
      break;
    }    
  }

  private enum TraceType
  {
    BEGIN,
    BRANCH,
    END
  }
}
