package com.cxplan.projection.command;

import com.cxplan.projection.core.command.AbstractCommandHandler;
import com.cxplan.projection.net.message.Message;
import com.cxplan.projection.net.message.MessageException;
import com.cxplan.projection.net.message.MessageUtil;
import com.cxplan.projection.script.ViewNode;
import com.cxplan.projection.script.io.ScriptDeviceConnection;
import com.cxplan.projection.util.StringUtil;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kenny
 * created on 2019/3/28
 */
public class ScriptViewNodeCommandHandler extends AbstractCommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(ScriptViewNodeCommandHandler.class);

    public ScriptViewNodeCommandHandler() {
        super(MessageUtil.CMD_CONTROLLER_SCRIPT_VIEW_NODE);
    }

    @Override
    public void process(IoSession session, Message message) throws MessageException {
        ScriptDeviceConnection connection = getConnection(session);
        String dataString = message.getParameter("data");
        int seqNum = message.getParameter("seq");
        ViewNode viewNode = StringUtil.json2Object(dataString, ViewNode.class);

        if (!connection.isRecording()) {
            logger.error("The script recording is not started: " + connection.getJId().getId());
            return;
        }
        connection.getScriptRecorder().updateKeyView(seqNum, viewNode);
    }
}
