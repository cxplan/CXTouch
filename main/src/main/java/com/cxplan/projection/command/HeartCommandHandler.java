package com.cxplan.projection.command;

import com.cxplan.projection.core.command.AbstractCommandHandler;
import com.cxplan.projection.core.connection.ClientConnection;
import com.cxplan.projection.net.message.Message;
import com.cxplan.projection.net.message.MessageException;
import com.cxplan.projection.net.message.MessageUtil;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author KennyLiu
 * @created on 2017/10/16
 */

public class HeartCommandHandler extends AbstractCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(HeartCommandHandler.class);

    public HeartCommandHandler() {
        super(MessageUtil.CMD_PING_HEART);
    }

    @Override
    public void process(IoSession session, Message message) throws MessageException {
        ClientConnection connection = getConnection(session);
    }
}
