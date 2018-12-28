package com.cxplan.projection.net.message;

import com.cxplan.projection.net.protocol.IFieldTypeHandler;
import com.cxplan.projection.util.StringUtil;
import org.apache.mina.core.buffer.IoBuffer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2017/4/11.
 *
 * @author kenny
 */
public class Message {

    public static Message createResultMessage(Message source) {

        return createResultMessage(source, null);
    }

    public static Message createResultMessage(Message source, Message dist) {
        if (dist == null) {
            dist = new Message(source.getCommand());
        }
        dist.setId(source.getId());

        return dist;
    }

    String id;
    String command;
    String error;//The error message.

    JID from;
    JID to;
    protected Map<String, Object> data;

    public Message(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public JID getFrom() {
        return from;
    }

    public void setFrom(JID from) {
        this.from = from;
    }

    public JID getTo() {
        return to;
    }

    public void setTo(JID to) {
        this.to = to;
    }

    public <T> T getParameter(String key) {
        if (data == null) {
            return null;
        }
        return (T)data.get(key);
    }
    public void setParameter(String key, Object value) {
        if (data == null) {
            data = new HashMap<String, Object>();
        }

        data.put(key, value);
    }

    public void removeParameter(String key) {
        if (data == null) {
            return;
        }

        data.remove(key);
    }

    public String getId() {
        if (id == null) {
            id = MessageUtil.nextID();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public IoBuffer getBinary() throws MessageException {
        IoBuffer ret = IoBuffer.allocate(30).setAutoExpand(true);

        byte[] bytes = command.getBytes(MessageUtil.CHARSET_UTF8);
        if (bytes.length > 127) {
            throw new MessageException("The length of command must be less than 128");
        }
        //start code | id | command
        ret.put(MessageUtil.START_CODE);

        //id
        MessageUtil.writeStringByByte(getId(), ret);

        //command
        ret.put((byte) bytes.length);
        ret.put(bytes);

        //from
        if (from == null) {
            ret.put((byte) -1);
        } else {
            ret.put(from.getType().getValue());
            if (StringUtil.isBlank(from.getId())) {
                throw new MessageException("The field 'from' is illegal: The id is missing!");
            }
            MessageUtil.writeStringByByte(from.getId(), ret);
        }

        //to
        //type
        if (to == null) {
            ret.put((byte) -1);
        } else {
            ret.put(to.getType().getValue());
            //id
            if (StringUtil.isBlank(to.getId())) {
                throw new MessageException("The field 'to' is illegal: The id is missing!");
            }
            MessageUtil.writeStringByByte(to.getId(), ret);
        }

        //parameter
        //parameter count(byte)
        if (data == null) {
            ret.put((byte) 0);
        } else {
            if (data.size() > 127) {
                throw new MessageException("The count of parameter must be less than 128");
            }

            byte dataSize = (byte) calculateSizeOfParameter();
            ret.put(dataSize);

            if (data != null && data.size() > 0) {
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    if (entry.getValue() == null) {
                        continue;
                    }
                    byte[] key = entry.getKey().getBytes(MessageUtil.CHARSET_UTF8);
                    byte length = (byte) key.length;

                    //key
                    ret.put(length);
                    ret.put(key);

                    //value
                    IFieldTypeHandler typeHandler = MessageUtil.getFieldTypeHandler(entry.getValue());
                    typeHandler.encode(entry.getValue(), ret);

                }
            }
        }

        //error
        MessageUtil.writeStringByShort(error, ret);


        return ret;
    }

    private int calculateSizeOfParameter() {
        int count = 0;
        if (data == null) {
            return count;
        }
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            count++;
        }

        return count;
    }

}
