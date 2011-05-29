package org.intrace.agent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.intrace.agent.server.AgentClientConnection;
import org.intrace.agent.server.AgentServer;
import org.intrace.output.AgentHelper;
import org.intrace.output.InstruRunnable;
import org.intrace.output.trace.TraceHandler;
import org.intrace.shared.AgentConfigConstants;
import org.objectweb.asm.ClassReader;

/**
 * Uses ASM2 to transform class files to add Trace instrumentation.
 */
public class ClassTransformer implements ClassFileTransformer
{
  /**
   * Set of modified class names
   */
  private final Set<ComparableClassName> modifiedClasses = new ConcurrentSkipListSet<ComparableClassName>();

  /**
   * Map of all seen class names
   */
  private final Set<ComparableClassName> allClasses = new ConcurrentSkipListSet<ComparableClassName>();

  /**
   * Instrumentation interface.
   */
  private final Instrumentation inst;

  /**
   * Settings for this Transformer
   */
  private final AgentSettings settings;
  
  /**
   * Marker indicating whether many classes are currently being updated
   */
  private final AtomicBoolean bulkUpdateActive = new AtomicBoolean(false);

  /**
   * cTor
   * 
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
    settings = xiArgs;
    if (settings.isVerboseMode())
    {
      TraceHandler.INSTANCE.writeTraceOutput("DEBUG: " + settings.toString());
    }
  }

  /**
   * Generate and return instrumented class bytes.
   * 
   * @param xiClassName
   * @param classfileBuffer
   * @return Instrumented class bytes
   */
  private byte[] getInstrumentedClassBytes(String xiClassName,
                                           byte[] classfileBuffer)
  {
    try
    {
      ClassReader cr = new ClassReader(classfileBuffer);
      ClassAnalysis analysis = new ClassAnalysis();
      cr.accept(analysis, 0);

      InstrumentedClassWriter writer = new InstrumentedClassWriter(xiClassName,
                                                                   cr, analysis);
      cr.accept(writer, 0);

      return writer.toByteArray();
    }
    catch (Throwable th)
    {
      System.err.println("Caught Throwable when trying to instrument: "
                         + xiClassName);
      th.printStackTrace();
      return null;
    }
  }

  /**
   * Determine whether a given className is eligible for modification. Any of
   * the following conditions will make a class ineligible for instrumentation.
   * <ul>
   * <li>Class name which begins with "org.intrace"
   * <li>Class name which begins with "org.objectweb.asm"
   * <li>The class has already been modified
   * <li>Class name ends with "Test"
   * <li>Class name doesn't match the regex
   * <li>Class is in a JAR and JAR instrumention is disabled
   * </ul>
   * 
   * @param klass
   * @param className
   * @param protectionDomain
   * @return True if the Class with name className should be instrumented.
   */
  private boolean isToBeConsideredForInstrumentation(
                                                     Class<?> klass,
                                                     ClassLoader klassloader,
                                                     String className,
                                                     ProtectionDomain protectionDomain)
  {
    ComparableClassName compklass = new ComparableClassName(className,
                                                            klassloader);

    // Record all class names which get this far
    allClasses.add(compklass);

    // Don't modify anything if tracing is disabled
    if (!settings.isInstrumentationEnabled())
    {
      return false;
    }
    
    // Don't sensitive classes
    if (isSensitiveClass(className))
    {
      if (settings.isVerboseMode())
      {
        TraceHandler.INSTANCE.writeTraceOutput("DEBUG: Ignoring system class: " + className);
      }
      return false;
    }

    // Don't modify a class which is already modified
    if (modifiedClasses.contains(compklass))
    {
      if (settings.isVerboseMode())
      {
        TraceHandler.INSTANCE.writeTraceOutput("DEBUG: Ignoring class already modified: " + compklass);
      }
      return false;
    }
    
    // Don't modify classes which match the exclude regex
    if ((settings.getExcludeClassRegex() == null)
        || settings.getExcludeClassRegex().matcher(className).matches())
    {
      if (settings.isVerboseMode())
      {
        TraceHandler.INSTANCE.writeTraceOutput("DEBUG: Ignoring class matching the active exclude regex: "
                           + className);
      }
      return false;
    }

    // Don't modify classes which fail to match the regex
    if ((settings.getClassRegex() == null)
        || !settings.getClassRegex().matcher(className).matches())
    {
      if (settings.isVerboseMode())
      {
        TraceHandler.INSTANCE.writeTraceOutput("DEBUG: Ignoring class not matching the active include regex: "
                           + className);
      }
      return false;
    }

    // All checks passed - class can be instrumented
    return true;
  }

  private boolean isSensitiveClass(String className)
  {
    return className.contains(".intrace.") || 
           className.contains("objectweb.asm") ||
           className.equals("java.lang.Thread") || 
           className.equals("java.lang.SecurityManager") ||
           className.equals("java.lang.System");
  }

  /**
   * java.lang.instrument Entry Point
   * <p>
   * Optionally transform a class file to add instrumentation.
   * {@link ClassTransformer#isToBeConsideredForInstrumentation(String, ProtectionDomain)}
   * determines whether a class is eligible for instrumentation.
   */
  @Override
  public byte[] transform(ClassLoader loader, String internalClassName,
                          Class<?> classBeingRedefined,
                          ProtectionDomain protectionDomain,
                          byte[] originalClassfile)
      throws IllegalClassFormatException
  {
    // Accessing the Thread to set the UncaughtExceptionHandler is safe as we
    // have some specific exclusions in the isSensitiveClass method to block
    // instrumentation of this class.
    Thread currentTh = Thread.currentThread();
    UncaughtExceptionHandler handler = currentTh.getUncaughtExceptionHandler();
    currentTh.setUncaughtExceptionHandler(AgentHelper.INSTRU_CRITICAL_BLOCK);
      
    try
    {
      String className = internalClassName.replace('/', '.');
      ComparableClassName compclass = new ComparableClassName(className, loader);
      int modifiedSize = modifiedClasses.size();
      int allClassesSize = allClasses.size();
  
      if (isToBeConsideredForInstrumentation(classBeingRedefined, loader,
                                             className, protectionDomain))
      {
        System.out.println("!! Instrumenting class: " + compclass);
  
        if (settings.saveTracedClassfiles())
        {
          writeClassBytes(originalClassfile, internalClassName + "_src.class");
        }
  
        byte[] newBytes;
        try
        {
          newBytes = getInstrumentedClassBytes(className, originalClassfile);
        }
        catch (RuntimeException th)
        {
          // Ensure the JVM doesn't silently swallow an unchecked exception
          th.printStackTrace();
          throw th;
        }
        catch (Error th)
        {
          // Ensure the JVM doesn't silently swallow an unchecked exception
          th.printStackTrace();
          throw th;
        }
  
        if (settings.saveTracedClassfiles())
        {
          writeClassBytes(newBytes, internalClassName + "_gen.class");
        }
  
        modifiedClasses.add(compclass);
        
        sendStatusUpdate(modifiedSize, allClassesSize);
              
        return newBytes;
      }
      else
      {
        modifiedClasses.remove(compclass);
        
        sendStatusUpdate(modifiedSize, allClassesSize);
        return null;
      }
    }
    finally
    {
      currentTh.setUncaughtExceptionHandler(handler);
    }
  }

  private static class StatusUpdate
  {
    public final int modifiedSize;
    public final int allClassesSize;
    
    public StatusUpdate(int modifiedSize, int allClassesSize)
    {
      this.modifiedSize = modifiedSize;
      this.allClassesSize = allClassesSize;
    }
  }
  
  private static class StatusHolder
  {
    private StatusUpdate update;
    public synchronized void setStatus(StatusUpdate update)
    {
      this.update = update;
      this.notifyAll();
    }
    
    public synchronized StatusUpdate getStatus() throws InterruptedException
    {
      while (update == null)
      {
        this.wait();
      }
      
      StatusUpdate retVal = update;
      update = null;
      
      return retVal;
    }
  }
  
  private class StatusUpdateThread extends InstruRunnable
  {    
    // Need more than 1 slot to allow for recursive status calls
    public final StatusHolder statusHolder = new StatusHolder();

    public void runMethod()
    {      
      while (true)
      {        
        try
        {
          StatusUpdate update = statusHolder.getStatus();
          int newModifiedSize = modifiedClasses.size();
          int newAllClassesSize = allClasses.size();
          if (!bulkUpdateActive.get() &&
              ((newModifiedSize != update.modifiedSize) ||
               (newAllClassesSize != update.allClassesSize)))
          {
            broadcastStatus(modifiedClasses.size(), allClasses.size());
          }
        }
        catch (InterruptedException e)
        {
          // Ignore - exit this thread
        }
      }
    }
    
    public StatusUpdateThread start()
    {
      Thread statusUpdateThread = new Thread(this);
      statusUpdateThread.setDaemon(true);
      statusUpdateThread.setName("Instrumentation Status Updates");
      statusUpdateThread.start();
      return this;
    }
  }
  
  private StatusUpdateThread statusUpdater = new StatusUpdateThread().start();
  
  /**
   * Asynchronously send a status update to all connected clients.
   * <p>
   * We do this asynchronously as it was observed that attempting to send responses from
   * the same thread that was doing the instrumentation caused problems.
   * @param modifiedSize
   * @param allClassesSize
   */
  private void sendStatusUpdate(int modifiedSize, int allClassesSize)
  {
    statusUpdater.statusHolder.setStatus(new StatusUpdate(modifiedSize, allClassesSize));
  }

  private void writeClassBytes(byte[] newBytes, String className)
  {
    File classOut = new File("./genbin/" + className);
    File parentDir = classOut.getParentFile();
    boolean dirExists = parentDir.exists();
    if (!dirExists)
    {
      dirExists = parentDir.mkdirs();
    }
    if (dirExists)
    {
      try
      {
        OutputStream out = new FileOutputStream(classOut);
        try
        {
          out.write(newBytes);
          out.flush();
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }
        finally
        {
          try
          {
            out.close();
          }
          catch (IOException ex)
          {
            ex.printStackTrace();
          }
        }
      }
      catch (FileNotFoundException ex)
      {
        ex.printStackTrace();
      }
    }
    else
    {
      System.out.println("Can't create directory " + parentDir
                         + " for saving traced classfiles.");
    }

  }

  /**
   * Toggle whether instrumentation is enabled
   * 
   * @param xiTracingEnabled
   */
  public void setInstrumentationEnabled(boolean xiInstrumentationEnabled)
  {
    Set<ComparableClass> klasses;
    if (xiInstrumentationEnabled)
    {
      klasses = getLoadedClassesForModification();
    }
    else
    {
      klasses = getModifiedClasses();
    }
    instrumentKlasses(klasses);
  }

  /**
   * @return The currently active settings.
   */
  public Map<String, String> getSettings()
  {
    Map<String, String> settingsMap = settings.getSettingsMap();
    settingsMap.put(AgentConfigConstants.STCLS,
                    Integer.toString(allClasses.size()));
    settingsMap.put(AgentConfigConstants.STINST,
                    Integer.toString(modifiedClasses.size()));
    return settingsMap;
  }

  /**
   * Handle a message and return a response.
   * @param connection 
   * 
   * @param message
   * @return Response or null if there is no response.
   */
  public List<String> getResponse(AgentClientConnection connection, String message)
  {
    List<String> responses = new ArrayList<String>();
    AgentSettings oldSettings = new AgentSettings(settings);
    settings.parseArgs(message);

    if (settings.isVerboseMode()
        && (oldSettings.isVerboseMode() != settings.isVerboseMode()))
    {
      TraceHandler.INSTANCE.writeTraceOutput("DEBUG: " + settings.toString());
    }
    else if (oldSettings.isInstrumentationEnabled() != settings
                                                               .isInstrumentationEnabled())
    {
      System.out.println("## Settings Changed");
      setInstrumentationEnabled(settings.isInstrumentationEnabled());
    }
    else if (!oldSettings.getClassRegex().pattern()
                         .equals(settings.getClassRegex().pattern()))
    {
      System.out.println("## Settings Changed");
      Set<ComparableClass> klasses = new HashSet<ComparableClass>(getModifiedClasses());
      modifiedClasses.clear();
      klasses.addAll(getLoadedClassesForModification());
      instrumentKlasses(klasses);
    }
    else if (oldSettings.saveTracedClassfiles() != settings
                                                           .saveTracedClassfiles())
    {
      System.out.println("## Settings Changed");
      Set<ComparableClass> klasses = getModifiedClasses();
      modifiedClasses.clear();
      klasses.addAll(getLoadedClassesForModification());
      instrumentKlasses(klasses);
    }
    else if (message.equals("[listmodifiedclasses"))
    {
      responses.add(modifiedClasses.toString());
    }

    responses.addAll(AgentHelper.getResponses(connection, message));

    return responses;
  }

  /**
   * Retransform all modified classes.
   * <p>
   * Iterates over all loaded classes and retransforms those which we know we
   * have modified.
   * 
   * @param xiInst
   */
  private Set<ComparableClass> getModifiedClasses()
  {
    Set<ComparableClass> modifiedKlasses = new ConcurrentSkipListSet<ComparableClass>();
    Class<?>[] loadedClasses = inst.getAllLoadedClasses();
    for (Class<?> loadedClass : loadedClasses)
    {
      ComparableClassName compclass = new ComparableClassName(
                                                              loadedClass
                                                                         .getName(),
                                                              loadedClass
                                                                         .getClassLoader());
      if (modifiedClasses.contains(compclass))
      {
        modifiedKlasses.add(new ComparableClass(loadedClass));
      }
    }
    return modifiedKlasses;
  }

  /**
   * Consider loaded classes for transformation. Any of the following reasons
   * would prevent a loaded class from being eligible for instrumentation.
   * <ul>
   * <li>Class is an annotation
   * <li>Class is synthetic
   * <li>Class is not modifiable
   * <li>Class is rejected by
   * {@link ClassTransformer#isToBeConsideredForInstrumentation(String, ProtectionDomain)}
   * </ul>
   */
  public Set<ComparableClass> getLoadedClassesForModification()
  {
    Set<ComparableClass> unmodifiedKlasses = new ConcurrentSkipListSet<ComparableClass>();

    Class<?>[] loadedClasses = inst.getAllLoadedClasses();
    for (Class<?> loadedClass : loadedClasses)
    {
      if (loadedClass.isAnnotation())
      {
        if (settings.isVerboseMode())
        {
          TraceHandler.INSTANCE.writeTraceOutput("DEBUG: Ignoring annotation class: "
                             + loadedClass.getCanonicalName());
        }
      }
      else if (loadedClass.isSynthetic())
      {
        if (settings.isVerboseMode())
        {
          TraceHandler.INSTANCE.writeTraceOutput("DEBUG: Ignoring synthetic class: "
                             + loadedClass.getCanonicalName());
        }
      }
      else if (!inst.isModifiableClass(loadedClass))
      {
        if (settings.isVerboseMode())
        {
          TraceHandler.INSTANCE.writeTraceOutput("DEBUG: Ignoring unmodifiable class: "
                             + loadedClass.getCanonicalName());
        }
      }
      else if (isToBeConsideredForInstrumentation(
                                                  loadedClass,
                                                  loadedClass.getClassLoader(),
                                                  loadedClass.getName(),
                                                  loadedClass
                                                             .getProtectionDomain()))
      {
        ComparableClass loadedKlass = new ComparableClass(loadedClass);
        unmodifiedKlasses.add(loadedKlass);
      }
    }
    return unmodifiedKlasses;
  }

  public void instrumentKlasses(Set<ComparableClass> klasses)
  {
    if (klasses.size() == 0)
    {
      return;
    }
    try
    {
      bulkUpdateActive.set(true);
      int countNumClasses = 0;
      int totalNumClasses = klasses.size();
      broadcastProgress(countNumClasses, totalNumClasses);
      for (ComparableClass klass : klasses)
      {
        try
        {
          inst.retransformClasses(klass.klass);
  
          countNumClasses++;
          if ((countNumClasses % 10) == 0)
          {
            broadcastProgress(countNumClasses, totalNumClasses);
          }
        }
        catch (Throwable e)
        {
          // Write exception to stdout
          System.out.println(klass.klass.getName());
          e.printStackTrace();
        }
      }
      broadcastProgress(totalNumClasses, totalNumClasses, true);
    }
    finally
    {
      bulkUpdateActive.set(false);
      broadcastStatus(modifiedClasses.size(), allClasses.size());
    }
  }

  private void broadcastProgress(int count, int total)
  {
    broadcastProgress(count, total, false);
  }
  
  private void broadcastProgress(int count, int total, boolean done)
  {
    Map<String, String> progressMap = new HashMap<String, String>();
    progressMap.put(AgentConfigConstants.NUM_PROGRESS_ID,
                    AgentConfigConstants.NUM_PROGRESS_ID);
    progressMap.put(AgentConfigConstants.NUM_PROGRESS_COUNT,
                    Integer.toString(count));
    progressMap.put(AgentConfigConstants.NUM_PROGRESS_TOTAL,
                    Integer.toString(total));
    if (done)
    {
      progressMap.put(AgentConfigConstants.NUM_PROGRESS_DONE, Boolean.TRUE.toString());
    }
    try
    {
      AgentServer.broadcastMessage(null, progressMap);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
  
  private void broadcastStatus(int count, int total)
  {
    Map<String, String> progressMap = new HashMap<String, String>();
    progressMap.put(AgentConfigConstants.STID,
                    AgentConfigConstants.STID);
    progressMap.put(AgentConfigConstants.STINST,
                    Integer.toString(count));
    progressMap.put(AgentConfigConstants.STCLS,
                    Integer.toString(total));
    try
    {
      AgentServer.broadcastMessage(null, progressMap);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Container for a Class Name to make it comparable. This is used when
   * collecting together a Set of which classes are already instrumented. We
   * cannot refer to the corresponding Class object as we need to construct
   * these objects before the corresponding Class object has been created by the
   * JVM.
   */
  private static class ComparableClassName implements
      Comparable<ComparableClassName>
  {
    public final String klassName;
    public final ClassLoader klassloader;

    /**
     * cTor
     * 
     * @param klassName
     *          , ClassLoader klassloader
     */
    public ComparableClassName(String klassName, ClassLoader klassloader)
    {
      this.klassName = klassName;
      this.klassloader = klassloader;
    }

    @Override
    public int compareTo(ComparableClassName other)
    {
      if (other.klassloader != this.klassloader)
      {
        // klasses loaded by different classloaders are never equal. Compare the
        // hashcodes of the names to come up with a number which satisfies the
        // requirement of
        // compareTo:
        // sgn(x.compareTo(y)) == -sgn(y.compareTo(x))
        //
        // Note that this approach is not guaranteed to work as the hashCode is
        // allowed to be the same for different objects.
        return this.toRegularString().compareTo(other.toRegularString());
      }
      else
      {
        // klasses loaded by the same classloader can be compared by name. This
        // allows us to use the String compareTo method.
        return this.klassName.compareTo(other.klassName);
      }
    }

    @Override
    public boolean equals(Object obj)
    {
      if (obj instanceof ComparableClassName)
      {
        ComparableClassName compClass = (ComparableClassName) obj;
        return (compClass.klassloader == this.klassloader)
               && compClass.klassName.equals(this.klassName);
      }
      else
      {
        return super.equals(obj);
      }
    }

    @Override
    public int hashCode()
    {
      return klassName.hashCode();
    }

    protected String toRegularString()
    {
      String klassloaderStr = "";
      if (klassloader != null)
      {
        klassloaderStr = klassloader.getClass().getName() + '@'
                         + Integer.toHexString(klassloader.hashCode()) + ":";
      }
      return klassloaderStr + klassName;
    }

    @Override
    public String toString()
    {
      String klassLoaderName = "";
      if (klassloader != null)
      {
        if (klassloader.getClass().getName()
                       .equals("org.apache.catalina.loader.WebappClassLoader"))
        {
          klassLoaderName = klassloader.getClass().getName() + '@'
                            + Integer.toHexString(klassloader.hashCode());

          try
          {
            Method klassLoaderJarPath = klassloader.getClass()
                                                   .getMethod("getURLs");
            URL[] classURLs = (URL[]) klassLoaderJarPath
                                                        .invoke(klassloader,
                                                                (Object[]) null);
            if (classURLs != null && classURLs.length > 0
                && classURLs[0] != null)
            {
              Set<String> urlSet = new HashSet<String>();
              for (URL classURL : classURLs)
              {
                if (classURL != null)
                {
                  String urlPath = classURL.getPath();
                  urlPath = urlPath
                                   .substring(
                                              0,
                                              urlPath
                                                     .lastIndexOf(File.separator) + 1);
                  urlSet.add(urlPath);
                }
              }
              if (urlSet.size() > 0)
              {
                klassLoaderName += "\nClasspaths:\n";
                StringBuilder classUrlStr = new StringBuilder();
                for (String classURLStr : urlSet)
                {
                  classUrlStr.append(classURLStr).append("\n");
                }
                klassLoaderName += classUrlStr.toString();
              }
            }
          }
          catch (Throwable th)
          {
            // Discard
          }
        }
        else
        {
          klassLoaderName = klassloader.toString();
        }
        klassLoaderName += ":";
      }
      return klassLoaderName + klassName;
    }
  }

  /**
   * Container for a Class to make it comparable. This is used when collecting
   * together a Set of Class objects for reinstrumentation.
   */
  private static class ComparableClass implements Comparable<ComparableClass>
  {
    public final Class<?> klass;
    public final ClassLoader klassloader;

    /**
     * cTor
     * 
     * @param klass
     */
    public ComparableClass(Class<?> klass)
    {
      this.klass = klass;
      this.klassloader = klass.getClassLoader();
    }

    @Override
    public int compareTo(ComparableClass other)
    {
      if (other.klassloader != this.klassloader)
      {
        // klasses loaded by different classloaders are never equal. Compare the
        // hashcodes to come up with a number which satisfies the requirement of
        // compareTo:
        // sgn(x.compareTo(y)) == -sgn(y.compareTo(x))
        //
        // Note that this approach is not guaranteed to work as the hashCode is
        // allowed to be the same for different objects.
        return this.toString().compareTo(other.toString());
      }
      else
      {
        // klasses loaded by the same classloader can be compared by name. This
        // allows us to use the String compareTo method.
        String thisName = this.klass.getName();
        String otherName = other.klass.getName();
        return thisName.compareTo(otherName);
      }
    }

    @Override
    public boolean equals(Object obj)
    {
      if (obj instanceof ComparableClass)
      {
        ComparableClass compClass = (ComparableClass) obj;
        return (compClass.klassloader == this.klassloader)
               && compClass.klass.equals(this.klass);
      }
      else
      {
        return super.equals(obj);
      }
    }

    @Override
    public int hashCode()
    {
      return klass.hashCode();
    }

    @Override
    public String toString()
    {
      String klassloaderStr = "";
      if (klassloader != null)
      {
        klassloaderStr = klassloader.getClass().getName() + '@'
                         + Integer.toHexString(klassloader.hashCode()) + ":";
      }
      return klassloaderStr + klass.getName();
    }
  }
}
