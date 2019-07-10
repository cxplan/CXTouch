package com.cxplan.projection.script.io;

import com.cxplan.projection.IApplication;
import com.cxplan.projection.net.DeviceIoHandlerAdapter;
import com.cxplan.projection.net.message.Message;
import com.cxplan.projection.net.message.MessageUtil;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kenny
 * created on 2019/3/22
 */
public class ScriptDeviceIoHandlerAdapter extends DeviceIoHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ScriptDeviceIoHandlerAdapter.class);

    public ScriptDeviceIoHandlerAdapter(IApplication application) {
        super(application);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        ScriptDeviceConnection connection = (ScriptDeviceConnection) session.getAttribute(CLIENT_SESSION);
        if (connection != null) {
            session.removeAttribute(CLIENT_SESSION);
            connection.close();
        }
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        int idleCount = session.getIdleCount(status);
        if (idleCount == 1) {
            ScriptDeviceConnection cd = (ScriptDeviceConnection)session.getAttribute(CLIENT_SESSION);
            Message pingMsg = new Message(MessageUtil.CMD_PING);
            cd.sendMessage(pingMsg);

        } else if (idleCount > 1) {
            ScriptDeviceConnection cd = (ScriptDeviceConnection)session.getAttribute(CLIENT_SESSION);
            logger.error("The script session :{} should be disposed!",
                    cd.getJId());
            cd.close();
        }
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
