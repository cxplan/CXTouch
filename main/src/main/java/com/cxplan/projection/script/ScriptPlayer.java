package com.cxplan.projection.script;

import com.cxplan.projection.IApplication;
import com.cxplan.projection.core.connection.ConnectException;
import com.cxplan.projection.net.message.MessageException;
import com.cxplan.projection.script.command.ScriptCommand;
import com.cxplan.projection.script.command.ScriptException;
import com.cxplan.projection.script.io.ScriptDeviceConnection;
import com.cxplan.projection.ui.util.GUIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * @author Kenny
 * created on 2019/3/19
 */
public class ScriptPlayer {

    private static final Logger logger = LoggerFactory.getLogger(ScriptPlayer.class);

    private IApplication application;
    private ScriptObject script;
    private boolean stop;
    private int loopCount;//The times of executing script loop.
    private PlayThread playThread;

    public ScriptPlayer(IApplication application, ScriptObject script) {
        this.application = application;
        this.script = script;

    }

    public void play(String deviceId) {
        if (playThread != null && playThread.isAlive()) {
            logger.warn("The play thread is started.");
            return;
        }
        stop = false;
        playThread = new PlayThread(deviceId);
        playThread.start();
    }

    public boolean isPlaying() {
        return playThread != null && playThread.isAlive();
    }

    public void stop() {
        stop = true;
        if (playThread != null && playThread.isAlive() && !playThread.isInterrupted()) {
            playThread.interrupt();
        }
    }

    protected void doPlay(String deviceId) {
        if (script == null) {
            logger.error("The script is empty!");
            return;
        }

        //validate the script connection.
        ScriptDeviceConnection connection = application.getScriptConnection(deviceId);
        if (connection == null) {
            throw new RuntimeException("The Script connection is missing: " + deviceId);
        }
        try {
            connection.openChannel();
        } catch (ConnectException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        long startTime = System.currentTimeMillis();
        int count = 0;
        Iterator<ScriptCommand> it = script.getCommandList().iterator();
        while (!stop && it.hasNext()) {
            ScriptCommand sc = it.next();
            logger.info("Command: {}", sc.toString());

            //If a view node should be checked.
            if (sc.getViewNode() != null) {
                long timeout = 10000;
                int ret;
                try {
                    ret = application.getScriptService().waitForView(deviceId, sc.getViewNode(), timeout);
                } catch (MessageException e) {
                    logger.error(e.getMessage(), e);
                    GUIUtil.showErrorMessageDialog(e.getMessage());
                    break;
                }

                if (ret == 2) {
                    GUIUtil.showErrorMessageDialog("The key view is not found, the script will be broken!");
                    return;
                } else if (ret == 3) {
                    GUIUtil.showErrorMessageDialog("Waiting key view presented is timeout[" + timeout + "], the script will be broken!");
                    return;
                }
            } else {
                try {
                    Thread.sleep(800);
                } catch (InterruptedException e) {
                }

                long timeout = 4000;
                boolean ret;
                try {
                    ret = application.getScriptService().waitIdle(deviceId, timeout);
                } catch (MessageException e) {
                    logger.error(e.getMessage(), e);
                    GUIUtil.showErrorMessageDialog(e.getMessage());
                    break;
                }

                if (!ret) {
                    GUIUtil.showErrorMessageDialog("Waiting device idle is timeout[" + timeout + "].");
                    return;
                }
            }

            //execute monkey operation.
            try {
                sc.execute(application, deviceId);
            } catch (ScriptException e) {
                logger.error(e.getMessage(), e);
                GUIUtil.showErrorMessageDialog(e.getMessage());
                break;
            }

            count++;
        }

        long endTime = System.currentTimeMillis();
        logger.info("The script is finished, the count of executed commands : {}, The time of executing: {}ms",
                count, (endTime - startTime));
    }
    private class PlayThread extends Thread {

        private String deviceId;

        public PlayThread(String deviceId) {
            this.deviceId = deviceId;
        }

        public void run() {
            doPlay(deviceId);
        }
    }
}
