/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.intrace.visualvm.view;

import com.sun.tools.visualvm.core.ui.DataSourceView;
import com.sun.tools.visualvm.core.ui.components.DataViewComponent;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.intrace.visualvm.Locator;
import org.intrace.visualvm.impl.InTraceLoader;
import org.intrace.visualvm.impl.InTraceDataSource;
import org.openide.util.Utilities;

/**
 *
 * @author mch50
 */
public class InTraceView extends DataSourceView {

    private final InTraceDataSource application;

    private JTextArea textArea;
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
                InTraceLoader.loadAgent(application.app, new InTraceLoader.StatusHandler() {
                    @Override
                    public void handleStatus(String statusLine) {
                        appendText(statusLine);
                    }
                });
            }
        });

        JButton launchClient = new JButton("Launch InTrace Client");
        launchClient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                InTraceLoader.launchClient(new InTraceLoader.StatusHandler() {
                    @Override
                    public void handleStatus(String statusLine) {
                        appendText(statusLine);
                    }
                });
            }
        });

        JButton clearStatus = new JButton("Clear Status Text");
        clearStatus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearText();
            }
        });

        generalDataArea.add(loadAgentButton);
        generalDataArea.add(launchClient);
        generalDataArea.add(clearStatus);

        //Master view:
        DataViewComponent.MasterView masterView = new DataViewComponent.MasterView
                ("Control", null, generalDataArea);

        //Configuration of master view:
        DataViewComponent.MasterViewConfiguration masterConfiguration =
                new DataViewComponent.MasterViewConfiguration(false);

        //Add the master view and configuration view to the component:
        dvc = new DataViewComponent(masterView, masterConfiguration);

        //Add detail view to the component:

        JPanel statusArea = new JPanel();
        statusArea.setOpaque(false);
        statusArea.setLayout(new BorderLayout());

        textArea = new JTextArea(5, 20);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(
		JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setMargin(new Insets(10,10,10,10));

        statusArea.add(scrollPane, BorderLayout.CENTER);

        dvc.addDetailsView(new DataViewComponent.DetailsView(
          "Status", null, 10, statusArea, null), DataViewComponent.BOTTOM_LEFT);

        appendText("Agent Path: " + Locator.agentPath);
        appendText("Client Path: " + Locator.clientPath);
        if (Locator.clientPath == null) {
          appendText("Warning: The Client is only supported on Windows " +
                     "and Linux");
        }
        else {
          appendText("Ready");
        }

        return dvc;
    }

    private void appendText(String str) {
        if (textArea != null) {
          textArea.append(str + "\n");
        }
    }

    private void clearText() {
        if (textArea != null) {
          textArea.setText("");
        }
    }
}
