
package de.jcm.discordgamesdk;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.Result;
import de.jcm.discordgamesdk.impl.Command;
import de.jcm.discordgamesdk.impl.commands.SetUserVoiceSettings;
import de.jcm.discordgamesdk.impl.commands.SetVoiceSettings2;
import de.jcm.discordgamesdk.voice.VoiceInputMode;
import java.util.function.Consumer;

public class VoiceManager {
    private final Core.CorePrivate core;

    VoiceManager(Core.CorePrivate core) {
        this.core = core;
    }

    public VoiceInputMode getInputMode() {
        return this.core.voiceData.getInputMode();
    }

    public void setInputMode(VoiceInputMode inputMode, Consumer<Result> callback) {
        this.core.sendCommand(Command.Type.SET_VOICE_SETTINGS_2, new SetVoiceSettings2.InputMode(inputMode), c -> callback.accept(this.core.checkError((Command)c)));
        this.core.voiceData.input_mode = inputMode;
    }

    public void setInputMode(VoiceInputMode inputMode) {
        this.setInputMode(inputMode, Core.DEFAULT_CALLBACK);
    }

    public boolean isSelfMute() {
        return this.core.voiceData.isSelfMute();
    }

    public void setSelfMute(boolean selfMute) {
        this.core.sendCommand(Command.Type.SET_VOICE_SETTINGS_2, new SetVoiceSettings2.SelfMute(selfMute), c -> Core.DEFAULT_CALLBACK.accept(this.core.checkError((Command)c)));
        this.core.voiceData.self_mute = selfMute;
    }

    public boolean isSelfDeaf() {
        return this.core.voiceData.isSelfDeaf();
    }

    public void setSelfDeaf(boolean selfDeaf) {
        this.core.sendCommand(Command.Type.SET_VOICE_SETTINGS_2, new SetVoiceSettings2.SelfDeaf(selfDeaf), c -> Core.DEFAULT_CALLBACK.accept(this.core.checkError((Command)c)));
        this.core.voiceData.self_deaf = selfDeaf;
    }

    public boolean isLocalMute(long userId) {
        return this.core.voiceData.getLocalMutes().contains(Long.toString(userId));
    }

    public void setLocalMute(long userId, boolean mute) {
        String user_id = Long.toString(userId);
        this.core.sendCommand(Command.Type.SET_USER_VOICE_SETTINGS_2, new SetUserVoiceSettings.Mute(user_id, mute), c -> Core.DEFAULT_CALLBACK.accept(this.core.checkError((Command)c)));
        boolean old = this.core.voiceData.local_mutes.contains(user_id);
        if (old && !mute) {
            this.core.voiceData.local_mutes.remove(user_id);
        } else if (!old && mute) {
            this.core.voiceData.local_mutes.add(user_id);
        }
    }

    public int getLocalVolume(long userId) {
        return this.core.voiceData.getLocalVolumes().getOrDefault(Long.toString(userId), 100);
    }

    public void setLocalVolume(long userId, int volume) {
        if (volume < 0 || volume > 200) {
            throw new IllegalArgumentException("volume out of range: " + volume);
        }
        String user_id = Long.toString(userId);
        this.core.sendCommand(Command.Type.SET_USER_VOICE_SETTINGS_2, new SetUserVoiceSettings.Volume(user_id, volume), c -> Core.DEFAULT_CALLBACK.accept(this.core.checkError((Command)c)));
        this.core.voiceData.getLocalVolumes().put(user_id, volume);
    }
}

