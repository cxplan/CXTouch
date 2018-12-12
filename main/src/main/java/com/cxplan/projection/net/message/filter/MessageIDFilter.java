package com.cxplan.projection.net.message.filter;


import com.cxplan.projection.core.connection.MessageFilter;
import com.cxplan.projection.net.message.Message;

/**
 * Filters for packets with a particular packet ID.
 *
 */
public class MessageIDFilter implements MessageFilter {

    private String packetID;
    private String cmd;

    /**
     * Creates a new packet ID filter using the specified packet ID.
     *
     * @param packetID the packet ID to filter for.
     */
    public MessageIDFilter(String packetID, String cmd) {
        if (packetID == null) {
            throw new IllegalArgumentException("Packet ID cannot be null.");
        }
        this.packetID = packetID;
        this.cmd = cmd;
    }

    public boolean accept(Message packet) {
        return packetID.equals(packet.getId());
    }

    public String toString() {
        return "MessageIDFilter by id: " + packetID;
    }
}
