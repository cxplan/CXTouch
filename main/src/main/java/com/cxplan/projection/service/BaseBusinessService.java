package com.cxplan.projection.service;

import com.cxplan.projection.IApplication;
import com.cxplan.projection.core.connection.ClientConnection;
import com.cxplan.projection.net.message.Message;
import com.cxplan.projection.net.message.MessageException;
import com.cxplan.projection.net.message.MessageListener;
import com.cxplan.projection.net.message.MessageUtil;

import java.util.List;

/**
 * Created on 2017/5/18.
 *
 * @author kenny
 */
public class BaseBusinessService {

    protected IApplication application;

    public IApplication getApplication() {
        return application;
    }

    public void setApplication(IApplication application) {
        this.application = application;
    }

    /**
     * Send a request, and then blocked util a response is received.
     * if operation is timeout, a message exception will be thrown.
     *
     * @param session session object which sent message.
     * @param message message object.
     * @return response message
     * @throws MessageException
     */
    public Message request(ClientConnection session, Message message) throws MessageException {
        return request(session, message, 15000);
    }
    /**
     * Send a request, and then blocked util a response is received.
     * if operation is timeout, a message exception will be thrown.
     *
     * @param session session object which sent message.
     * @param message message object.
     * @param timeout the max time in milliseconds that operation is allowed.
     * @return response message
     * @throws MessageException, MessageTimeoutException
     */
    public Message request(ClientConnection session, Message message, long timeout) throws MessageException {
        return MessageUtil.request(session, message, timeout);
    }
    public void requestWithCallback(ClientConnection session, Message message, MessageListener listener) throws MessageException {
        MessageUtil.requestWithCallback(session, message, listener);
    }
    public void sendMessage(ClientConnection session, Message message) throws MessageException {
        MessageUtil.sendMessage(session, message);
    }
    public List<Message> requestMultiSession(List<ClientConnection> sessionList, Message message) throws MessageException {
        return requestMultiSession(sessionList, message, 15000);
    }
    public List<Message> requestMultiSession(List<ClientConnection> sessionList, Message message, long timeout) throws MessageException {
        return MessageUtil.requestMultiSession(sessionList, message, timeout);
    }
}
