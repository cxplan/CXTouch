package com.cxplan.projection.core.setting;

import com.cxplan.projection.util.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class ParamProperties extends Properties {

	private static final long serialVersionUID = 1L;
	private Map<String,List<ConfigChangedListener>> changeListeners = new HashMap<String,List<ConfigChangedListener>>();

	private String deviceId;
	protected ParamProperties(String deviceId) {
		this.deviceId = deviceId;
	}

	public int getIntProperty(String property, int defaultValue) {
		String value = this.getProperty(property, null);
		if (value == null)
			return defaultValue;
		return StringUtil.getIntValue(value, defaultValue);
	}
	public long getLongProperty(String property, long defaultValue) {
		String value = this.getProperty(property, null);
		if (value == null)
			return defaultValue;
		return StringUtil.getLongValue(value, defaultValue);
	}
	public boolean getBoolProperty(String property, boolean defaultValue) {
		String value = this.getProperty(property, null);
		if (value == null)
			return defaultValue;
		return StringUtil.stringToBool(value);
	}
	public float getFloatProperty(String property, float defaultValue) {
		String value = this.getProperty(property, null);
		if (value == null)
			return defaultValue;
		return StringUtil.getFloatValue(value, defaultValue);
	}
	private Integer getIntProperty(String property) {
		String value = this.getProperty(property, null);
		if (value == null)
			return null;
		return Integer.parseInt(value);
	}
	private Long getLongProperty(String property) {
		String value = this.getProperty(property, null);
		if (value == null)
			return null;
		return Long.parseLong(value);
	}
	private Boolean getBoolProperty(String property) {
		String value = this.getProperty(property, null);
		if (value == null)
			return null;
		return StringUtil.stringToBool(value);
	}
	private Float getFloatProperty(String property) {
		String value = this.getProperty(property, null);
		if (value == null)
			return null;
		return Float.parseFloat(value);
	}
	private Double getDoubleProperty(String property) {
		String value = this.getProperty(property, null);
		if (value == null)
			return null;
		return Double.parseDouble(value);
	}
	private Short getShortProperty(String property) {
		String value = this.getProperty(property, null);
		if (value == null)
			return null;
		return Short.parseShort(value);
	}

	public Integer setIntProperty(String property, int value) {
		Integer oldValue = getIntProperty(property);
		this.doSetProperty(property, Integer.toString(value));
		firePropertyChanged(property, oldValue, value);

		return oldValue;
	}

	public Boolean setBooleanProperty(String property, boolean value) {
		Boolean oldValue = getBoolProperty(property);
		this.doSetProperty(property, Boolean.toString(value));
		firePropertyChanged(property, oldValue, value);

		return oldValue;
	}
	public Long setLongProperty(String property, long value) {
		Long oldValue = getLongProperty(property);
		this.doSetProperty(property, Long.toString(value));
		firePropertyChanged(property, oldValue, value);

		return oldValue;
	}
	public Float setFloatProperty(String property, float value) {
		Float oldValue = getFloatProperty(property);
		this.doSetProperty(property, Float.toString(value));
		firePropertyChanged(property, oldValue, value);

		return oldValue;
	}
	public Double setDoubleProperty(String property, double value) {
		Double oldValue = getDoubleProperty(property);
		this.doSetProperty(property, Double.toString(value));
		firePropertyChanged(property, oldValue, value);

		return oldValue;
	}
	public Short setShortProperty(String property, short value) {
		Short oldValue = getShortProperty(property);
		this.doSetProperty(property, Short.toString(value));
		firePropertyChanged(property, oldValue, value);

		return oldValue;
	}
	public String setProperty(String name, String value) {
		String oldValue = doSetProperty(name, value);
		firePropertyChanged(name, oldValue, value);

		return oldValue;
	}

	protected String doSetProperty(String name, String value) {
		if (name == null)
			return null;

		String oldValue;

		synchronized (this) {
			if (value == null) {
				super.remove(name);
				return null;
			}
			oldValue = (String) super.setProperty(name, value);
		}

		return oldValue;
	}
	/**
	 * Adds a property definition in the form key=value Lines starting with #
	 * are ignored Lines that do not contain a = character are ignored Any text
	 * after a # sign in the value is ignored
	 */
	public void addPropertyDefinition(String line) {
		if (line == null)
			return;
		if (line.trim().length() == 0)
			return;
		if (line.startsWith("#"))
			return;
		int pos = line.indexOf("=");
		if (pos == -1)
			return;
		String key = line.substring(0, pos);
		String value = line.substring(pos + 1);
		pos = value.indexOf('#');
		if (pos > -1) {
			value = value.substring(0, pos);
		}
		this.setProperty(key, value.trim());
	}

	/**
	 * Read the content of the file into this properties object. This method
	 * does not support line continuation, but supports an encoding (as opposed
	 * to the original properties class)
	 * 
	 */
	public void loadTextFile(String filename)
			throws IOException {
		File f = new File(filename);
		if (!f.exists()) {
		    return;
        }
        FileInputStream inputStream = null;
		try {
		    inputStream = new FileInputStream(f);
		    load(inputStream);
		} finally {
		    if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable th) {
                }
            }
		}
	}

	public synchronized void saveToFile(String filename) throws IOException {
		FileOutputStream out = null;
		File fileObj = new File(filename);
		if (!fileObj.getParentFile().exists()) {
            fileObj.getParentFile().mkdirs();
        }
		try {
			out = new FileOutputStream(fileObj);
			this.store(out, new Date().toString());
		} finally {
		    if (out != null) {
		        try {
                    out.close();
                } catch (Exception ex){}
            }
		}
	}

	private void firePropertyChanged(String name, Object oldValue, Object newValue) {
		Setting.getInstance().firePropertyChanged(deviceId, name, oldValue, newValue);
	}

	private String getSections(String aString, int aNum) {
		int pos = aString.indexOf(".");
		String result = null;
		for (int i = 1; i < aNum; i++) {
			int pos2 = aString.indexOf('.', pos + 1);
			if (pos2 > -1) {
				pos = pos2;
			} else {
				if (i == (aNum - 1)) {
					pos = aString.length();
				}
			}
		}
		result = aString.substring(0, pos);
		return result;
	}
}
