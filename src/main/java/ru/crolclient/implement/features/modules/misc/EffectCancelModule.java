package ru.crolclient.implement.features.modules.misc;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import ru.crolclient.api.event.EventHandler;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.MultiSelectSetting;
import ru.crolclient.implement.events.player.TickEvent;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class EffectCancelModule extends Module {

    MultiSelectSetting effectCancelSetting = new MultiSelectSetting("Cancel Effects", "Choose which effects to cancel")
            .value("Jump Boost", "Blindness", "Slow Falling", "Levitation");

    public EffectCancelModule() {
        super("EffectCancel", "Effect Cancel", ModuleCategory.MISC);
        setup(effectCancelSetting);
    }

    @EventHandler
    public void onTick(TickEvent tickEvent) {
        removeSelectedEffects();
    }

    private void removeSelectedEffects() {
        List<StatusEffectInstance> activeEffects = mc.player.getStatusEffects().stream().toList();

        for (StatusEffectInstance effect : activeEffects) {
            if (effectCancelSetting.isSelected("Jump Boost") && effect.getEffectType() == StatusEffects.JUMP_BOOST) {
                mc.player.removeStatusEffect(StatusEffects.JUMP_BOOST);
            }
            if (effectCancelSetting.isSelected("Blindness") && effect.getEffectType() == StatusEffects.BLINDNESS) {
                mc.player.removeStatusEffect(StatusEffects.BLINDNESS);
            }
            if (effectCancelSetting.isSelected("Slow Falling") && effect.getEffectType() == StatusEffects.SLOW_FALLING) {
                mc.player.removeStatusEffect(StatusEffects.SLOW_FALLING);
            }
            if (effectCancelSetting.isSelected("Levitation") && effect.getEffectType() == StatusEffects.LEVITATION) {
                mc.player.removeStatusEffect(StatusEffects.LEVITATION);
            }
            if (effectCancelSetting.isSelected("Nausea") && effect.getEffectType() == StatusEffects.NAUSEA) {
                mc.player.removeStatusEffect(StatusEffects.NAUSEA);
            }
        }
    }
}
