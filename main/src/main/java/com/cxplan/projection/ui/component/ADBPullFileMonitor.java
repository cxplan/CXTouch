package com.cxplan.projection.ui.component;

import com.alee.laf.label.WebLabel;
import com.alee.laf.progressbar.WebProgressBar;
import com.android.ddmlib.SyncService;
import com.cxplan.projection.ui.util.GUIUtil;
import com.jidesoft.swing.ResizableWindow;

import javax.swing.*;
import java.awt.*;

/**
 * @author Kenny
 * created on 2018/12/26
 */
public class ADBPullFileMonitor extends ResizableWindow implements SyncService.ISyncProgressMonitor {

    private WebProgressBar progressBar;
    private WebLabel titleText;
    private int preferredProgressWidth;
    private int totalWork = 0;

    private int currentWork = 0;

    public ADBPullFileMonitor(Window parent, int preferredProgressWidth) {
        super(parent);
        this.preferredProgressWidth = preferredProgressWidth;
        initView();
        pack();
    }

    private void initView() {
        JPanel contentPane = (JPanel) getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));

        //title label
        titleText = new WebLabel(" ");
        contentPane.add(titleText, BorderLayout.NORTH);
        //progress bar
        progressBar = new WebProgressBar() {
            @Override
            public Dimension getPreferredSize ()
            {
                final Dimension ps = super.getPreferredSize ();
                if ( preferredProgressWidth > 0 )
                {
                    ps.width = preferredProgressWidth;
                }
                return ps;
            }
        };
        progressBar.setStringPainted(true);
        progressBar.setMaximum(100);
        progressBar.setMinimum(0);
        contentPane.add(progressBar, BorderLayout.CENTER);
    }

    public int getTotalWork() {
        return totalWork;
    }

    public void setTotalWork(int totalWork) {
        this.totalWork = totalWork;
    }

    @Override
    public void start(final int totalWork) {
        if (totalWork > 0) {
            this.totalWork = totalWork;
        }
        titleText.setText("Start downloading video file...");
        GUIUtil.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setMaximum(ADBPullFileMonitor.this.totalWork);
                progressBar.setValue(0);
            }
        });

    }

    @Override
    public void stop() {
        removeAll();
        dispose();
        GUIUtil.showInfoMessageDialog("Downloading file is finished!");
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public void startSubTask(final String name) {
        GUIUtil.invokeLater(new Runnable() {
            @Override
            public void run() {
                titleText.setText("Sub Task: " + name);
            }
        });
    }

    @Override
    public void advance(final int work) {
        currentWork += work;
        GUIUtil.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setValue(currentWork);
            }
        });
    }
}
