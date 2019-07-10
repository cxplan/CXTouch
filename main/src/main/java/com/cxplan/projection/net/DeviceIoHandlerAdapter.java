package com.cxplan.projection.net;

import com.cxplan.projection.IApplication;
import com.cxplan.projection.core.BaseDeviceConnection;
import com.cxplan.projection.core.DefaultDeviceConnection;
import com.cxplan.projection.core.command.CommandHandlerFactory;
import com.cxplan.projection.core.command.ICommandHandler;
import com.cxplan.projection.net.message.Message;
import com.cxplan.projection.net.message.MessageException;
import com.cxplan.projection.net.message.MessageUtil;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on 2018/5/18.
 *
 * @author kenny
 */
public class DeviceIoHandlerAdapter extends BaseIoHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(DeviceIoHandlerAdapter.class);

    private IApplication application;

    public DeviceIoHandlerAdapter(IApplication application) {
        this.application = application;
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        if (!(message instanceof Message)) {
            throw new MessageException("The message format is illegal: " + message.getClass());
        }
        Message msg = (Message) message;

        if (MessageUtil.CMD_PING_HEART.equals(msg.getCommand())) {
            logger.debug("received a heart command");
        } else{
            logger.info("received command: {}", msg.getCommand());
        }

        //unresponsive command has some errors.
        if (msg.getError() != null) {
            logger.error("Executing command failed: cmd=" + msg.getCommand() + ",id=" + msg.getId()
                    + ",error=" + msg.getError() );
            return;
        }

        BaseDeviceConnection connection = (BaseDeviceConnection) session.getAttribute(CLIENT_SESSION);
        //2. Then span message collector.
        if (connection == null) {
            logger.error("The session is not initialized, but received a message:" + msg.getCommand());
            return;
        }

        // Loop through all collectors and notify the appropriate ones.
        boolean ret = connection.visitMessageCollectors(msg);
        if (ret) {
            return;
        }

        //3. transfer this message to controller.
        ICommandHandler handler = CommandHandlerFactory.getHandler(msg.getCommand());
        try {
            if (handler == null) {
                throw new RuntimeException("The handler is missing: " + msg.getCommand() + ",id=" + msg.getId());
            }
            handler.process(session, msg);
        } catch (Exception e) {
            exceptionCaught(session, e);
            return;
        }

    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        DefaultDeviceConnection connection = (DefaultDeviceConnection) session.getAttribute(CLIENT_SESSION);
        if (connection != null) {
            session.removeAttribute(CLIENT_SESSION);
            if (connection == application.getDeviceConnection(connection.getId())) {
                connection.close();
            }
        }
    }

    public void sessionCreated(IoSession session) throws Exception {
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        // Empty handler
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        int idleCount = session.getIdleCount(status);
        if (idleCount == 1) {
            DefaultDeviceConnection cd = (DefaultDeviceConnection)session.getAttribute(CLIENT_SESSION);
            Message pingMsg = new Message(MessageUtil.CMD_PING);
            cd.sendMessage(pingMsg);

        } else if (idleCount > 1) {
            DefaultDeviceConnection cd = (DefaultDeviceConnection)session.getAttribute(CLIENT_SESSION);
            logger.error("The client session :{} should be disposed!",
                    cd.getJId());
            cd.close();
        }
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
