package ru.crolclient.implement.features.modules.combat.killaura.attack;

import ru.crolclient.implement.features.modules.combat.killaura.rotation.Angle;
import ru.crolclient.implement.features.modules.combat.killaura.rotation.RotationController;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import ru.crolclient.common.QuickImports;
import ru.crolclient.common.util.player.PlayerIntersectionUtil;
import ru.crolclient.implement.events.packet.PacketEvent;
import ru.crolclient.implement.features.modules.combat.killaura.rotation.RaytracingUtil;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

@Getter
public class AttackHandler implements QuickImports {
    SprintManager sprintManager = new SprintManager();
    ClickScheduler clickScheduler = new ClickScheduler();

    void tick() {
        sprintManager.tick(this);
    }

    public void onPacket(PacketEvent packetEvent) {
        Packet<?> packet = packetEvent.getPacket();
        if (packet instanceof UpdateSelectedSlotC2SPacket) {
            clickScheduler.recalculate(650L);
        } else if (packet instanceof HandSwingC2SPacket) {
            clickScheduler.recalculate(500L);
        }
    }

    void handleAttack(AttackPerpetrator.AttackPerpetratorConfigurable configurable) {
        if (canAttack(configurable)) {
            if (mc.player.isBlocking() && configurable.isShouldUnpressShield()) {
                mc.interactionManager.stopUsingItem(mc.player);
            }
            sprintManager.preAttack();
            mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(configurable.getTarget(), mc.player.isSneaking()));
            mc.player.resetLastAttackedTicks();
            mc.player.swingHand(Hand.MAIN_HAND);
            sprintManager.postAttack();
        }
    }


    boolean canAttack(AttackPerpetrator.AttackPerpetratorConfigurable config) {

        Entity targetEntity = getRayTracingEntity(config);

        if (isRaytraceCheckFailed(config, targetEntity)) {
            return false;
        }

        if (config.isWallCheck()) {
            boolean canSeeTarget = mc.player.canSee(config.getTarget());

            if (!canSeeTarget) {
                Angle currentAngle = RotationController.INSTANCE.getCurrentAngle();
                if (currentAngle == null) {
                    return false;
                }

                EntityHitResult entityHit = RaytracingUtil.raytraceEntity(
                        config.getMaximumRange(),
                        currentAngle,
                        entity -> entity == config.getTarget()
                );

                BlockHitResult blockHit = RaytracingUtil.raycast(
                        config.getMaximumRange(),
                        currentAngle,
                        false
                );

                if (blockHit != null && (entityHit == null ||
                        mc.player.getEyePos().distanceTo(blockHit.getPos()) <
                                mc.player.getEyePos().distanceTo(entityHit.getPos()))) {
                    return false;
                }
            }
        }

        if (config.isEatingCheck()) {
            boolean isEating = mc.player.isUsingItem();
            boolean hasMainHandFood = mc.player.getMainHandStack().get(net.minecraft.component.DataComponentTypes.FOOD) != null;
            boolean hasOffHandFood = mc.player.getOffHandStack().get(net.minecraft.component.DataComponentTypes.FOOD) != null;
            boolean isUsingMainHand = mc.player.getActiveHand() == Hand.MAIN_HAND;
            boolean isUsingOffHand = mc.player.getActiveHand() == Hand.OFF_HAND;

            if (isEating && ((hasMainHandFood && isUsingMainHand) || (hasOffHandFood && isUsingOffHand))) {
                return false;
            }
        }

        if (config.isLegacyMode()) {
            if (!clickScheduler.canClick(config.getCps())) {
                return false;
            }
        } else {
            if (isCooldownNotComplete(config)) {
                return false;
            }
        }

        if (config.isOnlyCritical()) {
            if (hasMovementRestrictions()) {
                return false;
            }
            if (!isPlayerInCriticalState()) {
                return false;
            }
        } else if (config.isAdaptiveCritical()) {
            if (mc.player.input.playerInput.jump()) {
                if (hasMovementRestrictions()) {
                    return false;
                }
                if (!isPlayerInCriticalState()) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isRaytraceCheckFailed(AttackPerpetrator.AttackPerpetratorConfigurable config, Entity targetEntity) {
        return config.isRaytraceEnabled() && targetEntity != config.getTarget();
    }

    private boolean isCooldownNotComplete(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        return !clickScheduler.isCooldownComplete(config.isUseDynamicCooldown());
    }

    private boolean hasMovementRestrictions() {
        return mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                || mc.player.hasStatusEffect(StatusEffects.LEVITATION)
                || PlayerIntersectionUtil.isPlayerInWeb()
                || mc.player.isSubmergedInWater()
                || mc.player.isInLava()
                || mc.player.isClimbing();
    }

    private boolean isPlayerInCriticalState() {
        if (mc.player.isGliding()) {
            return true;
        }
        return !mc.player.isOnGround() && mc.player.fallDistance > 0;
    }
    private Entity getRayTracingEntity(AttackPerpetrator.AttackPerpetratorConfigurable configurable) {
        EntityHitResult entityHitResult = RaytracingUtil.raytraceEntity(
                configurable.getMaximumRange(),
                configurable.getAngle(),
                e -> configurable.isRaytraceEnabled());

        if (entityHitResult != null) {
            return entityHitResult.getEntity();
        }
        return null;
    }

    private boolean hasLineOfSight(Entity target) {
        Vec3d eyePos = mc.player.getEyePos();
        Box targetBox = target.getBoundingBox();
        Vec3d targetPos = target.getPos().add(0, target.getHeight() / 2, 0);

        return mc.world.raycast(new RaycastContext(
                eyePos,
                targetPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                mc.player
        )).getType() == HitResult.Type.MISS;
    }
}
