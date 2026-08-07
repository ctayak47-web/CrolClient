
package de.jcm.discordgamesdk.impl;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.impl.Command;

public abstract class EventHandler<Data> {
    protected final Core.CorePrivate core;

    protected EventHandler(Core.CorePrivate core) {
        this.core = core;
    }

    public abstract void handle(Command var1, Data var2);

    public final void handleObject(Command command, Object o) {
        this.handle(command, o);
    }

    public abstract Class<?> getDataClass();

    public boolean shouldRegister() {
        return true;
    }

    public Object getRegisterArgs() {
        return null;
    }
}

