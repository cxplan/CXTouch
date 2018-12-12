package com.cxplan.projection.core.connection;

import com.cxplan.projection.net.message.Message;
import com.cxplan.projection.net.message.MessageListener;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Provides a mechanism to collect packets into a result queue that pass a
 * specified filter. The collector lets you perform blocking and polling
 * operations on the result queue.<p>
 *
 * Each packet collector will queue up a configured number of packets for processing before
 * older packets are automatically dropped.  The default number is retrieved by 
 *
 * @author Matt Tucker
 */
public class MessageCollector {

    private MessageFilter messageFilter;
    private ArrayBlockingQueue<Message> resultQueue;
    private ClientConnection session;
    private boolean cancelled = false;

    private MessageListener messageListener;
    /**
     * Creates a new packet collector. If the packet filter is <tt>null</tt>, then
     * all packets will match this collector.
     *
     * @param session the session the collector is tied to.
     * @param messageFilter determines which packets will be returned by this collector.
     */
    protected MessageCollector(ClientConnection session, MessageFilter messageFilter) {
    	this(session, messageFilter, null, 1);
    }
    protected MessageCollector(ClientConnection session, MessageFilter messageFilter, MessageListener messageListener) {
    	this(session, messageFilter, messageListener, 1);
    }

    /**
     * Creates a new packet collector. If the packet filter is <tt>null</tt>, then
     * all packets will match this collector.
     *
     * @param session the session the collector is tied to.
     * @param messageFilter determines which packets will be returned by this collector.
     * @param messageListener listen when a accepted message is coming.
     * @param maxSize the maximum number of packets that will be stored in the collector.
     */
    protected MessageCollector(ClientConnection session, MessageFilter messageFilter, MessageListener messageListener, int maxSize) {
        this.session = session;
        this.messageFilter = messageFilter;
        this.messageListener = messageListener;
        if (messageListener == null) {
            this.resultQueue = new ArrayBlockingQueue<Message>(maxSize);
        }
    }

    /**
     * Explicitly cancels the packet collector so that no more results are
     * queued up. Once a packet collector has been cancelled, it cannot be
     * re-enabled. Instead, a new packet collector must be created.
     */
    public void cancel() {
        // If the packet collector has already been cancelled, do nothing.
        if (!cancelled) {
            cancelled = true;
            session.removePacketCollector(this);
        }
    }

    /**
     * Returns the packet filter associated with this message collector. The message
     * filter is used to determine what messages are queued as results.
     *
     * @return the packet filter.
     */
    public MessageFilter getMessageFilter() {
        return messageFilter;
    }

    /**
     * Return the message listener associated with this message collector. The listener will
     * be invoked when a message is accepted by message filter.
     */
    public MessageListener getMessageListener() {
        return messageListener;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    /**
     * Polls to see if a packet is currently available and returns it, or
     * immediately returns <tt>null</tt> if no packets are currently in the
     * result queue.
     *
     * @return the next packet result, or <tt>null</tt> if there are no more
     *      results.
     */
    public Message pollResult() {
    	return resultQueue.poll();
    }

    /**
     * Returns the next available packet. The method call will block (not return)
     * until a packet is available.
     *
     * @return the next available packet.
     */
    public Message nextResult() {
        try {
			return resultQueue.take();
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
    }

    /**
     * Returns the next available packet. The method call will block (not return)
     * until a packet is available or the <tt>timeout</tt> has elapased. If the
     * timeout elapses without a result, <tt>null</tt> will be returned.
     *
     * @param timeout the amount of time to wait for the next packet (in milleseconds).
     * @return the next available packet.
     */
    public Message nextResult(long timeout) {
    	try {
			return resultQueue.poll(timeout, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
    }

    /**
     * Processes a packet to see if it meets the criteria for this packet collector.
     * If so, the packet is added to the result queue.
     *
     * @param packet the packet to process.
     * @return return true if message is accepted, otherwise false is returned.
     */
    protected boolean processMessage(Message packet) {
        if (packet == null) {
            return false;
        }
        
        if (messageFilter == null || messageFilter.accept(packet)) {
            if (messageListener == null) {
                while (!resultQueue.offer(packet)) {
                    // Since we know the queue is full, this poll should never actually block.
                    resultQueue.poll();
                }
            } else {
                try {
                    messageListener.processPacket(packet);
                } finally {
                    cancel();
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
