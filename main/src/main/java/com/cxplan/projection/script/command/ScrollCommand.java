package com.cxplan.projection.script.command;

import com.cxplan.projection.IApplication;
import com.cxplan.projection.net.message.MessageException;
import com.cxplan.projection.script.CommandType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Kenny
 * created on 2019/3/13
 */
public class ScrollCommand extends ScriptCommand {

    private boolean upScroll;

    public ScrollCommand(boolean upScroll) {
        super(CommandType.SCROLL);
        this.upScroll = upScroll;
    }


    public boolean isUpScroll() {
        return upScroll;
    }

    public void setUpScroll(boolean upScroll) {
        this.upScroll = upScroll;
    }

    @Override
    public void encode(DataOutputStream dataOutput) throws IOException {
        dataOutput.writeBoolean(upScroll);
    }

    @Override
    public void decode(DataInputStream dataInput) throws IOException {
        upScroll = dataInput.readBoolean();
    }

    @Override
    public void execute(IApplication application, String deviceId) throws ScriptException {
        try {
            if (upScroll) {
                application.getDeviceService().scrollUp(deviceId);
            } else {
                application.getDeviceService().scrollDown(deviceId);
            }
        } catch (MessageException e) {
            throw new ScriptException(e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return type.name() + (upScroll ? " - UP" : " - DOWN");
    }
}
