
package com.cxplan.projection.core.adb;

import com.android.ddmlib.IShellOutputReceiver;

/**
 * @author kenny
 */
public class CommandOutputCapture implements IShellOutputReceiver {
    private final StringBuilder builder = new StringBuilder();

    public CommandOutputCapture() {
    }

    public void flush() {
    }

    public boolean isCancelled() {
        return false;
    }

    public void addOutput(byte[] data, int offset, int length) {
        String message = new String(data, offset, length);
        this.builder.append(message);
    }

    public String toString() {
        return this.builder.toString();
    }
}
