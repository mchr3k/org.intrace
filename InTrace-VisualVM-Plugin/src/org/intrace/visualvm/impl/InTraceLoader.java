/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.intrace.visualvm.impl;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.application.jvm.Jvm;
import com.sun.tools.visualvm.application.jvm.JvmFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.intrace.visualvm.Locator;
import org.openide.util.Exceptions;

/**
 *
 * @author mch50
 */
public class InTraceLoader {

    public static interface StatusHandler {

        public void handleStatus(String statusLine);
    }

    public static void loadAgent(Application app, StatusHandler handler) {
        Jvm jvm = JvmFactory.getJVMFor(app);
        if (app.isLocalApplication() && jvm.isAttachable() && jvm.isGetSystemPropertiesSupported()) {
            try {
                VirtualMachine vm = VirtualMachine.attach(String.valueOf(app.getPid()));
                handler.handleStatus("Attached to JVM (PID: " + app.getPid() + ")");
                vm.loadAgent(Locator.agentPath, "");
                handler.handleStatus("InTrace Agent loaded");
            } catch (Exception ex) {
                handler.handleStatus("Exception thrown:\n" + throwableToString(ex));
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public static void launchClient(final StatusHandler handler) {
        Runnable launchClientThread = new Runnable() {

            @Override
            public void run() {
                try {
                    List<String> command = new ArrayList<String>();
                    command.add("java");
                    command.add("-jar");
                    command.add(Locator.clientPath);

                    ProcessBuilder builder = new ProcessBuilder(command);

                    System.out.println("Directory : " + System.getenv("temp"));
                    builder.start();

                    handler.handleStatus("InTrace Client launched");
                } catch (Exception ex) {
                    handler.handleStatus("Exception thrown:\n" + throwableToString(ex));
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        new Thread(launchClientThread).start();
    }

    private static String throwableToString(Throwable throwable) {
        StringBuilder throwToStr = new StringBuilder();
        if (throwable == null) {
            throwToStr.append("null");
        } else {
            StringWriter strWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(strWriter);
            throwable.printStackTrace(writer);
            throwToStr.append(strWriter.toString());
        }
        return throwToStr.toString();
    }
}
