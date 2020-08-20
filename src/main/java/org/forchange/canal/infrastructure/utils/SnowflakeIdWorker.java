package org.forchange.canal.infrastructure.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnowflakeIdWorker {
    private static final Logger logger = LoggerFactory.getLogger(SnowflakeIdWorker.class);
    /**
     * 对象实例
     */
    private static SnowflakeIdWorker snowflakeIdWorker = null;
    /**
     * 开始时间截 (2015-01-01)
     */
    private final long twepoch = 1420041600000L;
    /**
     * 机器id所占的位数
     */
    private final long workerIdBits = 5L;
    /**
     * 数据标识id所占的位数
     */
    private final long datacenterIdBits = 5L;
    /**
     * 支持的最大机器id，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
     */
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    /**
     * 支持的最大数据标识id，结果是31
     */
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);
    /**
     * 序列在id中占的位数
     */
    private final long sequenceBits = 12L;
    /**
     * 机器ID向左移12位
     */
    private final long workerIdShift = sequenceBits;
    /**
     * 数据标识id向左移17位(12+5)
     */
    private final long datacenterIdShift = sequenceBits + workerIdBits;
    /**
     * 时间截向左移22位(5+5+12)
     */
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    /**
     * 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
     */
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);
    /**
     * 工作机器ID(0~31)
     */
    private long workerId = IdWorkerUtil.getWorkerId();
    /**
     * 数据中心ID(0~31)
     */
    private long datacenterId = IdWorkerUtil.getDatacenterId();
    /**
     * 毫秒内序列(0~4095)
     */
    private long sequence = 0L;
    /**
     * 上次生成ID的时间截
     */
    private long lastTimestamp = -1L;

    private SnowflakeIdWorker() {

    }

    /**
     * 单例构造
     *
     * @return
     */
    public static synchronized SnowflakeIdWorker getInstance() {
        if (snowflakeIdWorker == null) {
            synchronized (SnowflakeIdWorker.class) {
                if (snowflakeIdWorker == null) {
                    snowflakeIdWorker = new SnowflakeIdWorker();
                    logger.info("SnowflakeIdWorker -> workId={},datacenterId={}", snowflakeIdWorker.workerId, snowflakeIdWorker.datacenterId);
                }
            }
        }
        return snowflakeIdWorker;
    }

    /**
     * 测试
     */
//    public static void main(String[] args) {
//        for (int i = 0; i < 10; i++) {
//            long id = SnowflakeIdWorker.getInstance().nextId();
//            StringBuilder sb = new StringBuilder("6666");
//            sb.append(id);
//            System.out.println(sb.toString());
//        }
//    }

    /**
     * 获得下一个ID (该方法是线程安全的)
     *
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        //如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        //如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            //毫秒内序列溢出
            if (sequence == 0) {
                //阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        //时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }

        //上次生成ID的时间截
        lastTimestamp = timestamp;

        //移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - twepoch) << timestampLeftShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift)
                | sequence;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 返回以毫秒为单位的当前时间
     *
     * @return 当前时间(毫秒)
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }
}
