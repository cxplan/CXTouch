package com.cxplan.projection.net.protocol;

import com.cxplan.projection.net.message.MessageUtil;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created on 2017/4/21.
 *
 * @author kenny
 */
public class StringFieldTypeHandler implements IFieldTypeHandler<String> {
    @Override
    public void encode(String value, IoBuffer outBuffer) {
        byte[] bytes = value.getBytes(MessageUtil.CHARSET_UTF8);
        outBuffer.put(getType());
        outBuffer.putInt(bytes.length);
        outBuffer.put(bytes);
    }

    @Override
    public String decode(IoBuffer inBuffer) {
        if (inBuffer.remaining() < 4) {
            return null;
        }
        inBuffer.mark();
        int length = inBuffer.getInt();
        if (inBuffer.remaining() < length) {
            inBuffer.reset();
            return null;
        }
        byte[] bytes = new byte[length];
        inBuffer.get(bytes);
        return new String(bytes, 0, length, MessageUtil.CHARSET_UTF8);
    }

    public byte getType() {
        return MessageUtil.FIELD_TYPE_STRING;
    }
}
