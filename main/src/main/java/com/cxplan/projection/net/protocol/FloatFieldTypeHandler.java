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
public class FloatFieldTypeHandler implements IFieldTypeHandler<Float> {
    @Override
    public void encode(Float value, IoBuffer outBuffer) {
        float val = value;
        outBuffer.put(getType());
        outBuffer.putFloat(val);
    }

    @Override
    public Float decode(IoBuffer inBuffer) {
        if (inBuffer.remaining() < 4) {
            return null;
        } else {
            return inBuffer.getFloat();
        }
    }

    @Override
    public byte getType() {
        return MessageUtil.FIELD_TYPE_FLOAT;
    }
}
