package org.intrace.client.gui;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.intrace.client.gui.helper.Connection.ConnectState;
import org.intrace.client.gui.helper.InTraceUI;
import org.intrace.client.gui.helper.InTraceUI.IConnectionStateCallback;
import org.intrace.client.gui.helper.InTraceUI.UIMode;

public class InTraceStandaloneUI
{
  /**
   * @param args
   */
  public static void main(String[] args) throws IOException
  {    
    // Prepare window
    Display.setAppName("InTrace");
    final Shell window = new Shell();
    window.setSize(new Point(800, 800));
    window.setMinimumSize(new Point(800, 480));   
     
    // Load icons
    Display display = window.getDisplay();
    ClassLoader loader = InTraceStandaloneUI.class.getClassLoader();
    
    InputStream is16 = loader.getResourceAsStream(
                       "org/intrace/icons/intrace16.gif");    
    Image icon16 = new Image(display, is16);
    is16.close();
    
    InputStream is32 = loader.getResourceAsStream(
                       "org/intrace/icons/intrace32.gif");    
    Image icon32 = new Image(display, is32);
    is32.close();
    
    InputStream is48 = loader.getResourceAsStream(
                       "org/intrace/icons/intrace48.gif");    
    Image icon48 = new Image(display, is48);
    is48.close();

    InputStream is128 = loader.getResourceAsStream(
                        "org/intrace/icons/intrace128.png");    
    Image icon128 = new Image(display, is128);
    is128.close();
    
    window.setImages(new Image[] {icon16, icon32, icon48, icon128});
    
    // Do special OSX integration work
    doOSXWork();
    
    // Fill in UI
    InTraceUI ui = new InTraceUI(window, window, UIMode.STANDALONE);
    
    // Register title callback
    ui.setConnCallback(new IConnectionStateCallback()
    {      
      @Override
      public void setConnectionState(final ConnectState state)
      {
        if (!window.isDisposed())
        {
          window.getDisplay().syncExec(new Runnable()
          {            
            @Override
            public void run()
            {
              window.setText("InTrace UI: " + state.str);
            }
          });
        }
      }
    });
    
    // Open UI
    ui.open();
  }
  
  
  private static void doOSXWork()
  {
    String osName = System.getProperty("os.name").toLowerCase();    
    boolean isOSX = osName.contains("mac");
    if (isOSX)
    {
      System.setProperty("com.apple.mrj.application.apple.menu.about.name", "InTrace");
      
      try
      {
        // Load Apple class
        Class<?> appCls = Class.forName(
                            "com.apple.eawt.Application");
        Class<?> aboutHandlerCls = Class.forName("com.apple.eawt.AboutHandler");
        Class<?> prefHandlerCls = Class.forName("com.apple.eawt.PreferencesHandler");
        
        // Load methods
        Method getAppMthd = appCls.getMethod("getApplication", 
                                               (Class<?>[])null);
        Method setAboutHandlerMthd = appCls.getMethod("setAboutHandler", aboutHandlerCls);
        Method setPrefHandlerMthd = appCls.getMethod("setPreferencesHandler", prefHandlerCls);
        
        // Invoke methods
        Object app = getAppMthd.invoke(null, (Object[])null);
        setAboutHandlerMthd.invoke(app, new Object[] {null});
        setPrefHandlerMthd.invoke(app, new Object[] {null});
      }
      catch (Exception e)
      {
        System.out.println("Failed to load dock image: " + 
                           e.toString());
      }
    }
  }
}
