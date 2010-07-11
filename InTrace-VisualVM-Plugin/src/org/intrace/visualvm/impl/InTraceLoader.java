/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.intrace.visualvm.impl;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.application.jvm.Jvm;
import com.sun.tools.visualvm.application.jvm.JvmFactory;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import org.intrace.visualvm.Locator;
import org.openide.util.Exceptions;

/**
 *
 * @author mch50
 */
public class InTraceLoader {

    public static void loadAgent(Application app) {
        Jvm jvm = JvmFactory.getJVMFor(app);
        if (app.isLocalApplication() && jvm.isAttachable() && jvm.isGetSystemPropertiesSupported()) {
            try {
                VirtualMachine vm = VirtualMachine.attach(String.valueOf(app.getPid()));
                vm.loadAgent(Locator.agentPath, "");
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public static void launchClient() {
        Runnable launchClientThread = new Runnable() {
            @Override
            public void run() {
                URLClassLoader classLoader = new URLClassLoader(new URL[]{Locator.winClientPath});
                try {
                    Class<?> guiClass = classLoader.loadClass("org.intrace.client.gui.TraceWindow");
                    Object guiObj = guiClass.newInstance();
                    Method guiOpen = guiClass.getMethod("open");
                    guiOpen.invoke(guiObj, new Object[0]);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        new Thread(launchClientThread).start();
    }
}
