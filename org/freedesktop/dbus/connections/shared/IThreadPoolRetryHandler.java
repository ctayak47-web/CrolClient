
package org.freedesktop.dbus.connections.shared;

import org.freedesktop.dbus.connections.shared.ExecutorNames;

@FunctionalInterface
public interface IThreadPoolRetryHandler {
    public boolean handle(ExecutorNames var1, Exception var2);
}

