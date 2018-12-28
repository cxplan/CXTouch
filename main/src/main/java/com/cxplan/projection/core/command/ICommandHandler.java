package com.cxplan.projection.core.command;

import com.cxplan.projection.IApplication;
import com.cxplan.projection.net.message.Message;
import com.cxplan.projection.net.message.MessageException;
import org.apache.mina.core.session.IoSession;

/**
 * Created on 2017/4/20.
 *
 * @author kenny
 */
public interface ICommandHandler {

    /**
     * The business logic respond to message.
     * @param session session object
     * @param message received message object.
     * @return
     */
    void process(IoSession session, Message message) throws MessageException;

    String getCommand();

    IApplication getApplication();
}
