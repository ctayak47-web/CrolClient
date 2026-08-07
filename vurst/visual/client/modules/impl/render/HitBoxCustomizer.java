
package vurst.visual.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.StatusEffects;
import net.minecraft.Entity;
import net.minecraft.PlayerEntity;
import net.minecraft.BlockPos;
import net.minecraft.Box;
import net.minecraft.HitResult;
import net.minecraft.Vec3d;
import net.minecraft.RaycastContext;
import net.minecraft.BlockHitResult;
import net.minecraft.Perspective;
import vurst.visual.VurstVisual;
import vurst.visual.base.events.impl.render.EventRender3D;
import vurst.visual.base.theme.Theme;
import vurst.visual.base.theme.ThemeManager;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.BooleanSetting;
import vurst.visual.client.modules.api.setting.impl.ColorSetting;
import vurst.visual.client.modules.api.setting.impl.ModeSetting;
import vurst.visual.client.modules.api.setting.impl.NumberSetting;
import vurst.visual.utility.math.MathUtil;
import vurst.visual.utility.render.display.base.color.ColorRGBA;
import vurst.visual.utility.render.display.base.color.ColorUtil;
import vurst.visual.utility.render.level.Render3DUtil;

@ModuleAnnotation(name="HitBox Customizer", category=Category.RENDER, description="Цвета хитбоксов игроков.")
public final class HitBoxCustomizer
extends Module {
    public static final HitBoxCustomizer INSTANCE = new HitBoxCustomizer();
    private static final float DEFAULT_EYE_LINE_LENGTH = 2.0f;
    private static final String COLOR_CLIENT = "Клиентский";
    private static final String COLOR_CUSTOM = "Кастомный";
    private static final String RENDER_OUTLINE = "Контур";
    private static final String RENDER_FILL = "Заливка";
    private static final String RENDER_BOTH = "Оба";
    private static final String LINE_COLOR_MATCH = "Под цвет бокса";
    private static final String LINE_COLOR_CUSTOM = "Кастомный";
    private final ModeSetting colorMode = new ModeSetting("Цвет", "Клиентский", "Кастомный");
    private final ColorSetting customColor = new ColorSetting("Свой цвет", Theme.DARK.getColor(), () -> this.colorMode.is("Кастомный"), Theme.DARK::getColor);
    private final ModeSetting renderMode = new ModeSetting("Режим отрисовки", "Оба", "Контур", "Заливка");
    private final NumberSetting outlineWidth = new NumberSetting("Толщина контура", 1.0f, 0.5f, 4.0f, 0.1f, this::shouldRenderOutline);
    private final NumberSetting fillAlpha = new NumberSetting("Прозрачность заливки", 0.2f, 0.05f, 1.0f, 0.05f, this::shouldRenderFill);
    private final BooleanSetting lookLine = new BooleanSetting("Линия взгляда", true);
    private final ModeSetting lookLineColorMode = new ModeSetting("Цвет линии глаз", this.lookLine::isEnabled, "Под цвет бокса", "Кастомный");
    private final ColorSetting lookLineCustomColor = new ColorSetting("Свой цвет линии взгляда", Theme.DARK.getColor(), () -> this.lookLine.isEnabled() && this.lookLineColorMode.is("Кастомный"), Theme.DARK::getColor);
    private final NumberSetting lookLineWidth = new NumberSetting("Толщина линии взгляда", 1.0f, 0.5f, 4.0f, 0.1f, this.lookLine::isEnabled);
    private final ThemeManager themeManager = VurstVisual.getInstance().getThemeManager();

    private HitBoxCustomizer() {
    }

    public boolean isActiveForPlayer() {
        return this.isEnabled() && HitBoxCustomizer.mc.player != null && !HitBoxCustomizer.mc.player.hasStatusEffect(StatusEffects.BLINDNESS);
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (!this.isActiveForPlayer() || HitBoxCustomizer.mc.world == null) {
            return;
        }
        for (PlayerEntity player : HitBoxCustomizer.mc.world.getPlayers()) {
            BlockPos playerPos;
            if (player == null || !player.isAlive() || player.isInvisible() || player.hasStatusEffect(StatusEffects.INVISIBILITY) || player == HitBoxCustomizer.mc.player && HitBoxCustomizer.mc.options.getPerspective() == Perspective.FIRST_PERSON || !HitBoxCustomizer.mc.world.isChunkLoaded(playerPos = player.getBlockPos()) || !this.canSeePlayer(player)) continue;
            Box box = this.getInterpolatedBox(player).expand(0.001);
            int boxColor = this.getBoxColor().getRGB();
            if (this.shouldRenderOutline()) {
                Render3DUtil.drawBox(box, boxColor, this.outlineWidth.getCurrent(), true, false, true);
            }
            if (this.shouldRenderFill()) {
                this.drawFill(box, ColorUtil.multAlpha(boxColor, this.fillAlpha.getCurrent()));
            }
            if (!this.lookLine.isEnabled()) continue;
            this.drawLookLine(player, this.getLookLineColor().getRGB());
        }
    }

    private boolean shouldRenderOutline() {
        return this.renderMode.is(RENDER_OUTLINE) || this.renderMode.is(RENDER_BOTH);
    }

    private boolean shouldRenderFill() {
        return this.renderMode.is(RENDER_FILL) || this.renderMode.is(RENDER_BOTH);
    }

    private Box getInterpolatedBox(PlayerEntity player) {
        Vec3d interpolated = MathUtil.interpolate((Entity)player);
        Vec3d delta = interpolated.subtract(player.getPos());
        return player.getBoundingBox().offset(delta);
    }

    private ColorRGBA getBoxColor() {
        if (this.colorMode.is("Кастомный")) {
            return this.customColor.getColor();
        }
        return this.themeManager.getCurrentTheme().getColor();
    }

    private ColorRGBA getLookLineColor() {
        if (this.lookLineColorMode.is("Кастомный")) {
            return this.lookLineCustomColor.getColor();
        }
        return this.getBoxColor();
    }

    private void drawLookLine(PlayerEntity player, int color) {
        Vec3d origin = MathUtil.interpolate((Entity)player).add(0.0, (double)player.getEyeHeight(player.getPose()), 0.0);
        Vec3d direction = player.getRotationVec(1.0f);
        if (direction.lengthSquared() <= 1.0E-6) {
            return;
        }
        Vec3d end = origin.add(direction.normalize().multiply(2.0));
        Render3DUtil.drawLine(origin, end, color, this.lookLineWidth.getCurrent(), true);
    }

    private void drawFill(Box box, int color) {
        Render3DUtil.drawQuad(new Vec3d(box.minX, box.minY, box.minZ), new Vec3d(box.maxX, box.minY, box.minZ), new Vec3d(box.maxX, box.minY, box.maxZ), new Vec3d(box.minX, box.minY, box.maxZ), color, true);
        Render3DUtil.drawQuad(new Vec3d(box.minX, box.minY, box.minZ), new Vec3d(box.minX, box.maxY, box.minZ), new Vec3d(box.maxX, box.maxY, box.minZ), new Vec3d(box.maxX, box.minY, box.minZ), color, true);
        Render3DUtil.drawQuad(new Vec3d(box.maxX, box.minY, box.minZ), new Vec3d(box.maxX, box.maxY, box.minZ), new Vec3d(box.maxX, box.maxY, box.maxZ), new Vec3d(box.maxX, box.minY, box.maxZ), color, true);
        Render3DUtil.drawQuad(new Vec3d(box.minX, box.minY, box.maxZ), new Vec3d(box.maxX, box.minY, box.maxZ), new Vec3d(box.maxX, box.maxY, box.maxZ), new Vec3d(box.minX, box.maxY, box.maxZ), color, true);
        Render3DUtil.drawQuad(new Vec3d(box.minX, box.minY, box.minZ), new Vec3d(box.minX, box.minY, box.maxZ), new Vec3d(box.minX, box.maxY, box.maxZ), new Vec3d(box.minX, box.maxY, box.minZ), color, true);
        Render3DUtil.drawQuad(new Vec3d(box.minX, box.maxY, box.minZ), new Vec3d(box.minX, box.maxY, box.maxZ), new Vec3d(box.maxX, box.maxY, box.maxZ), new Vec3d(box.maxX, box.maxY, box.minZ), color, true);
    }

    private boolean canSeePlayer(PlayerEntity player) {
        Vec3d end;
        if (HitBoxCustomizer.mc.player == null || HitBoxCustomizer.mc.world == null) {
            return false;
        }
        Vec3d start = HitBoxCustomizer.mc.player.getCameraPosVec(1.0f);
        BlockHitResult hit = HitBoxCustomizer.mc.world.raycast(new RaycastContext(start, end = player.getCameraPosVec(1.0f), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)HitBoxCustomizer.mc.player));
        return hit.getType() == HitResult.Type.MISS;
    }
}

