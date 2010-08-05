/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.intrace.visualvm.actions;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.application.jvm.Jvm;
import com.sun.tools.visualvm.application.jvm.JvmFactory;
import com.sun.tools.visualvm.core.ui.DataSourceWindowManager;
import com.sun.tools.visualvm.core.ui.actions.SingleDataSourceAction;
import java.awt.event.ActionEvent;
import org.intrace.visualvm.impl.InTraceDataSource;
import org.openide.util.NbBundle;

/**
 *
 * @author mch50
 */
public class InTraceApplicationAction extends SingleDataSourceAction<Application> {
    
    @Override
    protected void actionPerformed(Application app, ActionEvent actionEvent) {
        InTraceDataSource dataSource = new InTraceDataSource(app);
        DataSourceWindowManager.sharedInstance().openDataSource(dataSource);
    }

    @Override
    protected boolean isEnabled(Application app) {
        Jvm jvm = JvmFactory.getJVMFor(app);
        return app.isLocalApplication() && jvm.isAttachable() && jvm.isGetSystemPropertiesSupported();
    }

    public static synchronized InTraceApplicationAction newInstance() {
        return new InTraceApplicationAction();
    }

    private InTraceApplicationAction() {
        super(Application.class);
        putValue(NAME, NbBundle.getMessage(InTraceApplicationAction.class, "InTraceApplicationAction.title")); // NOI18N
    }
}
