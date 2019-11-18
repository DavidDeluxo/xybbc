package com.xingyun.bbc.mallpc.common.utils;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-26
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public final class IdGeneratorUtil {

    private static final String DATE_FORMAT = "yyMMddHHmmss";

    private static final IdWorker idWorker = new IdWorker();
    private static final ObjectId objectId = new ObjectId();

    public static long getNumberId() {
        return idWorker.nextId();
    }

    public static String getStrId() {
        StringBuilder buf = new StringBuilder(28);
        buf.append(DateUtils.formatDate(new Date(), DATE_FORMAT));
        buf.append(objectId.nextId());
        return buf.toString();
    }

    public static long getSnowflakeId() {
        SnowFlake snowflake = new SnowFlake(0, 0, 0);
        return snowflake.nextId();
    }

    private static class IdWorker {
        private static final long IP;
        private long sequence = 0L;
        private long lastTimestamp = -1L;

        long nextId() {
            long timestamp = timeGen();
            if (timestamp < this.lastTimestamp) {
                throw new RuntimeException(
                        String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", new Object[]{
                                Long.valueOf(this.lastTimestamp - timestamp)
                        }));
            }
            if (this.lastTimestamp == timestamp) {
                this.sequence = this.sequence + 1L & 0xFFFL;
                if (this.sequence == 0L) {
                    timestamp = tilNextMillis(this.lastTimestamp);
                }
            } else {
                this.sequence = 0L;
            }

            this.lastTimestamp = timestamp;

            return timestamp - 1479785410983L << 20 | IP << 12 | this.sequence;
        }

        static {
            try {
                String[] arr = getIp().split("\\.");
                String str = String.format("%03d", new Object[]{Integer.valueOf(arr[3])});
                IP = Long.parseLong(str);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        static String getIp() {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }

        long tilNextMillis(long lastTimestamp) {
            long timestamp = timeGen();
            while (timestamp <= lastTimestamp) {
                timestamp = timeGen();
            }
            return timestamp;
        }


        long timeGen() {
            return DateUtils.getCurrentMillis();
        }


        private IdWorker() {
        }
    }


    private static class ObjectId {
        private static final int MACHINE;

        private static final AtomicInteger NEXT_INC = new AtomicInteger((new Random()).nextInt());

        private int sequence = 0;


        String nextId() {
            this.sequence = NEXT_INC.getAndIncrement();
            if (this.sequence < 0) {
                NEXT_INC.set(0);
                this.sequence = NEXT_INC.getAndIncrement();
            }
            return toHexString();
        }


        String toHexString() {
            StringBuilder buf = new StringBuilder(16);
            for (byte b : toByteArray()) {
                buf.append(String.format("%02x", new Object[]{Integer.valueOf(b & 0xFF)}));
            }
            return buf.toString();
        }


        byte[] toByteArray() {
            byte[] b = new byte[8];
            ByteBuffer bb = ByteBuffer.wrap(b);
            bb.putInt(MACHINE);
            bb.putInt(this.sequence);
            return b;
        }


        static {
            try {
                int j, i;
                try {
                    StringBuffer sb = new StringBuffer();
                    Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
                    while (e.hasMoreElements()) {
                        sb.append(e.nextElement());
                    }
                    i = sb.toString().hashCode() << 16;
                } catch (Throwable e) {

                    i = (new Random()).nextInt() << 16;
                }


                try {
                    j = ManagementFactory.getRuntimeMXBean().getName().hashCode();
                } catch (Throwable t) {
                    j = (new Random()).nextInt();
                }

                ClassLoader loader = IdGeneratorUtil.class.getClassLoader();
                int loaderId = (loader != null) ? System.identityHashCode(loader) : 0;

                StringBuilder sb = new StringBuilder();
                sb.append(Integer.toHexString(j));
                sb.append(Integer.toHexString(loaderId));
                int processPiece = sb.toString().hashCode() & 0xFFFF;

                MACHINE = i | processPiece;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private ObjectId() {
        }
    }

    private static class SnowFlake {
        private long workerId;
        private long datacenterId;
        private long sequence;
        private long twepoch = 1288834974657L;
        private long workerIdBits = 5L;
        private long datacenterIdBits = 5L;
        //机器id最多只能是32以内
        private long maxWorkerId = -1L ^ (-1L << workerIdBits);
        //机房id最多只能是32以内
        private long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);
        private long sequenceBits = 12L;

        private long workerIdShift = sequenceBits;
        private long datacenterIdShift = sequenceBits + workerIdBits;
        private long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
        private long sequenceMask = -1L ^ (-1L << sequenceBits);

        private long lastTimestamp = -1L;

        public SnowFlake(long workerId, long datacenterId, long sequence) {
            if (workerId > maxWorkerId || workerId < 0) {
                throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
            }
            if (datacenterId > maxDatacenterId || datacenterId < 0) {
                throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
            }
            this.workerId = workerId;
            this.datacenterId = datacenterId;
            this.sequence = sequence;
        }

        public synchronized long nextId() {
            // 获取当前时间戳，单位是毫秒
            long timestamp = timeGen();

            if (timestamp < lastTimestamp) {
                System.err.printf("clock is moving backwards.  Rejecting requests until %d.", lastTimestamp);
                throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds",
                        lastTimestamp - timestamp));
            }

            // 在同一个毫秒内，又发送了一个请求生成一个id，0 -> 1
            if (lastTimestamp == timestamp) {
                // 一个毫秒内最多只能有4096个数字，无论你传递多少进来，这个位运算保证始终就是在4096这个范围内，避免你自己传递个sequence超过了4096这个范围
                sequence = (sequence + 1) & sequenceMask;
                if (sequence == 0) {
                    timestamp = tilNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0;
            }

            //记录一下最近一次生成id的时间戳，单位是毫秒
            lastTimestamp = timestamp;

            return ((timestamp - twepoch) << timestampLeftShift) |
                    (datacenterId << datacenterIdShift) |
                    (workerId << workerIdShift) |
                    sequence;
        }

        private long tilNextMillis(long lastTimestamp) {
            long timestamp = timeGen();
            while (timestamp <= lastTimestamp) {
                timestamp = timeGen();
            }
            return timestamp;
        }

        private long timeGen() {
            return DateUtils.getCurrentMillis();
        }

    }

}
