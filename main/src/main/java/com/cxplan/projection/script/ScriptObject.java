package com.cxplan.projection.script;

import com.cxplan.projection.script.command.ScriptCommand;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Kenny
 * created on 2019/3/29
 */
public class ScriptObject {

    /**
     * Load script object from local file.
     *
     * @param scriptFile the path script file.
     * @return script object.
     * @throws IOException
     */
    public static ScriptObject load(String scriptFile) throws IOException {
        File file  = new File(scriptFile);
        if (!file.exists()) {
            throw new IOException("The script file is not found: " + scriptFile);
        }

        return load(new FileInputStream(file));
    }

    /**
     * Load script object from input stream.
     *
     * @return script object.
     * @throws IOException
     */
    public static ScriptObject load(InputStream inputStream) throws IOException {
        LinkedList<ScriptCommand> commandList = loadCommandList(inputStream);
        ScriptObject script = new ScriptObject(commandList);
        return script;
    }

    private static LinkedList<ScriptCommand> loadCommandList(InputStream inputStream) throws IOException {
        DataInputStream bis = new DataInputStream(inputStream);
        LinkedList<ScriptCommand> commandList = new LinkedList<>();

        int seq = 0;
        while (bis.available() > 0) {
            int commandType = bis.readInt();
            ScriptCommand sc = ScriptUtil.createCommand(CommandType.getType(commandType));
            sc.decode(bis);
            sc.setSequenceId(seq++);

            commandList.addLast(sc);
        }

        return commandList;
    }


    private LinkedList<ScriptCommand> eventList;

    public ScriptObject(List<ScriptCommand> eventList) {
        this.eventList = new LinkedList<>(eventList);
    }

    public ScriptObject(String scriptFile) {
        File file  = new File(scriptFile);
        if (!file.exists()) {
            throw new RuntimeException("The script file is not found: " + scriptFile);
        }

        try {
            eventList = loadCommandList(new FileInputStream(file));
        } catch (IOException e) {
            throw new RuntimeException("The format of script file is illegal: " + e.getMessage(), e);
        }
    }

    public LinkedList<ScriptCommand> getCommandList() {
        return new LinkedList<>(eventList);
    }

    public void export(OutputStream outputStream) throws IOException {
        if (eventList == null) {
            return;
        }
        DataOutputStream dataOutput = new DataOutputStream(outputStream);

        Iterator<ScriptCommand> it = eventList.iterator();
        while (it.hasNext()) {
            ScriptCommand sc = it.next();
            dataOutput.writeInt(sc.getType().getValue());

            sc.encode(dataOutput);
        }
    }

}
