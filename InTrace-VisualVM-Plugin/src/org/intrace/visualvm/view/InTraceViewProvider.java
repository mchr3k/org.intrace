/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.intrace.visualvm.view;

import com.sun.tools.visualvm.core.ui.DataSourceView;
import com.sun.tools.visualvm.core.ui.DataSourceViewProvider;
import com.sun.tools.visualvm.core.ui.DataSourceViewsManager;
import org.intrace.visualvm.impl.InTraceDataSource;

/**
 *
 * @author mch50
 */
public class InTraceViewProvider extends DataSourceViewProvider<InTraceDataSource> {

    private static DataSourceViewProvider<InTraceDataSource> instance =  new InTraceViewProvider();

    @Override
    public boolean supportsViewFor(InTraceDataSource application) {
        //Always shown:
        return true;
    }

    @Override
    public synchronized DataSourceView createView(final InTraceDataSource application) {
        return new InTraceView(application);
    }

    public static void initialize() {
        DataSourceViewsManager.sharedInstance().addViewProvider(instance, InTraceDataSource.class);
    }

    static void unregister() {
        DataSourceViewsManager.sharedInstance().removeViewProvider(instance);
    }
}
