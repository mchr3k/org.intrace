package org.intrace.agent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.objectweb.asm.ClassReader;

/**
 * Uses ASM2 to transform class files to add Trace output.
 */
public class ClassTransformer implements ClassFileTransformer
{
  /**
   * Map of modified class names to their original bytes
   */
  private final Set<String> modifiedClasses =
                                     new ConcurrentSkipListSet<String>();
  private final Instrumentation inst;
  private final AgentSettings args;  

  /**
   * cTor
   * @param xiInst
   * @param xiEnableTracing
   * @param xiClassRegex
   * @param xiWriteModifiedClassfiles
   * @param xiVerboseMode
   * @param xiEnableTraceJars
   */
  public ClassTransformer(Instrumentation xiInst, AgentSettings xiArgs)
  {
    inst = xiInst;
    args = xiArgs;    
    if (args.isVerboseMode())
    {
      System.out.println(args.toString());
    }
  }

  /**
   * Toggle whether tracing is enabled
   * @param xiTracingEnabled
   */
  public void setTracingEnabled(boolean xiOldTracingEnabled)
  {
    if (args.isTracingEnabled() && !xiOldTracingEnabled)
    {
      traceLoadedClasses();
    }
    else if (!args.isTracingEnabled() && xiOldTracingEnabled)
    {
      recheckModifiedClasses();
    }
  }

  /**
   * Apply Trace transformation to loaded classes.
   * @param xiInst
   */
  public void traceLoadedClasses()
  {
    Class<?>[] loadedClasses = inst.getAllLoadedClasses();
    for (Class<?> loadedClass : loadedClasses)
    {
      if (loadedClass.isAnnotation())
      {
        if (args.isVerboseMode())
        {
          System.out.println("Ignoring annotation class: " + loadedClass.getCanonicalName());
        }
      }
      else if (loadedClass.isSynthetic())
      {
        if (args.isVerboseMode())
        {
          System.out.println("Ignoring synthetic class: " + loadedClass.getCanonicalName());
        }
      }
      else if (!inst.isModifiableClass(loadedClass))
      {
        if (args.isVerboseMode())
        {
          System.out.println("Ignoring unmodifiable class: " + loadedClass.getCanonicalName());
        }
      }
      else if (args.isTracingEnabled() &&
               isToBeConsideredForCoverage(loadedClass.getName(), loadedClass.getProtectionDomain()))
      {
        try
        {
          inst.retransformClasses(loadedClass);
        }
        catch (UnmodifiableClassException e)
        {
          // Write exception to stdout
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Restore original class bytes
   * @param xiInst
   */
  private void recheckModifiedClasses()
  {
    Class<?>[] loadedClasses = inst.getAllLoadedClasses();
    for (Class<?> loadedClass : loadedClasses)
    {
      try
      {
        if (modifiedClasses.contains(loadedClass.getName()))
        {
          inst.retransformClasses(loadedClass);
        }
      }
      catch (UnmodifiableClassException e)
      {
        // Write exception to stdout
        e.printStackTrace();
      }
    }
  }

  private boolean isToBeConsideredForCoverage(String className,
                                              ProtectionDomain protectionDomain)
  {
    // Don't modify self
    if (className.startsWith("gb.instrument"))
    {
      if (args.isVerboseMode())
      {
        System.out.println("Ignoring class in gb.instrument package: " + className);
      }
      return false;
    }
    
    // Don't modify a class which is already modified
    if (modifiedClasses.contains(className))
    {
      if (args.isVerboseMode())
      {
        System.out.println("Ignoring class already modified: " + className);
      }
      return false;
    }

    // Don't modify test classes
    int p = className.lastIndexOf('$');
    if (className.endsWith("Test") || p > 0
        && className.substring(0, p).endsWith("Test"))
    {
      if (args.isVerboseMode())
      {
        System.out.println("Ignoring class name ending in Test: " + className);
      }
      return false;
    }

    // Don't modify classes which fail to match the regex
    if ((args.getClassRegex() == null) ||
        !args.getClassRegex().matcher(className).matches())
    {
      if (args.isVerboseMode())
      {
        System.out.println("Ignoring class not matching the active regex: " + className);
      }
      return false;
    }

    // Don't modify a class from a JAR file unless this is allowed
    CodeSource codeSource = protectionDomain.getCodeSource();
    if (!args.allowJarsToBeTraced() && 
        (codeSource != null) && 
         codeSource.getLocation().getPath().endsWith(".jar"))
    {
      if (args.isVerboseMode())
      {
        System.out.println("Ignoring class in a JAR: " + className);
      }
      return false;
    }
    return true;           
  }

  @Override
  public byte[] transform(ClassLoader loader,
                          String internalClassName,
                          Class<?> classBeingRedefined,
                          ProtectionDomain protectionDomain,
                          byte[] originalClassfile)
                          throws IllegalClassFormatException
  {
    String className = internalClassName.replace('/', '.');
    if (args.isTracingEnabled() &&
        isToBeConsideredForCoverage(className, protectionDomain))
    {      
      if (args.isVerboseMode())
      {
        System.out.println("!! Instrumenting class: " + className);
      }
      byte[] newBytes = readAndModifyClassForTracing(className, originalClassfile);

      if (args.saveTracedClassfiles())
      {
        try
        {
          File classOut = new File("./genbin/" + internalClassName + "_gen.class");
          File parentDir = classOut.getParentFile();
          boolean dirExists = parentDir.exists();
          if (!dirExists)
          {
            dirExists = parentDir.mkdirs();
          }
          if (dirExists)
          {
            OutputStream out = new FileOutputStream(classOut);
            out.write(newBytes);
            out.flush();
            out.close();
          }
          else
          {
            System.out.println("Can't create directory " + parentDir +
                               " for saving traced classfiles.");
          }
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
      
      modifiedClasses.add(className);
      return newBytes;
    }
    else
    {
      modifiedClasses.remove(className);
      return null;
    }
  }

  private byte[] readAndModifyClassForTracing(String xiClassName,
                                              byte[] classfileBuffer)
  {
     ClassReader cr = new ClassReader(classfileBuffer);
     ClassBranchLineAnalysis analysis = new ClassBranchLineAnalysis();
     cr.accept(analysis, false);
     InstrumentedClassWriter writer = new InstrumentedClassWriter(xiClassName,
                                                    cr,
                                             analysis.getMethodBranchLabels());
     cr.accept(writer, false);
     return writer.toByteArray();
  }

  public String getResponse(String message)
  {
    AgentSettings oldSettings = new AgentSettings(args);
    args.parseArgs(message);
    
    if (args.isVerboseMode() &&
        (oldSettings.isVerboseMode() != args.isVerboseMode()))
    {
      System.out.println(args.toString());
    }
    else if (oldSettings.isTracingEnabled() != args.isTracingEnabled())
    {
      System.out.println("## Settings Changed");
      setTracingEnabled(oldSettings.isTracingEnabled());
    }
    else if (!oldSettings.getClassRegex().pattern().equals(args.getClassRegex().pattern()))
    {
      System.out.println("## Settings Changed");
      recheckModifiedClasses();
      traceLoadedClasses();
    }
    else if (oldSettings.allowJarsToBeTraced() != args.allowJarsToBeTraced())
    {
      System.out.println("## Settings Changed");
      recheckModifiedClasses();
      traceLoadedClasses();
    }
    else if (oldSettings.saveTracedClassfiles() != args.saveTracedClassfiles())
    {
      System.out.println("## Settings Changed");
      recheckModifiedClasses();
      traceLoadedClasses();
    }
    
    return AgentHelper.getActiveOutputHandler().getResponse(message);    
  }

  public Map<String, String> getSettings()
  {
    return args.getSettingsMap();
  }
}
