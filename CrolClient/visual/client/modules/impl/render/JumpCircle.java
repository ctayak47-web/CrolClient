
package crol.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.ShaderProgramKeys;
import net.minecraft.ShaderProgramKey;
import net.minecraft.Entity;
import net.minecraft.BlockView;
import net.minecraft.BlockPos;
import net.minecraft.HitResult;
import net.minecraft.Vec3d;
import net.minecraft.VoxelShape;
import net.minecraft.BlockState;
import net.minecraft.BufferRenderer;
import net.minecraft.BufferBuilder;
import net.minecraft.Tessellator;
import net.minecraft.VertexFormats;
import net.minecraft.VertexFormat;
import net.minecraft.Identifier;
import net.minecraft.MathHelper;
import net.minecraft.RaycastContext;
import net.minecraft.BlockHitResult;
import net.minecraft.MatrixStack;
import net.minecraft.BuiltBuffer;
import org.joml.Matrix4f;
import crol.client.CrolClient;
import crol.client.base.animations.base.Easing;
import crol.client.base.events.impl.player.EventJump;
import crol.client.base.events.impl.render.EventRender3D;
import crol.client.base.theme.Theme;
import crol.client.base.theme.ThemeManager;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.impl.BooleanSetting;
import crol.client.modules.api.setting.impl.ColorSetting;
import crol.client.modules.api.setting.impl.ModeSetting;
import crol.client.modules.api.setting.impl.NumberSetting;
import crol.client.utility.interfaces.IMinecraft;
import crol.client.utility.render.display.base.color.ColorRGBA;
import crol.client.utility.render.level.Render3DUtil;

@ModuleAnnotation(name="Jump Circle", category=Category.RENDER, description="Рисует круг при прыжке.")
public final class JumpCircle
extends Module {
    public static final JumpCircle INSTANCE = new JumpCircle();
    private static final String MODE_CLASSIC_NAME = "Обычный";
    private static final String MODE_ROCKSTAR_NAME = "Рокстар";
    private static final String MODE_JUMPS_V1_NAME = "Пульс";
    private static final String MODE_JUMPS_V2_NAME = "Тонкие";
    private static final String MODE_JUMPS_V3_NAME = "Широкие";
    private static final String MODE_JUMPS_V4_NAME = "Ромбированный";
    private static final String MODE_VURST_VISUAL_NAME = "Вурст Визуал";
    private static final Identifier TEXTURE_DEFAULT = CrolClient.id("hud/jump_circle.png");
    private static final Identifier TEXTURE_JUMPS_V1 = CrolClient.id("hud/circle.png");
    private static final Identifier TEXTURE_JUMPS_V2 = CrolClient.id("hud/circle_2.png");
    private static final Identifier TEXTURE_JUMPS_V3 = CrolClient.id("hud/circle_bold.png");
    private static final Identifier TEXTURE_JUMPS_V4 = CrolClient.id("hud/hens.png");
    private static final Identifier TEXTURE_VURST_VISUAL = CrolClient.id("hud/CrolClient.png");
    private static final String MODE_LABEL = "Режим";
    private static final String CLASSIC_RADIUS_LABEL = "Обычный радиус";
    private static final String CLASSIC_EXPAND_LABEL = "Обычное расширение";
    private static final String CLASSIC_LIFETIME_LABEL = "Обычное время жизни";
    private static final String CLASSIC_CUSTOM_LABEL = "Обычный клиентский";
    private static final String CLASSIC_COLOR1_LABEL = "Обычный цвет 1";
    private static final String CLASSIC_COLOR2_LABEL = "Обычный цвет 2";
    private static final String ROCKSTAR_RADIUS_LABEL = "Рокстар радиус";
    private static final String ROCKSTAR_SPEED_LABEL = "Рокстар скорость";
    private static final String ROCKSTAR_COLOR1_LABEL = "Рокстар цвет 1";
    private static final String ROCKSTAR_COLOR2_LABEL = "Рокстар цвет 2";
    private static final String LEGACY_MODE_JUMPS_V1_NAME = "ПрыжкиV1";
    private static final String LEGACY_MODE_JUMPS_V2_NAME = "ПрыжкиV2";
    private static final String LEGACY_MODE_JUMPS_V3_NAME = "ПрыжкиV3";
    private static final String LEGACY_MODE_JUMPS_V4_NAME = "ПрыжкиV4";
    private final ModeSetting mode = new ModeSetting("Режим", "Обычный", "Рокстар", "Пульс", "Тонкие", "Широкие", "Ромбированный", "Вурст Визуал");
    private final ModeSetting.Value modeClassic = this.mode.getValues().get(0);
    private final ModeSetting.Value modeRockstar = this.mode.getValues().get(1);
    private final ModeSetting.Value modeJumpsV1 = this.mode.getValues().get(2);
    private final ModeSetting.Value modeJumpsV2 = this.mode.getValues().get(3);
    private final ModeSetting.Value modeJumpsV3 = this.mode.getValues().get(4);
    private final ModeSetting.Value modeJumpsV4 = this.mode.getValues().get(5);
    private final ModeSetting.Value modeCrolClient = this.mode.getValues().get(6);
    private final NumberSetting radius = new NumberSetting("Обычный радиус", 1.0f, 0.01f, 2.0f, 0.01f, this::isCircleTextureMode);
    private final NumberSetting expand = new NumberSetting("Обычное расширение", 1.0f, 0.2f, 6.0f, 0.1f, this::isCircleTextureMode);
    private final NumberSetting lifetime = new NumberSetting("Обычное время жизни", 2.0f, 0.5f, 10.0f, 0.1f, this::isCircleTextureMode);
    private final BooleanSetting customTheme = new BooleanSetting("Обычный клиентский", false, this::isCircleTextureMode);
    private final ColorSetting primaryColor = new ColorSetting("Обычный цвет 1", Theme.DARK.getColor(), () -> this.isCircleTextureMode() && this.customTheme.isEnabled(), Theme.DARK::getColor);
    private final ColorSetting secondaryColor = new ColorSetting("Обычный цвет 2", Theme.DARK.getSecondColor(), () -> this.isCircleTextureMode() && this.customTheme.isEnabled(), Theme.DARK::getSecondColor);
    private final NumberSetting rockstarRadius = new NumberSetting("Рокстар радиус", 4.0f, 2.0f, 8.0f, 0.1f, () -> this.mode.is(this.modeRockstar));
    private final NumberSetting rockstarSpeed = new NumberSetting("Рокстар скорость", 800.0f, 300.0f, 2000.0f, 10.0f, () -> this.mode.is(this.modeRockstar));
    private final ColorSetting rockstarColor1 = new ColorSetting("Рокстар цвет 1", new ColorRGBA(100, 200, 255, 255), () -> this.mode.is(this.modeRockstar));
    private final ColorSetting rockstarColor2 = new ColorSetting("Рокстар цвет 2", new ColorRGBA(255, 100, 200, 255), () -> this.mode.is(this.modeRockstar));
    private final List<Circle> circles = new ArrayList<Circle>();
    private final List<WaveEffect> waveEffects = new ArrayList<WaveEffect>();
    private final ThemeManager themeManager = CrolClient.getInstance().getThemeManager();
    private Object lastWorld;

    private JumpCircle() {
    }

    @Override
    public void load(JsonObject object) {
        this.migrateLegacyModeNames(object);
        super.load(object);
    }

    @Override
    public void onEnable() {
        this.circles.clear();
        this.waveEffects.clear();
        this.lastWorld = JumpCircle.mc.world;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.circles.clear();
        this.waveEffects.clear();
        this.lastWorld = null;
        super.onDisable();
    }

    @EventTarget
    public void onJump(EventJump event) {
        if (JumpCircle.mc.player == null || JumpCircle.mc.world == null) {
            return;
        }
        if (this.mode.is(this.modeRockstar)) {
            this.waveEffects.add(new WaveEffect(JumpCircle.mc.player.getBlockPos().down(), System.currentTimeMillis()));
            return;
        }
        this.circles.add(new Circle(JumpCircle.mc.player.getPos().add(0.0, 0.05, 0.0)));
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (JumpCircle.mc.player == null || JumpCircle.mc.world == null) {
            return;
        }
        if (this.lastWorld != JumpCircle.mc.world) {
            this.circles.clear();
            this.waveEffects.clear();
            this.lastWorld = JumpCircle.mc.world;
        }
        if (this.mode.is(this.modeRockstar)) {
            this.renderRockstar();
            return;
        }
        this.renderClassic(event);
    }

    private void renderClassic(EventRender3D event) {
        if (this.circles.isEmpty()) {
            return;
        }
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (Identifier)this.getCurrentTexture());
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        MatrixStack matrices = event.getMatrix();
        Vec3d camPos = JumpCircle.mc.getEntityRenderDispatcher().camera.getPos();
        long now = System.currentTimeMillis();
        long lifeMs = (long)Math.max(1.0f, this.lifetime.getCurrent() * 1000.0f);
        Iterator<Circle> iterator2 = this.circles.iterator();
        while (iterator2.hasNext()) {
            Circle circle = iterator2.next();
            long age = now - circle.spawnTime;
            if (age >= lifeMs) {
                iterator2.remove();
                continue;
            }
            if (this.isOccluded(camPos, circle.position.add(0.0, 0.1, 0.0))) continue;
            float progress = MathHelper.clamp((float)((float)age / (float)lifeMs), (float)0.0f, (float)1.0f);
            float fade = 1.0f - progress;
            float eased = Easing.CIRC_OUT.ease(progress, 0.0f, 1.0f, 1.0f);
            float diameter = this.radius.getCurrent() + this.expand.getCurrent() * eased;
            this.renderClassicCircle(matrices, camPos, circle.position, diameter, fade, age);
        }
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void renderClassicCircle(MatrixStack matrices, Vec3d camPos, Vec3d pos, float diameter, float alpha, long age) {
        float half = diameter / 2.0f;
        int alphaInt = JumpCircle.clamp255(255.0f * alpha);
        int primary = this.getPrimaryThemeColor().withAlpha(alphaInt).getRGB();
        int secondary = this.getSecondaryThemeColor().withAlpha(alphaInt).getRGB();
        float phase = (float)(age % 1600L) / 1600.0f;
        int topLeft = JumpCircle.lerpColor(primary, secondary, JumpCircle.animatedBlend(phase));
        int topRight = JumpCircle.lerpColor(primary, secondary, JumpCircle.animatedBlend(phase + 0.25f));
        int bottomRight = JumpCircle.lerpColor(primary, secondary, JumpCircle.animatedBlend(phase + 0.5f));
        int bottomLeft = JumpCircle.lerpColor(primary, secondary, JumpCircle.animatedBlend(phase + 0.75f));
        matrices.push();
        matrices.translate(pos.x - camPos.x, pos.y - camPos.y, pos.z - camPos.z);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        buffer.vertex(matrix, -half, 0.0f, -half).texture(0.0f, 0.0f).color(topLeft);
        buffer.vertex(matrix, half, 0.0f, -half).texture(1.0f, 0.0f).color(topRight);
        buffer.vertex(matrix, half, 0.0f, half).texture(1.0f, 1.0f).color(bottomRight);
        buffer.vertex(matrix, -half, 0.0f, half).texture(0.0f, 1.0f).color(bottomLeft);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        matrices.pop();
    }

    private void renderRockstar() {
        if (this.waveEffects.isEmpty() || JumpCircle.mc.world == null) {
            return;
        }
        long now = System.currentTimeMillis();
        Iterator<WaveEffect> iterator2 = this.waveEffects.iterator();
        while (iterator2.hasNext()) {
            WaveEffect wave = iterator2.next();
            if (wave.isExpired(now)) {
                iterator2.remove();
                continue;
            }
            wave.render(now);
        }
    }

    private ColorRGBA getPrimaryThemeColor() {
        if (this.customTheme.isEnabled()) {
            return this.primaryColor.getColor();
        }
        return this.themeManager.getCurrentTheme().getColor();
    }

    private ColorRGBA getSecondaryThemeColor() {
        if (this.customTheme.isEnabled()) {
            return this.secondaryColor.getColor();
        }
        return this.themeManager.getCurrentTheme().getSecondColor();
    }

    private static float animatedBlend(float phase) {
        float normalized = phase - (float)Math.floor(phase);
        return (float)((Math.sin((double)normalized * Math.PI * 2.0) + 1.0) * 0.5);
    }

    private boolean isCircleTextureMode() {
        return !this.mode.is(this.modeRockstar);
    }

    private Identifier getCurrentTexture() {
        if (this.mode.is(this.modeJumpsV1)) {
            return TEXTURE_JUMPS_V1;
        }
        if (this.mode.is(this.modeJumpsV2)) {
            return TEXTURE_JUMPS_V2;
        }
        if (this.mode.is(this.modeJumpsV3)) {
            return TEXTURE_JUMPS_V3;
        }
        if (this.mode.is(this.modeJumpsV4)) {
            return TEXTURE_JUMPS_V4;
        }
        if (this.mode.is(this.modeCrolClient)) {
            return TEXTURE_VURST_VISUAL;
        }
        return TEXTURE_DEFAULT;
    }

    private boolean isOccluded(Vec3d from, Vec3d to) {
        BlockHitResult hit = JumpCircle.mc.world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)JumpCircle.mc.player));
        return hit.getType() != HitResult.Type.MISS;
    }

    private static int clamp255(float value) {
        if (value < 0.0f) {
            return 0;
        }
        if (value > 255.0f) {
            return 255;
        }
        return Math.round(value);
    }

    private static int withAlpha(int color, int alpha) {
        int a = MathHelper.clamp((int)alpha, (int)0, (int)255);
        return a << 24 | color & 0xFFFFFF;
    }

    private static int lerpColor(int color1, int color2, float t) {
        float clamped = MathHelper.clamp((float)t, (float)0.0f, (float)1.0f);
        int a1 = color1 >>> 24 & 0xFF;
        int r1 = color1 >>> 16 & 0xFF;
        int g1 = color1 >>> 8 & 0xFF;
        int b1 = color1 & 0xFF;
        int a2 = color2 >>> 24 & 0xFF;
        int r2 = color2 >>> 16 & 0xFF;
        int g2 = color2 >>> 8 & 0xFF;
        int b2 = color2 & 0xFF;
        int a = Math.round((float)a1 + (float)(a2 - a1) * clamped);
        int r = Math.round((float)r1 + (float)(r2 - r1) * clamped);
        int g = Math.round((float)g1 + (float)(g2 - g1) * clamped);
        int b = Math.round((float)b1 + (float)(b2 - b1) * clamped);
        return a << 24 | r << 16 | g << 8 | b;
    }

    private static float easeOutCubic(float x) {
        return 1.0f - (float)Math.pow(1.0f - x, 3.0);
    }

    private static float easeInCubic(float x) {
        return x * x * x;
    }

    private void migrateLegacyModeNames(JsonObject object) {
        String normalized;
        String currentMode;
        if (object == null || !object.has("Settings") || !object.get("Settings").isJsonObject()) {
            return;
        }
        JsonObject settings = object.getAsJsonObject("Settings");
        if (!settings.has(MODE_LABEL)) {
            return;
        }
        switch (currentMode = settings.get(MODE_LABEL).getAsString()) {
            case "ПрыжкиV1": {
                String string = MODE_JUMPS_V1_NAME;
                break;
            }
            case "ПрыжкиV2": {
                String string = MODE_JUMPS_V2_NAME;
                break;
            }
            case "ПрыжкиV3": {
                String string = MODE_JUMPS_V3_NAME;
                break;
            }
            case "ПрыжкиV4": {
                String string = MODE_JUMPS_V4_NAME;
                break;
            }
            default: {
                String string = normalized = currentMode;
            }
        }
        if (!currentMode.equals(normalized)) {
            settings.addProperty(MODE_LABEL, normalized);
        }
    }

    private final class WaveEffect {
        private static final int MAX_PER_FRAME = 400;
        private static final float WAVE_WIDTH = 2.5f;
        private static final float LINE_WIDTH = 1.5f;
        private final BlockPos centerPos;
        private final long startTime;
        private final long duration;
        private final int maxRadius;

        private WaveEffect(BlockPos centerPos, long startTime) {
            this.centerPos = centerPos;
            this.startTime = startTime;
            this.duration = (long)JumpCircle.this.rockstarSpeed.getCurrent();
            this.maxRadius = Math.max(1, (int)Math.ceil(JumpCircle.this.rockstarRadius.getCurrent()));
        }

        private boolean isExpired(long now) {
            return now - this.startTime > this.duration;
        }

        private void render(long now) {
            float globalAlpha;
            if (IMinecraft.mc.world == null) {
                return;
            }
            long elapsed = now - this.startTime;
            float progress = MathHelper.clamp((float)((float)elapsed / (float)this.duration), (float)0.0f, (float)1.0f);
            float currentRadius = JumpCircle.easeOutCubic(progress) * (float)this.maxRadius;
            float fadeInDuration = 0.15f;
            float fadeOutStart = 0.75f;
            if (progress < fadeInDuration) {
                globalAlpha = progress / fadeInDuration;
            } else if (progress >= fadeOutStart) {
                float fadeOutProgress = (progress - fadeOutStart) / (1.0f - fadeOutStart);
                globalAlpha = 1.0f - JumpCircle.easeInCubic(fadeOutProgress);
            } else {
                globalAlpha = 1.0f;
            }
            int rendered = 0;
            int colorA = JumpCircle.this.rockstarColor1.getColor().getRGB();
            int colorB = JumpCircle.this.rockstarColor2.getColor().getRGB();
            for (int x = -this.maxRadius; x <= this.maxRadius; ++x) {
                for (int z = -this.maxRadius; z <= this.maxRadius; ++z) {
                    BlockState state;
                    VoxelShape shape;
                    BlockPos checkPos;
                    BlockPos renderPos;
                    if (rendered >= 400) {
                        return;
                    }
                    double distanceFromCenter = Math.sqrt(x * x + z * z);
                    if (distanceFromCenter > (double)(currentRadius + 0.5f) || distanceFromCenter < (double)(currentRadius - 2.5f) || (renderPos = this.findSurface(checkPos = this.centerPos.add(x, 0, z))) == null || (shape = (state = IMinecraft.mc.world.getBlockState(renderPos)).getOutlineShape((BlockView)IMinecraft.mc.world, renderPos)).isEmpty()) continue;
                    ++rendered;
                    float waveProgress = (float)(distanceFromCenter / (double)Math.max(1, this.maxRadius));
                    float localAlpha = 1.0f - Math.abs((float)distanceFromCenter - currentRadius) / 2.5f;
                    localAlpha = MathHelper.clamp((float)localAlpha, (float)0.0f, (float)1.0f);
                    float pulseOffset = waveProgress * 2.0f;
                    float pulse = (float)Math.sin(progress * (float)Math.PI * 4.0f - pulseOffset);
                    pulse = (pulse + 1.0f) / 2.0f;
                    localAlpha *= 0.5f + pulse * 0.5f;
                    if ((localAlpha *= globalAlpha) <= 0.02f) continue;
                    float smoothT = (float)(Math.sin((double)(waveProgress - 0.5f) * Math.PI) * 0.5 + 0.5);
                    int gradientColor = JumpCircle.lerpColor(colorA, colorB, smoothT);
                    int finalColor = JumpCircle.withAlpha(gradientColor, (int)(localAlpha * 180.0f));
                    try {
                        Render3DUtil.drawShapeAlternative(renderPos, shape, finalColor, 1.5f, true, true);
                        continue;
                    }
                    catch (Exception exception) {
                        
                    }
                }
            }
        }

        private BlockPos findSurface(BlockPos pos) {
            if (IMinecraft.mc.world == null) {
                return null;
            }
            for (int y = 2; y >= -4; --y) {
                BlockPos p = pos.add(0, y, 0);
                if (IMinecraft.mc.world.getBlockState(p).isAir() || !IMinecraft.mc.world.getBlockState(p.up()).isAir()) continue;
                return p;
            }
            return null;
        }
    }

    private static final class Circle {
        private final Vec3d position;
        private final long spawnTime;

        private Circle(Vec3d position) {
            this.position = position;
            this.spawnTime = System.currentTimeMillis();
        }
    }
}

