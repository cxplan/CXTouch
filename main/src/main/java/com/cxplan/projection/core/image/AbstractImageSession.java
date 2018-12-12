package com.cxplan.projection.core.image;

/**
 * @author Kenny
 * created on 2018/11/22
 */
public abstract class AbstractImageSession implements IImageSession {

    protected ImageSessionID sessionID;

    public AbstractImageSession(ImageSessionID sessionID) {
        this.sessionID = sessionID;
    }

    @Override
    public ImageSessionID getSessionID() {
        return sessionID;
    }

    public void setSessionID(ImageSessionID sessionID) {
        this.sessionID = sessionID;
    }
}
