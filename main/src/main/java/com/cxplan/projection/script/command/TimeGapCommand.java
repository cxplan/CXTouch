package com.cxplan.projection.script.command;

import com.cxplan.projection.IApplication;
import com.cxplan.projection.script.CommandType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Kenny
 * created on 2019/3/13
 */
public class TimeGapCommand extends ScriptCommand {

    private int gap;

    public TimeGapCommand(int gap) {
        super(CommandType.TIME_GAP);
        this.gap = gap;
    }

    public int getGap() {
        return gap;
    }

    public void setGap(int gap) {
        this.gap = gap;
    }

    @Override
    public void encode(DataOutputStream dataOutput) throws IOException {
        dataOutput.writeInt(gap);
    }

    @Override
    public void decode(DataInputStream dataInput) throws IOException {
        gap = dataInput.readInt();
    }

    @Override
    public void execute(IApplication application, String deviceId) throws ScriptException {
        try {
            Thread.sleep(gap);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return type.name() + " " + gap + "ms";
    }
}
