/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.intrace.visualvm.impl;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.core.datasource.DataSource;

/**
 *
 * @author mch50
 */
public class InTraceDataSource extends DataSource {

    public final Application app;

    public InTraceDataSource(Application app) {
        super(app);
        this.app = app;
    }
}
