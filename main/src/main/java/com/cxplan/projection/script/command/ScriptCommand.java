package com.cxplan.projection.script.command;

import com.cxplan.projection.IApplication;
import com.cxplan.projection.script.CommandType;
import com.cxplan.projection.script.ViewNode;
import com.cxplan.projection.ui.util.GUIUtil;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Kenny
 * created on 2019/3/13
 */
public abstract class ScriptCommand {

    protected CommandType type;
    /**
     * The key activity view for executing current command.
     * If this view is not presented on screen, this command should not be executed
     */
    private ViewNode viewNode;
    /**
     * Indicate whether a expected view should be validated, this view must be presented
     * on the window before executing this command.
     * If the value fo this variable is true, the variable {@link #viewNode} must be signed a not-null value.
     */
    private boolean validateView = false;
    //The sequence ID represented the sequence of this command in commands list.
    //warning: this sequence id may be changed when the structure of script is changed.
    private int sequenceId;

    public ScriptCommand(CommandType type) {
        this.type = type;
    }

    public int getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(int sequenceId) {
        this.sequenceId = sequenceId;
    }

    public CommandType getType() {
        return type;
    }

    public void setType(CommandType type) {
        this.type = type;
    }

    public void execute(IApplication application, String deviceId) throws ScriptException {

    }

    public ViewNode getViewNode() {
        return viewNode;
    }

    public void setViewNode(ViewNode viewNode) {
        this.viewNode = viewNode;
    }

    public void encode(DataOutputStream dataOutput) throws IOException {}

    public void decode(DataInputStream dataInput) throws IOException {}

    public boolean isValidateView() {
        return validateView;
    }

    public void setValidateView(boolean validateView) {
        this.validateView = validateView;
    }

    /**
     * To edit this command.
     * @return this command object will be returned if editing is successful, otherwise a null value will be returned.
     */
    public ScriptCommand toEdit(IApplication application, Window parent){
        GUIUtil.showInfoMessageDialog("Editing is not supported for this command ");
        return null;
    }
}
