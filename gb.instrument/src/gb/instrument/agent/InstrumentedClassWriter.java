package gb.instrument.agent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

/**
 * ASM2 ClassWriter used to transform class files to add Trace output.
 */
public class InstrumentedClassWriter extends ClassWriter
{
  private final String mClassName;
  private final Map<String, Set<Integer>> methodBranchTraceLines;

  /**
   * cTor
   * 
   * @param xiClassName
   * @param xiReader
   * @param xiMap
   */
  public InstrumentedClassWriter(String xiClassName, ClassReader xiReader,
      Map<String, Set<Integer>> xiMap)
  {
    super(xiReader, true);
    mClassName = xiClassName;
    methodBranchTraceLines = xiMap;
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc,
      String signature, String[] exceptions)
  {
    MethodVisitor mv = super.visitMethod(access, name, desc, signature,
        exceptions);
    Set<Integer> branchTraceLines = methodBranchTraceLines.get(name + desc);
    if (branchTraceLines == null)
    {
      branchTraceLines = new HashSet<Integer>();
    }
    return new InstrumentedMethodWriter(mv, mClassName, name, desc, branchTraceLines);
  }

}
