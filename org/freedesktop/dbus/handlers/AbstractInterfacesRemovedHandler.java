
package org.freedesktop.dbus.handlers;

import org.freedesktop.dbus.handlers.AbstractSignalHandlerBase;
import org.freedesktop.dbus.interfaces.ObjectManager;

public abstract class AbstractInterfacesRemovedHandler
extends AbstractSignalHandlerBase<ObjectManager.InterfacesRemoved> {
    @Override
    public final Class<ObjectManager.InterfacesRemoved> getImplementationClass() {
        return ObjectManager.InterfacesRemoved.class;
    }
}

