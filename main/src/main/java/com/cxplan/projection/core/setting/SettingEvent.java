package com.cxplan.projection.core.setting;

import java.beans.PropertyChangeEvent;

/**
 * @author Kenny
 * created on 2018/12/13
 */
public class SettingEvent extends PropertyChangeEvent {

    /**
     * Single item is changed.
     */
    public static final short TYPE_ITEM = 0;
    /**
     * a flag indicates whether current property is last one.
     * When you want to do one thing use all changed properties, this flag will be used.
     */
    public static final short TYPE_RESULT = 1;
    private short type;

    private String[] changedKeys;
    /**
     * Constructs a new {@code PropertyChangeEvent}.
     *
     * @param deviceId     The device ID
     * @param propertyName the programmatic name of the property that was changed
     * @param oldValue     the old value of the property
     * @param newValue     the new value of the property
     * @throws IllegalArgumentException if {@code source} is {@code null}
     */
    public SettingEvent(String deviceId, String propertyName, Object oldValue, Object newValue) {
        super(deviceId, propertyName, oldValue, newValue);
        type = TYPE_ITEM;
    }

    public SettingEvent(String deviceId, String[] changedKeys) {
        super(deviceId, "", null, null);
        type = TYPE_RESULT;
        this.changedKeys = changedKeys;
    }

    @Override
    public String getSource() {
        return (String) source;
    }

    public boolean isSystemSetting() {
        return getSource().equals("system");
    }

    public String[] getChangedKeys() {
        return changedKeys;
    }

    public boolean isResult() {
        return type == TYPE_RESULT;
    }

}
