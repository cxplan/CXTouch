package com.cxplan.projection.net.protocol;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created on 2017/4/21.
 *
 * @author kenny
 */
public interface IFieldTypeHandler<T> {

    void encode(T value, IoBuffer outBuffer);

    /**
     * deserialize binary data, and return field object.
     * @param inBuffer
     * @return A null value will be returned if the bytes in buffer is not enough.
     */
    T decode(IoBuffer inBuffer);

    byte getType();

}
