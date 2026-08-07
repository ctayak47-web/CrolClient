
package vurst.visual.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.BlockView;
import net.minecraft.Blocks;
import net.minecraft.BlockPos;
import net.minecraft.Box;
import net.minecraft.Vec3i;
import net.minecraft.HitResult;
import net.minecraft.Vec3d;
import net.minecraft.VoxelShapes;
import net.minecraft.VoxelShape;
import net.minecraft.BlockState;
import net.minecraft.MathHelper;
import net.minecraft.BlockHitResult;
import net.minecraft.ClientPlayerEntity;
import vurst.visual.VurstVisual;
import vurst.visual.base.events.impl.render.EventRender3D;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.BooleanSetting;
import vurst.visual.client.modules.api.setting.impl.NumberSetting;
import vurst.visual.utility.render.display.base.color.ColorRGBA;
import vurst.visual.utility.render.display.base.color.ColorUtil;
import vurst.visual.utility.render.level.Render3DUtil;

@ModuleAnnotation(name="Block Outline", category=Category.RENDER, description="Подсвечивает выбранный блок.")
public final class BlockOutline
extends Module {
    private static final String LEGACY_MODULE_NAME_RU = "Контур блока";
    private static final String SETTING_OUTLINE = "Контур";
    private static final String SETTING_FILL = "Заливка";
    private static final String SETTING_FILL_ALPHA = "Прозрачность заливки";
    private static final String SETTING_SMOOTH = "Плавный переход";
    private static final String SETTING_TRANSITION_TIME = "Время перехода";
    public static final BlockOutline INSTANCE = new BlockOutline();
    private final BooleanSetting outline = new BooleanSetting("Контур", true);
    private final BooleanSetting fill = new BooleanSetting("Заливка", true);
    private final NumberSetting fillAlpha = new NumberSetting("Прозрачность заливки", 0.2f, 0.05f, 1.0f, 0.05f, this.fill::isEnabled);
    private final BooleanSetting smooth = new BooleanSetting("Плавный переход", true);
    private final NumberSetting transitionTime = new NumberSetting("Время перехода", 0.18f, 0.05f, 1.0f, 0.05f, this.smooth::isEnabled);
    private BlockPos lastTargetPos;
    private List<Box> animationFromBoxes = List.of();
    private List<Box> animationToBoxes = List.of();
    private long animationStartedAt;

    private BlockOutline() {
    }

    @Override
    public String[] getLegacyNames() {
        return new String[]{LEGACY_MODULE_NAME_RU};
    }

    @Override
    public void load(JsonObject object) {
        this.migrateLegacySettings(object);
        super.load(object);
    }

    @Override
    public void onEnable() {
        this.resetAnimation();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.resetAnimation();
        super.onDisable();
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (BlockOutline.mc.player == null || BlockOutline.mc.world == null) {
            this.resetAnimation();
            return;
        }
        BlockPos blockPos = this.findBlockToDisplay();
        if (blockPos == null) {
            return;
        }
        List<Box> targetBoxes = this.getWorldBoxes(blockPos);
        if (targetBoxes.isEmpty()) {
            return;
        }
        this.updateAnimationTarget(blockPos, targetBoxes);
        List<Box> currentBoxes = this.getCurrentBoxes(System.currentTimeMillis());
        if (currentBoxes.isEmpty()) {
            return;
        }
        this.renderBlockOutline(event, currentBoxes);
    }

    private void renderBlockOutline(EventRender3D event, List<Box> boxes) {
        ColorRGBA accentColor = VurstVisual.getInstance().getThemeManager().getCurrentTheme().getColor();
        int outlineColor = accentColor.getRGB();
        if (this.fill.isEnabled()) {
            int fillColor = ColorUtil.multAlpha(outlineColor, this.fillAlpha.getCurrent());
            for (Box box : boxes) {
                this.drawFill(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, fillColor);
            }
        }
        if (!this.outline.isEnabled()) {
            return;
        }
        for (Box box : boxes) {
            Render3DUtil.drawBox(box, outlineColor, 1.0f, true, false, false);
        }
    }

    private void drawFill(double x1, double y1, double z1, double x2, double y2, double z2, int color) {
        Render3DUtil.drawQuad(new Vec3d(x1, y1, z1), new Vec3d(x2, y1, z1), new Vec3d(x2, y1, z2), new Vec3d(x1, y1, z2), color, false);
        Render3DUtil.drawQuad(new Vec3d(x1, y1, z1), new Vec3d(x1, y2, z1), new Vec3d(x2, y2, z1), new Vec3d(x2, y1, z1), color, false);
        Render3DUtil.drawQuad(new Vec3d(x2, y1, z1), new Vec3d(x2, y2, z1), new Vec3d(x2, y2, z2), new Vec3d(x2, y1, z2), color, false);
        Render3DUtil.drawQuad(new Vec3d(x1, y1, z2), new Vec3d(x2, y1, z2), new Vec3d(x2, y2, z2), new Vec3d(x1, y2, z2), color, false);
        Render3DUtil.drawQuad(new Vec3d(x1, y1, z1), new Vec3d(x1, y1, z2), new Vec3d(x1, y2, z2), new Vec3d(x1, y2, z1), color, false);
        Render3DUtil.drawQuad(new Vec3d(x1, y2, z1), new Vec3d(x1, y2, z2), new Vec3d(x2, y2, z2), new Vec3d(x2, y2, z1), color, false);
    }

    private void migrateLegacySettings(JsonObject object) {
        if (object == null || !object.has("Settings") || !object.get("Settings").isJsonObject()) {
            return;
        }
        JsonObject settings = object.getAsJsonObject("Settings");
        this.migrateSettingKey(settings, "Outline", SETTING_OUTLINE);
        this.migrateSettingKey(settings, "Fill", SETTING_FILL);
        this.migrateSettingKey(settings, "Fill Alpha", SETTING_FILL_ALPHA);
        this.migrateSettingKey(settings, "Smooth", SETTING_SMOOTH);
        this.migrateSettingKey(settings, "Transition Time", SETTING_TRANSITION_TIME);
    }

    private void migrateSettingKey(JsonObject settings, String legacyName, String newName) {
        if (!settings.has(newName) && settings.has(legacyName)) {
            settings.add(newName, settings.get(legacyName).deepCopy());
        }
    }

    private BlockPos findBlockToDisplay() {
        if (BlockOutline.mc.crosshairTarget == null || BlockOutline.mc.crosshairTarget.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockHitResult hit = (BlockHitResult)BlockOutline.mc.crosshairTarget;
        BlockPos pos = hit.getBlockPos();
        ClientPlayerEntity player = BlockOutline.mc.player;
        if (player == null) {
            return null;
        }
        double reachDistance = 6.0;
        double distanceSq = player.squaredDistanceTo(Vec3d.ofCenter((Vec3i)pos));
        if (distanceSq > reachDistance * reachDistance) {
            return null;
        }
        return pos;
    }

    private List<Box> getWorldBoxes(BlockPos blockPos) {
        BlockState state = BlockOutline.mc.world.getBlockState(blockPos);
        if (state.isAir()) {
            return List.of();
        }
        if (state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.LAVA || state.getBlock() == Blocks.CAVE_AIR || state.getBlock() == Blocks.VOID_AIR) {
            return List.of();
        }
        VoxelShape shape = state.getOutlineShape((BlockView)BlockOutline.mc.world, blockPos);
        if (shape.isEmpty()) {
            shape = VoxelShapes.fullCube();
        }
        ArrayList<Box> worldBoxes = new ArrayList<Box>(shape.getBoundingBoxes().size());
        for (Box box : shape.getBoundingBoxes()) {
            worldBoxes.add(new Box((double)blockPos.getX() + box.minX, (double)blockPos.getY() + box.minY, (double)blockPos.getZ() + box.minZ, (double)blockPos.getX() + box.maxX, (double)blockPos.getY() + box.maxY, (double)blockPos.getZ() + box.maxZ));
        }
        return worldBoxes;
    }

    private void updateAnimationTarget(BlockPos blockPos, List<Box> targetBoxes) {
        boolean shapeChanged;
        long now = System.currentTimeMillis();
        boolean targetChanged = this.lastTargetPos == null || !this.lastTargetPos.equals((Object)blockPos);
        boolean bl = shapeChanged = !this.sameBoxes(this.animationToBoxes, targetBoxes);
        if (!this.smooth.isEnabled()) {
            this.lastTargetPos = blockPos.toImmutable();
            this.animationFromBoxes = this.copyBoxes(targetBoxes);
            this.animationToBoxes = this.copyBoxes(targetBoxes);
            this.animationStartedAt = now;
            return;
        }
        if (!targetChanged && !shapeChanged) {
            return;
        }
        this.animationFromBoxes = this.animationToBoxes.isEmpty() ? this.copyBoxes(targetBoxes) : this.getCurrentBoxes(now);
        this.animationToBoxes = this.copyBoxes(targetBoxes);
        this.animationStartedAt = now;
        this.lastTargetPos = blockPos.toImmutable();
    }

    private List<Box> getCurrentBoxes(long now) {
        if (this.animationToBoxes.isEmpty()) {
            return List.of();
        }
        if (!this.smooth.isEnabled()) {
            return this.animationToBoxes;
        }
        float durationSeconds = this.transitionTime.getCurrent();
        if (durationSeconds <= 0.0f || this.animationFromBoxes.isEmpty()) {
            return this.animationToBoxes;
        }
        float progress = MathHelper.clamp((float)((float)(now - this.animationStartedAt) / (durationSeconds * 1000.0f)), (float)0.0f, (float)1.0f);
        if (progress >= 1.0f) {
            this.animationFromBoxes = this.animationToBoxes;
            return this.animationToBoxes;
        }
        int size = Math.max(this.animationFromBoxes.size(), this.animationToBoxes.size());
        ArrayList<Box> interpolated = new ArrayList<Box>(size);
        Box fromFallback = this.getCombinedBox(this.animationFromBoxes);
        Box toFallback = this.getCombinedBox(this.animationToBoxes);
        for (int i = 0; i < size; ++i) {
            Box from = this.getBoxAt(this.animationFromBoxes, i, fromFallback != null ? fromFallback : toFallback);
            Box to = this.getBoxAt(this.animationToBoxes, i, toFallback != null ? toFallback : fromFallback);
            if (from == null || to == null) continue;
            interpolated.add(this.lerpBox(from, to, progress));
        }
        return interpolated;
    }

    private Box getBoxAt(List<Box> boxes, int index, Box fallback) {
        if (index < boxes.size()) {
            return boxes.get(index);
        }
        return fallback;
    }

    private Box getCombinedBox(List<Box> boxes) {
        if (boxes.isEmpty()) {
            return null;
        }
        Box first = boxes.get(0);
        double minX = first.minX;
        double minY = first.minY;
        double minZ = first.minZ;
        double maxX = first.maxX;
        double maxY = first.maxY;
        double maxZ = first.maxZ;
        for (int i = 1; i < boxes.size(); ++i) {
            Box box = boxes.get(i);
            minX = Math.min(minX, box.minX);
            minY = Math.min(minY, box.minY);
            minZ = Math.min(minZ, box.minZ);
            maxX = Math.max(maxX, box.maxX);
            maxY = Math.max(maxY, box.maxY);
            maxZ = Math.max(maxZ, box.maxZ);
        }
        return new Box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private Box lerpBox(Box from, Box to, float progress) {
        return new Box(MathHelper.lerp((double)progress, (double)from.minX, (double)to.minX), MathHelper.lerp((double)progress, (double)from.minY, (double)to.minY), MathHelper.lerp((double)progress, (double)from.minZ, (double)to.minZ), MathHelper.lerp((double)progress, (double)from.maxX, (double)to.maxX), MathHelper.lerp((double)progress, (double)from.maxY, (double)to.maxY), MathHelper.lerp((double)progress, (double)from.maxZ, (double)to.maxZ));
    }

    private List<Box> copyBoxes(List<Box> boxes) {
        return new ArrayList<Box>(boxes);
    }

    private boolean sameBoxes(List<Box> first, List<Box> second) {
        if (first.size() != second.size()) {
            return false;
        }
        for (int i = 0; i < first.size(); ++i) {
            Box a = first.get(i);
            Box b = second.get(i);
            if (Double.compare(a.minX, b.minX) == 0 && Double.compare(a.minY, b.minY) == 0 && Double.compare(a.minZ, b.minZ) == 0 && Double.compare(a.maxX, b.maxX) == 0 && Double.compare(a.maxY, b.maxY) == 0 && Double.compare(a.maxZ, b.maxZ) == 0) continue;
            return false;
        }
        return true;
    }

    private void resetAnimation() {
        this.lastTargetPos = null;
        this.animationFromBoxes = List.of();
        this.animationToBoxes = List.of();
        this.animationStartedAt = 0L;
    }
}

