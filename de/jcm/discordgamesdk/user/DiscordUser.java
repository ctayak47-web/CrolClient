
package de.jcm.discordgamesdk.user;

import com.google.gson.annotations.SerializedName;

public class DiscordUser {
    @SerializedName(value="id")
    private final String userId;
    private final String username;
    private final String discriminator;
    private final String avatar;
    private final String avatar_decoration;
    private final Boolean bot;
    private final Integer flags;

    public DiscordUser(long userId, String username, String discriminator, String avatar, Boolean bot) {
        this.userId = Long.toString(userId);
        this.username = username;
        this.discriminator = discriminator;
        this.avatar = avatar;
        this.avatar_decoration = null;
        this.bot = bot;
        this.flags = null;
    }

    public DiscordUser(long userId) {
        this(userId, null, null, null, null);
    }

    public long getUserId() {
        return Long.parseLong(this.userId);
    }

    public String getUsername() {
        return this.username;
    }

    public String getDiscriminator() {
        return this.discriminator;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public boolean isBot() {
        return this.bot;
    }

    public int getFlags() {
        return this.flags;
    }

    public String toString() {
        return "DiscordUser{userId='" + this.userId + "', username='" + this.username + "', discriminator='" + this.discriminator + "', avatar='" + this.avatar + "', avatar_decoration='" + this.avatar_decoration + "', bot=" + this.bot + ", flags=" + this.flags + "}";
    }
}

