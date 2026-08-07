
package de.jcm.discordgamesdk.impl.events;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.impl.Command;
import de.jcm.discordgamesdk.impl.EventHandler;
import de.jcm.discordgamesdk.voice.VoiceInputMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoiceSettingsUpdate2Event {

    public static class Handler
    extends EventHandler<Data> {
        public Handler(Core.CorePrivate core) {
            super(core);
        }

        @Override
        public void handle(Command command, Data data) {
            this.core.voiceData = data;
        }

        @Override
        public Class<?> getDataClass() {
            return Data.class;
        }
    }

    public static class Data {
        public VoiceInputMode input_mode = new VoiceInputMode(VoiceInputMode.InputModeType.VOICE_ACTIVITY, "");
        public List<String> local_mutes = new ArrayList<String>();
        public Map<String, Integer> local_volumes = new HashMap<String, Integer>();
        public boolean self_mute;
        public boolean self_deaf;

        public VoiceInputMode getInputMode() {
            return this.input_mode;
        }

        public List<String> getLocalMutes() {
            return this.local_mutes;
        }

        public Map<String, Integer> getLocalVolumes() {
            return this.local_volumes;
        }

        public boolean isSelfMute() {
            return this.self_mute;
        }

        public boolean isSelfDeaf() {
            return this.self_deaf;
        }
    }
}

