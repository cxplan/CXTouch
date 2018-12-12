package com.cxplan.projection.command;

import com.cxplan.projection.core.command.AbstractCommandHandler;
import com.cxplan.projection.net.message.Message;
import com.cxplan.projection.net.message.MessageException;
import org.apache.mina.core.session.IoSession;

/**
 * Created on 2017/5/9.
 *
 * @author kenny
 */
public class DummyCommandHandler extends AbstractCommandHandler {

    public DummyCommandHandler() {
        super("dummy");
    }


    @Override
    public void process(IoSession session, Message message) throws MessageException {

    }
}
