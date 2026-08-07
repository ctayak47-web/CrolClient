
package vurst.visual.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import net.minecraft.ShaderProgramKeys;
import net.minecraft.ShaderProgramKey;
import net.minecraft.Entity;
import net.minecraft.ArrowEntity;
import net.minecraft.ProjectileEntity;
import net.minecraft.EnderPearlEntity;
import net.minecraft.TridentEntity;
import net.minecraft.PotionEntity;
import net.minecraft.BowItem;
import net.minecraft.CrossbowItem;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.BlockView;
import net.minecraft.BlockPos;
import net.minecraft.Position;
import net.minecraft.Box;
import net.minecraft.HitResult;
import net.minecraft.Vec3d;
import net.minecraft.BlockState;
import net.minecraft.BufferRenderer;
import net.minecraft.BufferBuilder;
import net.minecraft.Tessellator;
import net.minecraft.VertexFormats;
import net.minecraft.VertexFormat;
import net.minecraft.Identifier;
import net.minecraft.BlockTags;
import net.minecraft.MathHelper;
import net.minecraft.RaycastContext;
import net.minecraft.BlockHitResult;
import net.minecraft.ClientPlayerEntity;
import net.minecraft.RotationAxis;
import net.minecraft.BuiltBuffer;
import org.joml.Matrix4f;
import vurst.visual.VurstVisual;
import vurst.visual.base.events.impl.render.EventRender2D;
import vurst.visual.base.events.impl.render.EventRender3D;
import vurst.visual.base.font.Font;
import vurst.visual.base.font.Fonts;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.BooleanSetting;
import vurst.visual.client.modules.api.setting.impl.ColorSetting;
import vurst.visual.utility.math.ProjectionUtil;
import vurst.visual.utility.render.display.base.BorderRadius;
import vurst.visual.utility.render.display.base.CustomDrawContext;
import vurst.visual.utility.render.display.base.color.ColorRGBA;
import vurst.visual.utility.render.level.Render3DUtil;

@ModuleAnnotation(name="Predictions", category=Category.MOVEMENT, description="Предсказывает траекторию снарядов.")
public final class Predictions
extends Module {
    public static final Predictions INSTANCE = new Predictions();
    private static final int MAX_TICKS = 240;
    private static final int SUBSTEPS = 8;
    private static final float BASE_LINE_WIDTH = 4.0f;
    private static final double PEARL_SPEED = 1.5;
    private static final double PEARL_SPEED_FACTOR = 1.06;
    private static final double PEARL_GRAVITY = 0.03;
    private static final double ARROW_SPEED = 3.0;
    private static final double ARROW_GRAVITY = 0.05;
    private static final double TRIDENT_SPEED = 2.5;
    private static final double TRIDENT_GRAVITY = 0.03;
    private static final double POTION_SPEED = 0.5;
    private static final double POTION_GRAVITY = 0.05;
    private static final double POTION_SPLASH_RADIUS = 2.0;
    private static final double POTION_LINGERING_RADIUS = 2.0;
    private static final float POTION_RADIUS_STEP_DEGREES = 7.5f;
    private static final double WATER_DRAG = 0.8;
    private static final double AIR_DRAG = 0.99;
    private static final float CROSSBOW_SPREAD_DEGREES = 10.0f;
    private static final double PROJECTILE_SPAWN_OFFSET = 0.35;
    private static final Identifier ZALUPA_PARTICLE = VurstVisual.id("hud/particles/thor.png");
    private static final long TRAIL_LIFETIME_MS = 520L;
    private static final int MAX_TRAIL_PARTICLES = 420;
    private static final float TRAIL_UPWARD_DRIFT = 0.0022f;
    private static final float TRAIL_BASE_SIZE = 0.22f;
    private static final float TRAIL_SIZE_JITTER = 0.14f;
    private final List<ProjectilePoint> projectilePoints = new ArrayList<ProjectilePoint>();
    private final List<TrailParticle> trailParticles = new ArrayList<TrailParticle>();
    private final BooleanSetting showPearlLandingBlock = new BooleanSetting("Блок приземления перла", true);
    private final BooleanSetting showPearl = new BooleanSetting("Пёрл", true);
    private final BooleanSetting showBow = new BooleanSetting("Лук", true);
    private final BooleanSetting showCrossbow = new BooleanSetting("Арбалет", true);
    private final BooleanSetting showTrident = new BooleanSetting("Трезубец", true);
    private final BooleanSetting showPotions = new BooleanSetting("Взрывные зелья", true);
    private final BooleanSetting showWhenHolding = new BooleanSetting("Показывать в руке", true);
    private final BooleanSetting showImpactTime = new BooleanSetting("Время до падения", true);
    private final ColorSetting lineColor = new ColorSetting("Цвет линии", () -> VurstVisual.getInstance().getThemeManager().getCurrentTheme().getColor());

    private Predictions() {
    }

    @Override
    public void onDisable() {
        this.projectilePoints.clear();
        this.trailParticles.clear();
        super.onDisable();
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        boolean shouldShowHolding;
        if (Predictions.mc.player == null || Predictions.mc.world == null) {
            return;
        }
        this.projectilePoints.clear();
        this.updateTrailParticles();
        boolean hasProjectiles = false;
        for (Entity entity : Predictions.mc.world.getEntities()) {
            boolean unsupportedType;
            boolean isMoving;
            if (entity instanceof EnderPearlEntity) {
                EnderPearlEntity pearl = (EnderPearlEntity)entity;
                if (this.showPearl.isEnabled() && this.isMyProjectile(entity)) {
                    boolean bl = isMoving = !pearl.isOnGround() && pearl.getVelocity().lengthSquared() >= 1.0E-4;
                    if (!isMoving) continue;
                    this.spawnTrailFromProjectile((ProjectileEntity)pearl);
                    hasProjectiles = true;
                    this.simulatePearl(pearl, true);
                    continue;
                }
            }
            if (entity instanceof ArrowEntity) {
                ArrowEntity arrow = (ArrowEntity)entity;
                if ((this.showBow.isEnabled() || this.showCrossbow.isEnabled()) && this.isMyProjectile(entity)) {
                    isMoving = !arrow.isOnGround() && !arrow.isTouchingWater() && arrow.getVelocity().lengthSquared() >= 0.01;
                    if (!isMoving) continue;
                    this.spawnTrailFromProjectile((ProjectileEntity)arrow);
                    hasProjectiles = true;
                    this.simulateArrow(arrow, true);
                    continue;
                }
            }
            if (entity instanceof TridentEntity) {
                TridentEntity trident = (TridentEntity)entity;
                if (this.showTrident.isEnabled() && this.isMyProjectile(entity)) {
                    isMoving = !trident.isOnGround() && !trident.isTouchingWater() && trident.getVelocity().lengthSquared() >= 0.01;
                    if (!isMoving) continue;
                    this.spawnTrailFromProjectile((ProjectileEntity)trident);
                    hasProjectiles = true;
                    this.simulateTrident(trident, true);
                    continue;
                }
            }
            if (entity instanceof PotionEntity) {
                PotionEntity potion = (PotionEntity)entity;
                if (this.showPotions.isEnabled() && this.isMyProjectile(entity)) {
                    isMoving = !potion.isOnGround() && potion.getVelocity().lengthSquared() >= 1.0E-4;
                    if (!isMoving) continue;
                    this.spawnTrailFromProjectile((ProjectileEntity)potion);
                    hasProjectiles = true;
                    this.simulatePotion(potion, true);
                    continue;
                }
            }
            if (!(entity instanceof ProjectileEntity)) continue;
            ProjectileEntity projectile = (ProjectileEntity)entity;
            if (!this.isMyProjectile(entity) || !(unsupportedType = !(projectile instanceof EnderPearlEntity) && !(projectile instanceof ArrowEntity) && !(projectile instanceof TridentEntity) && !(projectile instanceof PotionEntity)) || projectile.isOnGround() || !(projectile.getVelocity().lengthSquared() > 1.0E-4)) continue;
            this.spawnTrailFromProjectile(projectile);
        }
        boolean holdingPearl = this.isHolding(Items.ENDER_PEARL);
        boolean holdingBow = this.isHolding(Items.BOW);
        boolean holdingCrossbow = this.isHolding(Items.CROSSBOW);
        boolean holdingTrident = this.isHolding(Items.TRIDENT);
        boolean holdingSplashPotion = this.isHolding(Items.SPLASH_POTION);
        boolean holdingLingeringPotion = this.isHolding(Items.LINGERING_POTION);
        boolean holdingPotion = holdingSplashPotion || holdingLingeringPotion;
        boolean isBowDrawn = Predictions.mc.player.isUsingItem() && Predictions.mc.player.getActiveItem().getItem() == Items.BOW;
        boolean isCrossbowCharged = holdingCrossbow && (CrossbowItem.isCharged((ItemStack)Predictions.mc.player.getMainHandStack()) || CrossbowItem.isCharged((ItemStack)Predictions.mc.player.getOffHandStack()));
        boolean isTridentCharging = Predictions.mc.player.isUsingItem() && Predictions.mc.player.getActiveItem().getItem() == Items.TRIDENT;
        boolean bl = shouldShowHolding = this.showWhenHolding.isEnabled() && (holdingPearl || holdingBow && isBowDrawn || holdingCrossbow && isCrossbowCharged || holdingTrident && isTridentCharging || holdingPotion);
        if (shouldShowHolding && !hasProjectiles) {
            if (holdingPearl && this.showPearl.isEnabled()) {
                this.simulateProjectileFromHand(ProjectileType.PEARL, event.getPartialTicks());
            }
            if (holdingBow && this.showBow.isEnabled() && isBowDrawn) {
                this.simulateProjectileFromHand(ProjectileType.ARROW, event.getPartialTicks());
            }
            if (holdingCrossbow && this.showCrossbow.isEnabled() && isCrossbowCharged) {
                this.simulateCrossbowFromHand(event.getPartialTicks());
            }
            if (holdingTrident && this.showTrident.isEnabled() && isTridentCharging) {
                this.simulateProjectileFromHand(ProjectileType.TRIDENT, event.getPartialTicks());
            }
            if (holdingPotion && this.showPotions.isEnabled()) {
                double radius = holdingLingeringPotion ? 2.0 : 2.0;
                this.simulateProjectileFromHand(ProjectileType.POTION, event.getPartialTicks(), radius);
            }
        }
        if (!this.projectilePoints.isEmpty()) {
            this.renderProjectileTrajectory3D();
        }
        this.renderTrailParticles(event);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        if (Predictions.mc.player == null || Predictions.mc.world == null || this.projectilePoints.isEmpty() || !this.showImpactTime.isEnabled()) {
            return;
        }
        this.renderImpactTime2D(event.getContext());
    }

    private void simulateProjectileFromHand(ProjectileType type, float partialTicks) {
        this.simulateProjectileFromHand(type, 0.0f, partialTicks, 0.0);
    }

    private void simulateProjectileFromHand(ProjectileType type, float partialTicks, double impactRadius) {
        this.simulateProjectileFromHand(type, 0.0f, partialTicks, impactRadius);
    }

    private void simulateProjectileFromHand(ProjectileType type, float yawOffsetDegrees, float partialTicks, double impactRadius) {
        Vec3d eyePos = this.getInterpolatedEyePos(Predictions.mc.player, partialTicks);
        float pitch = MathHelper.lerp((float)partialTicks, (float)Predictions.mc.player.prevPitch, (float)Predictions.mc.player.getPitch());
        float yaw = MathHelper.lerpAngleDegrees((float)partialTicks, (float)Predictions.mc.player.prevYaw, (float)Predictions.mc.player.getYaw()) + yawOffsetDegrees;
        Vec3d direction = this.getDirectionFromYawPitch(yaw, pitch);
        Vec3d startPos = eyePos.add(direction.multiply(0.35));
        double speed = switch (type.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> 1.5;
            case 1 -> this.getBowArrowSpeed();
            case 2 -> 2.5;
            case 3 -> 0.5;
        };
        this.simulateProjectileLanding(startPos, direction.multiply(speed), type, false, true, impactRadius);
    }

    private double getBowArrowSpeed() {
        int useTicks = Predictions.mc.player.getItemUseTime();
        float pullProgress = BowItem.getPullProgress((int)useTicks);
        return 3.0 * Math.max(0.1, (double)pullProgress);
    }

    private void simulateCrossbowFromHand(float partialTicks) {
        Vec3d eyePos = this.getInterpolatedEyePos(Predictions.mc.player, partialTicks);
        float pitch = MathHelper.lerp((float)partialTicks, (float)Predictions.mc.player.prevPitch, (float)Predictions.mc.player.getPitch());
        float yaw = MathHelper.lerpAngleDegrees((float)partialTicks, (float)Predictions.mc.player.prevYaw, (float)Predictions.mc.player.getYaw());
        Vec3d dir = this.getCrossbowSpreadDirection(yaw, pitch, 0.0f);
        Vec3d startPos = eyePos.add(dir.multiply(0.35));
        this.simulateProjectileLanding(startPos, dir.multiply(3.0), ProjectileType.ARROW, false, true);
        dir = this.getCrossbowSpreadDirection(yaw, pitch, -10.0f);
        startPos = eyePos.add(dir.multiply(0.35));
        this.simulateProjectileLanding(startPos, dir.multiply(3.0), ProjectileType.ARROW, false, true);
        dir = this.getCrossbowSpreadDirection(yaw, pitch, 10.0f);
        startPos = eyePos.add(dir.multiply(0.35));
        this.simulateProjectileLanding(startPos, dir.multiply(3.0), ProjectileType.ARROW, false, true);
    }

    private Vec3d getInterpolatedEyePos(ClientPlayerEntity player, float partialTicks) {
        if (player == null) {
            return Vec3d.ZERO;
        }
        double x = MathHelper.lerp((double)partialTicks, (double)player.prevX, (double)player.getX());
        double y = MathHelper.lerp((double)partialTicks, (double)player.prevY, (double)player.getY()) + (double)player.getEyeHeight(player.getPose());
        double z = MathHelper.lerp((double)partialTicks, (double)player.prevZ, (double)player.getZ());
        return new Vec3d(x, y, z);
    }

    private Vec3d getDirectionFromYawPitch(float yaw, float pitch) {
        return new Vec3d(-Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)), -Math.sin(Math.toRadians(pitch)), Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
    }

    private Vec3d getCrossbowSpreadDirection(float yaw, float pitch, float spreadDegrees) {
        Vec3d forward = this.getDirectionFromYawPitch(yaw, pitch);
        if (Math.abs(spreadDegrees) <= 1.0E-4f) {
            return forward;
        }
        double yawRad = Math.toRadians(yaw);
        Vec3d right = new Vec3d(-Math.cos(yawRad), 0.0, -Math.sin(yawRad));
        double spreadRad = Math.toRadians(spreadDegrees);
        Vec3d offset = right.multiply(Math.tan(spreadRad));
        return forward.add(offset).normalize();
    }

    private void simulatePearl(EnderPearlEntity pearl, boolean isPlayerThrown) {
        Vec3d pos = pearl.getPos();
        Vec3d vel = pearl.getVelocity();
        boolean inWater = pearl.isTouchingWater();
        int ticks = 0;
        block0: for (int t = 0; t < 240; ++t) {
            for (int s = 0; s < 8; ++s) {
                Vec3d prev = pos;
                inWater = this.isInWater(pos) || inWater;
                double dragPerSub = Math.pow(inWater ? 0.8 : 0.99, 0.125);
                Vec3d step = vel.multiply(0.1325);
                pos = pos.add(step);
                this.projectilePoints.add(new ProjectilePoint(pos, ticks, false, ProjectileType.PEARL, true, null, isPlayerThrown));
                HitResult hit = this.raycast(prev, pos, (Entity)pearl);
                if (hit.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult bhr = (BlockHitResult)hit;
                    this.projectilePoints.add(new ProjectilePoint(bhr.getPos(), ticks, true, ProjectileType.PEARL, true, null, isPlayerThrown));
                    break block0;
                }
                if (pos.y < -128.0) {
                    this.projectilePoints.add(new ProjectilePoint(pos, ticks, true, ProjectileType.PEARL, true, null, isPlayerThrown));
                    break block0;
                }
                double gravityMultiplier = inWater ? 0.2 : 1.0;
                vel = vel.subtract(0.0, 0.0318 * gravityMultiplier / 8.0, 0.0).multiply(dragPerSub);
            }
            ++ticks;
        }
    }

    private void simulateArrow(ArrowEntity arrow, boolean isPlayerThrown) {
        Vec3d pos = arrow.getPos();
        Vec3d vel = arrow.getVelocity();
        boolean inWater = arrow.isTouchingWater();
        int ticks = 0;
        block0: for (int t = 0; t < 240; ++t) {
            for (int s = 0; s < 8; ++s) {
                Vec3d prev = pos;
                inWater = this.isInWater(pos) || inWater;
                double dragPerSub = Math.pow(inWater ? 0.8 : 0.99, 0.125);
                Vec3d step = vel.multiply(0.125);
                Entity hitEntity = this.findHitEntity((Entity)arrow, pos = pos.add(step));
                if (hitEntity != null) {
                    this.projectilePoints.add(new ProjectilePoint(pos, ticks, true, ProjectileType.ARROW, true, hitEntity, isPlayerThrown));
                    break block0;
                }
                this.projectilePoints.add(new ProjectilePoint(pos, ticks, false, ProjectileType.ARROW, true, null, isPlayerThrown));
                HitResult hit = this.raycast(prev, pos, (Entity)arrow);
                if (hit.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult bhr = (BlockHitResult)hit;
                    this.projectilePoints.add(new ProjectilePoint(bhr.getPos(), ticks, true, ProjectileType.ARROW, true, null, isPlayerThrown));
                    break block0;
                }
                if (pos.y < -128.0) {
                    this.projectilePoints.add(new ProjectilePoint(pos, ticks, true, ProjectileType.ARROW, true, null, isPlayerThrown));
                    break block0;
                }
                double gravityMultiplier = inWater ? 0.2 : 1.0;
                vel = vel.subtract(0.0, 0.05 * gravityMultiplier / 8.0, 0.0).multiply(dragPerSub);
            }
            ++ticks;
        }
    }

    private void simulateTrident(TridentEntity trident, boolean isPlayerThrown) {
        Vec3d pos = trident.getPos();
        Vec3d vel = trident.getVelocity();
        boolean inWater = trident.isTouchingWater();
        int ticks = 0;
        block0: for (int t = 0; t < 240; ++t) {
            for (int s = 0; s < 8; ++s) {
                Vec3d prev = pos;
                inWater = this.isInWater(pos) || inWater;
                double dragPerSub = Math.pow(inWater ? 0.8 : 0.99, 0.125);
                Vec3d step = vel.multiply(0.125);
                Entity hitEntity = this.findHitEntity((Entity)trident, pos = pos.add(step));
                if (hitEntity != null) {
                    this.projectilePoints.add(new ProjectilePoint(pos, ticks, true, ProjectileType.TRIDENT, true, hitEntity, isPlayerThrown));
                    break block0;
                }
                this.projectilePoints.add(new ProjectilePoint(pos, ticks, false, ProjectileType.TRIDENT, true, null, isPlayerThrown));
                HitResult hit = this.raycast(prev, pos, (Entity)trident);
                if (hit.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult bhr = (BlockHitResult)hit;
                    this.projectilePoints.add(new ProjectilePoint(bhr.getPos(), ticks, true, ProjectileType.TRIDENT, true, null, isPlayerThrown));
                    break block0;
                }
                if (pos.y < -128.0) {
                    this.projectilePoints.add(new ProjectilePoint(pos, ticks, true, ProjectileType.TRIDENT, true, null, isPlayerThrown));
                    break block0;
                }
                double gravityMultiplier = inWater ? 0.2 : 1.0;
                vel = vel.subtract(0.0, 0.03 * gravityMultiplier / 8.0, 0.0).multiply(dragPerSub);
            }
            ++ticks;
        }
    }

    private void simulatePotion(PotionEntity potion, boolean isPlayerThrown) {
        Vec3d pos = potion.getPos();
        Vec3d vel = potion.getVelocity();
        boolean inWater = potion.isTouchingWater();
        double impactRadius = this.resolvePotionImpactRadius(potion.getStack());
        int ticks = 0;
        block0: for (int t = 0; t < 240; ++t) {
            for (int s = 0; s < 8; ++s) {
                Vec3d prev = pos;
                inWater = this.isInWater(pos) || inWater;
                double dragPerSub = Math.pow(inWater ? 0.8 : 0.99, 0.125);
                Vec3d step = vel.multiply(0.125);
                Entity hitEntity = this.findHitEntity((Entity)potion, pos = pos.add(step));
                if (hitEntity != null) {
                    this.projectilePoints.add(new ProjectilePoint(pos, ticks, true, ProjectileType.POTION, true, hitEntity, isPlayerThrown, impactRadius));
                    break block0;
                }
                this.projectilePoints.add(new ProjectilePoint(pos, ticks, false, ProjectileType.POTION, true, null, isPlayerThrown));
                HitResult hit = this.raycast(prev, pos, (Entity)potion);
                if (hit.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult bhr = (BlockHitResult)hit;
                    this.projectilePoints.add(new ProjectilePoint(bhr.getPos(), ticks, true, ProjectileType.POTION, true, null, isPlayerThrown, impactRadius));
                    break block0;
                }
                if (pos.y < -128.0) {
                    this.projectilePoints.add(new ProjectilePoint(pos, ticks, true, ProjectileType.POTION, true, null, isPlayerThrown, impactRadius));
                    break block0;
                }
                double gravityMultiplier = inWater ? 0.2 : 1.0;
                vel = vel.subtract(0.0, 0.05 * gravityMultiplier / 8.0, 0.0).multiply(dragPerSub);
            }
            ++ticks;
        }
    }

    private void simulateProjectileLanding(Vec3d startPos, Vec3d initialVelocity, ProjectileType type, boolean isMoving, boolean isPlayerThrown) {
        this.simulateProjectileLanding(startPos, initialVelocity, type, isMoving, isPlayerThrown, 0.0);
    }

    private void simulateProjectileLanding(Vec3d startPos, Vec3d initialVelocity, ProjectileType type, boolean isMoving, boolean isPlayerThrown, double impactRadius) {
        Vec3d pos = startPos;
        Vec3d vel = initialVelocity;
        int ticks = 0;
        boolean inWater = false;
        block6: for (int t = 0; t < 240; ++t) {
            for (int s = 0; s < 8; ++s) {
                Entity hitEntity;
                Vec3d prev = pos;
                inWater = this.isInWater(pos) || inWater;
                double dragPerSub = Math.pow(inWater ? 0.8 : 0.99, 0.125);
                double speedFactor = type == ProjectileType.PEARL ? 1.06 : 1.0;
                Vec3d step = vel.multiply(speedFactor / 8.0);
                pos = pos.add(step);
                if (isPlayerThrown && type != ProjectileType.PEARL && (hitEntity = this.findHitEntity((Entity)Predictions.mc.player, pos)) != null) {
                    this.projectilePoints.add(new ProjectilePoint(pos, ticks, true, type, isMoving, hitEntity, true, impactRadius));
                    break block6;
                }
                this.projectilePoints.add(new ProjectilePoint(pos, ticks, false, type, isMoving, null, isPlayerThrown));
                HitResult hit = this.raycast(prev, pos, (Entity)Predictions.mc.player);
                if (hit.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult bhr = (BlockHitResult)hit;
                    this.projectilePoints.add(new ProjectilePoint(bhr.getPos(), ticks, true, type, isMoving, null, isPlayerThrown, impactRadius));
                    break block6;
                }
                if (pos.y < -128.0) {
                    this.projectilePoints.add(new ProjectilePoint(pos, ticks, true, type, isMoving, null, isPlayerThrown, impactRadius));
                    break block6;
                }
                double gravity = switch (type.ordinal()) {
                    default -> throw new MatchException(null, null);
                    case 0 -> 0.03;
                    case 1 -> 0.05;
                    case 2 -> 0.03;
                    case 3 -> 0.05;
                };
                double gravityMultiplier = inWater ? 0.2 : 1.0;
                vel = vel.subtract(0.0, gravity * speedFactor * gravityMultiplier / 8.0, 0.0).multiply(dragPerSub);
            }
            ++ticks;
        }
    }

    private void renderProjectileTrajectory3D() {
        List<List<ProjectilePoint>> trajectories = this.groupTrajectories(this.projectilePoints);
        ColorRGBA baseColor = this.lineColor.getColor(1.0f);
        for (int g = 0; g < trajectories.size(); ++g) {
            int i;
            List<ProjectilePoint> trajectory = trajectories.get(g);
            if (trajectory.size() < 2 || !this.isPlayerTrajectory(trajectory)) continue;
            Vec3d[] interpolated = new Vec3d[trajectory.size()];
            boolean[] visible = new boolean[trajectory.size()];
            for (i = 0; i < trajectory.size(); ++i) {
                Vec3d pointPos;
                interpolated[i] = pointPos = trajectory.get(i).pos();
                visible[i] = this.isTrajectoryPointVisible(pointPos);
            }
            for (i = 1; i < trajectory.size(); ++i) {
                if (!visible[i - 1] || !visible[i]) continue;
                float t = (float)i / (float)trajectory.size();
                int alpha = Math.max(24, Math.min(255, Math.round(255.0f * (1.0f - t))));
                ColorRGBA line = baseColor.withAlpha(alpha);
                Render3DUtil.drawLine(interpolated[i - 1], interpolated[i], line.getRGB(), 4.0f, false);
            }
            int landingIndex = this.findLandingIndex(trajectory);
            if (landingIndex < 0) continue;
            ProjectilePoint landing = trajectory.get(landingIndex);
            Vec3d landingPos = interpolated[landingIndex];
            if (landing.type() == ProjectileType.PEARL && this.showPearlLandingBlock.isEnabled() && this.isLandingVisible(landingPos, false)) {
                this.renderPearlLandingBlock(landingPos, baseColor);
            }
            if (landing.type() != ProjectileType.POTION || !(landing.impactRadius() > 0.0)) continue;
            int radiusOutline = baseColor.withAlpha(220).getRGB();
            int radiusFill = baseColor.withAlpha(58).getRGB();
            this.renderImpactRadiusCircle(landingPos.add(0.0, 0.03, 0.0), landing.impactRadius(), radiusFill, radiusOutline);
        }
    }

    private boolean isTrajectoryPointVisible(Vec3d pointPos) {
        return this.isLandingVisible(pointPos, false);
    }

    private void renderPearlLandingBlock(Vec3d landingPos, ColorRGBA baseColor) {
        BlockPos blockPos = BlockPos.ofFloored((Position)landingPos);
        Box blockBox = new Box(blockPos).expand(0.002);
        int color = baseColor.withAlpha(220).getRGB();
        Render3DUtil.drawBox(blockBox, color, 2.0f, true, true, false);
    }

    private void renderImpactRadiusCircle(Vec3d center, double radius, int fillColor, int outlineColor) {
        Vec3d firstPoint = null;
        Vec3d previousPoint = null;
        for (float angle = 0.0f; angle <= 360.0f; angle += 7.5f) {
            double radians = Math.toRadians(angle);
            Vec3d currentPoint = new Vec3d(center.x + Math.sin(radians) * radius, center.y, center.z - Math.cos(radians) * radius);
            if (previousPoint != null) {
                Render3DUtil.drawLine(previousPoint, currentPoint, outlineColor, 2.2f, false);
                Render3DUtil.drawQuad(center, previousPoint, currentPoint, center, fillColor, false);
            } else {
                firstPoint = currentPoint;
            }
            previousPoint = currentPoint;
        }
        if (previousPoint != null && firstPoint != null) {
            Render3DUtil.drawLine(previousPoint, firstPoint, outlineColor, 2.2f, false);
            Render3DUtil.drawQuad(center, previousPoint, firstPoint, center, fillColor, false);
        }
    }

    private void renderImpactTime2D(CustomDrawContext ctx) {
        List<List<ProjectilePoint>> trajectories = this.groupTrajectories(this.projectilePoints);
        Font font = Fonts.MEDIUM.getFont(7.0f);
        for (int g = 0; g < trajectories.size(); ++g) {
            ProjectilePoint landing;
            int landingIndex;
            List<ProjectilePoint> trajectory = trajectories.get(g);
            if (trajectory.isEmpty() || !this.isPlayerTrajectory(trajectory) || (landingIndex = this.findLandingIndex(trajectory)) < 0 || (landing = trajectory.get(landingIndex)).hitEntity() != null) continue;
            Vec3d impactPos = landing.pos();
            if (!this.isLandingVisible(impactPos, landing.type() == ProjectileType.PEARL)) continue;
            Vec3d screen = ProjectionUtil.worldSpaceToScreenSpace(impactPos.add(0.0, -0.3, 0.0));
            if (screen.z <= 0.0 || screen.z >= 1.0) continue;
            String timeText = this.formatImpactTime(landing.ticks());
            float textWidth = font.width(timeText);
            float iconSize = 11.0f;
            float panelPadding = 5.0f;
            float panelHeight = panelPadding + font.height() + panelPadding;
            float panelWidth = panelPadding + iconSize + 3.0f + textWidth + panelPadding;
            float x = (float)screen.x - panelWidth / 2.0f;
            float y = (float)screen.y - 2.0f;
            ctx.drawRoundedRect(x, y, panelWidth, panelHeight, BorderRadius.all(2.0f), new ColorRGBA(24, 24, 24, 80));
            Item iconItem = this.getImpactIcon(landing.type());
            float iconY = y + (panelHeight - iconSize) * 0.5f;
            float textX = x + panelPadding + iconSize + 3.0f;
            float textY = y + panelPadding;
            ctx.drawItem(iconItem.getDefaultStack(), (int)(x + panelPadding), (int)iconY);
            ctx.drawText(font, timeText, textX, textY, new ColorRGBA(196, 218, 255, 255));
        }
    }

    private int findLandingIndex(List<ProjectilePoint> trajectory) {
        for (int i = 0; i < trajectory.size(); ++i) {
            if (!trajectory.get(i).isLandingPoint()) continue;
            return i;
        }
        return -1;
    }

    private boolean isPlayerTrajectory(List<ProjectilePoint> trajectory) {
        return !trajectory.isEmpty() && trajectory.get(0).isPlayerThrown();
    }

    private String formatImpactTime(int ticks) {
        float seconds = Math.max(0.0f, (float)ticks / 20.0f);
        return String.format(Locale.US, "%.1fs", Float.valueOf(seconds));
    }

    private Item getImpactIcon(ProjectileType type) {
        return switch (type.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> Items.ENDER_PEARL;
            case 1 -> Items.ARROW;
            case 2 -> Items.TRIDENT;
            case 3 -> Items.SPLASH_POTION;
        };
    }

    private boolean isLandingVisible(Vec3d landingPos, boolean ignoreFoliage) {
        Vec3d cameraPos = Predictions.mc.player.getCameraPosVec(1.0f);
        BlockPos targetPos = BlockPos.ofFloored((Position)landingPos);
        Vec3d direction = landingPos.subtract(cameraPos);
        Vec3d stepDirection = direction.lengthSquared() > 1.0E-6 ? direction.normalize().multiply(0.08) : Vec3d.ZERO;
        Vec3d rayStart = cameraPos;
        for (int pass = 0; pass < 12; ++pass) {
            BlockHitResult hit = Predictions.mc.world.raycast(new RaycastContext(rayStart, landingPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)Predictions.mc.player));
            if (hit.getType() == HitResult.Type.MISS) {
                return true;
            }
            if (!(hit instanceof BlockHitResult)) {
                return false;
            }
            BlockHitResult blockHit = hit;
            BlockPos hitPos = blockHit.getBlockPos();
            if (hitPos.equals((Object)targetPos) || hitPos.equals((Object)targetPos.up()) || hitPos.equals((Object)targetPos.down())) {
                return true;
            }
            if (!ignoreFoliage || !this.isFoliageVisionPassable(hitPos)) {
                return false;
            }
            rayStart = blockHit.getPos().add(stepDirection);
            if (!(rayStart.squaredDistanceTo(landingPos) <= 1.0E-4)) continue;
            return true;
        }
        return false;
    }

    private boolean isFoliageVisionPassable(BlockPos pos) {
        BlockState state = Predictions.mc.world.getBlockState(pos);
        return state.isReplaceable() || state.isIn(BlockTags.LEAVES) || state.getCollisionShape((BlockView)Predictions.mc.world, pos).isEmpty();
    }

    private List<List<ProjectilePoint>> groupTrajectories(List<ProjectilePoint> points) {
        ArrayList<List<ProjectilePoint>> out = new ArrayList<List<ProjectilePoint>>();
        ArrayList<ProjectilePoint> current = null;
        ProjectileType lastType = null;
        boolean lastPlayerThrown = false;
        boolean lastLanding = false;
        for (ProjectilePoint point : points) {
            boolean newGroup;
            boolean bl = newGroup = lastType != point.type() || lastPlayerThrown != point.isPlayerThrown() || lastLanding;
            if (newGroup || current == null) {
                if (current != null && !current.isEmpty()) {
                    out.add(current);
                }
                current = new ArrayList<ProjectilePoint>();
            }
            current.add(point);
            lastType = point.type();
            lastPlayerThrown = point.isPlayerThrown();
            lastLanding = point.isLandingPoint();
        }
        if (current != null && !current.isEmpty()) {
            out.add((List<ProjectilePoint>)current);
        }
        return out;
    }

    private void updateTrailParticles() {
        if (this.trailParticles.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        Iterator<TrailParticle> iterator2 = this.trailParticles.iterator();
        while (iterator2.hasNext()) {
            TrailParticle particle = iterator2.next();
            particle.prevPosition = particle.position;
            particle.velocity = particle.velocity.multiply(0.86, 0.92, 0.86).add(0.0, (double)0.0022f, 0.0);
            particle.position = particle.position.add(particle.velocity);
            if (now - particle.spawnTime <= 520L) continue;
            iterator2.remove();
        }
    }

    private void spawnTrailFromProjectile(ProjectileEntity projectile) {
    }

    private void renderTrailParticles(EventRender3D event) {
        if (this.trailParticles.isEmpty() || Predictions.mc.getEntityRenderDispatcher().camera == null) {
            return;
        }
        ColorRGBA base = this.lineColor.getColor(1.0f);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (Identifier)ZALUPA_PARTICLE);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.enableDepthTest();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        for (TrailParticle particle : this.trailParticles) {
            this.renderTrailParticle(buffer, event, particle, base);
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void renderTrailParticle(BufferBuilder buffer, EventRender3D event, TrailParticle particle, ColorRGBA baseColor) {
        long ageMs = System.currentTimeMillis() - particle.spawnTime;
        float progress = MathHelper.clamp((float)((float)ageMs / 520.0f), (float)0.0f, (float)1.0f);
        if (progress >= 1.0f) {
            return;
        }
        float life = 1.0f - progress;
        float eased = 1.0f - (1.0f - life) * (1.0f - life);
        float scale = particle.size * (0.55f + eased * 0.85f);
        if (scale <= 0.0f) {
            return;
        }
        int alpha = Math.max(1, Math.round(230.0f * eased));
        int red = Math.min(255, baseColor.getRed() + 28);
        int green = Math.min(255, baseColor.getGreen() + 28);
        int blue = Math.min(255, baseColor.getBlue() + 28);
        int color = new ColorRGBA(red, green, blue, alpha).getRGB();
        Vec3d camPos = Predictions.mc.getEntityRenderDispatcher().camera.getPos();
        Vec3d pos = particle.renderPos(event.getPartialTicks());
        float rotation = particle.rotation + (float)ageMs * particle.rotationSpeed;
        event.getMatrix().push();
        event.getMatrix().translate(pos.x - camPos.x, pos.y - camPos.y, pos.z - camPos.z);
        event.getMatrix().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-Predictions.mc.getEntityRenderDispatcher().camera.getYaw()));
        event.getMatrix().multiply(RotationAxis.POSITIVE_X.rotationDegrees(Predictions.mc.getEntityRenderDispatcher().camera.getPitch()));
        event.getMatrix().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotation));
        event.getMatrix().scale(-scale, -scale, scale);
        Matrix4f matrix = event.getMatrix().peek().getPositionMatrix();
        buffer.vertex(matrix, -0.5f, -0.5f, 0.0f).texture(0.0f, 1.0f).color(color);
        buffer.vertex(matrix, 0.5f, -0.5f, 0.0f).texture(1.0f, 1.0f).color(color);
        buffer.vertex(matrix, 0.5f, 0.5f, 0.0f).texture(1.0f, 0.0f).color(color);
        buffer.vertex(matrix, -0.5f, 0.5f, 0.0f).texture(0.0f, 0.0f).color(color);
        event.getMatrix().pop();
    }

    private boolean isInWater(Vec3d pos) {
        BlockPos blockPos = BlockPos.ofFloored((Position)pos);
        return Predictions.mc.world.getBlockState(blockPos).getFluidState().isStill();
    }

    private double resolvePotionImpactRadius(ItemStack stack) {
        if (stack != null && stack.isOf(Items.LINGERING_POTION)) {
            return 2.0;
        }
        return 2.0;
    }

    private boolean isHolding(Item item) {
        return Predictions.mc.player.getMainHandStack().getItem() == item || Predictions.mc.player.getOffHandStack().getItem() == item;
    }

    private boolean isMyProjectile(Entity entity) {
        ProjectileEntity projectile;
        block3: {
            block2: {
                if (!(entity instanceof ProjectileEntity)) break block2;
                projectile = (ProjectileEntity)entity;
                if (Predictions.mc.player != null) break block3;
            }
            return false;
        }
        Entity owner = projectile.getOwner();
        return owner != null && owner.getUuid().equals(Predictions.mc.player.getUuid());
    }

    private Entity findHitEntity(Entity projectile, Vec3d pos) {
        for (Entity entity : Predictions.mc.world.getEntities()) {
            if (entity == projectile || entity == Predictions.mc.player || !entity.getBoundingBox().expand(0.3).contains(pos)) continue;
            return entity;
        }
        return null;
    }

    private HitResult raycast(Vec3d start, Vec3d end, Entity entity) {
        RaycastContext ctx = new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, entity);
        return Predictions.mc.world.raycast(ctx);
    }

    private static enum ProjectileType {
        PEARL,
        ARROW,
        TRIDENT,
        POTION;

    }

    private record ProjectilePoint(Vec3d pos, int ticks, boolean isLandingPoint, ProjectileType type, boolean isMoving, Entity hitEntity, boolean isPlayerThrown, double impactRadius) {
        private ProjectilePoint(Vec3d pos, int ticks, boolean isLandingPoint, ProjectileType type, boolean isMoving, Entity hitEntity, boolean isPlayerThrown) {
            this(pos, ticks, isLandingPoint, type, isMoving, hitEntity, isPlayerThrown, 0.0);
        }
    }

    private static final class TrailParticle {
        private Vec3d position;
        private Vec3d prevPosition;
        private Vec3d velocity;
        private final long spawnTime;
        private final float size;
        private final float rotation;
        private final float rotationSpeed;

        private TrailParticle(Vec3d position, Vec3d velocity, float size, float rotation, float rotationSpeed) {
            this.position = position;
            this.prevPosition = position;
            this.velocity = velocity;
            this.size = size;
            this.rotation = rotation;
            this.rotationSpeed = rotationSpeed;
            this.spawnTime = System.currentTimeMillis();
        }

        private Vec3d renderPos(float tickDelta) {
            return new Vec3d(MathHelper.lerp((double)tickDelta, (double)this.prevPosition.x, (double)this.position.x), MathHelper.lerp((double)tickDelta, (double)this.prevPosition.y, (double)this.position.y), MathHelper.lerp((double)tickDelta, (double)this.prevPosition.z, (double)this.position.z));
        }
    }
}

