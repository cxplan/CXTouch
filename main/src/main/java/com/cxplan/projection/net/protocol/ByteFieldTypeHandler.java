package com.cxplan.projection.net.protocol;

import com.cxplan.projection.net.message.MessageUtil;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created on 2017/4/21.
 *
 * @author kenny
 */
public class ByteFieldTypeHandler implements IFieldTypeHandler<Byte> {
    @Override
    public void encode(Byte value, IoBuffer outBuffer) {
        byte val = value;
        outBuffer.put(getType());
        outBuffer.put(val);
    }

    @Override
    public Byte decode(IoBuffer inBuffer) {
        if (inBuffer.remaining() < 1) {
            return null;
        } else {
            return inBuffer.get();
        }
    }

    @Override
    public byte getType() {
        return MessageUtil.FIELD_TYPE_BYTE;
    }
}
