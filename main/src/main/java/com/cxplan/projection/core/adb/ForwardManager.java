package com.cxplan.projection.core.adb;

import java.util.*;

/**
 * @author KennyLiu
 * @created on 2018/5/4
 */
public class ForwardManager {

    public static final int MESSAGE_PORT_START = 20000;
    public static final int MESSAGE_REMOTE_PORT = 2013;
    public static final int IMAGE_PORT_START = 30000;
    public static final int MONKEY_PORT_START = 40000;
    public static final int MONKEY_REMOTE_PORT = 12345;
    public static final String IMAGE_REMOTE_SOCKET_NAME = "minicap";
    private static ForwardManager instance;

    public static ForwardManager getInstance() {
        if (instance == null) {
            instance = new ForwardManager();
        }

        return instance;
    }

    /**
     * key: device id, value: message forward related with device.
     */
    private Map<String, DeviceForward> messageForwardMap;
    private Set<Integer> messagePortQueue;
    private int currentMessagePort;

    /**
     * key: device id, value: image forward related with device.
     */
    private Map<String, DeviceForward> imageForwardMap;
    private Set<Integer> imagePortQueue;
    private int currentImagePort;

    /**
     * key: device id, value: monkey forward related with device.
     */
    private Map<String, DeviceForward> monkeyForwardMap;
    private Set<Integer> monkeyPortQueue;
    private int currentMonkeyPort;


    private ForwardManager() {
        messageForwardMap = Collections.synchronizedMap(new HashMap<String, DeviceForward>());
        messagePortQueue = new HashSet<>();
        currentMessagePort = MESSAGE_PORT_START;

        imageForwardMap = Collections.synchronizedMap(new HashMap<String, DeviceForward>());
        imagePortQueue = new HashSet<>();
        currentImagePort = IMAGE_PORT_START;

        monkeyForwardMap = Collections.synchronizedMap(new HashMap<String, DeviceForward>());
        monkeyPortQueue = new HashSet<>();
        currentMonkeyPort = MONKEY_PORT_START;
    }

    public synchronized int putMessageForward(String deviceId) {
        DeviceForward forward = messageForwardMap.get(deviceId);
        if (forward == null) {
            forward = new DeviceForward();
            forward.setRemotePort(MESSAGE_REMOTE_PORT);
            forward.setLocalPort(takeMessagePort());
            messageForwardMap.put(deviceId, forward);
        }
        return forward.getLocalPort();
    }

    public synchronized DeviceForward removeMessageForward(String deviceId) {
        DeviceForward forward = messageForwardMap.remove(deviceId);
        if (forward == null) {
            return null;
        }
        returnPort(messagePortQueue, forward.getLocalPort());
        return forward;
    }
    public synchronized int putImageForward(String deviceId) {
        DeviceForward forward = imageForwardMap.get(deviceId);
        if (forward == null) {
            forward = new DeviceForward();
            forward.setRemoteSocketName(IMAGE_REMOTE_SOCKET_NAME);
            forward.setLocalPort(takeImagePort());
            imageForwardMap.put(deviceId, forward);
        }
        return forward.getLocalPort();
    }

    public synchronized DeviceForward removeImageForward(String deviceId) {
        DeviceForward forward = imageForwardMap.remove(deviceId);
        if (forward == null) {
            return null;
        }
        returnPort(imagePortQueue, forward.getLocalPort());
        return forward;
    }
    public synchronized int putMonkeyForward(String deviceId) {
        DeviceForward forward = monkeyForwardMap.get(deviceId);
        if (forward == null) {
            forward = new DeviceForward();
            forward.setRemotePort(MONKEY_REMOTE_PORT);
            forward.setLocalPort(takeMonkeyPort());
            monkeyForwardMap.put(deviceId, forward);
        }
        return forward.getLocalPort();
    }

    public synchronized DeviceForward removeMonkeyForward(String deviceId) {
        DeviceForward forward = monkeyForwardMap.remove(deviceId);
        if (forward == null) {
            return null;
        }
        returnPort(monkeyPortQueue, forward.getLocalPort());
        return forward;
    }

    private void returnPort(Set<Integer> portQueue, int localPort) {
        portQueue.add(localPort);
    }
    private int takeMessagePort() {
        int port = -1;
        for (int p : messagePortQueue) {
            port = p;
            break;
        }
        if (port != -1) {
            messagePortQueue.remove(port);
            return port;
        }

        if (currentMessagePort > 29999) {
            throw new RuntimeException("allocating message forward port failed: the max value is reached: " + currentMessagePort);
        }
        return currentMessagePort++;
    }
    private int takeImagePort() {
        int port = -1;
        for (int p : imagePortQueue) {
            port = p;
            break;
        }
        if (port != -1) {
            imagePortQueue.remove(port);
            return port;
        }

        if (currentImagePort > 39999) {
            throw new RuntimeException("allocating image forward port failed: the max value is reached: " + currentImagePort);
        }
        return currentImagePort++;
    }
    private int takeMonkeyPort() {
        int port = -1;
        for (int p : monkeyPortQueue) {
            port = p;
            break;
        }
        if (port != -1) {
            monkeyPortQueue.remove(port);
            return port;
        }

        if (currentMonkeyPort > 49999) {
            throw new RuntimeException("allocating monkey forward port failed: the max value is reached: " + currentMonkeyPort);
        }
        return currentMonkeyPort++;
    }

}
