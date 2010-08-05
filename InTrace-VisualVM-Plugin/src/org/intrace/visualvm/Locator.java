/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.intrace.visualvm;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author mch50
 */
public class Locator {

    public static final String agentPath;
    public static final String clientPath;

    static {
        File locatedFile = InstalledFileLocator.getDefault().locate("modules/ext/intrace-agent.jar", null, false); // NOI18N
        if (locatedFile != null) {
            agentPath = locatedFile.getAbsolutePath();
        } else {
            agentPath = null;
        }

        String osName = System.getProperty("os.name", "unknown");
        if (osName.contains("Windows")) {
            locatedFile = InstalledFileLocator.getDefault().locate("modules/ext/intrace-client-gui-win.jar", null, false); // NOI18N
            if (locatedFile != null) {
                clientPath = locatedFile.getAbsolutePath();
            } else {
                clientPath = null;
            }
        }
        else if (osName.contains("Linux")) {
            locatedFile = InstalledFileLocator.getDefault().locate("modules/ext/intrace-client-gui-linux.jar", null, false); // NOI18N
            if (locatedFile != null) {
                clientPath = locatedFile.getAbsolutePath();
            } else {
                clientPath = null;
            }
        }
        else {
            clientPath = null;
        }
    }
}
