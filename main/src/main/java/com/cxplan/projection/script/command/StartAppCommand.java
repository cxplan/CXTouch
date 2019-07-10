package com.cxplan.projection.script.command;

import com.cxplan.projection.IApplication;
import com.cxplan.projection.net.message.MessageException;
import com.cxplan.projection.script.CommandType;
import com.cxplan.projection.script.ScriptUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Kenny
 * created on 2019/3/13
 */
public class StartAppCommand extends ScriptCommand {
    private String appPackage;

    public StartAppCommand(String appPackage) {
        super(CommandType.START_APP);
        this.appPackage = appPackage;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
    }

    @Override
    public void encode(DataOutputStream dataOutput) throws IOException {
        ScriptUtil.writeString(dataOutput, appPackage);
    }

    @Override
    public void decode(DataInputStream dataInput) throws IOException {
        appPackage = ScriptUtil.readString(dataInput);
    }

    @Override
    public void execute(IApplication application, String deviceId) throws ScriptException {
        try {
            application.getDeviceService().startActivity(deviceId, null, null
            , null, null, new ArrayList<String>(0), new HashMap<String, Object>(),
                    appPackage, 0);
        } catch (MessageException e) {
            throw new ScriptException(e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return type.name() + " " + appPackage;
    }
}
