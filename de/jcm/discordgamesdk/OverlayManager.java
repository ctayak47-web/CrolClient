
package de.jcm.discordgamesdk;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.Result;
import de.jcm.discordgamesdk.activity.ActivityActionType;
import de.jcm.discordgamesdk.impl.Command;
import de.jcm.discordgamesdk.impl.commands.OpenOverlayActivityInvite;
import de.jcm.discordgamesdk.impl.commands.OpenOverlayGuildInvite;
import de.jcm.discordgamesdk.impl.commands.OpenOverlayVoiceSettings;
import de.jcm.discordgamesdk.impl.commands.SetOverlayLocked;
import java.util.function.Consumer;

public class OverlayManager {
    private final Core.CorePrivate core;

    OverlayManager(Core.CorePrivate core) {
        this.core = core;
    }

    public boolean isEnabled() {
        return this.core.overlayData.isEnabled();
    }

    public boolean isLocked() {
        return this.core.overlayData.isLocked();
    }

    public void setLocked(boolean locked) {
        this.setLocked(locked, Core.DEFAULT_CALLBACK);
    }

    public void setLocked(boolean locked, Consumer<Result> callback) {
        this.core.sendCommand(Command.Type.SET_OVERLAY_LOCKED, new SetOverlayLocked.Args(locked, this.core.pid), c -> callback.accept(this.core.checkError((Command)c)));
    }

    public void openActivityInvite(ActivityActionType type) {
        this.openActivityInvite(type, Core.DEFAULT_CALLBACK);
    }

    public void openActivityInvite(ActivityActionType type, Consumer<Result> callback) {
        this.core.sendCommand(Command.Type.OPEN_OVERLAY_ACTIVITY_INVITE, new OpenOverlayActivityInvite.Args(type.nativeValue(), this.core.pid), c -> callback.accept(this.core.checkError((Command)c)));
    }

    public void openGuildInvite(String code) {
        this.openGuildInvite(code, Core.DEFAULT_CALLBACK);
    }

    public void openGuildInvite(String code, Consumer<Result> callback) {
        this.core.sendCommandNoResponse(Command.Type.OPEN_OVERLAY_GUILD_INVITE, new OpenOverlayGuildInvite.Args(code, this.core.pid), c -> callback.accept(this.core.checkError((Command)c)));
    }

    public void openVoiceSettings() {
        this.openVoiceSettings(Core.DEFAULT_CALLBACK);
    }

    public void openVoiceSettings(Consumer<Result> callback) {
        this.core.sendCommandNoResponse(Command.Type.OPEN_OVERLAY_VOICE_SETTINGS, new OpenOverlayVoiceSettings.Args(this.core.pid), c -> callback.accept(this.core.checkError((Command)c)));
    }
}

