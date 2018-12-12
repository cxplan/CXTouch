package com.cxplan.projection.net;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * Factory that specifies the encode and decoder to use for parsing Image stanzas.
 *
 * @author Kenny
 */
public class DSCodecFactory implements ProtocolCodecFactory {

    private final DsEncoder encoder;
    private final DsDecoder decoder;

    public DSCodecFactory() {
        encoder = new DsEncoder();
        decoder = new DsDecoder();
    }

    public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
        return encoder;
    }

    public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
        return decoder;
    }
}
