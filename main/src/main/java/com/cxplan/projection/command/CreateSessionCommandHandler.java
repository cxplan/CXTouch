package com.cxplan.projection.command;

import com.cxplan.projection.core.DefaultDeviceConnection;
import com.cxplan.projection.core.command.AbstractCommandHandler;
import com.cxplan.projection.core.connection.IDeviceConnection;
import com.cxplan.projection.net.message.Message;
import com.cxplan.projection.net.message.MessageException;
import com.cxplan.projection.net.message.MessageUtil;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kenny
 * created on 2019/1/9
 */
public class CreateSessionCommandHandler extends AbstractCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(CreateSessionCommandHandler.class);

    public CreateSessionCommandHandler() {
        super(MessageUtil.CMD_DEVICE_CREATE_SESSION);
    }

    @Override
    public void process(IoSession session, Message message) throws MessageException {
        IDeviceConnection connection = getConnection(session);
        if (connection instanceof DefaultDeviceConnection) {
            ((DefaultDeviceConnection) connection).prepareConnection(message);
        }
    }

}
