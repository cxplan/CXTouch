package com.cxplan.projection.core.image;

import com.cxplan.projection.IApplication;
import com.cxplan.projection.core.connection.IDeviceConnection;
import com.cxplan.projection.util.CommonUtil;
import com.cxplan.projection.util.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.InputStream;

/**
 * Created on 2017/4/6.
 *
 * @author kenny
 */
public class ImageProcessThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(ImageProcessThread.class);

    private IDeviceConnection connection;
    private boolean stop = false;
    private IApplication application;

    public ImageProcessThread(IDeviceConnection connection, IApplication application) {
        super("image_process_" + connection.getId());

        this.application = application;
        this.connection = connection;
    }

    public void startMonitor() {
        stop = false;
        start();
    }

    public void stopMonitor() {
        stop = true;
    }

    @Override
    public void run() {
        InputStream input;
        try {
            input = connection.getImageChannel().socket().getInputStream();
        } catch (Exception e) {
            logger.error("Retrieving video stream failed(" + connection.getId() + "): " + e.getMessage(), e);
            return;
        }

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("version: ").append(input.read());
            sb.append("\nlength: ").append(input.read());
            sb.append("\npid: ").append(CommonUtil.readIntLowEndian(input));
            int realWidth = CommonUtil.readIntLowEndian(input);
            int realHeight = CommonUtil.readIntLowEndian(input);
            sb.append("\nreal width: ").append(realWidth);
            sb.append("\nreal height: ").append(realHeight);
            sb.append("\nvirtual width: ").append(CommonUtil.readIntLowEndian(input));
            sb.append("\nvirtual height: ").append(CommonUtil.readIntLowEndian(input));
            sb.append("\nDisplay orientation: ").append(input.read());
            sb.append("\nQuirk bitflags: ").append(input.read());
            logger.info(sb.toString());

            byte[] buffer = new byte[2048];
            //get fire first frame
            //byte[] firstFrameData = extraFirstFrame(realWidth, realHeight);
            //the size of frame
            //ImageSessionManager.getInstance().fireImage(connection.getId(), CommonUtil.int2LowEndianBytes(firstFrameData.length), 4);
            //frame data.
            //.getInstance().fireImage(connection.getId(), firstFrameData, firstFrameData.length);

//            logger.info("The first frame is sent to nodes: " + firstFrameData.length);

            while (!stop && connection.isOnline()) {
                int count = input.read(buffer);
                if (count == -1) {
                    break;
                }
                boolean hasNode = ImageSessionManager.getInstance().fireImage(connection.getId(), buffer, count);
                if (!hasNode) {

                    break;
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            logger.warn("The image server is over: " + connection.getId());
            connection.closeImageChannel();
        }

    }

}
