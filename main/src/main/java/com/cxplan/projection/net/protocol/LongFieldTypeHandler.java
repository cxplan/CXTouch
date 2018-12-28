package com.cxplan.projection.net.protocol;

import com.cxplan.projection.net.message.MessageUtil;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created on 2017/4/21.
 *
 * @author kenny
 */
public class LongFieldTypeHandler implements IFieldTypeHandler<Long> {
    @Override
    public void encode(Long value, IoBuffer outBuffer) {
        long val = value;
        outBuffer.put(getType());
        outBuffer.putLong(val);
    }

    @Override
    public Long decode(IoBuffer inBuffer) {
        if (inBuffer.remaining() < 8) {
            return null;
        } else {
            return inBuffer.getLong();
        }
    }

    @Override
    public byte getType() {
        return MessageUtil.FIELD_TYPE_LONG;
    }
}
