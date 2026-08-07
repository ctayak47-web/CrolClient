package com.crolclient.sound;
import com.crolclient.CrolClientClient;
import com.crolclient.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
public class SoundManager {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public static void playUI(String name) {
        if (!ConfigManager.getConfig().uiSoundsEnabled) return;
        play("crolclient", "sounds/" + name);
    }
    public static void playHit(String name) {
        play("crolclient", "hit/" + name);
    }
    public static void playDeath(String name) {
        play("crolclient", "death/" + name);
    }
    public static void playLowHP() {
        if (!ConfigManager.getConfig().lowHpSoundEnabled) return;
        play("crolclient", "sounds/low");
    }
    private static void play(String namespace, String path) {
        if (mc.world == null) return;
        Identifier id = Identifier.of(namespace, path);
        SoundEvent event = SoundEvent.of(id);
        mc.getSoundManager().play(PositionedSoundInstance.master(event, 1.0f, 1.0f));
    }
}
