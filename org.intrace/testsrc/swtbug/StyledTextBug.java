package swtbug;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class StyledTextBug
{
  private final Shell window;

  public StyledTextBug(Shell xiWindow)
  {
    window = xiWindow;
    
    window.setLayout(new FillLayout());
    
    StyledText text = new StyledText(window, SWT.MULTI | SWT.V_SCROLL
                                    | SWT.H_SCROLL | SWT.BORDER);
    
    long startTime = System.currentTimeMillis();
    long intervalStartTime = System.currentTimeMillis();
    for (int line = 0; line < 100; line++)
    {
      int[] data = new int[100000];
      String dataStr = Arrays.toString(data) + "\n";
      // dataStr.length() = (100000 * 3) + 1 = 300,001
      text.append(dataStr);
      
      if ((line % 10) == 0)
      {
        long nowTime = System.currentTimeMillis();
        long elapsed = nowTime - startTime;
        long intervalElapsed = nowTime - intervalStartTime;
        System.out.println("Written " + line + " lines after " + elapsed + "ms (total), " + 
                           intervalElapsed + "ms (interval)");
        intervalStartTime = nowTime;
      }
    }
    System.out.println("All lines written");
  }

  public static void main(String[] args)
  {
    final Shell window = new Shell();
    window.setSize(new Point(400, 400));   
    
    // Fill in UI
    StyledTextBug ui = new StyledTextBug(window);
    
    // Open UI
    ui.open();
  }
  

  public void open()
  {
    System.out.println("UI ready - now try scrolling vertically");
    window.open();
    Display display = Display.getDefault();
    while (!window.isDisposed())
    {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }
}
