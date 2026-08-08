package ru.crolclient.implement.features.modules.combat.killaura.attack;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.LivingEntity;
import ru.crolclient.common.QuickImports;
import ru.crolclient.implement.events.packet.PacketEvent;
import ru.crolclient.implement.features.modules.combat.killaura.rotation.Angle;

import java.util.List;

@Getter
public class AttackPerpetrator implements QuickImports {
    AttackHandler attackHandler = new AttackHandler();

    public void tick() {
        attackHandler.tick();
    }

    public void onPacket(PacketEvent packet) {
        attackHandler.onPacket(packet);
    }

    public void performAttack(AttackPerpetratorConfigurable configurable) {
        attackHandler.sprintManager.setCurrentMode(configurable.getMode());
        attackHandler.handleAttack(configurable);
    }

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class AttackPerpetratorConfigurable {
        LivingEntity target;
        Angle angle;
        float maximumRange;
        boolean raytraceEnabled;
        boolean onlyCritical;
        boolean adaptiveCritical;
        boolean shouldBreakShield;
        boolean shouldUnpressShield;
        boolean useDynamicCooldown;
        boolean wallCheck;
        boolean eatingCheck;
        SprintManager.Mode mode;
        boolean legacyMode;
        float cps;

        public AttackPerpetratorConfigurable(LivingEntity target, Angle angle, float maximumRange,
                                             List<String> options, SprintManager.Mode mode, boolean legacyMode, float cps) {
            this.target = target;
            this.angle = angle;
            this.maximumRange = maximumRange;
            this.raytraceEnabled = options.contains("Raytrace check");
            this.onlyCritical = options.contains("Only critical");
            this.adaptiveCritical = options.contains("Adaptive critical");
            this.shouldBreakShield = options.contains("Break shield");
            this.shouldUnpressShield = options.contains("Un press shield");
            this.useDynamicCooldown = options.contains("Dynamic cooldown");
            this.wallCheck = options.contains("Check walls");
            this.eatingCheck = options.contains("Check eating");
            this.mode = mode;
            this.legacyMode = legacyMode;
            this.cps = cps;
        }
    }
}
