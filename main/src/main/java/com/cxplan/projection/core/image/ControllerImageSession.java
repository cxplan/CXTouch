package com.cxplan.projection.core.image;

import com.cxplan.projection.core.Application;
import com.cxplan.projection.core.connection.IDeviceConnection;
import com.cxplan.projection.util.CommonUtil;
import com.cxplan.projection.util.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * @author Kenny
 * created on 2018/11/22
 */
public class ControllerImageSession extends AbstractImageSession {

    private static final Logger logger = LoggerFactory.getLogger(ControllerImageSession.class);

    private IDeviceConnection deviceConnection;
    private ImageParserThread parserThread;
    private PipedOutputStream outputStream;

    public ControllerImageSession(String deviceId) {
        super(new ImageSessionID(ImageSessionID.TYPE_CONTROLLER, deviceId));
        deviceConnection = Application.getInstance().getDeviceConnection(deviceId);
        if (deviceConnection == null) {
            throw new RuntimeException("The device connection doesn't exist: " + deviceId);
        }

        outputStream = new PipedOutputStream();
        parserThread = new ImageParserThread();
        parserThread.start();
    }

    @Override
    public void writeImageData(byte[] data, int offset, int size) {
        if (!parserThread.isAlive() || parserThread.stop) {
            throw new RuntimeException("The parser thread is not running.");
        }
        try {
            outputStream.write(data, offset, size);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        if (parserThread != null) {
            parserThread.toStop();
        }
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
            }
        }
    }

    private class ImageParserThread extends Thread {

        private boolean stop;
        private PipedInputStream inputStream;

        public ImageParserThread() {
            inputStream = new PipedInputStream();
            stop = true;
        }

        @Override
        public void start() {
            stop = false;
            try {
                inputStream.connect(outputStream);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            super.start();
        }

        public void toStop() {
            stop = true;
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        }

        public void run() {
            byte[] frameData = null;
            int frameSize;
            try {
                while (!stop) {
                    frameSize = CommonUtil.readIntLowEndian(inputStream);
                    if (frameData == null || frameSize > frameData.length) {
                        frameData = new byte[frameSize];
                    }
                    for (int i = 0; i < frameSize; i++) {
                        frameData[i] = (byte) inputStream.read();
                    }

                    if (frameSize <= 0) {
                        logger.error("Reading frame size failed: " + frameSize);
                        break;
                    }
                    Image src = ImageUtil.readImage(frameData, 0, frameSize);
                    if (!Application.getInstance().fireOnDeviceImageEvent(deviceConnection, src)) {//there is no image consumer, the image channel should be closed.
                        logger.info("There is no image consumer, the image channel should be closed. device: {}", getSessionID().getId());
                        break;
                    }
                }
            } catch (Exception ex) {
                stop = true;
                logger.error("Parser thread will be ended: " + ex.getMessage());
            }
            try {
                inputStream.close();
            } catch (IOException e) {
            }
            logger.info("The image parser Thread is over: " + deviceConnection.getId());
        }
    }
}
