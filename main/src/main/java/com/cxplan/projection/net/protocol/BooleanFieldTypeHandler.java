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
public class BooleanFieldTypeHandler implements IFieldTypeHandler<Boolean> {
    @Override
    public void encode(Boolean value, IoBuffer outBuffer) {
        boolean val = value;
        outBuffer.put(getType());
        outBuffer.put(val ? (byte) 1 : (byte) 0);
    }

    @Override
    public Boolean decode(IoBuffer inBuffer) {
        if (inBuffer.remaining() < 1) {
            return null;
        } else {
            byte ret = inBuffer.get();
            return ret == 1;
        }
    }

    @Override
    public byte getType() {
        return MessageUtil.FIELD_TYPE_BOOLEAN;
    }
}
