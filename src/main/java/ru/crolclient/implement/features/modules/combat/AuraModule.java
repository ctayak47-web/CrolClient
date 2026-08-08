package ru.crolclient.implement.features.modules.combat;

import ru.crolclient.common.util.target.TargetSelector;
import ru.crolclient.implement.features.modules.combat.killaura.points.MultiPoint;
import ru.crolclient.implement.features.modules.combat.killaura.points.PointFinder;
import ru.crolclient.implement.features.modules.combat.killaura.points.SmartPoint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import ru.crolclient.api.event.EventHandler;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.*;
import ru.crolclient.common.util.task.TaskPriority;
import ru.crolclient.core.Extra;
import ru.crolclient.implement.events.player.PostRotationMovementInputEvent;
import ru.crolclient.implement.events.player.TickEvent;
import ru.crolclient.implement.features.commands.defaults.DebugCommand;
import ru.crolclient.implement.features.modules.combat.killaura.attack.AttackHandler;
import ru.crolclient.implement.features.modules.combat.killaura.attack.AttackPerpetrator;
import ru.crolclient.implement.features.modules.combat.killaura.attack.ClickScheduler;
import ru.crolclient.implement.features.modules.combat.killaura.attack.SprintManager;
import ru.crolclient.implement.features.modules.combat.killaura.rotation.*;
import ru.crolclient.implement.features.modules.combat.killaura.rotation.angle.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuraModule extends Module {
    TargetSelector targetSelector = new TargetSelector();
    PointFinder pointFinder = new PointFinder();
    MultiPoint multiPoint = new MultiPoint();
    SmartPoint smartPoint = new SmartPoint();

    @NonFinal
    LivingEntity target = null;

    SelectSetting pvpVersion = new SelectSetting("PvP Version", "Select PvP version")
            .value("1.8", "1.9+");

    MultiSelectSetting targetTypeSetting = new MultiSelectSetting("Target Type", "Filters the entire list of targets by type")
            .value(Stream.of(TargetSelector.TargetType.values())
                    .map(TargetSelector.TargetType::getDisplayName)
                    .toArray(String[]::new));

    ValueSetting maxDistanceSetting = new ValueSetting("Max Distance", "Sets the value of the maximum target search distance")
            .setValue(3.0f).range(1.0f, 6.0f).increment(0.1000000f);

    ValueSetting preAimDistance = new ValueSetting("Pre-aim Distance", "Distance for target acquisition and aiming")
            .setValue(6.0f).range(3.0f, 12.0f).increment(0.1000000f);

    ValueSetting elytraDistance = new ValueSetting("Elytra Distance", "Attack distance while using elytra")
            .setValue(4.5f).range(3.0f, 8.0f).increment(0.1000000f);

    RangeSetting cpsSetting = new RangeSetting("CPS", "Clicks per second in 1.8 mode")
            .range(1.0f, 20.0f).increment(0.5f, 0.5f).setValue(8.0f, 12.0f)
            .visible(() -> pvpVersion.isSelected("1.8"));

    MultiSelectSetting attackSetting = new MultiSelectSetting("Attack setting", "Allows you to customize the attack")
            .value("Only critical", "Adaptive critical", "Raytrace check", "Dynamic cooldown", "Break shield", "Un press shield",
                    "Check walls", "Check eating");

    SelectSetting correctionType = new SelectSetting("Correction Type", "Selects the type of correction")
            .value("Free", "Focused");

    GroupSetting correctionGroupSetting = new GroupSetting("Move correction", "Prevents detection by movement sensitive anticheats.")
            .settings(correctionType);

    SelectSetting sprintMode = new SelectSetting("Sprint Mode", "Allows you to select a sprint mod")
            .value("None", "Dynamic", "Legacy", "Legit");

    SelectSetting aimMode = new SelectSetting("Aim Time", "Allows you to select the timing of the rotation")
            .value("Normal", "Snap", "One Tick");

    SelectSetting pointsMode = new SelectSetting("Target Mode", "Выберите режим поиска точки атаки")
            .value("Point Finder", "Multi Point", "Smart Point");

    SelectSetting rotationModeSetting = new SelectSetting("Rotation Mode", "Select the mode for aim rotation correction")
            //.value("ReallyWorld", "Adaptive", "FunTime", "HolyWorld Classic", "HolyWorld Lite");
            .value("Advanced", "Adaptive");

    AttackPerpetrator attackPerpetrator = new AttackPerpetrator();

    public AuraModule() {
        super("Aura", "Aura", ModuleCategory.COMBAT);
        setup(pvpVersion, maxDistanceSetting, preAimDistance, elytraDistance, cpsSetting, targetTypeSetting, attackSetting,
                sprintMode, aimMode, pointsMode, rotationModeSetting, correctionGroupSetting);
    }

    @Override
    public void deactivate() {
        targetSelector.releaseTarget();
        target = null;
        super.deactivate();
    }

    @EventHandler
    public void onPostRotationMovementInput(PostRotationMovementInputEvent postRotationMovementInputEvent) {
        target = updateTarget();
        if (target != null) {
            RotationController rotationController = RotationController.INSTANCE;
            Vec3d attackVector = pointFinder.computeVector(target, maxDistanceSetting.getValue(), rotationController.getRotation(),
                    getSmoothMode().randomValue());
            Angle angle = AngleUtil.fromVec3d(attackVector.subtract(mc.player.getEyePos()));
            rotateToTarget(target, new Angle.VecRotation(angle, attackVector), rotationController);
        }
    }

    @EventHandler
    public void onTick(TickEvent tickEvent) {
        if (target != null) {
            attackTarget(target, RotationController.INSTANCE.getCurrentAngle());
        }
    }

    private LivingEntity updateTarget() {
        Set<TargetSelector.TargetType> targetTypes = new HashSet<>();

        for (String selected : targetTypeSetting.getSelected()) {
            for (TargetSelector.TargetType type : TargetSelector.TargetType.values()) {
                if (type.getDisplayName().equals(selected)) {
                    targetTypes.add(type);
                    break;
                }
            }
        }

        return targetSelector.updateTarget(targetTypes, mc.world.getEntities(), preAimDistance.getValue());
    }

    private void attackTarget(LivingEntity target, Angle angle) {
        AttackPerpetrator attackPerpetrator = Extra.getInstance().getAttackPerpetrator();

        float attackDistance = mc.player.isGliding() ? 
            elytraDistance.getValue() : maxDistanceSetting.getValue();

        AttackPerpetrator.AttackPerpetratorConfigurable configurable = new AttackPerpetrator.AttackPerpetratorConfigurable(
                target,
                RotationController.INSTANCE.getServerAngle(),
                attackDistance,
                attackSetting.getSelected(),
                getSprintMode(),
                pvpVersion.isSelected("1.8"),
                cpsSetting.getValue()
        );

        if (angle != null && aimMode.isSelected("One Tick")) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    angle.getYaw(), angle.getPitch(), mc.player.isOnGround(), mc.player.horizontalCollision));
        }

        attackPerpetrator.performAttack(configurable);

        if (angle != null && aimMode.isSelected("One Tick")) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround(), mc.player.horizontalCollision));
        }
    }

    private void rotateToTarget(LivingEntity target, Angle.VecRotation rotation, RotationController rotationController) {
        Vec3d attackVector;
        if (pointsMode.isSelected("Point Finder")) {
            attackVector = pointFinder.computeVector(target, maxDistanceSetting.getValue(),
                    rotationController.getRotation(), getSmoothMode().randomValue());
        } else if (pointsMode.isSelected("Multi Point")) {
            attackVector = multiPoint.computeVector(target, maxDistanceSetting.getValue(),
                    rotationController.getRotation(), getSmoothMode().randomValue());
        } else {
            attackVector = smartPoint.computeVector(target, maxDistanceSetting.getValue(),
                    rotationController.getRotation(), getSmoothMode().randomValue());
        }

        Angle angle = AngleUtil.fromVec3d(attackVector.subtract(mc.player.getEyePos()));
        Angle.VecRotation vecRotation = new Angle.VecRotation(angle, attackVector);

        RotationConfig configurable = new RotationConfig(getSmoothMode(),
                DebugCommand.debug,
                correctionGroupSetting.isValue(),
                ((SelectSetting) correctionGroupSetting.getSubSetting("Correction Type")).isSelected("Free")
        );

        AttackHandler attackHandler = Extra.getInstance().getAttackPerpetrator().getAttackHandler();
        ClickScheduler clickScheduler = attackHandler.getClickScheduler();

        if (aimMode.isSelected("Snap") && clickScheduler.hasTicksElapsedSinceLastClick(2)) {
            return;
        }

        if (aimMode.isSelected("One Tick")) {
            return;
        }

        rotationController.rotateTo(vecRotation, target, configurable, TaskPriority.HIGH_IMPORTANCE_1, this);
    }

    public SprintManager.Mode getSprintMode() {
        switch (sprintMode.getSelected()) {
            case "Dynamic" -> {
                return SprintManager.Mode.DYNAMIC;
            }
            case "Legacy" -> {
                return SprintManager.Mode.LEGACY;
            }
            case "Legit" -> {
                return SprintManager.Mode.LEGIT;
            }
        }
        return SprintManager.Mode.NONE;
    }

    public AngleSmoothMode getSmoothMode() {
        if (mc.player != null && mc.player.isGliding()) {
            return new ElytraAdaptiveSmoothMode();
        }

        if (aimMode.isSelected("Snap")) {
            return new LinearSmoothMode();
        } else {
            switch (rotationModeSetting.getSelected()) {
                case "Adaptive" -> {
                    return new AdaptiveSmoothMode();
                }
                case "FunTime" -> {
                    return new FunTimeSmoothMode();
                }
                case "Advanced" -> {
                    return new ReallyWorldSmoothMode();
                }
                case "HolyWorld Classic" -> {
                    return new HolyWorldClassicSmoothMode();
                }
                case "HolyWorld Lite" -> {
                    return new HolyWorldLiteSmoothMode();
                }
            }
        }
        return new ReallyWorldSmoothMode();
    }
}
