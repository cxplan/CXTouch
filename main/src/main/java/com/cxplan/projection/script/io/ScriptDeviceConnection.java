package com.cxplan.projection.script.io;

import com.android.ddmlib.IDevice;
import com.cxplan.projection.IApplication;
import com.cxplan.projection.core.Application;
import com.cxplan.projection.core.BaseDeviceConnection;
import com.cxplan.projection.core.adb.ForwardManager;
import com.cxplan.projection.core.connection.ConnectException;
import com.cxplan.projection.net.DeviceIoHandlerAdapter;
import com.cxplan.projection.net.message.*;
import com.cxplan.projection.script.ScriptObject;
import com.cxplan.projection.script.ScriptPlayer;
import com.cxplan.projection.script.ScriptRecorder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * @author Kenny
 * created on 2019/3/21
 */
public class ScriptDeviceConnection extends BaseDeviceConnection {

    private static final Logger logger = LoggerFactory.getLogger(ScriptDeviceConnection.class);

    private IApplication application;
    private IDevice device;
    private ScriptRecorder recorder;
    private ScriptPlayer player;

    private volatile boolean isConnecting;

    public ScriptDeviceConnection(String id, IDevice device, IApplication application) {
        setId(new JID(id, JID.Type.DEVICE));
        this.application = application;
        this.device = device;
        recorder = new ScriptRecorder(getJId().getId(), application);
        isConnecting = false;
    }

    public void playRecorder() {
        if (recorder == null) {
            throw new RuntimeException("There is no recorder found");
        }
        if (recorder.isRecording()) {
            throw new RuntimeException("The recorder is running, the recorder should be stopped before play it.");
        }

        if (player != null) {
            player.stop();
        }
        ScriptObject script = new ScriptObject(recorder.getCommandList());
        player = new ScriptPlayer(application, script);
        player.play(getJId().getId());
    }

    /**
     * Execute a script on this device.
     * A runtime exception will be thrown if another script is running on this device.
     *
     * @param script script object including some commands.
     */
    public void playScript(ScriptObject script) {
        if (player == null) {
            player = new ScriptPlayer(application, script);
        } else {
            if (player.isPlaying()) {
                throw new RuntimeException("There is a script running on this device already.");
            }
        }

        player.play(getJId().getId());
    }

    /**
     * Validate the script service, including actions below:
     * 1. check whether the application is installed.
     * 2. start service process.
     * 3. connect to script service.
     *
     * @throws ConnectException
     */
    public void openChannel() throws ConnectException {
        if (isConnected()) {
            return;
        }

        //start and connect to service.
        connect();
    }

    @Override
    public void close() {
        super.close();
        Application.getInstance().fireOnScriptConnectionClosedEvent(this);
    }

    @Override
    public void connect() throws ConnectException {
        if (isConnected() || isConnecting) {
            return;
        }

        isConnecting = true;
        try {
            //1. check script service process.
            if (!application.getScriptService().startScriptService(device)) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                }
            }

            //2. check port forwarding.
            int port;
            String host = "localhost";
            port = getScriptForwardPort();

            logger.info("script port: " + port);
            //ensure the forward available.
            try {
                device.createForward(port, ForwardManager.SCRIPT_REMOTE_PORT);
            } catch (Exception e) {
                throw new ConnectException("Forwarding port for script failed: local port=" + port
                        + ", remote port=" + ForwardManager.SCRIPT_REMOTE_PORT, e);
            }

            //3. connect to script service.
            NioSocketConnector connector = Application.getInstance().getScriptDeviceConnector();
            ConnectFuture connFuture = connector.connect(new InetSocketAddress(host, port));
            connFuture.awaitUninterruptibly();

            while (!connFuture.isDone() && !connFuture.isConnected()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }
            if (!connFuture.isConnected()) {
                connFuture.cancel();
                String errorMsg = "Connecting to script server is timeout: host:" + host + ", port=" + port;
                logger.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            String deviceId = getJId().getId();
            messageSession = connFuture.getSession();
            if (messageSession.isConnected()) {
                messageSession.setAttribute(DeviceIoHandlerAdapter.CLIENT_SESSION, this);
                messageSession.setAttribute(DeviceIoHandlerAdapter.CLIENT_ID, getJId());
                logger.info("Connect to script server({}) on port({}) successfully!", deviceId, port);
            } else {
                logger.error("Connecting to script server({}) on port({}) failed", deviceId, port);
                return;
            }

            //4. initialize session.
            int tryCount = 0;
            while (tryCount < 4) {
                Message createMsg = new Message(MessageUtil.CMD_DEVICE_CREATE_SESSION);
                try {
                    Message retMsg = MessageUtil.request(this, createMsg, 2000);
                    break;
                } catch (MessageException e) {
                    if (e instanceof MessageTimeoutException || messageSession == null) {
                        tryCount++;
                        continue;
                    }
                    if (messageSession != null) {
                        messageSession.closeNow();
                    }
                    throw new ConnectException(e.getMessage(), e);
                }
            }

            logger.info("Connect to script server successfully!");
            Application.getInstance().fireOnScriptConnectedEvent(this);
        } finally {
            isConnecting = false;
        }
    }

    public void startRecord() {
        try {
            openChannel();
        } catch (ConnectException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        if (recorder == null) {
            recorder = new ScriptRecorder(getJId().getId(), application);
        }
        recorder.start();
    }

    public void stopRecord() {
        if (recorder != null) {
            recorder.stop();
        }
    }

    public ScriptRecorder getScriptRecorder() {
        return recorder;
    }

    public boolean isRecording() {
        return recorder != null && recorder.isRecording();
    }


    public int getScriptForwardPort() {
        return ForwardManager.getInstance().putScriptForward(getJId().getId());
    }

}
