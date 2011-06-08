package org.intrace.client.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import javax.imageio.ImageIO;

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
    window.setImages(new Image[] {icon16, icon32});
    
    // Load a high res dock icon on OSX
    loadOSXDockImage();
    
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

  private static void loadOSXDockImage()
  {
    String osName = System.getProperty("os.name").toLowerCase();    
    boolean isOSX = osName.contains("mac");
    if (isOSX)
    {
      try
      {
        // Load Apple class
        Class<?> appClass = Class.forName(
                            "com.apple.eawt.Application");
        
        // Load methods
        Method getAppMthd = appClass.getMethod("getApplication", 
                                               (Class<?>[])null);
        Method setDockIconMthd = appClass.getMethod("setDockIconImage", 
                                                    java.awt.Image.class);
        
        // Load high res icon
        InputStream imgInput = InTraceStandaloneUI.class.getClassLoader().
                               getResourceAsStream("osxlogo.png");
        BufferedImage img = ImageIO.read(imgInput);
        
        // Invoke methods
        Object app = getAppMthd.invoke(null, (Object[])null);
        setDockIconMthd.invoke(app, img);
      }
      catch (Exception e)
      {
        System.out.println("Failed to load dock image: " + 
                           e.toString());
      }
    }
  }
}
