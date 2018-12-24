package com.cxplan.projection.core.setting;

import com.cxplan.projection.ui.util.GUIUtil;
import com.cxplan.projection.util.StringUtil;
import com.cxplan.projection.util.SystemUtil;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created on 2017/5/31.
 *
 * @author kenny
 */
public class Setting {

    private static Setting setting = null;

    private ParamProperties systemProp;
    private Map<String, ParamProperties> devicePropMap;
    private List<ConfigChangedListener> listenerList;

    private String configDir;

    private String filename;

    private Setting() {
        systemProp = new ParamProperties("system");
        devicePropMap = new HashMap<>();
        listenerList = new CopyOnWriteArrayList<>();
        configDir = SystemUtil.basePath;
        this.filename = SystemUtil.SETTING_FILE;

        try {
            systemProp.loadTextFile(this.filename);
        } catch (IOException e) {
            SystemUtil.error("Loading system setting failed:" + e.getMessage(), e);
            systemProp = new ParamProperties("system");
        }
    }


    public synchronized static Setting getInstance() {
        if (setting == null) {
            setting = new Setting();
        }
        return setting;
    }

    public void reload() {

    }

    public void saveSystemSetting() {
        //system
        try {
            systemProp.saveToFile(this.filename);
        } catch (IOException e) {
            SystemUtil.error(e.getMessage(), e);
        }

        //device
        if (devicePropMap.size() > 0) {
            for (String deviceId : devicePropMap.keySet()) {
                saveDeviceSetting(deviceId);
            }
        }
    }

    //--------------------system setting------------------------
    public void putProperty(String name, String value) {
        this.systemProp.setProperty(name, value);
    }
    public void putBooleanProperty(String name, boolean value) {
        this.systemProp.setBooleanProperty(name, value);
    }
    public void putIntProperty(String name, int value) {
        this.systemProp.setIntProperty(name, value);
    }
    public void putLongProperty(String name, long value) {
        this.systemProp.setLongProperty(name, value);
    }

    public String getProperty(String aProperty, String aDefault) {
        return System.getProperty(aProperty, this.systemProp.getProperty(aProperty, aDefault));
    }
    public int getIntProperty(String property, int defaultValue) {
        return this.systemProp.getIntProperty(property, defaultValue);
    }
    public boolean getBooleanProperty(String property, boolean defaultValue) {
        return this.systemProp.getBoolProperty(property, defaultValue);
    }
    public long getLongProperty(String property, long defaultValue) {
        return this.systemProp.getLongProperty(property, defaultValue);
    }
    //--------------------device setting------------------------
    public boolean putProperty(String deviceId, String name, String value) {
        if (StringUtil.isEmpty(deviceId)) {
            return false;
        }
        ParamProperties properties = devicePropMap.get(deviceId);
        if (properties == null) {
            properties = new ParamProperties(deviceId);
            devicePropMap.put(deviceId, properties);
        }
        String oldValue = properties.setProperty(name, value);

        if (oldValue != null && value != null && oldValue.equals(value)) {
            return false;
        } else {
            return true;
        }
    }
    public boolean putBooleanProperty(String deviceId, String name, boolean value) {
        if (StringUtil.isEmpty(deviceId)) {
            return false;
        }
        ParamProperties properties = devicePropMap.get(deviceId);
        if (properties == null) {
            properties = new ParamProperties(deviceId);
            devicePropMap.put(deviceId, properties);
        }
        Boolean oldValue = properties.setBooleanProperty(name, value);
        Boolean newValue = value;
        if (oldValue != null && oldValue.equals(newValue)) {
            return false;
        } else {
            return true;
        }
    }
    public boolean putIntProperty(String deviceId, String name, int value) {
        if (StringUtil.isEmpty(deviceId)) {
            return false;
        }
        ParamProperties properties = devicePropMap.get(deviceId);
        if (properties == null) {
            properties = new ParamProperties(deviceId);
            devicePropMap.put(deviceId, properties);
        }
        Integer oldValue = properties.setIntProperty(name, value);
        Integer newValue = value;
        if (oldValue != null && oldValue.equals(newValue)) {
            return false;
        } else {
            return true;
        }
    }
    public boolean putFloatProperty(String deviceId, String name, float value) {
        if (StringUtil.isEmpty(deviceId)) {
            return false;
        }
        ParamProperties properties = devicePropMap.get(deviceId);
        if (properties == null) {
            properties = new ParamProperties(deviceId);
            devicePropMap.put(deviceId, properties);
        }
        Float oldValue = properties.setFloatProperty(name, value);
        Float newValue = value;
        if (oldValue != null && oldValue.equals(newValue)) {
            return false;
        } else {
            return true;
        }
    }
    public boolean putLongProperty(String deviceId, String name, long value) {
        if (StringUtil.isEmpty(deviceId)) {
            return false;
        }
        ParamProperties properties = devicePropMap.get(deviceId);
        if (properties == null) {
            properties = new ParamProperties(deviceId);
            devicePropMap.put(deviceId, properties);
        }
        Long oldValue = properties.setLongProperty(name, value);
        Long newValue = value;
        if (oldValue != null && oldValue.equals(newValue)) {
            return false;
        } else {
            return true;
        }
    }

    public String getProperty(String deviceId, String aProperty, String aDefault) {
        if (StringUtil.isEmpty(deviceId)) {
            return aDefault;
        }
        ParamProperties properties = devicePropMap.get(deviceId);
        if (properties == null) {
            return aDefault;
        }
        return properties.getProperty(aProperty, aDefault);
    }
    public int getIntProperty(String deviceId, String property, int defaultValue) {
        if (StringUtil.isEmpty(deviceId)) {
            return defaultValue;
        }
        ParamProperties properties = devicePropMap.get(deviceId);
        if (properties == null) {
            return defaultValue;
        }
        return properties.getIntProperty(property, defaultValue);
    }
    public boolean getBooleanProperty(String deviceId, String property, boolean defaultValue) {
        if (StringUtil.isEmpty(deviceId)) {
            return defaultValue;
        }
        ParamProperties properties = devicePropMap.get(deviceId);
        if (properties == null) {
            return defaultValue;
        }
        return properties.getBoolProperty(property, defaultValue);
    }
    public float getFloatProperty(String deviceId, String property, float defaultValue) {
        if (StringUtil.isEmpty(deviceId)) {
            return defaultValue;
        }
        ParamProperties properties = devicePropMap.get(deviceId);
        if (properties == null) {
            return defaultValue;
        }
        return properties.getFloatProperty(property, defaultValue);
    }
    public long getLongProperty(String deviceId, String property, long defaultValue) {
        if (StringUtil.isEmpty(deviceId)) {
            return defaultValue;
        }
        ParamProperties properties = devicePropMap.get(deviceId);
        if (properties == null) {
            return defaultValue;
        }
        return properties.getLongProperty(property, defaultValue);
    }

    public void addPropertyChangeListener(ConfigChangedListener aListener) {
        listenerList.add(aListener);
    }

    public void removePropertyChangeListener(ConfigChangedListener aListener) {
        listenerList.remove(aListener);
    }

    void firePropertyChanged(String deviceId, String name, Object oldValue,
                                     Object newValue) {
        if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
            return;
        }
        if (listenerList.size() == 0) {
            return;
        }
        // Making a shallow copy of the list prevents a
        // ConcurrentModificationException
        SettingEvent event = new SettingEvent(deviceId, name, oldValue, newValue);
        for (ConfigChangedListener l : listenerList) {
            l.changed(event);
        }
    }
    public void fireSettingResult(String deviceId, String[] changedKeys) {
        if (changedKeys == null || changedKeys.length == 0) {
            return;
        }
        if (listenerList.size() == 0) {
            return;
        }
        // Making a shallow copy of the list prevents a
        // ConcurrentModificationException
        SettingEvent event = new SettingEvent(deviceId, changedKeys);
        for (ConfigChangedListener l : listenerList) {
            l.changed(event);
        }
    }

    public void saveDeviceSetting(String deviceId) {
        ParamProperties properties = devicePropMap.get(deviceId);
        if (properties != null) {
            String deviceFileName = configDir + SystemUtil.separator + deviceId + ".setting";
            try {
                properties.saveToFile(deviceFileName);
            } catch (IOException e) {
                SystemUtil.error(e.getMessage(), e);
            }
        }
    }

    public void loadDeviceSetting(String deviceId) {
        if (StringUtil.isEmpty(deviceId)) {
            return;
        }
        if (devicePropMap.containsKey(deviceId)) {
            return;
        }

        String deviceFileName = configDir + SystemUtil.separator + deviceId + ".setting";
        ParamProperties properties = new ParamProperties(deviceId);
        try {
            properties.loadTextFile(deviceFileName);
        } catch (IOException e) {
            SystemUtil.error(e.getMessage(), e);
            return;
        }

        devicePropMap.put(deviceId, properties);
    }
}
