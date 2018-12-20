package com.cxplan.projection.command;

import com.cxplan.projection.core.command.AbstractCommandHandler;
import com.cxplan.projection.core.connection.IDeviceConnection;
import com.cxplan.projection.net.message.Message;
import com.cxplan.projection.net.message.MessageException;
import com.cxplan.projection.net.message.MessageUtil;
import com.cxplan.projection.ui.DeviceImageFrame;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kenny
 * created on 2018/12/14
 */
public class ImageChannelCommandHandler extends AbstractCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(ImageChannelCommandHandler.class);

    public ImageChannelCommandHandler() {
        super(MessageUtil.CMD_CONTROLLER_IMAGE);
    }

    @Override
    public void process(IoSession session, Message message) throws MessageException {
        IDeviceConnection connection = getConnection(session);
        short type = message.getParameter("type");
        switch (type) {
            case 1:
                processImageConfigChange(connection, message);
                break;
            case 2:
                processImageConfigFinished(connection, message);
                break;
            default:
                throw new MessageException("Unsupported type: " + type);
        }
    }

    private void processImageConfigChange(IDeviceConnection connection, Message message) {
        //update rotation.
        short rotation = message.getParameter("ro");
        logger.info("new rotation: {}" , rotation);
        connection.setRotation(rotation);

        DeviceImageFrame instance = DeviceImageFrame.getInstance(connection.getId(), application, false);
        if (instance == null) {
            return;
        }
        String msg = message.getParameter("msg");
        instance.waitImageChannelChanged(msg);
        logger.info("The device({}) environment is changed: [{}], the image channel will be closed", connection.getId(), msg);

        connection.closeImageChannel();
    }
    private void processImageConfigFinished(IDeviceConnection connection, Message message) {
        logger.info("The device({}) environment is prepared!", connection.getId());

        DeviceImageFrame instance = DeviceImageFrame.getInstance(connection.getId(), application, false);
        if (instance == null) {
            return;
        }
        instance.openImageChannel();
    }
}
