package com.cxplan.projection.core.adb;

/**
 * Describe the recording file.
 *
 * @author Kenny
 * created on 2018/12/26
 */
public class RecordMeta {

    private String file;
    private long size;

    public RecordMeta(String file, long size) {
        this.file = file;
        this.size = size;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
