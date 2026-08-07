
package org.freedesktop.dbus.messages.constants;

public final class MessageType {
    public static final byte METHOD_CALL = 1;
    public static final byte METHOD_RETURN = 2;
    public static final byte ERROR = 3;
    public static final byte SIGNAL = 4;

    private MessageType() {
    }
}

