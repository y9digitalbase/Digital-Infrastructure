package net.risesoft.y9.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

import net.risesoft.y9.Y9Context;

/**
 * 网络地址工具类
 * 
 * @author dingzhaojun
 * @author qinman
 * @author mengjuhua
 * @date 2022/2/10
 */
@Slf4j
public class InetAddressUtil {

    public static final String LOCALHOST = "127.0.0.1";

    public static final String ANYHOST = "0.0.0.0";
    private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}\\:\\d{1,5}$");
    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");
    public static Properties properties = new Properties();
    public static String ips = null;
    private static volatile InetAddress LOCAL_ADDRESS = null;

    static {
        Environment environment = Y9Context.getEnvironment();
        if (environment == null) {
            YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
            yaml.setResources(new ClassPathResource("application.yml"));
            properties = yaml.getObject();
            if (properties.isEmpty()) {
                getApplicationProperties();
            }
            ips = properties.getProperty("y9.internalIp");
        } else {
            ips = environment.getProperty("y9.internalIp");
        }
    }

    public static void getApplicationProperties() {
        try (InputStream inputStream =
            InetAddressUtil.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(inputStream);
        } catch (IOException ignored) {
        }

        if (properties.isEmpty()) {
            try (InputStream inputStream2 =
                InetAddressUtil.class.getClassLoader().getResourceAsStream("properties/application.properties")) {
                properties.load(inputStream2);
            } catch (IOException ignored) {
            }
        }

        if (properties.isEmpty()) {
            LOGGER.error("application.properties not found!");
            System.exit(0);
        }
    }

    // return ip to avoid lookup dns
    public static String getHostName(SocketAddress socketAddress) {
        if (socketAddress == null) {
            return null;
        }

        if (socketAddress instanceof InetSocketAddress) {
            InetAddress addr = ((InetSocketAddress)socketAddress).getAddress();
            if (addr != null) {
                return addr.getHostAddress();
            }
        }

        return null;
    }

    /**
     * {@link #getLocalAddress(Map)}
     * 
     * @return InetAddress IP地址
     */
    public static InetAddress getLocalAddress() {
        return getLocalAddress(null);
    }

    /**
     * <pre>
     * 查找策略：首先看是否已经查到 ip --{@literal>} hostname 对应的ip --{@literal>}  根据连接目标端口得到的本地ip --{@literal>}  轮询网卡
     * </pre>
     * 
     * @param destHostPorts host端口信息
     * @return InetAddress locaip
     */
    public static InetAddress getLocalAddress(Map<String, Integer> destHostPorts) {
        if (LOCAL_ADDRESS != null) {
            return LOCAL_ADDRESS;
        }

        InetAddress localAddress = getLocalAddressByHostname();
        if (!isValidAddress(localAddress)) {
            localAddress = getLocalAddressBySocket(destHostPorts);
        }

        if (!isValidAddress(localAddress)) {
            localAddress = getLocalAddressByNetworkInterface();
        }

        if (isValidAddress(localAddress)) {
            LOCAL_ADDRESS = localAddress;
        }

        return localAddress;
    }

    private static InetAddress getLocalAddressByHostname() {
        try {
            InetAddress localAddress = InetAddress.getLocalHost();
            if (isValidAddress(localAddress)) {
                return localAddress;
            }
        } catch (Throwable e) {
            LOGGER.warn("Failed to retrieve local address by hostname:" + e);
        }
        return null;
    }

    private static InetAddress getLocalAddressByNetworkInterface() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    try {
                        NetworkInterface network = interfaces.nextElement();
                        Enumeration<InetAddress> addresses = network.getInetAddresses();
                        while (addresses.hasMoreElements()) {
                            try {
                                InetAddress address = addresses.nextElement();
                                if (isValidAddress(address)) {
                                    return address;
                                }
                            } catch (Throwable e) {
                                LOGGER.warn("Failed to retrieve ip address", e);
                            }
                        }
                    } catch (Throwable e) {
                        LOGGER.warn("Failed to retrieve ip address", e);
                    }
                }
            }
        } catch (Throwable e) {
            LOGGER.warn("Failed to retrieve ip address", e);
        }
        return null;
    }

    private static InetAddress getLocalAddressBySocket(Map<String, Integer> destHostPorts) {
        if (destHostPorts == null || destHostPorts.size() == 0) {
            return null;
        }

        for (Map.Entry<String, Integer> entry : destHostPorts.entrySet()) {
            String host = entry.getKey();
            int port = entry.getValue();
            try (Socket socket = new Socket()) {
                SocketAddress addr = new InetSocketAddress(host, port);
                socket.connect(addr, 1000);
                return socket.getLocalAddress();
            } catch (Exception e) {
                LOGGER.warn(String.format(
                    "Failed to retrieve local address by connecting to dest host:port(%s:%s) false", host, port), e);
            }
        }
        return null;
    }

    public static boolean isInvalidLocalHost(String host) {
        return host == null || host.length() == 0 || "localhost".equalsIgnoreCase(host) || "0.0.0.0".equals(host)
            || (LOCAL_IP_PATTERN.matcher(host).matches());
    }

    public static boolean isValidAddress(InetAddress address) {
        if (address == null || address.isLoopbackAddress()) {
            return false;
        }

        String ipAddress = address.getHostAddress();
        boolean valid = ipAddress != null && !ANYHOST.equals(ipAddress) && !LOCALHOST.equals(ipAddress)
            && IP_PATTERN.matcher(ipAddress).matches();
        if (!valid) {
            return false;
        }

        // application.propters中y9.internalIp=192.168.1.x,10.0.x.x
        // String ips = Y9Context.getProperty("y9.internalIp");
        if (StringUtils.hasText(ips)) {
            valid = false;
            String[] arry = ips.toLowerCase().split(",");
            for (String ip : arry) {
                String ipSegment = ip;
                int i = ip.trim().indexOf(".x");
                if (i > -1) {
                    ipSegment = ip.substring(0, i);
                }
                if (ipAddress.contains(ipSegment)) {
                    valid = true;
                    break;
                }
            }
            return valid;
        }
        return true;
    }

    public static boolean isValidAddress(String address) {
        return ADDRESS_PATTERN.matcher(address).matches();
    }

    public static boolean isValidLocalHost(String host) {
        return !isInvalidLocalHost(host);
    }
}
