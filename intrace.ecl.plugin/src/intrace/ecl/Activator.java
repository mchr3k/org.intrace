package intrace.ecl;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "intrace.ecl"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	// Agent arg
	public String baseAgentArg = "";
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;	
		getJar();
	}
	
	private void getJar()
	{
	  Bundle b = getBundle();
	  try
    {
      File bundleDir = FileLocator.getBundleFile(b);
      File jarFile = new File(bundleDir, "lib/intrace-agent.jar");
      String absPath = jarFile.getAbsolutePath();
      baseAgentArg = " -javaagent:" + absPath;
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
