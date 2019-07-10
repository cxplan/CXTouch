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
public class ToggleScreenCommand extends ScriptCommand {

    private boolean wake;

    public ToggleScreenCommand(boolean wake) {
        super(CommandType.TOGGLE_SCREEN);
        this.wake = wake;
    }


    public boolean isWake() {
        return wake;
    }

    public void setWake(boolean wake) {
        this.wake = wake;
    }

    @Override
    public void encode(DataOutputStream dataOutput) throws IOException {
        dataOutput.writeBoolean(wake);
    }

    @Override
    public void decode(DataInputStream dataInput) throws IOException {
        wake = dataInput.readBoolean();
    }

    @Override
    public void execute(IApplication application, String deviceId) throws ScriptException {
        try {
            if (wake) {
                application.getDeviceService().wake(deviceId);
            } else {
                application.getDeviceService().sleep(deviceId);
            }
        } catch (MessageException e) {
            throw new ScriptException(e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return type.name() + (wake ? " - WAKE" : " - SLEEP");
    }
}
