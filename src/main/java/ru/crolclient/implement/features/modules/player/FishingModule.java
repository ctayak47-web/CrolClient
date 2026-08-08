package ru.crolclient.implement.features.modules.player;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import ru.crolclient.api.event.EventHandler;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.BooleanSetting;
import ru.crolclient.api.feature.module.setting.implement.ValueSetting;
import ru.crolclient.implement.events.player.TickEvent;
import ru.crolclient.implement.events.packet.PacketEvent;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FishingModule extends Module {
    final MinecraftClient mc = MinecraftClient.getInstance();

    BooleanSetting autoCastSetting = new BooleanSetting("Auto Cast", "Automatically cast the fishing rod");
    ValueSetting delaySetting = new ValueSetting("Delay", "Delay (in seconds) before re-casting")
            .setValue(0.0F)
            .range(0.0F, 1.0F);

    boolean fishCaughtFlag = false;
    long lastCastTime = 0;

    public FishingModule() {
        super("Fishing", "Fishing", ModuleCategory.PLAYER);
        setup(autoCastSetting, delaySetting);
    }

    @EventHandler
    public void onTick(TickEvent tickEvent) {
        if (mc.player == null || mc.world == null) return;

        ClientPlayerEntity player = mc.player;

        if (player.getMainHandStack().getItem() != Items.FISHING_ROD) {
            return;
        }

        FishingBobberEntity bobber = player.fishHook;

        if (bobber == null && autoCastSetting.isValue() && canCast()) {
            pressUseKey();
        } else if (bobber != null && fishCaughtFlag) {
            pressUseKey();
            fishCaughtFlag = false;
            lastCastTime = System.currentTimeMillis();
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent packetEvent) {
        if (packetEvent.getPacket() instanceof PlaySoundS2CPacket sound) {
            if (sound.getSound().value().equals(SoundEvents.ENTITY_FISHING_BOBBER_SPLASH)) {
                FishingBobberEntity bobber = mc.player.fishHook;

                if (bobber != null && bobber.squaredDistanceTo(sound.getX(), sound.getY(), sound.getZ()) < 4.0F) {
                    fishCaughtFlag = true;
                }
            }
        }
    }

    private boolean canCast() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastCastTime) >= delaySetting.getValue() * 1000;
    }

    private void pressUseKey() {
        mc.options.useKey.setPressed(true);
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            mc.options.useKey.setPressed(false);
        }).start();
    }
}
