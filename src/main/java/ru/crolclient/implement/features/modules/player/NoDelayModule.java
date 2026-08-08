package ru.crolclient.implement.features.modules.player;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.BooleanSetting;
import ru.crolclient.api.event.EventHandler;
import ru.crolclient.implement.events.player.TickEvent;
import ru.crolclient.mixins.accessors.LivingEntityAccessor;
import ru.crolclient.mixins.accessors.MinecraftClientAccessor;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NoDelayModule extends Module {

    BooleanSetting jumpDelaySetting = new BooleanSetting("Jump", "Disables the jump delay").setValue(true);
    BooleanSetting rightClickDelaySetting = new BooleanSetting("Right Click", "Disables the right click delay").setValue(false);
    //BooleanSetting leftClickDelaySetting = new BooleanSetting("Left Click", "Disables the left click delay").setValue(false);

    public NoDelayModule() {
        super("NoDelay", "No Delay", ModuleCategory.PLAYER);
        setup(jumpDelaySetting, rightClickDelaySetting);
    }

    @EventHandler
    public void onTick(TickEvent tickEvent) {
        if (jumpDelaySetting.isValue()) {
            resetJumpCooldown();
        }
        if (rightClickDelaySetting.isValue()) {
            resetItemUseCooldown();
        }
    }

    private void resetJumpCooldown() {
        LivingEntityAccessor livingEntityAccessor = (LivingEntityAccessor) mc.player;
        if (livingEntityAccessor != null && livingEntityAccessor.getLastJumpCooldown() > 0) {
            livingEntityAccessor.setLastJumpCooldown(0);
        }
    }

    private void resetItemUseCooldown() {
        MinecraftClientAccessor minecraftClientAccessor = (MinecraftClientAccessor) mc;
        if (minecraftClientAccessor != null && minecraftClientAccessor.getItemUseCooldown() > 0) {
            minecraftClientAccessor.setItemUseCooldown(0);
        }
    }
}