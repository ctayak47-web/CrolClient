
package org.freedesktop.dbus.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.freedesktop.dbus.utils.Util;

public class NameableThreadFactory
implements ThreadFactory {
    private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final int threadPriority;
    private final boolean daemonizeThreads;

    public NameableThreadFactory(String _name, boolean _daemonizeThreads) {
        this(_name, _daemonizeThreads, 5);
    }

    public NameableThreadFactory(String _name, boolean _daemonizeThreads, int _threadPriority) {
        this.group = Thread.currentThread().getThreadGroup();
        this.namePrefix = Util.isBlank(_name) ? "UnnamedThreadPool-" + POOL_NUMBER.getAndIncrement() + "-thread-" : _name;
        this.daemonizeThreads = _daemonizeThreads;
        this.threadPriority = _threadPriority;
    }

    @Override
    public Thread newThread(Runnable _runnable) {
        Thread t = new Thread(this.group, _runnable, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
        t.setDaemon(this.daemonizeThreads);
        t.setPriority(this.threadPriority);
        return t;
    }
}

