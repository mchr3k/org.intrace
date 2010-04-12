package org.intrace.agent;

import java.util.HashSet;
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
  private final ClassBranchLineAnalysis analysis;

  /**
   * cTor
   * 
   * @param xiClassName
   * @param xiReader
   * @param analysis
   */
  public InstrumentedClassWriter(String xiClassName, ClassReader xiReader,
                                 ClassBranchLineAnalysis xiAnalysis)
  {
    super(xiReader, true);
    mClassName = xiClassName;
    analysis = xiAnalysis;
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc,
                                   String signature, String[] exceptions)
  {
    MethodVisitor mv = super.visitMethod(access, name, desc, signature,
                                         exceptions);
    Set<Integer> branchTraceLines = analysis.methodReverseGOTOLines.get(name + desc);
    Integer entryLine = analysis.methodEntryLines.get(name + desc);
    if (branchTraceLines == null)
    {
      branchTraceLines = new HashSet<Integer>();
    }
    return new InstrumentedMethodWriter(mv, mClassName, name, desc, branchTraceLines, entryLine);
  }

}
