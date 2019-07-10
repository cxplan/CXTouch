package com.cxplan.projection.script;

import com.cxplan.projection.net.message.MessageUtil;
import com.cxplan.projection.script.command.*;
import com.cxplan.projection.util.StringUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Kenny
 * created on 2019/3/30
 */
public class ScriptUtil {

    /**
     * Create script command object according to specified type.
     *
     * @param type the command type.
     * @return script command object.
     */
    public static ScriptCommand createCommand(CommandType type) {
        switch (type) {
            case MOUSE_UP:
                return new MouseCommand(CommandType.MOUSE_UP, 0, 0);
            case MOUSE_DOWN:
                return new MouseCommand(CommandType.MOUSE_DOWN, 0, 0);
            case MOUSE_MOVE:
                return new MouseCommand(CommandType.MOUSE_MOVE, 0, 0);
            case PRESS:
                return new PressCommand(0);
            case TYPE:
                return new TypeCommand(null);
            case SCROLL:
                return new ScrollCommand(true);
            case TIME_GAP:
                return new TimeGapCommand(0);
            case START_APP:
                return new StartAppCommand(null);

                default:
                    throw new IllegalArgumentException("Unknown command type: " + type);
        }
    }

    /**
     * Write a string to output stream.
     * protocol:
     * length(int) | content(byte[])
     *
     * @throws IOException
     */
    public static void writeString(DataOutputStream dataOutput, String content) throws IOException {
        int length = 0;
        byte[] data = null;
        if (StringUtil.isNotEmpty(content)) {
            data = content.getBytes(MessageUtil.CHARSET_UTF8);
            length = data.length;
        }
        dataOutput.writeInt(length);
        if (length > 0) {
            dataOutput.write(data);
        }
    }

    /**
     * Read a string from input stream.
     * protocol:
     * length(int) | content(byte[])
     *
     * @throws IOException
     */
    public static String readString(DataInputStream dataInput) throws IOException {
        int length = dataInput.readInt();
        String content;
        if (length == 0) {
            content = null;
        } else {
            byte[] data = new byte[length];
            dataInput.readFully(data);
            content = new String(data, MessageUtil.CHARSET_UTF8);
        }

        return content;
    }
}
