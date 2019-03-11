package com.cxplan.projection.command;

import com.cxplan.projection.core.command.AbstractCommandHandler;
import com.cxplan.projection.net.message.Message;
import com.cxplan.projection.net.message.MessageException;
import com.cxplan.projection.net.message.MessageUtil;
import com.cxplan.projection.ui.util.GUIUtil;
import com.cxplan.projection.util.StringUtil;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kenny
 * created on 2018/12/14
 */
public class ClipboardCommandHandler extends AbstractCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(ClipboardCommandHandler.class);

    public ClipboardCommandHandler() {
        super(MessageUtil.CMD_CONTROLLER_CLIPBOARD);
    }

    @Override
    public void process(IoSession session, Message message) throws MessageException {
        String text = message.getParameter("c");
        if (StringUtil.isEmpty(text)) {
            logger.error("The content on clipboard from android is missing");
            return;
        }

        GUIUtil.copyText2Clipboard(text);
    }

}
