
package de.jcm.discordgamesdk.impl.events;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.impl.Command;
import de.jcm.discordgamesdk.impl.EventHandler;
import de.jcm.discordgamesdk.user.DiscordUser;

public class CurrentUserUpdateEvent {

    public static class Handler
    extends EventHandler<DiscordUser> {
        public Handler(Core.CorePrivate core) {
            super(core);
        }

        @Override
        public void handle(Command command, DiscordUser user) {
            this.core.currentUser = user;
            this.core.getEventAdapter().onCurrentUserUpdate();
        }

        @Override
        public Class<?> getDataClass() {
            return DiscordUser.class;
        }

        @Override
        public boolean shouldRegister() {
            return false;
        }
    }
}

