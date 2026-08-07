
package de.jcm.discordgamesdk.impl.events;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.impl.Command;
import de.jcm.discordgamesdk.impl.EventHandler;

public class OverlayUpdateEvent {

    public static class Handler
    extends EventHandler<Data> {
        public Handler(Core.CorePrivate core) {
            super(core);
        }

        @Override
        public void handle(Command command, Data data) {
            this.core.overlayData = data;
        }

        @Override
        public Class<?> getDataClass() {
            return Data.class;
        }

        @Override
        public Object getRegisterArgs() {
            return new Args(this.core.pid);
        }

        private static class Args {
            private final int pid;

            public Args(int pid) {
                this.pid = pid;
            }
        }
    }

    public static class Data {
        private boolean enabled;
        private boolean locked;

        public boolean isEnabled() {
            return this.enabled;
        }

        public boolean isLocked() {
            return this.locked;
        }
    }
}

