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
public class MouseCommand extends ScriptCommand {
    private int x, y;

    public MouseCommand(CommandType type, int x, int y) {
        super(type);
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public void encode(DataOutputStream dataOutput) throws IOException {
        dataOutput.writeInt(x);
        dataOutput.writeInt(y);
    }

    @Override
    public void decode(DataInputStream dataInput) throws IOException {
        x = dataInput.readInt();
        y = dataInput.readInt();
    }

    @Override
    public void execute(IApplication application, String deviceId) throws ScriptException {
        try {
            if (getType() == CommandType.MOUSE_DOWN) {
                application.getDeviceService().touchDown(deviceId, x, y);
            } else if (getType() == CommandType.MOUSE_MOVE) {
                application.getDeviceService().touchMove(deviceId, x, y);
            } else if (getType() == CommandType.MOUSE_UP) {
                application.getDeviceService().touchUp(deviceId, x, y);
            } else {
                throw new ScriptException("The mouse command type is illegal: " + getType());
            }
        } catch (MessageException e) {
            throw new ScriptException(e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return type.name() + " " + x + "," + y;
    }
}
