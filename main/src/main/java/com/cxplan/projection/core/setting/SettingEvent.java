package com.cxplan.projection.core.setting;

import java.beans.PropertyChangeEvent;

/**
 * @author Kenny
 * created on 2018/12/13
 */
public class SettingEvent extends PropertyChangeEvent {
    /**
     * Constructs a new {@code PropertyChangeEvent}.
     *
     * @param source       the bean that fired the event
     * @param propertyName the programmatic name of the property that was changed
     * @param oldValue     the old value of the property
     * @param newValue     the new value of the property
     * @throws IllegalArgumentException if {@code source} is {@code null}
     */
    public SettingEvent(Object source, String propertyName, Object oldValue, Object newValue) {
        super(source, propertyName, oldValue, newValue);
    }

    @Override
    public String getSource() {
        return (String) source;
    }

    public boolean isSystemSetting() {
        return getSource().equals("system");
    }
}
