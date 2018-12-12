/**
 * The code is written by ytx, and is confidential.
 * Anybody must not broadcast these files without authorization.
 */
package com.cxplan.projection.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created on 2017/4/12.
 *
 * @author kenny
 */
public class NetUtil {

    /**
     * Return the ip address of local host.
     */
    public static String getLocalIp() throws SocketException {
        Enumeration<NetworkInterface> enu = NetworkInterface.getNetworkInterfaces();
        while (enu.hasMoreElements()) {
            NetworkInterface ni = enu.nextElement();
            Enumeration<InetAddress> addressList = ni.getInetAddresses();
            while (addressList.hasMoreElements()) {
                InetAddress address = addressList.nextElement();
                String ip = address.getHostAddress();
                int dotIndex = ip.indexOf(".");
                if (dotIndex < 0) {
                    continue;
                }
                if (ip.startsWith("127")) {
                    continue;
                }

                return ip;
            }
        }

        return null;
    }
}
