package com.cxplan.projection.command;

import com.cxplan.projection.core.command.AbstractCommandHandler;
import com.cxplan.projection.core.connection.IDeviceConnection;
import com.cxplan.projection.net.message.Message;
import com.cxplan.projection.net.message.MessageException;
import com.cxplan.projection.net.message.MessageUtil;
import com.cxplan.projection.ui.DeviceImageFrame;
import org.apache.mina.core.session.IoSession;

/**
 * @author Kenny
 * created on 2018/12/14
 */
public class ImageChannelCommandHandler extends AbstractCommandHandler {

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
        DeviceImageFrame instance = DeviceImageFrame.getInstance(connection.getId(), application, false);
        if (instance == null) {
            return;
        }

        instance.waitImageChannelChanged();
    }
    private void processImageConfigFinished(IDeviceConnection connection, Message message) {
        DeviceImageFrame instance = DeviceImageFrame.getInstance(connection.getId(), application, false);
        if (instance == null) {
            return;
        }

        instance.openImageChannel();
    }
}
