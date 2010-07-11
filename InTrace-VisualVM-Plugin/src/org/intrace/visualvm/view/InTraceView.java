/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.intrace.visualvm.view;

import com.sun.tools.visualvm.core.ui.DataSourceView;
import com.sun.tools.visualvm.core.ui.components.DataViewComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.intrace.visualvm.impl.InTraceLoader;
import org.intrace.visualvm.impl.InTraceDataSource;
import org.openide.util.Utilities;

/**
 *
 * @author mch50
 */
public class InTraceView extends DataSourceView {

    private final InTraceDataSource application;

    private DataViewComponent dvc;

    //Reusing an image from the sources:
    private static final String IMAGE_PATH = "com/sun/tools/visualvm/coredump/resources/coredump.png"; // NOI18N

    public InTraceView(InTraceDataSource application) {
        super(application, "InTrace", new ImageIcon(Utilities.loadImage(IMAGE_PATH, true)).getImage(), 60, false);
        this.application = application;
    }

    @Override
    protected DataViewComponent createComponent() {

        //Data area for master view:
        JPanel generalDataArea = new JPanel();
        generalDataArea.setBorder(BorderFactory.createEmptyBorder(14, 8, 14, 8));
        generalDataArea.setOpaque(false);

        JButton loadAgentButton = new JButton("Load InTrace Agent");
        loadAgentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                InTraceLoader.loadAgent(application.app);
            }
        });

        JButton launchClient = new JButton("Launch InTrace Client");
        launchClient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                InTraceLoader.launchClient();
            }
        });

        generalDataArea.add(loadAgentButton);
        generalDataArea.add(launchClient);

        //Master view:
        DataViewComponent.MasterView masterView = new DataViewComponent.MasterView
                ("InTrace", null, generalDataArea);

        //Configuration of master view:
        DataViewComponent.MasterViewConfiguration masterConfiguration =
                new DataViewComponent.MasterViewConfiguration(false);

        //Add the master view and configuration view to the component:
        dvc = new DataViewComponent(masterView, masterConfiguration);

        return dvc;

    }

}
