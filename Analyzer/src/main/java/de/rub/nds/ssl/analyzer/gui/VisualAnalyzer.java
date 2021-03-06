package de.rub.nds.ssl.analyzer.gui;

import de.rub.nds.ssl.analyzer.db.Database;
import de.rub.nds.ssl.analyzer.executor.EFingerprintTests;
import de.rub.nds.ssl.analyzer.executor.Launcher;
import de.rub.nds.ssl.analyzer.fingerprinter.ETLSImplementation;
import de.rub.nds.ssl.analyzer.gui.models.JTextAreaLog4JAppender;
import de.rub.nds.ssl.analyzer.gui.models.ScannerConfigurationData;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 * Jan 03, 2013
 */
public class VisualAnalyzer extends javax.swing.JFrame {

    private static final String PROPERTIES_FILE = "logging.properties";
    private static final String LINE_SEPARATOR = System.getProperty(
            "line.separator");
    private ScannerConfigurationData scannerConfigurationData;
    private DefaultComboBoxModel fuzzerConfigurationData;
    /**
     * Log4j logger initialization.
     */
    private static Logger logger = Logger.getRootLogger();

    /**
     * Creates new form VisualAnalyzer
     */
    public VisualAnalyzer() {
        scannerConfigurationData = new ScannerConfigurationData();
//        attackerConfigurationData = new AttackerConfigurationData();
        fuzzerConfigurationData = new DefaultComboBoxModel(ETLSImplementation.
                values());
        initComponents();
        logger.addAppender(new JTextAreaLog4JAppender(outputTextArea));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooser1 = new javax.swing.JFileChooser();
        errorDialog = new javax.swing.JDialog();
        errorDecoratorLabel = new javax.swing.JLabel();
        errorLabel = new javax.swing.JLabel();
        errorOKButton = new javax.swing.JButton();
        tabbedPane = new javax.swing.JTabbedPane();
        scannerConfiguration = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        scannerConfigurationTable = new javax.swing.JTable();
        fuzzerPanel = new javax.swing.JPanel();
        fuzzingLabel = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        createFingerprintButton = new javax.swing.JButton();
        outputPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        outputTextArea = new javax.swing.JTextArea();
        progressBar = new javax.swing.JProgressBar();
        openListButton = new javax.swing.JButton();
        targetListScrollPane = new javax.swing.JScrollPane();
        targetListTextArea = new javax.swing.JTextArea();
        scanTargetsButton = new javax.swing.JButton();
        attackTargetsButton = new javax.swing.JButton();
        targetListLabel = new javax.swing.JLabel();
        uncheckTestsButton = new javax.swing.JButton();

        errorDialog.setMinimumSize(new java.awt.Dimension(300, 120));
        errorDialog.setModal(true);
        errorDialog.setName("errorDialog"); // NOI18N

        errorDecoratorLabel.setText("An error occured:");

        errorOKButton.setText("OK");
        errorOKButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                errorOKButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout errorDialogLayout = new javax.swing.GroupLayout(errorDialog.getContentPane());
        errorDialog.getContentPane().setLayout(errorDialogLayout);
        errorDialogLayout.setHorizontalGroup(
            errorDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(errorDialogLayout.createSequentialGroup()
                .addGroup(errorDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(errorDialogLayout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(errorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(errorDialogLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(errorDecoratorLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(errorDialogLayout.createSequentialGroup()
                .addGap(94, 94, 94)
                .addComponent(errorOKButton)
                .addContainerGap(105, Short.MAX_VALUE))
        );
        errorDialogLayout.setVerticalGroup(
            errorDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(errorDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(errorDecoratorLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(errorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(errorOKButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("T.I.M.E. to look at SSL/TLS");
        setName("MainFrame"); // NOI18N

        tabbedPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        scannerConfigurationTable.setModel(scannerConfigurationData);
        scannerConfigurationTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        scannerConfigurationTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(scannerConfigurationTable);

        javax.swing.GroupLayout scannerConfigurationLayout = new javax.swing.GroupLayout(scannerConfiguration);
        scannerConfiguration.setLayout(scannerConfigurationLayout);
        scannerConfigurationLayout.setHorizontalGroup(
            scannerConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scannerConfigurationLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 751, Short.MAX_VALUE)
                .addContainerGap())
        );
        scannerConfigurationLayout.setVerticalGroup(
            scannerConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, scannerConfigurationLayout.createSequentialGroup()
                .addGap(0, 11, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        tabbedPane.addTab("Scanner Configuration", scannerConfiguration);

        fuzzingLabel.setText("Implementation of target");

        jComboBox1.setModel(fuzzerConfigurationData);

        createFingerprintButton.setText("Create fingerprint(s)");
        createFingerprintButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createFingerprintButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout fuzzerPanelLayout = new javax.swing.GroupLayout(fuzzerPanel);
        fuzzerPanel.setLayout(fuzzerPanelLayout);
        fuzzerPanelLayout.setHorizontalGroup(
            fuzzerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fuzzerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(fuzzerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(fuzzerPanelLayout.createSequentialGroup()
                        .addComponent(fuzzingLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox1, 0, 627, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, fuzzerPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(createFingerprintButton)))
                .addContainerGap())
        );
        fuzzerPanelLayout.setVerticalGroup(
            fuzzerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fuzzerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(fuzzerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fuzzingLabel)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(createFingerprintButton)
                .addContainerGap(167, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Implementation Fuzzing", fuzzerPanel);

        outputTextArea.setColumns(20);
        outputTextArea.setRows(5);
        jScrollPane3.setViewportView(outputTextArea);

        javax.swing.GroupLayout outputPanelLayout = new javax.swing.GroupLayout(outputPanel);
        outputPanel.setLayout(outputPanelLayout);
        outputPanelLayout.setHorizontalGroup(
            outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(outputPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 751, Short.MAX_VALUE)
                .addContainerGap())
        );
        outputPanelLayout.setVerticalGroup(
            outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, outputPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab("Results", outputPanel);

        openListButton.setLabel("Open list...");
        openListButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                openFileDialog(evt);
            }
        });

        targetListTextArea.setColumns(20);
        targetListTextArea.setRows(5);
        targetListTextArea.setText("https://127.0.0.1:8000");
        targetListTextArea.setToolTipText("[ PROTOCOL:// IP or DOMAIN (: PORT) ] - single line per target");
        targetListScrollPane.setViewportView(targetListTextArea);

        scanTargetsButton.setActionCommand("Fingerprint target(s)");
        scanTargetsButton.setLabel("Fingerprint target(s)");
        scanTargetsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scanTargetsButtonActionPerformed(evt);
            }
        });

        attackTargetsButton.setText("Attack target(s)");
        attackTargetsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attackTargetsButtonActionPerformed(evt);
            }
        });

        targetListLabel.setText("Please select target(s)");

        uncheckTestsButton.setText("Uncheck tests");
        uncheckTestsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uncheckTestsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(progressBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(targetListScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 284, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(openListButton, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                            .addComponent(attackTargetsButton, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                            .addComponent(scanTargetsButton, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                            .addComponent(uncheckTestsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(targetListLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(targetListLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(attackTargetsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scanTargetsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(uncheckTestsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(openListButton))
                    .addComponent(targetListScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 99, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void openFileDialog(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openFileDialog
        int result = jFileChooser1.showOpenDialog(this);

        try {
            // if file successfully selected
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = jFileChooser1.getSelectedFile();
                List<String> targets = parseTargetsFile(selectedFile);

                for (String tmpTarget : targets) {
                    targetListTextArea.append(tmpTarget);
                    targetListTextArea.append("\n");
                }
            }
        } catch (IOException e) {
            createErrorDialog("Error processing target file", e.getMessage());
        }
    }//GEN-LAST:event_openFileDialog

    private void errorOKButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_errorOKButtonActionPerformed
        errorDialog.setVisible(false);
    }//GEN-LAST:event_errorOKButtonActionPerformed

    private void attackTargetsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attackTargetsButtonActionPerformed
    }//GEN-LAST:event_attackTargetsButtonActionPerformed

    private void scanTargetsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scanTargetsButtonActionPerformed
        Object[][] conf = scannerConfigurationData.getConfiguration();
        String[] targets = getTargets();

        List<EFingerprintTests> selectedTests =
                new ArrayList<EFingerprintTests>(10);
        for (Object[] confSet : conf) {
            if ((Boolean) confSet[1]) {
                selectedTests.add((EFingerprintTests) confSet[2]);
            }
        }

        try {
            Launcher.startScan(targets, selectedTests.toArray(
                    new EFingerprintTests[selectedTests.size()]));
        } catch (ExecutionException e) {
            logger.error("Could not execute component.", e);

        } catch (InterruptedException e) {
            logger.error("Execution interrupted.", e);
        }
    }//GEN-LAST:event_scanTargetsButtonActionPerformed

    private void createFingerprintButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createFingerprintButtonActionPerformed
        String[] targets = getTargets();
        ETLSImplementation implementation =
                (ETLSImplementation) fuzzerConfigurationData.getSelectedItem();
        try {
            Launcher.startFuzzing(targets, implementation);
        } catch (ExecutionException e) {
            logger.error("Could not execute component.", e);

        } catch (InterruptedException e) {
            logger.error("Execution interrupted.", e);
        }
    }//GEN-LAST:event_createFingerprintButtonActionPerformed

    private void uncheckTestsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uncheckTestsButtonActionPerformed
        ScannerConfigurationData x = (ScannerConfigurationData)scannerConfigurationTable.getModel();
        for(int i = 0; i < x.getRowCount(); i++)
            x.setValueAt(false, i, 1);
        x.fireTableDataChanged();
    }//GEN-LAST:event_uncheckTestsButtonActionPerformed

    private void createErrorDialog(final String title, final String message) {
        errorDialog.setTitle(title);
        errorLabel.setText(message);
        errorDialog.setVisible(true);
    }

    private List<String> parseTargetsFile(final File targetsFile) throws
            IOException {
        FileReader fileReader = new FileReader(targetsFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        ArrayList<String> targets = new ArrayList<String>(20);
        String line = bufferedReader.readLine();
        while (line != null) {
            targets.add(line);
            line = bufferedReader.readLine();
        }

        return targets;
    }

    private String[] getTargets() {
        String lineSeparator = LINE_SEPARATOR;
        String[] targets = targetListTextArea.getText().split(lineSeparator);

        return targets;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(final String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.
                    getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(VisualAnalyzer.class.getName()).
                    log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(VisualAnalyzer.class.getName()).
                    log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(VisualAnalyzer.class.getName()).
                    log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(VisualAnalyzer.class.getName()).
                    log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        URL propertiesFile = VisualAnalyzer.class.getResource(PROPERTIES_FILE);
        PropertyConfigurator.configure(propertiesFile);
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new VisualAnalyzer().setVisible(true);
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    // remove any existing locks if necessary
                    Database.getInstance().shutdownDB();
                } catch (Exception e) {
                    // don't bother
                }
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton attackTargetsButton;
    private javax.swing.JButton createFingerprintButton;
    private javax.swing.JLabel errorDecoratorLabel;
    private javax.swing.JDialog errorDialog;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JButton errorOKButton;
    private javax.swing.JPanel fuzzerPanel;
    private javax.swing.JLabel fuzzingLabel;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JButton openListButton;
    private javax.swing.JPanel outputPanel;
    private javax.swing.JTextArea outputTextArea;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton scanTargetsButton;
    private javax.swing.JPanel scannerConfiguration;
    private javax.swing.JTable scannerConfigurationTable;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JLabel targetListLabel;
    private javax.swing.JScrollPane targetListScrollPane;
    private javax.swing.JTextArea targetListTextArea;
    private javax.swing.JButton uncheckTestsButton;
    // End of variables declaration//GEN-END:variables

    private static class RunnableImpl implements Runnable {

        public RunnableImpl() {
        }

        @Override
        public void run() {
            new VisualAnalyzer().setVisible(true);
        }
    }
}
