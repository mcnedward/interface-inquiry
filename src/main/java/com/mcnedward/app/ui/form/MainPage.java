package com.mcnedward.app.ui.form;

import com.mcnedward.app.InheritanceInquiry;
import com.mcnedward.app.ui.dialog.ExportGraphDialog;
import com.mcnedward.app.ui.dialog.ExportMetricFileDialog;
import com.mcnedward.app.ui.dialog.GitDialog;
import com.mcnedward.app.ui.dialog.ProjectFileDialog;
import com.mcnedward.app.ui.listener.GraphPanelListener;
import com.mcnedward.app.ui.listener.GraphRequestListener;
import com.mcnedward.ii.builder.GraphBuilder;
import com.mcnedward.ii.builder.MetricBuilder;
import com.mcnedward.ii.builder.ProjectBuilder;
import com.mcnedward.ii.element.JavaSolution;
import com.mcnedward.ii.listener.GraphExportListener;
import com.mcnedward.ii.listener.MetricExportListener;
import com.mcnedward.ii.listener.SolutionBuildListener;
import com.mcnedward.ii.service.graph.IGraphService;
import com.mcnedward.ii.service.graph.element.GraphOptions;
import com.mcnedward.ii.service.graph.jung.JungGraph;
import com.mcnedward.ii.service.metric.element.DitMetric;
import com.mcnedward.ii.service.metric.element.MetricOptions;
import com.mcnedward.ii.service.metric.element.NocMetric;
import com.mcnedward.ii.service.metric.element.WmcMetric;
import com.mcnedward.ii.utils.IILogger;
import com.mcnedward.ii.utils.ServiceFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Edward on 9/25/2016.
 */
public class MainPage implements MetricExportListener, GraphExportListener {

    private static final String HELP_CARD = "HelpCard";

    private static final String PROGRESS_CARD = "ProgressCard";
    private static final String SOLUTION_CARD = "SolutionCard";
    private JPanel mRoot;

    private JPanel mCards;
    private ProgressCard mMainProgressCard;
    private JButton mBtnFile;
    private JButton mBtnGit;
    private JPanel mHelpCard;
    private JPanel mTitlePanel;
    private JLabel mProjectName;
    private JLabel mProjectVersion;
    private JLabel mClassCount;
    private JLabel mInheritanceCount;
    private JMenuItem mSheetSettings;
    private JMenuItem mGraphSettings;
    private MetricPanel<DitMetric> mDitPanel;
    private MetricPanel<NocMetric> mNocPanel;
    private MetricPanel<WmcMetric> mWmcPanel;
    private FullHierarchyPanel mFullHierarchyPanel;

    private static ProjectBuilder mProjectBuilder;
    private JavaSolution mSolution;

    // Dialogs
    private static ProjectFileDialog mFileDialog;
    private static GitDialog mGitDialog;
    private ExportMetricFileDialog mExportMetricFileDialog;
    private ExportGraphDialog mExportGraphMetricDialog;
    private java.util.List<GraphRequestListener> mGraphListeners;

    public MainPage() {
        mProjectBuilder = new ProjectBuilder(new SolutionBuildListener() {
            public void finished(JavaSolution solution) {
                IILogger.info("Build complete!");
                loadSolution(solution);
                showCard(SOLUTION_CARD);
            }

            @Override
            public void onProgressChange(String message, int progress) {
                mMainProgressCard.update(message, progress);
            }

            @Override
            public void onBuildError(String message, Exception exception) {
                JOptionPane.showMessageDialog(null, message, "Build Error", JOptionPane.ERROR_MESSAGE);
                IILogger.error(exception);
                showCard(HELP_CARD);
            }
        });
        initMenu(InheritanceInquiry.PARENT_FRAME);
        initDialogs(InheritanceInquiry.PARENT_FRAME);
    }

    private void initMenu(JFrame parent) {
        JMenuBar menuBar = new JMenuBar();
        parent.setJMenuBar(menuBar);

        JMenu mnAnalyze = new JMenu("Analyze");
        menuBar.add(mnAnalyze);

        mFileDialog = new ProjectFileDialog(parent);
        JMenuItem mntmFromFile = new JMenuItem("From file");
        mntmFromFile.addActionListener(e -> openFileDialog());
        mnAnalyze.add(mntmFromFile);

        mGitDialog = new GitDialog(parent);
        JMenuItem mntmFromGit = new JMenuItem("From git");
        mnAnalyze.add(mntmFromGit);
        mntmFromGit.addActionListener(e -> openGitDialog());

        JMenu exportMenu = new JMenu("Export");
        menuBar.add(exportMenu);
        mGraphSettings = new JMenuItem("Export Metric Graphs");
        mGraphSettings.addActionListener(e -> openExportGraphDialog());
        mGraphSettings.setEnabled(false);
        exportMenu.add(mGraphSettings);
        mSheetSettings = new JMenuItem("Export Metric Sheets");
        mSheetSettings.addActionListener(e -> openExportMetricFileDialog());
        mSheetSettings.setEnabled(false);
        exportMenu.add(mSheetSettings);

        JMenu settingMenu = new JMenu("Settings");
        menuBar.add(settingMenu);
        JMenuItem fileSettings = new JMenuItem("Clear File Settings");
        fileSettings.addActionListener(e -> mFileDialog.clearPreference());
        settingMenu.add(fileSettings);
        JMenuItem gitSettings = new JMenuItem("Clear Git Settings");
        gitSettings.addActionListener(e -> GitDialog.clearPreference());
        settingMenu.add(gitSettings);
    }

    private void initDialogs(JFrame parent) {
        mExportMetricFileDialog = new ExportMetricFileDialog(parent);
        mExportGraphMetricDialog = new ExportGraphDialog(parent);
    }

    private void loadSolution(JavaSolution solution) {
        mSolution = solution;
        mTitlePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK));
        mProjectName.setText(solution.getProjectName());
        mProjectVersion.setText(solution.getVersion());
        int classCount = solution.getClassCount();
        int inheritanceCount = solution.getInheritanceCount();
        int percentage = (int) (((double) inheritanceCount / classCount) * 100);
        mClassCount.setText(String.format("Classes [%s]", classCount));
        mInheritanceCount.setText(String.format("Inheritance Use [%s%%]", percentage));
        mDitPanel.update(solution, ServiceFactory.ditGraphService(), solution.getDitMetricInfo(), solution.getDitMetrics());
        mNocPanel.update(solution, ServiceFactory.nocGraphService(), solution.getNocMetricInfo(), solution.getNocMetrics());
        mWmcPanel.update(solution, null, solution.getWmcMetricInfo(), solution.getWmcMetrics());
        mFullHierarchyPanel.update(solution, ServiceFactory.fullGraphService());

        mGraphSettings.setEnabled(true);
        mSheetSettings.setEnabled(true);
    }

    /**
     * Loads a file or directory.
     *
     * @param file The File to loadFile
     */
    private void loadFile(File file) {
        showCard(PROGRESS_CARD);
        mProjectBuilder.setup(file).build();
    }

    private void openFileDialog() {
        mFileDialog.setVisible(true);
        if (mFileDialog.isSuccessful()) {
            loadFile(mFileDialog.getDirectory());
        }
    }

    private void openGitDialog() {
        mGitDialog.setVisible(true);
        if (mGitDialog.isSuccessful()) {
            loadFile(mGitDialog.getGitFile());
        }
    }

    private void openExportMetricFileDialog() {
        mExportMetricFileDialog.setVisible(true);
        if (mExportMetricFileDialog.isSuccessful()) {
            MetricOptions options = new MetricOptions(mSolution, mExportMetricFileDialog.getDirectory(), mExportMetricFileDialog.exportDit(), mExportMetricFileDialog.exportNoc(), mExportMetricFileDialog.exportWmc(), mExportMetricFileDialog.exportFull());
            options.setUseCsvFormat(mExportMetricFileDialog.useCsvFormat());
            new MetricBuilder(this).setup(ServiceFactory.metricService(), options).build();
        }
    }

    private void openExportGraphDialog() {
        mExportGraphMetricDialog.setVisible(true);
        if (mExportGraphMetricDialog.isSuccessful()) {
            String projectName = mExportGraphMetricDialog.useProjectName() ? mSolution.getProjectName() : null;
            GraphOptions options = new GraphOptions(mExportGraphMetricDialog.getDirectory(), projectName, mExportGraphMetricDialog.usePackages());
            GraphBuilder builder = new GraphBuilder(this);
            // The graph service type doesn't matter here, since we're just doing an export
            IGraphService service = ServiceFactory.ditGraphService();
            for (GraphRequestListener listener : mGraphListeners) {
                Collection<JungGraph> graphs = listener.requestGraphs();
                builder.setupForExport(service, graphs, options).build();
            }
        }
    }

    private void showCard(String card) {
        ((CardLayout) mCards.getLayout()).show(mCards, card);
    }

    public JPanel getRoot() {
        return mRoot;
    }

    private void createUIComponents() {
        mBtnFile = new JButton("From File");
        mBtnFile.addActionListener(e -> openFileDialog());
        mBtnGit = new JButton("From Git");
        mBtnGit.addActionListener(e -> openGitDialog());

        mDitPanel = new MetricPanel<>();
        mNocPanel = new MetricPanel<>();
        mWmcPanel = new MetricPanel<>();
        mFullHierarchyPanel = new FullHierarchyPanel();

        // Register the panels as listeners
        mGraphListeners = new ArrayList<>();
        mGraphListeners.add(mDitPanel);
        mGraphListeners.add(mNocPanel);
        mGraphListeners.add(mFullHierarchyPanel);
    }

    @Override
    public void onProgressChange(String message, int progress) {
        IILogger.debug(message + " - " + progress);
    }

    @Override
    public void onBuildError(String message, Exception exception) {
        JOptionPane.showMessageDialog(null, message, "Metric Export Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void onMetricsExported() {
        JOptionPane.showMessageDialog(null, "Metric details exported!", "Metric Export Completed", JOptionPane.INFORMATION_MESSAGE);
    }

    private int mGraphExportCount;
    @Override
    public void onGraphsExport() {
        mGraphExportCount++;
        if (mGraphExportCount == mGraphListeners.size()) {
            mGraphExportCount = 0;
            JOptionPane.showMessageDialog(null, "Metric graphs exported!", "Metric Graph Export Completed", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
