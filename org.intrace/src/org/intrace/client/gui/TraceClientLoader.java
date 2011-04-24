package org.intrace.client.gui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.jdt.internal.jarinjarloader.RsrcURLStreamHandlerFactory;

public class TraceClientLoader
{
  public static void main(String[] args) throws Throwable
  {
    ClassLoader cl = getSWTClassloader();    
    Thread.currentThread().setContextClassLoader(cl);
    try
    {
      Class<?> c = Class.forName("org.intrace.client.gui.TraceClient", true, cl);
      Method main = c.getMethod("main", new Class[]{args.getClass()});
      main.invoke((Object)null, new Object[]{args});
    }
    catch (ClassNotFoundException ex)
    {
      System.err.println("Failed to find main class - org.intrace.client.gui.TraceClient");
    }
    catch (NoSuchMethodException ex)
    {
      System.err.println("Failed to find main method");
    }
    catch (InvocationTargetException ex)
    {
      throw ex.getCause();
    }
  }

  private static ClassLoader getSWTClassloader()
  {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    try
    {
      // Check if SWT if already loaded
      Class.forName("org.eclipse.swt.widgets.Layout");
    }
    catch (ClassNotFoundException ex)
    {
      // SWT not available - we need to load the correct one      
      ClassLoader parent = cl;
      URL.setURLStreamHandlerFactory(new RsrcURLStreamHandlerFactory(cl));
      
      String swtFileName = getSwtJarName();      
      try
      {
        URL swtFileUrl = new URL("rsrc:" + swtFileName);
        cl = new URLClassLoader(new URL[] {swtFileUrl}, parent);
        
        try
        {
          // Check we can now load the SWT class
          Class.forName("org.eclipse.swt.widgets.Layout", true, cl);
        }
        catch (ClassNotFoundException exx)
        {
          System.err.println("Failed to load SWT jar: " + swtFileName);
          throw new RuntimeException(exx);
        }
      }
      catch (MalformedURLException exx)
      {
        throw new RuntimeException(exx);
      }                  
    }  
    return cl;    
  }
  
  private static String getSwtJarName()
  {
    String osName = System.getProperty("os.name").toLowerCase();
    String osArch = System.getProperty("os.arch").toLowerCase();
    String swtFileNameOsPart = osName.contains("win") ? "win" : osName
        .contains("mac") ? "osx" : osName.contains("linux")
        || osName.contains("nix") ? "linux" : "";
    if ("".equals(swtFileNameOsPart))
    {
      throw new RuntimeException("Unknown OS name: " + osName);
    }

    String swtFileNameArchPart = osArch.contains("64") ? "64" : "32";
    String swtFileName = "swt-" + swtFileNameOsPart + swtFileNameArchPart
        + "-3.6.2.jar";
    return swtFileName;
  }
}
