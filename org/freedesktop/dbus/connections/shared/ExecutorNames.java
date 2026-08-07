
package org.freedesktop.dbus.connections.shared;

public enum ExecutorNames {
    SIGNAL("SignalExecutor"),
    ERROR("ErrorExecutor"),
    METHODCALL("MethodCallExecutor"),
    METHODRETURN("MethodReturnExecutor");

    private final String description;

    private ExecutorNames(String _name) {
        this.description = _name;
    }

    public String getDescription() {
        return this.description;
    }

    public String toString() {
        return this.description;
    }
}

