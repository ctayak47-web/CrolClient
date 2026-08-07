
package org.freedesktop.dbus.connections.config;

import org.freedesktop.dbus.connections.shared.IThreadPoolRetryHandler;

public final class ReceivingServiceConfig {
    private int signalThreadPoolSize = 1;
    private int errorThreadPoolSize = 1;
    private int methodCallThreadPoolSize = 4;
    private int methodReturnThreadPoolSize = 1;
    private int signalThreadPriority = 5;
    private int methodCallThreadPriority = 5;
    private int errorThreadPriority = 5;
    private int methodReturnThreadPriority = 5;
    private IThreadPoolRetryHandler retryHandler;

    ReceivingServiceConfig() {
    }

    public int getSignalThreadPoolSize() {
        return this.signalThreadPoolSize;
    }

    public int getErrorThreadPoolSize() {
        return this.errorThreadPoolSize;
    }

    public int getMethodCallThreadPoolSize() {
        return this.methodCallThreadPoolSize;
    }

    public int getMethodReturnThreadPoolSize() {
        return this.methodReturnThreadPoolSize;
    }

    public int getSignalThreadPriority() {
        return this.signalThreadPriority;
    }

    public int getMethodCallThreadPriority() {
        return this.methodCallThreadPriority;
    }

    public int getErrorThreadPriority() {
        return this.errorThreadPriority;
    }

    public int getMethodReturnThreadPriority() {
        return this.methodReturnThreadPriority;
    }

    public IThreadPoolRetryHandler getRetryHandler() {
        return this.retryHandler;
    }

    void setSignalThreadPoolSize(int _signalThreadPoolSize) {
        this.signalThreadPoolSize = _signalThreadPoolSize;
    }

    void setErrorThreadPoolSize(int _errorThreadPoolSize) {
        this.errorThreadPoolSize = _errorThreadPoolSize;
    }

    void setMethodCallThreadPoolSize(int _methodCallThreadPoolSize) {
        this.methodCallThreadPoolSize = _methodCallThreadPoolSize;
    }

    void setMethodReturnThreadPoolSize(int _methodReturnThreadPoolSize) {
        this.methodReturnThreadPoolSize = _methodReturnThreadPoolSize;
    }

    void setSignalThreadPriority(int _signalThreadPriority) {
        this.signalThreadPriority = _signalThreadPriority;
    }

    void setMethodCallThreadPriority(int _methodCallThreadPriority) {
        this.methodCallThreadPriority = _methodCallThreadPriority;
    }

    void setErrorThreadPriority(int _errorThreadPriority) {
        this.errorThreadPriority = _errorThreadPriority;
    }

    void setMethodReturnThreadPriority(int _methodReturnThreadPriority) {
        this.methodReturnThreadPriority = _methodReturnThreadPriority;
    }

    void setRetryHandler(IThreadPoolRetryHandler _retryHandler) {
        this.retryHandler = _retryHandler;
    }
}

