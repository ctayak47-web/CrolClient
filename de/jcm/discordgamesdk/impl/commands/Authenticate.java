
package de.jcm.discordgamesdk.impl.commands;

import de.jcm.discordgamesdk.ApplicationManager;
import de.jcm.discordgamesdk.user.DiscordUser;
import java.util.Date;
import java.util.Set;

public class Authenticate {
    private Authenticate() {
    }

    public static class Response {
        public String access_token;
        public Set<String> scopes;
        public Date expires;
        public DiscordUser user;
        public Application application;

        public ApplicationManager.DiscordOAuth2Token toDiscordOAuth2Token() {
            return new ApplicationManager.DiscordOAuth2Token(this.access_token, this.scopes, this.expires);
        }

        public static class Application {
            String id;
            String name;
            String icon;
            String description;
            String type;
            String cover_image;
            String summary;
            boolean is_monetized;
            boolean is_verified;
            String verify_key;
            int flags;
            boolean hook;
            boolean storefront_available;

            public ApplicationManager.Application toApplication() {
                return new ApplicationManager.Application(Long.parseLong(this.id), this.name, this.icon, this.description, this.type, this.cover_image, this.summary, this.is_monetized, this.is_verified, this.verify_key, this.flags, this.hook, this.storefront_available);
            }
        }
    }
}

