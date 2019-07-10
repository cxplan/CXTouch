package com.cxplan.projection.script;

import com.cxplan.projection.IApplication;
import com.cxplan.projection.script.command.ScriptCommand;
import com.cxplan.projection.script.command.TimeGapCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Kenny
 * created on 2019/3/13
 */
public class ScriptRecorder {

    private static final Logger logger = LoggerFactory.getLogger(ScriptRecorder.class);

    /**
     * key: The sequence number of command
     * value: The script command.
     */
    private LinkedHashMap<Integer, ScriptCommand> seq2CommandMap;
    private long startTime;
    private long endTime;
    private boolean running;
    /**
     * current sequence number of command event.
     * The start value is 0.
     */
    private volatile int num;

    private IApplication application;
    private String deviceId;

    //the list of listeners to script
    private CopyOnWriteArrayList<RecorderListener> listenerList;

    //the status fields for recording.
    private long lastEventTime = 0L;
    private CommandType lastCommandType;

    public ScriptRecorder(String deviceId, IApplication application) {
        this.deviceId = deviceId;
        this.application = application;
        startTime = 0;
        endTime = 0;
        running = false;
        seq2CommandMap = new LinkedHashMap<>();
        listenerList = new CopyOnWriteArrayList<>();
    }

    public int addEvent(ScriptCommand command) {
        command.setSequenceId(num);
        seq2CommandMap.put(num, command);
        lastEventTime = System.currentTimeMillis();
        lastCommandType = command.getType();
        //fire event
        fireCommandCreatedEvent(command);

        return num++;
    }

    public long getLastEventTime() {
        return lastEventTime;
    }

    public CommandType getLastCommandType() {
        return lastCommandType;
    }

    public void addRecorderListener(RecorderListener listener) {
        listenerList.add(listener);
    }

    public void removeRecorderListener(RecorderListener listener) {
        listenerList.remove(listener);
    }

    public void updateKeyView(int seqNum, ViewNode view) {
        if ( view == null) {
            throw new IllegalArgumentException("The view node is missing");
        }
        ScriptCommand command = seq2CommandMap.get(seqNum);
        if (command == null) {
            logger.error("The command is not found: " + seqNum);
            return;
        }

        command.setViewNode(view);
    }

    public void removeCommand(int seqNum) {

    }

    public void addTimeGap(int gap) {
        addEvent(new TimeGapCommand(gap));
    }

    public void clearCommands() {
        seq2CommandMap.clear();
        startTime = 0;
        endTime = 0;
    }

    public List<ScriptCommand> getCommandList() {
        return new ArrayList<>(seq2CommandMap.values());
    }

    public void start() {
        if (running) {
            logger.warn("The script recorder is running: " + deviceId);
            return;
        }

        running = true;
        startTime = System.currentTimeMillis();
        num = 0;
        seq2CommandMap.clear();
    }

    public void stop() {
        if (!validateCommands()) {
            throw new RuntimeException("Some commands may not be completed!");
        }
        running = false;
        endTime = System.currentTimeMillis();
    }

    public int getCurrentSequence() {
        return num - 1;
    }

    public boolean isRecording() {
        return running;
    }

    void fireCommandCreatedEvent(ScriptCommand command) {
        for (RecorderListener listener : listenerList) {
            listener.onCommandCreated(command);
        }
    }

    /**
     * Return true if all commands is legal, otherwise a false value will be returned.
     */
    boolean validateCommands() {
        for (ScriptCommand command : seq2CommandMap.values()) {
            if (command.isValidateView() && command.getViewNode() == null) {
                return false;
            }
        }

        return true;
    }

    public interface RecorderListener {

        void onCommandCreated(ScriptCommand command);
    }

}
