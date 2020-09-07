/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.net;

import madgik.exareme.utils.properties.AdpProperties;
import org.apache.log4j.Logger;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class NetUtil {

    private static Logger log = Logger.getLogger(NetUtil.class);

    private NetUtil() {
        throw new RuntimeException("Cannot create instances of this class");
    }

    public static long getIPLongRepresentation(String ip) {
        String[] parts = ip.split("\\.");
        // If is a valid IP, split and create a unuque number from the address.
        if (parts.length == 4) {
            long id = 0;
            for (int i = 0; i < 4; ++i) {
                long pInt = Integer.parseInt(parts[i]);
                id += pInt << (8 * (3 - i));
            }
            return id;
        }
        return ip.hashCode();
    }

    public static String getIPv4() {
        // Return localhost in development environment
        if (AdpProperties.getEnvProps().getString("run_level").equals("develop")) {
            return "127.0.0.1";
        }
        // Return Djava.rmi.server.hostname if exists
        List<String> aList = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (String a : aList) {
            if (a.startsWith("-Djava.rmi.server.hostname")) {
                log.debug("netutil bef split $$$ " + a);
                return a.split("=")[1].trim();
            }
        }
        // Guess the ip from network interfaces
        String privIp = getPrivateIp();
        String pubIp = getPublicIp();
        if (privIp.equals("127.0.0.1") == false) {
            log.debug("ret privip $$$ " + privIp);
            return privIp;
        } else {
            log.debug("nret pubip $$$ " + pubIp);
            return pubIp;
        }
    }

    /**
     * Get the private ip of the localhost.
     *
     * @return the private ip of the localhost.
     */
    private static String getPrivateIp() {
        try {
            Enumeration<NetworkInterface> nienum = NetworkInterface.getNetworkInterfaces();
            while (nienum.hasMoreElements()) {
                NetworkInterface i = nienum.nextElement();
                Enumeration<InetAddress> addresses = i.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    String ipAddress = addr.getHostAddress();
                    if (isIpAddress(ipAddress) && isPrivate(ipAddress)) {
                        return ipAddress;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error reading ip", e);
        }
        return "127.0.0.1";
    }

    /**
     * Get the public ip of the localhost.
     *
     * @return the public ip of the localhost.
     */
    private static String getPublicIp() {
        try {
            Enumeration<NetworkInterface> nienum = NetworkInterface.getNetworkInterfaces();
            while (nienum.hasMoreElements()) {
                NetworkInterface i = nienum.nextElement();
                Enumeration<InetAddress> addresses = i.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    String ipAddress = addr.getHostAddress();
                    if (!ipAddress.equalsIgnoreCase("127.0.0.1")) {
                        if (isIpAddress(ipAddress) && !isPrivate(ipAddress)) {
                            return ipAddress;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error reading ip", e);
        }
        return "127.0.0.1";
    }

    private static boolean isIpAddress(String address) {
        return address.matches("\\d\\d?\\d?\\.\\d\\d?\\d?\\.\\d\\d?\\d?\\.\\d\\d?\\d?");
    }

    private static boolean isPrivate(String ip) {
        if (ip.startsWith("10.")) {
            return true;
        }
        if (ip.startsWith("172.")) {
            String[] parts = ip.split("\\.");
            try {
                int secondPart = Integer.parseInt(parts[1]);
                if (secondPart >= 16 && secondPart <= 31) {
                    return true;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return ip.startsWith("192.168.");
    }
}
