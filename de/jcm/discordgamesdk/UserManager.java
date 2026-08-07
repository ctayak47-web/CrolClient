
package de.jcm.discordgamesdk;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.GameSDKException;
import de.jcm.discordgamesdk.Result;
import de.jcm.discordgamesdk.impl.Command;
import de.jcm.discordgamesdk.user.DiscordUser;
import de.jcm.discordgamesdk.user.PremiumType;
import java.util.function.BiConsumer;

public class UserManager {
    private final Core.CorePrivate core;
    public static final int USER_FLAG_PARTNER = 2;
    public static final int USER_FLAG_HYPE_SQUAD_EVENTS = 4;
    public static final int USER_FLAG_HYPE_SQUAD_HOUSE1 = 64;
    public static final int USER_FLAG_HYPE_SQUAD_HOUSE2 = 128;
    public static final int USER_FLAG_HYPE_SQUAD_HOUSE3 = 256;

    UserManager(Core.CorePrivate core) {
        this.core = core;
    }

    public DiscordUser getCurrentUser() {
        if (this.core.currentUser == null) {
            throw new GameSDKException(Result.NOT_FOUND);
        }
        return this.core.currentUser;
    }

    public void getUser(long userId, BiConsumer<Result, DiscordUser> callback) {
        this.core.sendCommand(Command.Type.GET_USER, new DiscordUser(userId), c -> {
            DiscordUser user = this.core.getGson().fromJson(c.getData(), DiscordUser.class);
            callback.accept(this.core.checkError((Command)c), user);
        });
    }

    public PremiumType getCurrentUserPremiumType() {
        throw new RuntimeException("not implemented");
    }

    public boolean currentUserHasFlag(int flag) {
        return (this.core.currentUser.getFlags() & flag) != 0;
    }
}

