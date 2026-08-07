
package de.jcm.discordgamesdk.impl.events;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.impl.Command;
import de.jcm.discordgamesdk.impl.EventHandler;
import de.jcm.discordgamesdk.user.DiscordUser;

public class ReadyEvent {

    public static class Handler
    extends EventHandler<Data> {
        public Handler(Core.CorePrivate core) {
            super(core);
        }

        @Override
        public void handle(Command command, Data data) {
            this.core.ready();
            this.core.currentUser = data.user;
            this.core.workQueue.add(() -> this.core.getEventAdapter().onCurrentUserUpdate());
        }

        @Override
        public Class<?> getDataClass() {
            return Data.class;
        }

        @Override
        public boolean shouldRegister() {
            return false;
        }
    }

    public static class Data {
        int v;
        Config config;
        DiscordUser user;

        public String toString() {
            return "ReadyData{v=" + this.v + ", config=" + this.config + ", user=" + this.user + "}";
        }

        static class Config {
            String cdn_host;
            String api_endpoint;
            String environment;

            Config() {
            }

            public String toString() {
                return "Config{cdn_host='" + this.cdn_host + "', api_endpoint='" + this.api_endpoint + "', environment='" + this.environment + "'}";
            }
        }
    }
}

