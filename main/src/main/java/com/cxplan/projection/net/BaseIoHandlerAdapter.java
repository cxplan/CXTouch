package com.cxplan.projection.net;

import com.cxplan.projection.net.message.Message;
import com.cxplan.projection.net.message.MessageException;
import com.cxplan.projection.net.message.MessageUtil;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;

/**
 * @author KennyLiu
 * @created on 2018/5/19
 */
public abstract class BaseIoHandlerAdapter extends IoHandlerAdapter {
    public static final String CLIENT_SESSION = "client_session";
    public static final String CLIENT_ID = "ID";

    protected void processException(Exception e, Message msg, IoSession session) throws Exception {
        getLogger().error(e.getMessage(), e);
        Message ret = Message.createResultMessage(msg);
        if (e instanceof MessageException) {
            ret.setError(e.getMessage());
            MessageUtil.sendMessage(session, ret);

            return;
        } else {
            ret.setError(e.getMessage());
            MessageUtil.sendMessage(session, ret);
            throw e;
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        getLogger().error(cause.getMessage(), cause);
    }

    protected abstract Logger getLogger();
}
