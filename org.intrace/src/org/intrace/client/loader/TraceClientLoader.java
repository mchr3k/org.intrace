package org.intrace.client.loader;

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
    ClassLoader cl = getSWTClassloader(false);
    Thread.currentThread().setContextClassLoader(cl);    
    try
    {
      try
      {
        System.err.println("Launching InTrace UI");
        Class<?> c = Class.forName("org.intrace.client.gui.TraceClient", true, cl);
        Method main = c.getMethod("main", new Class[]{args.getClass()});
        main.invoke((Object)null, new Object[]{args});
      }
      catch (InvocationTargetException ex)
      {
        if (ex.getCause() instanceof UnsatisfiedLinkError)
        {
          System.err.println("Launch failed (UnsatisfiedLinkError), try other bitness");
          cl = getSWTClassloader(true);
          Thread.currentThread().setContextClassLoader(cl);
          System.err.println("Launching InTrace UI");
          Class<?> c = Class.forName("org.intrace.client.gui.TraceClient", true, cl);
          Method main = c.getMethod("main", new Class[]{args.getClass()});
          main.invoke((Object)null, new Object[]{args});
        }
        else
        {
          throw ex;
        }
      }
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
      Throwable th = ex.getCause();
      if ((th.getMessage() != null) &&
          th.getMessage().toLowerCase().contains("invalid thread access"))
      {
        System.err.println("If you are running on OSX, try restarting with " + 
            "command line option -XstartOnFirstThread");
      }
      else
      {
        throw th;
      }
    }
  }

  private static ClassLoader getSWTClassloader(boolean xiInvertArch)
  {
    ClassLoader parent = Thread.currentThread().getContextClassLoader();
    URL.setURLStreamHandlerFactory(new RsrcURLStreamHandlerFactory(parent));
    
    String swtFileName = getSwtJarName(xiInvertArch);      
    try
    {
      URL intraceFileUrl = new URL("rsrc:intrace-ui-wrapper.jar");
      URL swtFileUrl = new URL("rsrc:" + swtFileName);
      System.err.println("Using SWT Jar: " + swtFileUrl);
      ClassLoader cl = new URLClassLoader(new URL[] {intraceFileUrl, swtFileUrl}, parent);
      
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
      
      return cl;
    }
    catch (MalformedURLException exx)
    {
      throw new RuntimeException(exx);
    }                   
  }
  
  private static String getSwtJarName(boolean xiInvertArch)
  {
    // Detect OS
    String osName = System.getProperty("os.name").toLowerCase();    
    String swtFileNameOsPart = osName.contains("win") ? "win" : osName
        .contains("mac") ? "osx" : osName.contains("linux")
        || osName.contains("nix") ? "linux" : "";
    if ("".equals(swtFileNameOsPart))
    {
      throw new RuntimeException("Unknown OS name: " + osName);
    }

    // Detect 32bit vs 64 bit
    String jvmArch = System.getProperty("os.arch").toLowerCase();
    String swtFileNameArchPart = (jvmArch.contains("64") ? "64" : "32");
    if (xiInvertArch)
    {
      if ("64".equals(swtFileNameArchPart))
      {
        swtFileNameArchPart = "32";
      }
      else if ("32".equals(swtFileNameArchPart))
      {
        swtFileNameArchPart = "64";
      }
    }
    String swtFileName = "swt-" + swtFileNameOsPart + swtFileNameArchPart
        + "-3.6.2.jar";
    return swtFileName;
  }
}
