
package vurst.visual.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.Entity;
import net.minecraft.PlayerEntity;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.Direction;
import net.minecraft.Box;
import net.minecraft.HitResult;
import net.minecraft.Vec3d;
import net.minecraft.RaycastContext;
import net.minecraft.BlockHitResult;
import net.minecraft.ClientPlayerEntity;
import vurst.visual.base.events.impl.render.EventRender3D;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.ItemIconProvider;
import vurst.visual.client.modules.api.setting.impl.BooleanSetting;
import vurst.visual.client.modules.api.setting.impl.ColorSetting;
import vurst.visual.client.modules.api.setting.impl.MultiBooleanSetting;
import vurst.visual.utility.math.MathUtil;
import vurst.visual.utility.render.display.base.color.ColorRGBA;
import vurst.visual.utility.render.level.Render3DUtil;

@ModuleAnnotation(name="Ft Helper", category=Category.MOVEMENT, description="Показывает радиус/зону действия предметов.")
public final class RadiusHelper
extends Module {
    public static final RadiusHelper INSTANCE = new RadiusHelper();
    private static final String ITEM_DEZKA = "Дезка";
    private static final String ITEM_YAVKA = "Явка";
    private static final String ITEM_FIRE_CHARGE = "Огненый Заряд";
    private static final String ITEM_GOD_AURA = "Божья Аура";
    private static final String ITEM_TRAPKA = "Трапка";
    private static final String ITEM_PLAST = "Пласт";
    private static final String ITEM_SNOWBALL = "Снежок";
    private static final int IDX_DEZKA = 0;
    private static final int IDX_YAVKA = 1;
    private static final int IDX_FIRE_CHARGE = 2;
    private static final int IDX_GOD_AURA = 3;
    private static final int IDX_TRAPKA = 4;
    private static final int IDX_PLAST = 5;
    private static final int IDX_SNOWBALL = 6;
    private static final int FILL_ALPHA = 85;
    private static final int OUTLINE_ALPHA = 255;
    private static final float TRANSITION_DURATION = 0.5f;
    private static final float CIRCLE_STEP_DEGREES = 5.0f;
    private static final float OUTLINE_WIDTH = 3.0f;
    private static final double PLAST_EXTRA_EXPAND = 0.5;
    private static final double PLAST_SURFACE_OFFSET = 0.01;
    private static final double DEZKA_RADIUS = 10.0;
    private static final double YAVKA_RADIUS = 10.0;
    private static final double GOD_AURA_RADIUS = 2.0;
    private static final double DRAGON_SKIN_SIZE = 7.0;
    private static final double DRAGON_SKIN_HALF_SIZE = 3.5;
    private static final double DRAGON_PLAST_DEPTH = 2.0;
    private static final double DRAGON_PLAST_HALF_DEPTH = 1.0;
    private static final float TRAJECTORY_WIDTH = 2.25f;
    private static final double SNOWBALL_RADIUS = 7.0;
    private static final double SNOWBALL_SPEED = 1.5;
    private static final double SNOWBALL_GRAVITY = 0.03;
    private static final double SNOWBALL_DRAG = 0.99;
    private static final int SNOWBALL_MAX_STEPS = 160;
    private static final int SNOWBALL_SUBSTEPS = 6;
    private final MultiBooleanSetting items = new MultiBooleanSetting("Предметы", MultiBooleanSetting.Value.of("Дезка", true), MultiBooleanSetting.Value.of("Явка", true), MultiBooleanSetting.Value.of("Огненый Заряд", true), MultiBooleanSetting.Value.of("Божья Аура", true), MultiBooleanSetting.Value.of("Трапка", true), MultiBooleanSetting.Value.of("Пласт", true), MultiBooleanSetting.Value.of("Снежок", true));
    private final ColorSetting dezkaColor = new FtItemColorSetting("Цвет Дезки", new ColorRGBA(0, 85, 0), () -> this.items.isEnable(0), Items.ENDER_EYE);
    private final ColorSetting yavkaColor = new FtItemColorSetting("Цвет Явки", new ColorRGBA(153, 153, 153), () -> this.items.isEnable(1), Items.SUGAR);
    private final ColorSetting fireChargeColor = new FtItemColorSetting("Цвет Огненого Заряда", new ColorRGBA(85, 0, 0), () -> this.items.isEnable(2), Items.FIRE_CHARGE);
    private final ColorSetting godAuraColor = new FtItemColorSetting("Цвет Божьей Ауры", new ColorRGBA(0, 153, 153), () -> this.items.isEnable(3), Items.PHANTOM_MEMBRANE);
    private final ColorSetting trapkaColor = new FtItemColorSetting("Цвет Трапки", new ColorRGBA(139, 69, 19), () -> this.items.isEnable(4), Items.NETHERITE_SCRAP);
    private final ColorSetting plastColor = new FtItemColorSetting("Цвет Пласта", new ColorRGBA(51, 51, 51), () -> this.items.isEnable(5), Items.DRIED_KELP);
    private final ColorSetting snowballColor = new FtItemColorSetting("Цвет Снежка", new ColorRGBA(160, 220, 255), () -> this.items.isEnable(6), Items.SNOWBALL);
    private final BooleanSetting hitIndicator = new BooleanSetting("Показывать попадание", true);
    private final ColorSetting hitColor = new ColorSetting("Цвет попадания", new ColorRGBA(0, 255, 136), this.hitIndicator::isEnabled);
    private final BooleanSetting fillEnabled = new BooleanSetting("Заполнение", true);
    private final BooleanSetting dragonSkin = new BooleanSetting("Драконий скин", false);
    private int currentFillColor = RadiusHelper.withAlpha(new ColorRGBA(0, 85, 0), 85);
    private int currentOutlineColor = RadiusHelper.withAlpha(new ColorRGBA(0, 85, 0), 255);
    private int targetFillColor = this.currentFillColor;
    private int targetOutlineColor = this.currentOutlineColor;
    private float transitionTimer = 0.0f;
    private boolean lastPlayersInRadius = false;
    private int activeTransitionItem = -1;

    private RadiusHelper() {
    }

    @Override
    public void onDisable() {
        this.resetSnowballSmoothing();
        this.activeTransitionItem = -1;
        super.onDisable();
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (RadiusHelper.mc.player == null || RadiusHelper.mc.world == null) {
            return;
        }
        ClientPlayerEntity player = RadiusHelper.mc.player;
        ItemStack mainHand = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();
        int activeItemIndex = this.resolveActiveItemIndex(mainHand, offHand);
        Vec3d centerPos = player.getPos().add(0.0, -1.4, 0.0);
        if (activeItemIndex == 0) {
            this.renderRadiusPreset(event, player, centerPos, 10.0, 0, this.dezkaColor.getColor());
            this.resetSnowballSmoothing();
            return;
        }
        if (activeItemIndex == 1) {
            this.renderRadiusPreset(event, player, centerPos, 10.0, 1, this.yavkaColor.getColor());
            this.resetSnowballSmoothing();
            return;
        }
        if (activeItemIndex == 2) {
            this.renderRadiusPreset(event, player, centerPos, 10.0, 2, this.fireChargeColor.getColor());
            this.resetSnowballSmoothing();
            return;
        }
        if (activeItemIndex == 3) {
            this.renderRadiusPreset(event, player, centerPos, 2.0, 3, this.godAuraColor.getColor());
            this.resetSnowballSmoothing();
            return;
        }
        if (activeItemIndex == 6) {
            this.renderSnowballPrediction(event, player, 6, this.snowballColor.getColor());
            return;
        }
        if (activeItemIndex == 4) {
            Box cube = this.getTrapkaBox(player);
            ColorRGBA baseColor = this.trapkaColor.getColor();
            boolean playersInRadius = this.hasPlayersInBox(player, cube);
            boolean highlight = this.hitIndicator.isEnabled() && playersInRadius;
            ColorRGBA highlightColor = this.hitIndicator.isEnabled() ? this.hitColor.getColor() : baseColor;
            this.updateTransition(event, 4, highlight, baseColor, highlightColor);
            this.renderCubeOutline(cube, this.currentOutlineColor);
            this.resetSnowballSmoothing();
            return;
        }
        if (activeItemIndex == 5) {
            Box plane = this.getPlastBox(event, player);
            ColorRGBA baseColor = this.plastColor.getColor();
            boolean playersInRadius = this.hasPlayersInPlastBox(player, plane);
            boolean highlight = this.hitIndicator.isEnabled() && playersInRadius;
            ColorRGBA highlightColor = this.hitIndicator.isEnabled() ? this.hitColor.getColor() : baseColor;
            this.updateTransition(event, 5, highlight, baseColor, highlightColor);
            this.renderPlane(plane, this.currentFillColor, this.currentOutlineColor);
            this.resetSnowballSmoothing();
            return;
        }
        this.activeTransitionItem = -1;
        this.resetSnowballSmoothing();
    }

    private int resolveActiveItemIndex(ItemStack mainHand, ItemStack offHand) {
        int mainIndex = this.getEnabledItemIndex(mainHand);
        if (mainIndex != -1) {
            return mainIndex;
        }
        return this.getEnabledItemIndex(offHand);
    }

    private int getEnabledItemIndex(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return -1;
        }
        Item item = stack.getItem();
        if (item == Items.ENDER_EYE) {
            return this.items.isEnable(0) ? 0 : -1;
        }
        if (item == Items.SUGAR) {
            return this.items.isEnable(1) ? 1 : -1;
        }
        if (item == Items.FIRE_CHARGE) {
            return this.items.isEnable(2) ? 2 : -1;
        }
        if (item == Items.PHANTOM_MEMBRANE) {
            return this.items.isEnable(3) ? 3 : -1;
        }
        if (item == Items.NETHERITE_SCRAP) {
            return this.items.isEnable(4) ? 4 : -1;
        }
        if (item == Items.DRIED_KELP) {
            return this.items.isEnable(5) ? 5 : -1;
        }
        if (item == Items.SNOWBALL) {
            return this.items.isEnable(6) ? 6 : -1;
        }
        return -1;
    }

    private void renderRadiusPreset(EventRender3D event, ClientPlayerEntity player, Vec3d centerPos, double radius, int itemIndex, ColorRGBA baseColor) {
        if (this.activeTransitionItem != itemIndex) {
            this.activeTransitionItem = itemIndex;
            this.lastPlayersInRadius = false;
            this.transitionTimer = 0.0f;
            this.currentFillColor = RadiusHelper.withAlpha(baseColor, 85);
            this.currentOutlineColor = RadiusHelper.withAlpha(baseColor, 255);
            this.targetFillColor = this.currentFillColor;
            this.targetOutlineColor = this.currentOutlineColor;
        }
        boolean playersInRadius = this.hasPlayersInRadius(player, centerPos, radius);
        boolean highlight = this.hitIndicator.isEnabled() && playersInRadius;
        ColorRGBA highlightColor = this.hitIndicator.isEnabled() ? this.hitColor.getColor() : baseColor;
        this.updateColors(highlight, RadiusHelper.withAlpha(baseColor, 85), RadiusHelper.withAlpha(baseColor, 255), RadiusHelper.withAlpha(highlightColor, 85), RadiusHelper.withAlpha(highlightColor, 255), event.getPartialTicks());
        this.renderRadiusCircle(player, radius, this.currentFillColor, this.currentOutlineColor);
    }

    private void renderSnowballPrediction(EventRender3D event, ClientPlayerEntity player, int itemIndex, ColorRGBA baseColor) {
        SnowballPrediction prediction = this.predictSnowballPrediction(player, event.getPartialTicks());
        if (prediction == null || prediction.trajectory().size() < 2) {
            this.activeTransitionItem = -1;
            this.resetSnowballSmoothing();
            return;
        }
        if (this.activeTransitionItem != itemIndex) {
            this.activeTransitionItem = itemIndex;
            this.lastPlayersInRadius = false;
            this.transitionTimer = 0.0f;
            this.currentFillColor = RadiusHelper.withAlpha(baseColor, 85);
            this.currentOutlineColor = RadiusHelper.withAlpha(baseColor, 255);
            this.targetFillColor = this.currentFillColor;
            this.targetOutlineColor = this.currentOutlineColor;
            this.resetSnowballSmoothing();
        }
        Vec3d landingCenter = prediction.landingPos().add(0.0, 0.03, 0.0);
        boolean playersInRadius = this.hasPlayersInRadius(player, landingCenter, 7.0);
        boolean highlight = this.hitIndicator.isEnabled() && playersInRadius;
        ColorRGBA highlightColor = this.hitIndicator.isEnabled() ? this.hitColor.getColor() : baseColor;
        this.updateColors(highlight, RadiusHelper.withAlpha(baseColor, 85), RadiusHelper.withAlpha(baseColor, 255), RadiusHelper.withAlpha(highlightColor, 85), RadiusHelper.withAlpha(highlightColor, 255), event.getPartialTicks());
        this.renderTrajectory(prediction.trajectory(), this.currentOutlineColor);
        this.renderRadiusCircle(landingCenter, 7.0, this.currentFillColor, this.currentOutlineColor);
    }

    private SnowballPrediction predictSnowballPrediction(ClientPlayerEntity player, float partialTicks) {
        if (RadiusHelper.mc.world == null) {
            return null;
        }
        ArrayList<Vec3d> points = new ArrayList<Vec3d>();
        Vec3d position = this.getInterpolatedEyePos(player, partialTicks);
        Vec3d velocity = player.getRotationVec(partialTicks).normalize().multiply(1.5);
        points.add(position);
        Vec3d landing = null;
        block0: for (int i = 0; i < 160; ++i) {
            for (int stepIndex = 0; stepIndex < 6; ++stepIndex) {
                Vec3d prev = position;
                Vec3d step = velocity.multiply(0.16666666666666666);
                BlockHitResult hit = RadiusHelper.mc.world.raycast(new RaycastContext(prev, position = position.add(step), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, (Entity)player));
                if (hit.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHit = hit;
                    landing = blockHit.getPos();
                    points.add(landing);
                    break block0;
                }
                points.add(position);
                if (position.y < (double)RadiusHelper.mc.world.getBottomY() - 8.0) {
                    landing = position;
                    break block0;
                }
                double drag = Math.pow(0.99, 0.16666666666666666);
                velocity = velocity.subtract(0.0, 0.005, 0.0).multiply(drag);
            }
        }
        if (points.size() < 2) {
            return null;
        }
        if (landing == null) {
            landing = (Vec3d)points.get(points.size() - 1);
        }
        return new SnowballPrediction(points, landing);
    }

    private void renderTrajectory(List<Vec3d> points, int color) {
        for (int i = 1; i < points.size(); ++i) {
            Render3DUtil.drawLine(points.get(i - 1), points.get(i), color, 2.25f, false);
        }
    }

    private void updateColors(boolean playersInRadius, int baseFillColor, int baseOutlineColor, int lightFillColor, int lightOutlineColor, float partialTicks) {
        if (playersInRadius != this.lastPlayersInRadius) {
            this.transitionTimer = 0.0f;
            this.lastPlayersInRadius = playersInRadius;
        }
        this.targetFillColor = playersInRadius ? lightFillColor : baseFillColor;
        this.targetOutlineColor = playersInRadius ? lightOutlineColor : baseOutlineColor;
        this.transitionTimer = Math.min(this.transitionTimer + partialTicks / 0.5f, 1.0f);
        this.currentFillColor = this.lerpColor(this.currentFillColor, this.targetFillColor, this.transitionTimer);
        this.currentOutlineColor = this.lerpColor(this.currentOutlineColor, this.targetOutlineColor, this.transitionTimer);
    }

    private void updateTransition(EventRender3D event, int itemIndex, boolean highlight, ColorRGBA baseColor, ColorRGBA highlightColor) {
        if (this.activeTransitionItem != itemIndex) {
            this.activeTransitionItem = itemIndex;
            this.lastPlayersInRadius = false;
            this.transitionTimer = 0.0f;
            this.currentFillColor = RadiusHelper.withAlpha(baseColor, 85);
            this.currentOutlineColor = RadiusHelper.withAlpha(baseColor, 255);
            this.targetFillColor = this.currentFillColor;
            this.targetOutlineColor = this.currentOutlineColor;
        }
        this.updateColors(highlight, RadiusHelper.withAlpha(baseColor, 85), RadiusHelper.withAlpha(baseColor, 255), RadiusHelper.withAlpha(highlightColor, 85), RadiusHelper.withAlpha(highlightColor, 255), event.getPartialTicks());
    }

    private int lerpColor(int startColor, int endColor, float t) {
        int startA = startColor >> 24 & 0xFF;
        int startR = startColor >> 16 & 0xFF;
        int startG = startColor >> 8 & 0xFF;
        int startB = startColor & 0xFF;
        int endA = endColor >> 24 & 0xFF;
        int endR = endColor >> 16 & 0xFF;
        int endG = endColor >> 8 & 0xFF;
        int endB = endColor & 0xFF;
        int a = (int)((float)startA + (float)(endA - startA) * t);
        int r = (int)((float)startR + (float)(endR - startR) * t);
        int g = (int)((float)startG + (float)(endG - startG) * t);
        int b = (int)((float)startB + (float)(endB - startB) * t);
        return a << 24 | r << 16 | g << 8 | b;
    }

    private boolean hasPlayersInRadius(ClientPlayerEntity player, Vec3d centerPos, double radius) {
        double radiusSq = radius * radius;
        for (PlayerEntity entity : RadiusHelper.mc.world.getPlayers()) {
            if (this.shouldIgnoreHitHighlightTarget(player, entity) || !this.canSeeTarget(player, entity) || !(entity.getPos().squaredDistanceTo(centerPos) <= radiusSq)) continue;
            return true;
        }
        return false;
    }

    private boolean hasPlayersInBox(ClientPlayerEntity player, Box box) {
        for (PlayerEntity entity : RadiusHelper.mc.world.getPlayers()) {
            if (this.shouldIgnoreHitHighlightTarget(player, entity) || !this.canSeeTarget(player, entity) || !entity.getBoundingBox().intersects(box)) continue;
            return true;
        }
        return false;
    }

    private boolean hasPlayersInPlastBox(ClientPlayerEntity player, Box box) {
        for (PlayerEntity entity : RadiusHelper.mc.world.getPlayers()) {
            if (this.shouldIgnoreHitHighlightTarget(player, entity) || !this.canSeeTarget(player, entity) || !entity.getBoundingBox().intersects(box)) continue;
            return true;
        }
        return false;
    }

    private boolean canSeeTarget(ClientPlayerEntity player, PlayerEntity target) {
        Vec3d[] checks;
        if (RadiusHelper.mc.world == null) {
            return false;
        }
        Vec3d from = player.getEyePos();
        Box box = target.getBoundingBox();
        for (Vec3d to : checks = new Vec3d[]{target.getEyePos(), box.getCenter(), new Vec3d(box.minX, box.getCenter().y, box.minZ), new Vec3d(box.maxX, box.getCenter().y, box.maxZ)}) {
            BlockHitResult hit = RadiusHelper.mc.world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)player));
            if (hit.getType() != HitResult.Type.MISS) continue;
            return true;
        }
        return false;
    }

    private boolean shouldIgnoreHitHighlightTarget(ClientPlayerEntity player, PlayerEntity entity) {
        return entity == player || !entity.isAlive() || entity.isSpectator() || entity.isInvisible() || entity.isInvisibleTo((PlayerEntity)player);
    }

    private boolean isHolding(ItemStack stack, Item item) {
        return !stack.isEmpty() && stack.isOf(item);
    }

    private void renderRadiusCircle(ClientPlayerEntity player, double radius, int fillColor, int outlineColor) {
        Vec3d interpolatedPos = MathUtil.interpolate((Entity)player);
        double y = interpolatedPos.y + (double)player.getHeight() - 1.4;
        Vec3d center = new Vec3d(interpolatedPos.x, y, interpolatedPos.z);
        this.renderRadiusCircle(center, radius, fillColor, outlineColor);
    }

    private void renderRadiusCircle(Vec3d center, double radius, int fillColor, int outlineColor) {
        Vec3d firstPoint = null;
        Vec3d previousPoint = null;
        for (float angle = 0.0f; angle <= 360.0f; angle += 5.0f) {
            double radians = Math.toRadians(angle);
            Vec3d currentPoint = new Vec3d(center.x + Math.sin(radians) * radius, center.y, center.z - Math.cos(radians) * radius);
            if (previousPoint != null) {
                Render3DUtil.drawLine(previousPoint, currentPoint, outlineColor, 3.0f, false);
                if (this.fillEnabled.isEnabled()) {
                    Render3DUtil.drawQuad(center, previousPoint, currentPoint, center, fillColor, false);
                }
            } else {
                firstPoint = currentPoint;
            }
            previousPoint = currentPoint;
        }
        if (previousPoint != null && firstPoint != null) {
            Render3DUtil.drawLine(previousPoint, firstPoint, outlineColor, 3.0f, false);
            if (this.fillEnabled.isEnabled()) {
                Render3DUtil.drawQuad(center, previousPoint, firstPoint, center, fillColor, false);
            }
        }
    }

    private void renderCube(ClientPlayerEntity player, int fillColor, int outlineColor) {
        Vec3d position = MathUtil.interpolate((Entity)player);
        double cubeX = Math.floor(position.x) + 0.5;
        double cubeY = Math.floor(position.y) + 0.5 + 1.625;
        double cubeZ = Math.floor(position.z) + 0.5;
        float halfSize = 2.0f;
        Box cube = new Box(cubeX - (double)halfSize, cubeY - (double)halfSize, cubeZ - (double)halfSize, cubeX + (double)halfSize, cubeY + (double)halfSize, cubeZ + (double)halfSize);
        if (this.fillEnabled.isEnabled()) {
            this.drawFilledBox(cube, fillColor);
        }
        Render3DUtil.drawBox(cube, outlineColor, 3.0f, true, false, false);
    }

    private Box getTrapkaBox(ClientPlayerEntity player) {
        Vec3d position = MathUtil.interpolate((Entity)player);
        double cubeX = Math.floor(position.x) + 0.5;
        double cubeY = Math.floor(position.y) + 0.5 + 1.625;
        double cubeZ = Math.floor(position.z) + 0.5;
        float halfSize = this.dragonSkin.isEnabled() ? 3.5f : 2.0f;
        return new Box(cubeX - (double)halfSize, cubeY - (double)halfSize, cubeZ - (double)halfSize, cubeX + (double)halfSize, cubeY + (double)halfSize, cubeZ + (double)halfSize);
    }

    private void renderCubeOutline(Box cube, int outlineColor) {
        Render3DUtil.drawBox(cube, outlineColor, 3.0f, true, false, false);
    }

    private Box getPlastBox(EventRender3D event, ClientPlayerEntity player) {
        if (RadiusHelper.mc.world == null) {
            return new Box(player.getPos(), player.getPos());
        }
        if (this.dragonSkin.isEnabled()) {
            return this.getDragonPlastBox(player, event.getPartialTicks());
        }
        float width = 4.0f;
        float height = 4.0f;
        float thickness = 1.5f;
        float halfWidth = width / 2.0f;
        float halfHeight = height / 2.0f;
        float halfThickness = thickness / 2.0f;
        Vec3d lookVec = player.getRotationVec(event.getPartialTicks());
        Vec3d eyePos = this.getInterpolatedEyePos(player, event.getPartialTicks());
        Direction normal = this.getDominantLookDirection(lookVec);
        double shift = (double)halfThickness + 0.01;
        Vec3d center = eyePos.add(lookVec.multiply(4.0)).add((double)normal.getOffsetX() * shift, (double)normal.getOffsetY() * shift, (double)normal.getOffsetZ() * shift);
        Box plane = switch (normal.getAxis()) {
            default -> throw new MatchException(null, null);
            case Direction.Axis.X -> new Box(center.x - (double)halfThickness, center.y - (double)halfHeight, center.z - (double)halfWidth, center.x + (double)halfThickness, center.y + (double)halfHeight, center.z + (double)halfWidth);
            case Direction.Axis.Y -> new Box(center.x - (double)halfWidth, center.y - (double)halfThickness, center.z - (double)halfHeight, center.x + (double)halfWidth, center.y + (double)halfThickness, center.z + (double)halfHeight);
            case Direction.Axis.Z -> new Box(center.x - (double)halfWidth, center.y - (double)halfHeight, center.z - (double)halfThickness, center.x + (double)halfWidth, center.y + (double)halfHeight, center.z + (double)halfThickness);
        };
        return switch (normal.getAxis()) {
            default -> throw new MatchException(null, null);
            case Direction.Axis.X -> new Box(plane.minX, plane.minY - 0.5, plane.minZ - 0.5, plane.maxX, plane.maxY + 0.5, plane.maxZ + 0.5);
            case Direction.Axis.Y -> new Box(plane.minX - 0.5, plane.minY, plane.minZ - 0.5, plane.maxX + 0.5, plane.maxY, plane.maxZ + 0.5);
            case Direction.Axis.Z -> new Box(plane.minX - 0.5, plane.minY - 0.5, plane.minZ, plane.maxX + 0.5, plane.maxY + 0.5, plane.maxZ);
        };
    }

    private Box getDragonPlastBox(ClientPlayerEntity player, float partialTicks) {
        Vec3d lookVec = player.getRotationVec(partialTicks);
        Vec3d eyePos = this.getInterpolatedEyePos(player, partialTicks);
        Direction normal = this.getDominantLookDirection(lookVec);
        double shift = 1.01;
        Vec3d center = eyePos.add(lookVec.multiply(4.0)).add((double)normal.getOffsetX() * shift, (double)normal.getOffsetY() * shift, (double)normal.getOffsetZ() * shift);
        return switch (normal.getAxis()) {
            default -> throw new MatchException(null, null);
            case Direction.Axis.X -> new Box(center.x - 1.0, center.y - 3.5, center.z - 3.5, center.x + 1.0, center.y + 3.5, center.z + 3.5);
            case Direction.Axis.Y -> new Box(center.x - 3.5, center.y - 1.0, center.z - 3.5, center.x + 3.5, center.y + 1.0, center.z + 3.5);
            case Direction.Axis.Z -> new Box(center.x - 3.5, center.y - 3.5, center.z - 1.0, center.x + 3.5, center.y + 3.5, center.z + 1.0);
        };
    }

    private void renderPlane(Box plane, int fillColor, int outlineColor) {
        if (this.fillEnabled.isEnabled()) {
            this.drawFilledBox(plane, fillColor);
        }
        Render3DUtil.drawBox(plane, outlineColor, 3.0f, true, false, false);
    }

    private Direction getDominantLookDirection(Vec3d lookVec) {
        double ax = Math.abs(lookVec.x);
        double ay = Math.abs(lookVec.y);
        double az = Math.abs(lookVec.z);
        if (ay >= ax && ay >= az) {
            return lookVec.y >= 0.0 ? Direction.UP : Direction.DOWN;
        }
        if (ax >= az) {
            return lookVec.x >= 0.0 ? Direction.EAST : Direction.WEST;
        }
        return lookVec.z >= 0.0 ? Direction.SOUTH : Direction.NORTH;
    }

    private void drawFilledBox(Box box, int color) {
        Render3DUtil.drawQuad(new Vec3d(box.minX, box.minY, box.minZ), new Vec3d(box.maxX, box.minY, box.minZ), new Vec3d(box.maxX, box.minY, box.maxZ), new Vec3d(box.minX, box.minY, box.maxZ), color, false);
        Render3DUtil.drawQuad(new Vec3d(box.minX, box.minY, box.minZ), new Vec3d(box.minX, box.maxY, box.minZ), new Vec3d(box.maxX, box.maxY, box.minZ), new Vec3d(box.maxX, box.minY, box.minZ), color, false);
        Render3DUtil.drawQuad(new Vec3d(box.maxX, box.minY, box.minZ), new Vec3d(box.maxX, box.maxY, box.minZ), new Vec3d(box.maxX, box.maxY, box.maxZ), new Vec3d(box.maxX, box.minY, box.maxZ), color, false);
        Render3DUtil.drawQuad(new Vec3d(box.minX, box.minY, box.maxZ), new Vec3d(box.maxX, box.minY, box.maxZ), new Vec3d(box.maxX, box.maxY, box.maxZ), new Vec3d(box.minX, box.maxY, box.maxZ), color, false);
        Render3DUtil.drawQuad(new Vec3d(box.minX, box.minY, box.minZ), new Vec3d(box.minX, box.minY, box.maxZ), new Vec3d(box.minX, box.maxY, box.maxZ), new Vec3d(box.minX, box.maxY, box.minZ), color, false);
        Render3DUtil.drawQuad(new Vec3d(box.minX, box.maxY, box.minZ), new Vec3d(box.minX, box.maxY, box.maxZ), new Vec3d(box.maxX, box.maxY, box.maxZ), new Vec3d(box.maxX, box.maxY, box.minZ), color, false);
    }

    private static int withAlpha(ColorRGBA color, int alpha) {
        return new ColorRGBA(color.getRed(), color.getGreen(), color.getBlue(), alpha).getRGB();
    }

    private void resetSnowballSmoothing() {
    }

    private Vec3d getInterpolatedEyePos(ClientPlayerEntity player, float partialTicks) {
        double x = player.prevX + (player.getX() - player.prevX) * (double)partialTicks;
        double y = player.prevY + (player.getY() - player.prevY) * (double)partialTicks + (double)player.getEyeHeight(player.getPose());
        double z = player.prevZ + (player.getZ() - player.prevZ) * (double)partialTicks;
        return new Vec3d(x, y, z);
    }

    @Override
    public String[] getLegacyNames() {
        return new String[]{"Radius Helper"};
    }

    private static final class FtItemColorSetting
    extends ColorSetting
    implements ItemIconProvider {
        private final Item item;

        private FtItemColorSetting(String name, ColorRGBA color, Supplier<Boolean> visible, Item item) {
            super(name, color, visible);
            this.item = item;
        }

        @Override
        public ItemStack getMenuIconStack() {
            return this.item.getDefaultStack();
        }
    }

    private record SnowballPrediction(List<Vec3d> trajectory, Vec3d landingPos) {
    }
}

