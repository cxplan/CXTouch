package com.cxplan.projection.script.command;

import com.cxplan.projection.IApplication;
import com.cxplan.projection.MonkeyConstant;
import com.cxplan.projection.net.message.MessageException;
import com.cxplan.projection.script.CommandType;
import com.cxplan.projection.ui.component.ItemMeta;
import com.cxplan.projection.ui.script.KeycodeDialog;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Kenny
 * created on 2019/3/13
 */
public class PressCommand extends ScriptCommand {
    private int keyCode;

    public PressCommand(int keyCode) {
        super(CommandType.PRESS);
        this.keyCode = keyCode;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    @Override
    public void encode(DataOutputStream dataOutput) throws IOException {
        dataOutput.writeInt(keyCode);
    }

    @Override
    public void decode(DataInputStream dataInput) throws IOException {
        keyCode = dataInput.readInt();
    }

    @Override
    public void execute(IApplication application, String deviceId) throws ScriptException {
        try {
            application.getDeviceService().press(deviceId, keyCode);
        } catch (MessageException e) {
            throw new ScriptException(e.getMessage(), e);
        }
    }

    @Override
    public ScriptCommand toEdit(IApplication application, Window parent) {
        KeycodeDialog dialog = new KeycodeDialog(parent, keyCode);
        dialog.showCenterToOwner();

        if (dialog.isSelected()) {
            keyCode = dialog.getSelectedKeyCode();
            return this;
        } else {
            return null;
        }

    }

    @Override
    public String toString() {
        ItemMeta meta = MonkeyConstant.KEY_CODE_MAP.get(keyCode);
        if (meta != null) {
            return type.name() + " " + keyCode + "(" + meta.getName() + ")";
        } else {
            return type.name() + " " + keyCode;
        }
    }
}
