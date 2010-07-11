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
    public static final URL linuxClientPath;
    public static final URL winClientPath;

    static {
        File locatedFile = InstalledFileLocator.getDefault().locate("modules/ext/intrace-agent.jar", "com.sun.btrace", false); // NOI18N
        if (locatedFile != null) {
            agentPath = locatedFile.getAbsolutePath();
        } else {
            agentPath = null;
        }

        locatedFile = InstalledFileLocator.getDefault().locate("modules/ext/intrace-client-gui-win.jar", "com.sun.btrace", false); // NOI18N
        if (locatedFile != null) {
            URL lWinClientPath = null;
            try {
                lWinClientPath = locatedFile.toURI().toURL();
            } catch (MalformedURLException ex) {
                //
            }
            if (lWinClientPath != null) {
                winClientPath = lWinClientPath;
            } else {
                winClientPath = null;
            }
        } else {
            winClientPath = null;
        }

        locatedFile = InstalledFileLocator.getDefault().locate("modules/ext/intrace-client-gui-linux.jar", "com.sun.btrace", false); // NOI18N
        if (locatedFile != null) {
            URL lLinuxClientPath = null;
            try {
                lLinuxClientPath = locatedFile.toURI().toURL();
            } catch (MalformedURLException ex) {
                //
            }
            if (lLinuxClientPath != null) {
                linuxClientPath = lLinuxClientPath;
            } else {
                linuxClientPath = null;
            }
        } else {
            linuxClientPath = null;
        }
    }
}
