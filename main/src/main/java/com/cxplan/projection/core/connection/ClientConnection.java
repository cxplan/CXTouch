package com.cxplan.projection.core.connection;

import com.cxplan.projection.net.message.JID;
import com.cxplan.projection.net.message.Message;
import com.cxplan.projection.net.message.MessageException;
import com.cxplan.projection.net.message.MessageListener;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created on 2017/5/17.
 *
 * @author kenny
 */
public class ClientConnection {

    protected JID id;
    protected IoSession messageSession;
    //When connection is
    protected boolean isConnecting = false;
    /**
     * A collection of PacketCollectors which collects packets for a specified filter
     * and perform blocking and polling operations on the result queue.
     */
    protected final Collection<MessageCollector> collectors = new ConcurrentLinkedQueue<MessageCollector>();

    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);


    public JID getJId() {
        return id;
    }

    public void setId(JID id) {
        this.id = id;
    }

    public IoSession getMessageSession() {
        return messageSession;
    }

    public void setMessageSession(IoSession messageSession) {
        if (this.messageSession == messageSession) {
            return;
        }
        if (this.messageSession != null && this.messageSession.isConnected()) {
            try {
                this.messageSession.closeNow();
            } catch (Exception e) {
            }
        }
        this.messageSession = messageSession;
    }

    public boolean isConnected() {
        return messageSession != null && messageSession.isConnected();
    }

    public boolean isConnecting() {
        return isConnecting;
    }

    public void setConnecting(boolean connecting) {
        isConnecting = connecting;
    }

    /**
     * Send message to device.
     * @param msg
     * @throws IOException
     */
    public void sendMessage(Message msg) throws MessageException {
        if (messageSession == null || !messageSession.isConnected()) {
            throw new MessageException("与客户端无连接，无法发送, 当前连接：" + id + ", message ID: " + msg.getId());
        }

        IoBuffer buffer = msg.getBinary();
        buffer.flip();
        messageSession.write(buffer);
    }

    /**
     * Connect to server.
     */
    public void connect() {}
    /**
     * Close all network connection.
     */
    protected void closeNetworkResource() {
        if (messageSession != null) {
            messageSession.closeOnFlush();
            messageSession = null;
        }
    }

    /**
     * Disconnect current connection.
     * This method is same with method {@link #closeNetworkResource}
     */
    public void close() {
        closeNetworkResource();
    }

    /**
     * Creates a new packet collector for this connection. A packet filter determines
     * which messages will be accumulated by the collector.
     * This mode is suit for synchronized operation.
     *
     * @param messageFilter the message filter to use.
     * @return a new message collector.
     */
    public MessageCollector createPacketCollector(MessageFilter messageFilter) {
        MessageCollector collector = new MessageCollector(this, messageFilter);
        // Add the collector to the list of active collectors.
        collectors.add(collector);
        return collector;
    }

    /**
     * This mode is suit for asynchronous operation.
     * @param messageFilter the message filter to use.
     * @param listener The message listener to message.
     * @return a new message collector.
     */
    public MessageCollector createPacketCollector(MessageFilter messageFilter, MessageListener listener) {
        MessageCollector collector = new MessageCollector(this, messageFilter, listener);
        // Add the collector to the list of active collectors.
        collectors.add(collector);
        return collector;
    }

    public boolean visitMessageCollectors(Message message) {
        boolean ret = false;
        for (MessageCollector collector : getPacketCollectors()) {
            if (processMessage(collector, message)) {
                ret = true;
            }
        }

        return ret;
    }
    /**
     * Remove a packet collector of this connection.
     *
     * @param collector a packet collectors which was created for this connection.
     */
    protected void removePacketCollector(MessageCollector collector) {
        collectors.remove(collector);
    }

    /**
     * Get the collection of all packet collectors for this connection.
     *
     * @return a collection of packet collectors for this connection.
     */
    protected Collection<MessageCollector> getPacketCollectors() {
        return collectors;
    }

    /**
     * The delegate for Message Collector because the method(processMessage) of message collector
     * can't be accessed by subclass of client connection.
     * @param collector message collector.
     * @param message message
     * @return
     */
    protected boolean processMessage(MessageCollector collector, Message message) {
        return collector.processMessage(message);
    }

    public void addPropertyListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
    public void removePropertyListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * Dispose current connection object.
     * The subclass should implement the logic of disposing,
     * normally the connection object should be removed from cache.
     */
    public void dispose() {
       closeNetworkResource();
    }
}
