package org.forchange.canal.infrastructure.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * 雪花算法IdWorker使用唯一组ID生成策略
 */
public class IdWorkerUtil {
    private static final Logger logger = LoggerFactory.getLogger(IdWorkerUtil.class);
    private static InetAddress inetAddress;
    private static long addr_t;
    private static long addr_f;
    private static int MIN = 0;
    private static int MAX = 31;

    static {
        try {
            inetAddress = getLocalHostLANAddress();
            String[] ipArray = inetAddress.getHostAddress().split("\\.");
            addr_t = Long.valueOf(ipArray[2]);
            addr_f = Long.valueOf(ipArray[3]);
        } catch (UnknownHostException e) {
            logger.error("IdWorkerUtil Get Ip Address Exception {}", e.getMessage());
        }
    }

    /**
     * 获取本地IP地址 (虚拟机排除获取127.0.0.1的情况)
     *
     * @return
     * @throws UnknownHostException
     */
    public static InetAddress getLocalHostLANAddress() throws UnknownHostException {
        try {
            InetAddress candidateAddress = null;
            // 遍历所有的网络接口
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // 在所有的接口下再遍历IP
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    /*** 排除loopback类型地址 ***/
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址,就是它了
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            // site-local类型的地址未被发现,先记录候选地址
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress;
            }
            /*** 如果没有发现 non-loopback地址.只能用最次选的方案 ***/
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return jdkSuppliedAddress;
        } catch (Exception e) {
            UnknownHostException unknownHostException = new UnknownHostException(
                    "Failed to determine LAN address: " + e);
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }

    /**
     * 获取工作机器ID(0-31)
     *
     * @return
     */
    public static long getWorkerId() {
        return getIdScope(addr_t);
    }

    /**
     * 获取工作机器ID(0-31)
     *
     * @return
     */
    public static long getDatacenterId() {
        return getIdScope(addr_f);
    }

    private static long getIdScope(long addr) {
        long r = addr;
        if (r >= 0 && r <= 31) {
            return r;
        } else {
            r = r - 31;
            if (r >= 0 && r <= 31) {
                return r;
            } else {
                while (true) {
                    r = r >> 2;
                    if (r >= 0 && r <= 31) break;
                }
                return r;
            }
        }
    }

//    public static void main(String[] args) {
//        for (int i = 0; i < 10; i++) {
//            System.out.println("WorkerId=" + getWorkerId() + ",DatacenterId=" + getDatacenterId());
//        }
//    }

}
