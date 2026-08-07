
package org.freedesktop.dbus.connections.base;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.freedesktop.dbus.connections.config.ReceivingServiceConfig;
import org.freedesktop.dbus.connections.config.ReceivingServiceConfigBuilder;
import org.freedesktop.dbus.connections.shared.ExecutorNames;
import org.freedesktop.dbus.connections.shared.IThreadPoolRetryHandler;
import org.freedesktop.dbus.exceptions.IllegalThreadPoolStateException;
import org.freedesktop.dbus.utils.NameableThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceivingService {
    static final int MAX_RETRIES = 50;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private boolean closed = false;
    private final Map<ExecutorNames, ExecutorService> executors = new ConcurrentHashMap<ExecutorNames, ExecutorService>();
    private final IThreadPoolRetryHandler retryHandler;

    ReceivingService(String _namePrefix, ReceivingServiceConfig _rsCfg) {
        String prefix = _namePrefix == null ? "" : _namePrefix;
        ReceivingServiceConfig rsCfg = Optional.ofNullable(_rsCfg).orElse(ReceivingServiceConfigBuilder.getDefaultConfig());
        this.executors.put(ExecutorNames.SIGNAL, Executors.newFixedThreadPool(rsCfg.getSignalThreadPoolSize(), new NameableThreadFactory(prefix + "DBus-Signal-Receiver-", true, rsCfg.getSignalThreadPriority())));
        this.executors.put(ExecutorNames.ERROR, Executors.newFixedThreadPool(rsCfg.getErrorThreadPoolSize(), new NameableThreadFactory(prefix + "DBus-Error-Receiver-", true, rsCfg.getErrorThreadPriority())));
        this.executors.put(ExecutorNames.METHODCALL, Executors.newFixedThreadPool(rsCfg.getMethodCallThreadPoolSize(), new NameableThreadFactory(prefix + "DBus-MethodCall-Receiver-", true, rsCfg.getMethodCallThreadPriority())));
        this.executors.put(ExecutorNames.METHODRETURN, Executors.newFixedThreadPool(rsCfg.getMethodReturnThreadPoolSize(), new NameableThreadFactory(prefix + "DBus-MethodReturn-Receiver-", true, rsCfg.getMethodReturnThreadPriority())));
        this.retryHandler = rsCfg.getRetryHandler();
    }

    int execSignalHandler(Runnable _r) {
        return this.execOrFail(ExecutorNames.SIGNAL, _r);
    }

    int execErrorHandler(Runnable _r) {
        return this.execOrFail(ExecutorNames.ERROR, _r);
    }

    int execMethodCallHandler(Runnable _r) {
        return this.execOrFail(ExecutorNames.METHODCALL, _r);
    }

    int execMethodReturnHandler(Runnable _r) {
        return this.execOrFail(ExecutorNames.METHODRETURN, _r);
    }

    int execOrFail(ExecutorNames _executor, Runnable _r) {
        int failCount;
        if (_r == null || _executor == null) {
            return -1;
        }
        for (failCount = 0; failCount < 50; ++failCount) {
            try {
                ExecutorService exec = this.getExecutor(_executor);
                if (exec == null) {
                    throw new IllegalThreadPoolStateException("No executor found for " + String.valueOf((Object)_executor));
                }
                if (this.closed || exec.isShutdown() || exec.isTerminated()) {
                    throw new IllegalThreadPoolStateException("Receiving service already closed");
                }
                exec.execute(_r);
                break;
            }
            catch (IllegalThreadPoolStateException _ex) {
                throw _ex;
            }
            catch (Exception _ex) {
                if (this.retryHandler != null) continue;
                this.logger.error("Could not handle runnable for executor {}, runnable will be dropped", (Object)_executor, (Object)_ex);
                break;
            }
        }
        if (failCount >= 50) {
            this.logger.error("Could not handle runnable for executor {} after {} retries, runnable will be dropped", (Object)_executor, (Object)failCount);
        }
        return failCount;
    }

    ExecutorService getExecutor(ExecutorNames _executor) {
        return this.executors.get((Object)_executor);
    }

    public synchronized void shutdown(int _timeout, TimeUnit _unit) {
        for (Map.Entry<ExecutorNames, ExecutorService> es : this.executors.entrySet()) {
            this.logger.debug("Shutting down executor: {}", (Object)es.getKey());
            es.getValue().shutdown();
        }
        for (Map.Entry<ExecutorNames, ExecutorService> es : this.executors.entrySet()) {
            try {
                es.getValue().awaitTermination(_timeout, _unit);
            }
            catch (InterruptedException _ex) {
                this.logger.debug("Interrupted while waiting for termination of executor");
                Thread.currentThread().interrupt();
            }
        }
        this.closed = true;
    }

    public synchronized void shutdownNow() {
        for (Map.Entry<ExecutorNames, ExecutorService> es : this.executors.entrySet()) {
            if (es.getValue().isTerminated()) continue;
            this.logger.debug("Forcefully stopping {}", (Object)es.getKey());
            es.getValue().shutdownNow();
        }
        this.closed = true;
    }
}

