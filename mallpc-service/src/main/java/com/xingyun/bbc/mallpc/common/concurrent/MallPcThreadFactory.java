package com.xingyun.bbc.mallpc.common.concurrent;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-25
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public class MallPcThreadFactory implements ThreadFactory {

    private static volatile boolean daemon;

    private static final ThreadGroup THREAD_GROUP = new ThreadGroup("mall-pc-thread-group");

    private final AtomicLong threadNumber = new AtomicLong(1);

    private final String namePrefix;

    private MallPcThreadFactory(final String namePrefix, final boolean daemon) {
        this.namePrefix = namePrefix;
        MallPcThreadFactory.daemon = daemon;
    }

    /**
     * create ThreadFactory.
     *
     * @param namePrefix namePrefix
     * @param daemon     daemon
     * @return ThreadFactory
     */
    public static ThreadFactory create(final String namePrefix, final boolean daemon) {
        return new MallPcThreadFactory(namePrefix, daemon);
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        Thread thread = new Thread(THREAD_GROUP, runnable, StringUtils.joinWith("-", THREAD_GROUP.getName(), namePrefix, threadNumber.getAndIncrement()));
        thread.setDaemon(daemon);
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        return thread;
    }
}
