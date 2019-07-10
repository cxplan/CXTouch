package com.cxplan.projection.script.command;

import com.cxplan.projection.IApplication;
import com.cxplan.projection.net.message.MessageException;
import com.cxplan.projection.script.CommandType;
import com.cxplan.projection.script.ScriptUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Kenny
 * created on 2019/3/13
 */
public class TypeCommand extends ScriptCommand {

    private String content;

    public TypeCommand(String content) {
        super(CommandType.TYPE);
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public void encode(DataOutputStream dataOutput) throws IOException {
        ScriptUtil.writeString(dataOutput, content);
    }

    @Override
    public void decode(DataInputStream dataInput) throws IOException {
        content = ScriptUtil.readString(dataInput);

    }

    @Override
    public void execute(IApplication application, String deviceId) throws ScriptException {
        try {
            application.getDeviceService().type(deviceId, content);
        } catch (MessageException e) {
            throw new ScriptException(e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return type.name() + " [" + content + "]";
    }
}
