/**
 * The code is written by ytx, and is confidential.
 * Anybody must not broadcast these files without authorization.
 */
package com.cxplan.projection.net.protocol;

import com.cxplan.projection.net.message.MessageUtil;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created on 2017/4/21.
 *
 * @author kenny
 */
public class ShortFieldTypeHandler implements IFieldTypeHandler<Short> {
    @Override
    public void encode(Short value, IoBuffer outBuffer) {
        short val = value;
        outBuffer.put(getType());
        outBuffer.putShort(val);
    }

    @Override
    public Short decode(IoBuffer inBuffer) {
        if (inBuffer.remaining() < 2) {
            return null;
        } else {
            return inBuffer.getShort();
        }
    }

    @Override
    public byte getType() {
        return MessageUtil.FIELD_TYPE_SHORT;
    }
}
