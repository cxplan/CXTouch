package com.cxplan.projection.core.command;

import com.cxplan.projection.IApplication;
import com.cxplan.projection.core.connection.ClientConnection;
import org.apache.mina.core.session.IoSession;

/**
 * Created on 2017/5/9.
 *
 * @author kenny
 */
public abstract class AbstractCommandHandler implements ICommandHandler {

    private String command;
    protected IApplication application;

    public AbstractCommandHandler(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public IApplication getApplication() {
        return application;
    }

    public void setApplication(IApplication application) {
        this.application = application;
    }

    protected <T extends ClientConnection> T getConnection(IoSession session) {
        return (T) session.getAttribute(IApplication.CLIENT_SESSION);
    }
}
