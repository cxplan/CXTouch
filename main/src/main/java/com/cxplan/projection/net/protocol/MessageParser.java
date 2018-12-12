/**
 * The code is written by ytx, and is confidential.
 * Anybody must not broadcast these files without authorization.
 */
package com.cxplan.projection.net.protocol;

import com.cxplan.projection.net.message.JID;
import com.cxplan.projection.net.message.Message;
import com.cxplan.projection.net.message.MessageUtil;
import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2017/4/19.
 *
 * @author kenny
 */
public class MessageParser {

    private static final Logger logger = LoggerFactory.getLogger(MessageParser.class);

    private static final byte STARTCODE = (byte) 0;

    private Charset charset;
    private CharsetDecoder charsetDecoder;

    private String id;
    private String command;
    private JID from;
    private JID to;
    private Map<String, Object> dataMap;
    private String error;

    Status status = Status.NONE;
    int dataCount = 0;
    int length = 0;
    String dataKey;
    IFieldTypeHandler fieldHandler;

    List<Message> messageList;

    public MessageParser() {
        dataMap = new HashMap<String, Object>();
        messageList = new ArrayList<Message>(10);

        charset = MessageUtil.CHARSET_UTF8;
        charsetDecoder = charset.newDecoder();
    }

    public void readMessage(IoBuffer buffer) throws IOException {

        readCommand(buffer);
    }

    public boolean hasResult() {
        return messageList.size() > 0;
    }

    public List<Message> getResult() {
        return messageList;
    }

    public void clearResult() {
        messageList.clear();
    }

    /**
     * Protocol: startCode |id | command| error
     *
     * startCode: 1byte(00000000)
     * id: length(byte) | content
     * command: length(1byte) | content
     * data: count(1byte) | {key: length(1byte) | content, value: type(1byte)|content}
     * error: length(short) | content
     *
     * @throws IOException
     */
    private void readCommand(IoBuffer buffer) throws IOException {

        byte startCode;
        while (buffer.hasRemaining()) {
            switch (status) {
                case NONE:
                    startCode = buffer.get();
                    if (startCode != STARTCODE) {
                        throw new IOException("The data serial is error: Expect start code");
                    }
                    status = Status.START;
                    break;
                case START:
                    length = buffer.get();//id length
                    if (length < 1) {
                        throw new IOException("The data serial is error: The length of message id is illegal!");
                    }
                    status = Status.ID_CONTENT;
                    break;
                case ID_CONTENT:
                    if(buffer.remaining() < length) {
                        return;
                    }
                    id = buffer.getString(length, charsetDecoder);
                    status = Status.COMMAND_LENGTH;
                    break;
                case COMMAND_LENGTH:
                    length = buffer.get();//command length
                    if (length < 1) {
                        throw new IOException("The data serial is error: The length of command is illegal!");
                    }
                    status = Status.COMMAND_CONTENT;
                    break;
                case COMMAND_CONTENT:
                    if (buffer.remaining() < length) {
                        return;
                    }
                    command = buffer.getString(length, charsetDecoder);
                    status = Status.FROM_TYPE;
                    break;
                case FROM_TYPE:
                    JID.Type fromType = JID.Type.getType(buffer.get());
                    if (fromType == JID.Type.NONE) {
                        status = Status.TO_TYPE;
                        from = null;
                    } else {
                        status = Status.FROM_ID_LENGTH;
                        from = new JID(null, fromType);
                    }
                    break;
                case FROM_ID_LENGTH:
                    length = buffer.get();
                    status = Status.FROM_ID_CONTENT;
                    break;
                case FROM_ID_CONTENT:
                    if (buffer.remaining() < length) {
                        return;
                    }
                    from.setId(buffer.getString(length, charsetDecoder));
                    status = Status.TO_TYPE;
                    break;
                case TO_TYPE:
                    JID.Type tmpType = JID.Type.getType(buffer.get());
                    if (tmpType == JID.Type.NONE) {
                        status = Status.DATA_LENGTH;
                        to = null;
                    } else {
                        status = Status.TO_ID_LENGTH;
                        to = new JID(null, tmpType);
                    }
                    break;
                case TO_ID_LENGTH:
                    length = buffer.get();
                    status = Status.TO_ID_CONTENT;
                    break;
                case TO_ID_CONTENT:
                    if (buffer.remaining() < length) {
                        return;
                    }
                    to.setId(buffer.getString(length, charsetDecoder));
                    status = Status.DATA_LENGTH;
                    break;
                case DATA_LENGTH:
                    dataCount = buffer.get();
                    dataMap = new HashMap<>();
                    if (dataCount == 0) {
                        status = Status.ERROR_LENGTH;
                    } else {
                        status = Status.DATA_KEY_LENGTH;
                    }
                    break;

                case DATA_KEY_LENGTH:
                    if (dataCount == 0) {
                        status = Status.ERROR_LENGTH;
                        break;
                    }

                    length = buffer.get();
                    status = Status.DATA_KEY_CONTENT;
                    break;
                case DATA_KEY_CONTENT:
                    if (buffer.remaining() < length) {
                        return;
                    }
                    dataKey = buffer.getString(length, charsetDecoder);
                    status = Status.DATA_VALUE_TYPE;
                    break;
                case DATA_VALUE_TYPE:
                    byte fieldType = buffer.get();
                    try {
                        fieldHandler = MessageUtil.getFieldTypeHandler(fieldType);
                    } catch (Exception e) {
                        StringBuilder sb = new StringBuilder("data value type error: cmd=");
                        sb.append(command).append(",key=").append(dataKey);
                        logger.error(sb.toString());
                        throw e;
                    }
                    status = Status.DATA_VALUE_CONTENT;
                    break;

                case DATA_VALUE_CONTENT:
                    Object value = fieldHandler.decode(buffer);
                    if (value == null) {
                        return ;
                    } else {
                        dataMap.put(dataKey, value);
                        dataCount --;
                        if (dataCount == 0) {//the end of round.
                            status = Status.ERROR_LENGTH;
                        } else {
                            status = Status.DATA_KEY_LENGTH;
                        }
                    }
                    break;
                case ERROR_LENGTH:
                    if (buffer.remaining() < 2) {
                        return;
                    }
                    length = buffer.getShort();
                    if (length == -1 || length == 0) {
                        error = null;
                        status = Status.NONE;
                        buildMessage();
                    } else {
                        status = Status.ERROR_CONTENT;
                    }
                    break;
                case ERROR_CONTENT:
                    if (buffer.remaining() < length) {
                        return;
                    }
                    error = buffer.getString(length, charsetDecoder);
                    status = Status.NONE;
                    buildMessage();
                    break;
                default:
            }
        }

    }

    private void buildMessage() {
        Message message = new Message(command);
        message.setFrom(from);
        message.setTo(to);
        message.setData(dataMap);
        message.setId(id);
        message.setError(error);

        messageList.add(message);
    }

    enum Status {
        NONE,
        START,
        ID_CONTENT,
        COMMAND_LENGTH,
        COMMAND_CONTENT,
        FROM_TYPE,
        FROM_ID_LENGTH,
        FROM_ID_CONTENT,
        TO_TYPE,
        TO_ID_LENGTH,
        TO_ID_CONTENT,
        DATA_LENGTH,
        DATA_KEY_LENGTH,
        DATA_KEY_CONTENT,
        DATA_VALUE_TYPE,
        DATA_VALUE_CONTENT,
        ERROR_LENGTH,
        ERROR_CONTENT
    }
}
