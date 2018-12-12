package com.cxplan.projection.net;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 *
 * @author kenny
 * Created on 2013-8-13
 */
public class DsEncoder extends ProtocolEncoderAdapter {

	public void encode(IoSession session, Object message, ProtocolEncoderOutput out)
            throws Exception {
        // Ignore. Do nothing. Content being sent is already a bytebuffer (of strings) 
    }
}

