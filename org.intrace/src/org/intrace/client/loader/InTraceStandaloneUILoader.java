package org.intrace.client.loader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.jdt.internal.jarinjarloader.RsrcURLStreamHandlerFactory;

public class InTraceStandaloneUILoader
{
  public static void main(String[] args) throws Throwable
  {    
    ClassLoader cl = getSWTClassloader();
    Thread.currentThread().setContextClassLoader(cl);    
    try
    {
      try
      {
        System.err.println("Launching InTrace UI ...");
        Class<?> c = Class.forName("org.intrace.client.gui.InTraceStandaloneUI", true, cl);
        Method main = c.getMethod("main", new Class[]{args.getClass()});
        main.invoke((Object)null, new Object[]{args});
      }
      catch (InvocationTargetException ex)
      {
        if (ex.getCause() instanceof UnsatisfiedLinkError)
        {
          UnsatisfiedLinkError linkError = (UnsatisfiedLinkError)ex.getCause();
          System.err.println("Launch failed: (UnsatisfiedLinkError: " + linkError.getMessage() + ")");
          String arch = getArch();
          if ("32".equals(arch))
          {
            System.err.println("Try adding '-d64' to your command line arguments");
          }
          else if ("64".equals(arch))
          {
            System.err.println("Try adding '-d32' to your command line arguments");
          }
        }
        else
        {
          throw ex;
        }
      }
    }
    catch (ClassNotFoundException ex)
    {
      System.err.println("Launch failed: Failed to find main class - org.intrace.client.gui.TraceClient");
    }
    catch (NoSuchMethodException ex)
    {
      System.err.println("Launch failed: Failed to find main method");
    }
    catch (InvocationTargetException ex)
    {
      Throwable th = ex.getCause();
      if ((th.getMessage() != null) &&
          th.getMessage().toLowerCase().contains("invalid thread access"))
      {
        System.err.println("Launch failed: (SWTException: Invalid thread access)");
        System.err.println("Try adding '-XstartOnFirstThread' to your command line arguments");
      }
      else
      {
        throw th;
      }
    }
  }

  private static ClassLoader getSWTClassloader()
  {
    ClassLoader parent = InTraceStandaloneUILoader.class.getClassLoader();    
    URL.setURLStreamHandlerFactory(new RsrcURLStreamHandlerFactory(parent));
    String swtFileName = getSwtJarName();      
    try
    {
      URL intraceFileUrl = new URL("rsrc:intrace-ui-wrapper.jar");
      URL swtFileUrl = new URL("rsrc:" + swtFileName);
      System.err.println("Using SWT Jar: " + swtFileName);
      ClassLoader cl = new URLClassLoader(new URL[] {intraceFileUrl, swtFileUrl}, parent);
      
      try
      {
        // Check we can now load the SWT class
        Class.forName("org.eclipse.swt.widgets.Layout", true, cl);
      }
      catch (ClassNotFoundException exx)
      {
        System.err.println("Launch failed: Failed to load SWT class from jar: " + swtFileName);
        throw new RuntimeException(exx);
      }
      
      return cl;
    }
    catch (MalformedURLException exx)
    {
      throw new RuntimeException(exx);
    }                   
  }
  
  private static String getSwtJarName()
  {
    // Detect OS
    String osName = System.getProperty("os.name").toLowerCase();    
    String swtFileNameOsPart = osName.contains("win") ? "win" : osName
        .contains("mac") ? "osx" : osName.contains("linux")
        || osName.contains("nix") ? "linux" : "";
    if ("".equals(swtFileNameOsPart))
    {
      throw new RuntimeException("Launch failed: Unknown OS name: " + osName);
    }

    // Detect 32bit vs 64 bit
    String swtFileNameArchPart = getArch();

    String swtFileName = "swt-" + swtFileNameOsPart + swtFileNameArchPart
        + "-3.6.2.jar";
    return swtFileName;
  }
  
  private static String getArch()
  {
    // Detect 32bit vs 64 bit
    String jvmArch = System.getProperty("os.arch").toLowerCase();
    String arch = (jvmArch.contains("64") ? "64" : "32");
    return arch;
  }
}
