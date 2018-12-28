package com.cxplan.projection.net.protocol;

import com.cxplan.projection.net.message.MessageUtil;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created on 2017/4/21.
 *
 * @author kenny
 */
public class IntegerFieldTypeHandler implements IFieldTypeHandler<Integer> {
    @Override
    public void encode(Integer value, IoBuffer outBuffer) {
        int val = value;
        outBuffer.put(getType());
        outBuffer.putInt(val);
    }

    @Override
    public Integer decode(IoBuffer inBuffer) {
        if (inBuffer.remaining() < 4) {
            return null;
        } else {
            return inBuffer.getInt();
        }
    }

    @Override
    public byte getType() {
        return MessageUtil.FIELD_TYPE_INT;
    }
}
