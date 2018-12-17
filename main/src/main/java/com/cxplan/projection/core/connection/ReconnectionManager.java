package com.cxplan.projection.core.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Kenny
 * created on 2018/11/20
 */
public class ReconnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(ReconnectionManager.class);
    // the connections need to be reconnected.
    private Set<ConnectionMeta> connectionSet;
    private Set<ConnectionMeta> waitingSet;
    private Thread reconnectionThread;
    protected String title;

    // Holds the state of the reconnection
    protected boolean done = true;
    protected int maxTryCount = 20000;

    protected boolean isInLoop;

    public ReconnectionManager(String title) {
        this.title = title;
        connectionSet = Collections.synchronizedSet(new HashSet<ConnectionMeta>());
        waitingSet = Collections.synchronizedSet(new HashSet<ConnectionMeta>());
        isInLoop = false;
    }

    public void openMonitor() {
        done = false;
    }
    public void closeMonitor() {
        done = true;
    }

    public boolean isMonitoring() {
        return !done;
    }

    /**
     * Add a connection to reconnect queue. The connection should wait for next attempt
     * if the reconnect thread is in loop.
     *
     * The return value indicates whether given connection is in reconnect queue.
     *
     * @param connection the connection object should be reconnected to service.
     * @return the flag indicates whether given connection is in reconnect queue.
     */
    public boolean addConnection(ClientConnection connection) {
        String id = connection.getJId().getId();
        if (!isReconnectionAllowed(connection)) {
            logger.info("The {} ({}) need not to be reconnected" , title, id);
            return false;
        }

        ConnectionMeta meta = new ConnectionMeta();
        meta.connection = connection;
        meta.lastAttemptTime = System.currentTimeMillis();
        if (isInLoop) {
            if (connectionSet.contains(meta)) {
                logger.info("The {} ({}) is executing reconnection operation.", title, id);
            } else {
                if (waitingSet.add(meta)) {
                    logger.info("The {} ({}) should wait for next attempt.", title, id);
                } else {
                    logger.info("The {} ({}) should wait for next attempt(already in waiting queue).", title, id);
                }
            }

            return true;
        }
        if (connectionSet.add(meta)) {
            logger.info("The {} ({}) is added to reconnection queue." , title, id);
            reconnect();
        } else {
            logger.info("The {} ({}) exists in queue already!", title, id);
        }
        return true;
    }

    /**
     * Returns true if the reconnection mechanism is enabled.
     *
     * @return true if automatic reconnections are allowed.
     */
    protected boolean isReconnectionAllowed(ClientConnection connection) {
        if (connection == null) {
            return false;
        }
        if (done) {
            return false;
        }
        if (isConnected(connection)) {
            removeReconnect(connection, true);
            return false;
        } else {
            return true;
        }
    }

    private boolean isConnected(ClientConnection connection) {
        return connection != null && connection.isConnected();
    }

    /**
     * Starts a reconnection mechanism if it was configured to do that.
     * The algorithm is been executed when the first connection error is detected.
     * <p/>
     * The reconnection mechanism will try to reconnect periodically in this way:
     * <ol>
     * <li>First it will try 6 times every 10 seconds.
     * <li>Then it will try 10 times every 1 minute.
     * <li>Finally it will try indefinitely every 5 minutes.
     * </ol>
     */
    synchronized protected void reconnect() {
        // Since there is no thread running, creates a new one to attempt
        // the reconnection.
        // avoid to run duplicated reconnectionThread -- fd: 16/09/2010
        if (reconnectionThread!=null && reconnectionThread.isAlive()) return;

        reconnectionThread = new Thread("Device Reconnection monitor") {

            /**
             * The process will try the reconnection until the connection succeed or the user
             * cancell it
             */
            public void run() {
                Map<String, Integer> tryCountMap = new HashMap<>();
                while (true) {
                    if (connectionSet.size() > 0) {
                        List<ConnectionMeta> connectionList;
                        try {
                            connectionList = new ArrayList<>(connectionSet);
                        } catch (Exception ex) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                            }
                            continue;
                        }

                        isInLoop = true;
                        for (ConnectionMeta meta : connectionList) {
                            Integer tryCount = tryCountMap.get(meta.connection.getJId().getId());
                            if (tryCount == null) {
                                tryCount = 0;
                            }
                            if (isReconnectionAllowed(meta.connection) && meta.isTimeUp()) {
                                try {
                                    meta.lastAttemptTime = System.currentTimeMillis();
                                    if (!isConnected(meta.connection)) {
                                        meta.connection.connect();
                                    }
                                } catch (Exception e) {
                                    logger.error("[" + meta.connection.getJId().getId() + "]尝试重新连接手机服务失败: " + e.getMessage(), e);
                                }finally {
                                    tryCount++;
                                    logger.info("[" + meta.connection.getJId().getId() + "]try to reconnect: " + tryCount);
                                    if (tryCount > maxTryCount) {
                                        connectionSet.remove(meta);
                                    } else {
                                        tryCountMap.put(meta.connection.getJId().getId(), tryCount);
                                    }
                                }
                            }
                        }
                        isInLoop = false;
                        if (waitingSet.size() > 0) {
                            logger.info("add waiting connection: " + waitingSet.size());
                            connectionSet.addAll(waitingSet);
                            waitingSet.clear();
                        }
                    } else {
                        logger.info("Reconnection is finished!");
                        break;
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }

                }

            }
        };
        reconnectionThread.setName(title + " Reconnection Manager");
        reconnectionThread.setDaemon(true);
        reconnectionThread.start();
    }

    /**
     * Remove given connection object from reconnect queue.
     * This method will be invoked when reconnection is finished or aborted, parameter 'finished' indicates two cases.
     *
     * @param connection connection shoule be removed.
     * @param finished the case flag. true: reconnection is finished successfully, false: aborted.
     */
    public void removeReconnect(ClientConnection connection, boolean finished) {
        ConnectionMeta meta = new ConnectionMeta();
        meta.connection = connection;
        if (connectionSet.remove(meta)) {
            if (finished) {
                logger.info("The {} reconnection is successfully: {}", title, connection.getJId().getId());
            } else {
                logger.info("The reconnection to {}({}) should be ignored: the {} is removed",
                        title, meta.connection.getJId().getId(), title);
            }
        }
        if (waitingSet.remove(meta)) {
            if (finished) {
                logger.info("The {}({}) reconnection should be ignored: the {} has connected to controller.",
                        title, connection.getJId().getId());
            } else {
                logger.info("The {}({}) is removed from waiting queue: the {} is onRemoved",
                        title, meta.connection.getJId().getId(), title);
            }
        }
    }
    protected class ConnectionMeta {
        public ClientConnection connection;
        /**
         * Holds the current number of reconnection attempts
         */
        int attempts = 0;

        long lastAttemptTime = 0;

        public ConnectionMeta(){}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ConnectionMeta)) return false;

            ConnectionMeta that = (ConnectionMeta) o;

            return connection.getJId().getId().equals(that.connection.getJId().getId());
        }

        @Override
        public int hashCode() {
            return connection.getJId().getId().hashCode();
        }

        public boolean isTimeUp() {
            return (System.currentTimeMillis() - lastAttemptTime) >= (timeDelay()*1000);
        }
        /**
         * Returns the number of seconds until the next reconnection attempt.
         *
         * @return the number of seconds until the next reconnection attempt.
         */
        private int timeDelay() {
            attempts++;
            if (attempts < 3) {
                return 1;
            } else if (attempts < 100) {
                return 5;
            } else {
                return 10;
            }
        }
    }
}
