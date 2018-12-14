package com.cxplan.projection.core.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author KennyLiu
 * @created on 2018/6/4
 */
public class ImageSessionManager {

    private static final Logger logger = LoggerFactory.getLogger(ImageSessionManager.class);

    private static ImageSessionManager instance;

    public static ImageSessionManager getInstance() {
        if (instance == null) {
            instance = new ImageSessionManager();
        }

        return instance;
    }

    private Map<String, Map<ImageSessionID, IImageSession>> nodeSetMap;
    private ImageSessionManager() {
        nodeSetMap = new ConcurrentHashMap<>();
    }

    public void addImageSession(String deviceId, IImageSession session) {
        Map<ImageSessionID, IImageSession> account2SessionMap = nodeSetMap.get(deviceId);
        if (account2SessionMap == null) {
            account2SessionMap = Collections.synchronizedMap(new HashMap<ImageSessionID, IImageSession>());
            nodeSetMap.put(deviceId, account2SessionMap);
        }

        IImageSession oldSession = account2SessionMap.put(session.getSessionID(), session);
        if (oldSession != null) {
            try {
                oldSession.close();
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    public void removeImageSession(String deviceId, ImageSessionID sessionID) {
        Map<ImageSessionID, IImageSession> account2SessionMap = nodeSetMap.get(deviceId);
        if (account2SessionMap == null) {
            return;
        }
        IImageSession oldSession = account2SessionMap.remove(sessionID);
        if (oldSession != null) {
            try {
                oldSession.close();
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    /**
     * Dispatch image frame to all nodes which accept specified device screenT=.
     * @return true: one or more then one nodes are receiving image.
     *         false: there is no node found.
     */
    public boolean fireImage(String deviceId, byte[] bytes, int length) {
        Map<ImageSessionID, IImageSession> account2SessionMap = nodeSetMap.get(deviceId);
        if (account2SessionMap == null) {
            return false;
        }
        synchronized (account2SessionMap) {
            Iterator<Map.Entry<ImageSessionID, IImageSession>> it = account2SessionMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<ImageSessionID, IImageSession> entry = it.next();
                IImageSession session = entry.getValue();
                try {
                    session.writeImageData(bytes, 0, length);
                } catch (Exception e) {
                    session.close();
                    it.remove();//the connection is broken.
                    System.out.println("remove node[exception]:" + session.getSessionID() + ", current size:" + account2SessionMap.size());
                }
            }
        }

        return account2SessionMap.size() > 0;
    }
}
