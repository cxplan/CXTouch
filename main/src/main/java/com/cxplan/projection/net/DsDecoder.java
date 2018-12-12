package com.cxplan.projection.net;

import com.cxplan.projection.IApplication;
import com.cxplan.projection.core.connection.ClientConnection;
import com.cxplan.projection.net.message.Message;
import com.cxplan.projection.net.protocol.MessageParser;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author kenny
 * Created on 2013-8-12
 */
public class DsDecoder extends CumulativeProtocolDecoder {

	private final static Logger logger = LoggerFactory.getLogger(DsDecoder.class);

    public static final String MESSAGE_PARSER = "message_parser";

	public DsDecoder() {
	}
	
	@Override
	protected boolean doDecode(IoSession session, IoBuffer in,
			ProtocolDecoderOutput out) throws Exception {

        MessageParser parser = (MessageParser)session.getAttribute(MESSAGE_PARSER);
		if (parser == null) {
			parser = new MessageParser();
	    	session.setAttribute(MESSAGE_PARSER, parser);
	    	System.out.println("--------------Protocol Parser Created------------------");
		}
		try {
			parser.readMessage(in);
		} catch (Exception e) {
			ClientConnection connection = (ClientConnection)session.getAttribute(IApplication.CLIENT_SESSION);
			logger.error("protocol error, the connection: " +
					(connection == null ? "" : connection.getJId()));
			throw e;
		}
		
		if (parser.hasResult()) {
			for (Message md : parser.getResult()) {
				out.write(md);
			}
			
			parser.clearResult();
		}
		
		return false;
		
	}
	
}

