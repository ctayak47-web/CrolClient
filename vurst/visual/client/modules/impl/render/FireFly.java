
package vurst.visual.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.ShaderProgramKeys;
import net.minecraft.ShaderProgramKey;
import net.minecraft.Entity;
import net.minecraft.BlockPos;
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
import net.minecraft.MathHelper;
import net.minecraft.RaycastContext;
import net.minecraft.BlockHitResult;
import net.minecraft.MatrixStack;
import net.minecraft.RotationAxis;
import net.minecraft.BuiltBuffer;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import vurst.visual.VurstVisual;
import vurst.visual.base.events.impl.player.EventUpdate;
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
import vurst.visual.utility.interfaces.IMinecraft;
import vurst.visual.utility.math.MathUtil;
import vurst.visual.utility.render.display.base.color.ColorRGBA;

@ModuleAnnotation(name="World Particles", category=Category.RENDER, description="Частицы вокруг игрока.")
public final class FireFly
extends Module {
    public static final FireFly INSTANCE = new FireFly();
    private static final int MAX_NORMAL_SPAWN_PER_TICK = 64;
    private static final int MAX_FIREFLY_SPAWN_PER_TICK = 24;
    private static final long LIFE_TIME_MS = 5000L;
    private static final long FIREFLY_LIFE_TIME_MS = 8000L;
    private static final long FIREFLY_FADE_TIME_MS = 500L;
    private static final Identifier HEART_TEXTURE = VurstVisual.id("hud/particles/heart.png");
    private static final Identifier THOR_TEXTURE = VurstVisual.id("hud/particles/thor.png");
    private static final Identifier LIGHTNING_TEXTURE = VurstVisual.id("hud/particles/lightning.png");
    private static final Identifier SNOW_TEXTURE = VurstVisual.id("hud/particles/snowflake.png");
    private static final Identifier SNOW_NEW_TEXTURE = VurstVisual.id("hud/particles/snownew1.png");
    private static final Identifier SNOW_BRICH_TEXTURE = VurstVisual.id("hud/particles/snowbrich1.png");
    private static final Identifier SNOW_BLAST_TEXTURE = VurstVisual.id("hud/particles/snowblast1.png");
    private static final Identifier SNOW_BAG_TEXTURE = VurstVisual.id("hud/particles/snowbag1.png");
    private static final Identifier ORB_TEXTURE = VurstVisual.id("hud/particles/orb.png");
    private static final Identifier CROWN_TEXTURE = VurstVisual.id("hud/particles/crown.png");
    private static final Identifier DOLLAR_TEXTURE = VurstVisual.id("hud/particles/dollar.png");
    private static final Identifier SKULL_TEXTURE = VurstVisual.id("hud/particles/skull.png");
    private static final Identifier STAR_TEXTURE = VurstVisual.id("hud/particles/star.png");
    private static final Identifier FIREFLY_TEXTURE = VurstVisual.id("hud/particles/firefly.png");
    private static final Identifier BLOOM_TEXTURE = VurstVisual.id("hud/particles/bloom.png");
    private final ModeSetting view = new ModeSetting("Вид", "Сердечки", "Залупа", "Молния", "Снежинки", "Снег", "Снежок", "Снежки", "Снегокак", "Еще снег", "Орбизы", "Короны", "Доллар", "Скелеты", "Звезда", "3D", "Огоньки");
    private final ModeSetting fallMode = new ModeSetting("Режим", () -> !this.isFireflyMode(), "Простой", "Отскоки");
    private final NumberSetting count = new NumberSetting("Количество", 350.0f, 100.0f, 5000.0f, 50.0f, () -> !this.isFireflyMode());
    private final NumberSetting fireflyCount = new NumberSetting("Количество", 100.0f, 10.0f, 5000.0f, 50.0f, this::isFireflyMode);
    private final NumberSetting fireflySpeed = new NumberSetting("Скорость", 0.15f, 0.05f, 0.5f, 0.05f, this::isFireflyMode);
    private final NumberSetting fireflyRadius = new NumberSetting("Радиус спавна", 25.0f, 10.0f, 50.0f, 5.0f, this::isFireflyMode);
    private final NumberSetting fireflyTrail = new NumberSetting("Длина шлейфа", 20.0f, 5.0f, 40.0f, 5.0f, this::isFireflyMode);
    private final BooleanSetting randomColor = new BooleanSetting("Рандомные цвета", false, () -> !this.isFireflyMode());
    private final BooleanSetting fireflyRandomColor = new BooleanSetting("Рандомный цвет", true, this::isFireflyMode);
    private final BooleanSetting customTheme = new BooleanSetting("Клиентский", false);
    private final ColorSetting primaryColor = new ColorSetting("Первый цвет", Theme.DARK.getColor(), this.customTheme::isEnabled, Theme.DARK::getColor);
    private final ColorSetting secondaryColor = new ColorSetting("Второй цвет", Theme.DARK.getSecondColor(), this.customTheme::isEnabled, Theme.DARK::getSecondColor);
    private final List<Particle> particles = new ArrayList<Particle>();
    private final List<FireflyParticle> fireflies = new ArrayList<FireflyParticle>();
    private final ThemeManager themeManager = VurstVisual.getInstance().getThemeManager();
    private Object lastWorld;
    private String lastView;

    private FireFly() {
    }

    @Override
    public void onEnable() {
        this.particles.clear();
        this.fireflies.clear();
        this.lastWorld = null;
        this.lastView = this.view.get();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.particles.clear();
        this.fireflies.clear();
        this.lastWorld = null;
        this.lastView = null;
        super.onDisable();
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (FireFly.mc.player == null || FireFly.mc.world == null) {
            this.particles.clear();
            this.fireflies.clear();
            this.lastWorld = null;
            return;
        }
        if (this.lastWorld != FireFly.mc.world) {
            this.particles.clear();
            this.fireflies.clear();
            this.lastWorld = FireFly.mc.world;
        }
        String currentView = this.view.get();
        if (this.lastView == null || !this.lastView.equals(currentView)) {
            this.particles.clear();
            this.fireflies.clear();
            this.lastView = currentView;
        }
        if (this.isFireflyMode()) {
            this.updateFireflies();
            return;
        }
        for (int i = this.particles.size() - 1; i >= 0; --i) {
            if (!this.particles.get(i).tick()) continue;
            this.particles.remove(i);
        }
        int targetCount = Math.max(0, Math.round(this.count.getCurrent()));
        int spawnCount = Math.min(64, Math.max(0, targetCount - this.particles.size()));
        for (int i = 0; i < spawnCount; ++i) {
            this.spawnParticle();
        }
    }

    private void updateFireflies() {
        long now = System.currentTimeMillis();
        Vec3d playerPos = FireFly.mc.player.getPos();
        double maxDistance = Math.max(60.0, (double)this.fireflyRadius.getCurrent() * 2.5);
        for (int i = this.fireflies.size() - 1; i >= 0; --i) {
            FireflyParticle particle = this.fireflies.get(i);
            if (!particle.isExpired(now) && !(particle.distanceTo(playerPos) > maxDistance)) continue;
            this.fireflies.remove(i);
        }
        int targetCount = Math.max(0, Math.round(this.fireflyCount.getCurrent()));
        int spawnCount = Math.min(24, Math.max(0, targetCount - this.fireflies.size()));
        for (int i = 0; i < spawnCount; ++i) {
            this.spawnFirefly(playerPos);
        }
        int maxTrail = Math.max(2, Math.round(this.fireflyTrail.getCurrent()));
        float speedValue = this.fireflySpeed.getCurrent();
        for (FireflyParticle particle : this.fireflies) {
            particle.update(speedValue, maxTrail);
        }
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (FireFly.mc.player == null || FireFly.mc.world == null) {
            return;
        }
        if (this.isFireflyMode()) {
            if (!this.fireflies.isEmpty()) {
                this.renderFireflies(event.getMatrix());
            }
            return;
        }
        if (this.is3DMode()) {
            if (!this.particles.isEmpty()) {
                this.render3DParticles(event);
            }
            return;
        }
        if (this.particles.isEmpty()) {
            return;
        }
        Identifier texture = this.getTexture();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (Identifier)texture);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask((boolean)false);
        MatrixStack matrices = event.getMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        Vec3d camPos = FireFly.mc.getEntityRenderDispatcher().camera.getPos();
        long now = System.currentTimeMillis();
        float tickDelta = event.getPartialTicks();
        for (Particle particle : this.particles) {
            particle.render(buffer, matrices, camPos, tickDelta, now);
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableCull();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private void render3DParticles(EventRender3D event) {
        long now = System.currentTimeMillis();
        float tickDelta = event.getPartialTicks();
        for (Particle particle : this.particles) {
            particle.prepareRender(tickDelta, now);
        }
        MatrixStack matrices = event.getMatrix();
        Vec3d camPos = FireFly.mc.getEntityRenderDispatcher().camera.getPos();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (Identifier)BLOOM_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask((boolean)false);
        BufferBuilder bloomBuffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        for (Particle particle : this.particles) {
            if (!particle.isRenderVisible()) continue;
            particle.renderBloom(bloomBuffer, matrices, camPos);
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bloomBuffer.end());
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder linesBuffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        for (Particle particle : this.particles) {
            if (!particle.isRenderVisible()) continue;
            particle.renderBoxLines(linesBuffer, matrices, camPos);
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)linesBuffer.end());
        RenderSystem.depthMask((boolean)true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private void renderFireflies(MatrixStack matrices) {
        long now = System.currentTimeMillis();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (Identifier)FIREFLY_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        Vec3d camPos = FireFly.mc.getEntityRenderDispatcher().camera.getPos();
        for (FireflyParticle particle : this.fireflies) {
            float alpha = particle.getAlpha(now);
            if (alpha <= 0.0f || this.isOccluded(camPos, particle.getPosition())) continue;
            ColorRGBA base = this.fireflyRandomColor.isEnabled() ? particle.getColor() : this.getThemeColor(particle.getIndex() * 50);
            this.renderFireflyTrail(buffer, matrices, camPos, particle, base, alpha);
            this.renderFireflyParticle(buffer, matrices, camPos, particle.getPosition(), base, alpha);
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableCull();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableBlend();
    }

    private void renderFireflyTrail(BufferBuilder buffer, MatrixStack matrices, Vec3d camPos, FireflyParticle particle, ColorRGBA base, float alpha) {
        List<Vec3d> trail = particle.getTrail();
        int trailSize = trail.size();
        if (trailSize < 2) {
            return;
        }
        for (int i = 0; i < trailSize; ++i) {
            Vec3d pos = trail.get(i);
            float fade = ((float)i + 1.0f) / (float)trailSize;
            float size = 0.15f * fade;
            int color = base.withAlpha(FireFly.clamp255(200.0f * alpha * fade)).getRGB();
            this.drawBillboard(buffer, matrices, camPos, pos, size, color);
        }
    }

    private void renderFireflyParticle(BufferBuilder buffer, MatrixStack matrices, Vec3d camPos, Vec3d pos, ColorRGBA base, float alpha) {
        float glowSize = 0.35f;
        int glowColor = base.withAlpha(FireFly.clamp255(180.0f * alpha)).getRGB();
        this.drawBillboard(buffer, matrices, camPos, pos, glowSize, glowColor);
        float mainSize = 0.22f;
        int mainColor = base.withAlpha(FireFly.clamp255(255.0f * alpha)).getRGB();
        this.drawBillboard(buffer, matrices, camPos, pos, mainSize, mainColor);
        float coreSize = 0.1f;
        int coreColor = new ColorRGBA(255, 255, 255, FireFly.clamp255(220.0f * alpha)).getRGB();
        this.drawBillboard(buffer, matrices, camPos, pos, coreSize, coreColor);
    }

    private void drawBillboard(BufferBuilder buffer, MatrixStack matrices, Vec3d camPos, Vec3d pos, float size, int color) {
        this.drawBillboard(buffer, matrices, camPos, pos.x, pos.y, pos.z, size, color);
    }

    private void drawBillboard(BufferBuilder buffer, MatrixStack matrices, Vec3d camPos, double x, double y, double z, float size, int color) {
        matrices.push();
        matrices.translate(x - camPos.x, y - camPos.y, z - camPos.z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-FireFly.mc.getEntityRenderDispatcher().camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(FireFly.mc.getEntityRenderDispatcher().camera.getPitch()));
        matrices.scale(-size, -size, size);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        buffer.vertex(matrix, -0.5f, -0.5f, 0.0f).texture(0.0f, 1.0f).color(color);
        buffer.vertex(matrix, 0.5f, -0.5f, 0.0f).texture(1.0f, 1.0f).color(color);
        buffer.vertex(matrix, 0.5f, 0.5f, 0.0f).texture(1.0f, 0.0f).color(color);
        buffer.vertex(matrix, -0.5f, 0.5f, 0.0f).texture(0.0f, 0.0f).color(color);
        matrices.pop();
    }

    private void renderBoxInternalDiagonals(MatrixStack matrices, BufferBuilder buffer, Box box, int color) {
        float r = (float)(color >> 16 & 0xFF) / 255.0f;
        float g = (float)(color >> 8 & 0xFF) / 255.0f;
        float b = (float)(color & 0xFF) / 255.0f;
        float a = (float)(color >> 24 & 0xFF) / 255.0f;
        Matrix4f m = matrices.peek().getPositionMatrix();
        buffer.vertex(m, (float)box.minX, (float)box.minY, (float)box.minZ).color(r, g, b, a);
        buffer.vertex(m, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(r, g, b, a);
        buffer.vertex(m, (float)box.maxX, (float)box.minY, (float)box.minZ).color(r, g, b, a);
        buffer.vertex(m, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(r, g, b, a);
        buffer.vertex(m, (float)box.minX, (float)box.minY, (float)box.maxZ).color(r, g, b, a);
        buffer.vertex(m, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(r, g, b, a);
        buffer.vertex(m, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(r, g, b, a);
        buffer.vertex(m, (float)box.minX, (float)box.maxY, (float)box.minZ).color(r, g, b, a);
    }

    private void renderOutlinedBox(MatrixStack matrices, BufferBuilder buffer, Box box, int color) {
        float r = (float)(color >> 16 & 0xFF) / 255.0f;
        float g = (float)(color >> 8 & 0xFF) / 255.0f;
        float b = (float)(color & 0xFF) / 255.0f;
        float a = (float)(color >> 24 & 0xFF) / 255.0f;
        Matrix4f m = matrices.peek().getPositionMatrix();
        float x1 = (float)box.minX;
        float y1 = (float)box.minY;
        float z1 = (float)box.minZ;
        float x2 = (float)box.maxX;
        float y2 = (float)box.maxY;
        float z2 = (float)box.maxZ;
        buffer.vertex(m, x1, y1, z1).color(r, g, b, a);
        buffer.vertex(m, x2, y1, z1).color(r, g, b, a);
        buffer.vertex(m, x2, y1, z1).color(r, g, b, a);
        buffer.vertex(m, x2, y1, z2).color(r, g, b, a);
        buffer.vertex(m, x2, y1, z2).color(r, g, b, a);
        buffer.vertex(m, x1, y1, z2).color(r, g, b, a);
        buffer.vertex(m, x1, y1, z2).color(r, g, b, a);
        buffer.vertex(m, x1, y1, z1).color(r, g, b, a);
        buffer.vertex(m, x1, y2, z1).color(r, g, b, a);
        buffer.vertex(m, x2, y2, z1).color(r, g, b, a);
        buffer.vertex(m, x2, y2, z1).color(r, g, b, a);
        buffer.vertex(m, x2, y2, z2).color(r, g, b, a);
        buffer.vertex(m, x2, y2, z2).color(r, g, b, a);
        buffer.vertex(m, x1, y2, z2).color(r, g, b, a);
        buffer.vertex(m, x1, y2, z2).color(r, g, b, a);
        buffer.vertex(m, x1, y2, z1).color(r, g, b, a);
        buffer.vertex(m, x1, y1, z1).color(r, g, b, a);
        buffer.vertex(m, x1, y2, z1).color(r, g, b, a);
        buffer.vertex(m, x2, y1, z1).color(r, g, b, a);
        buffer.vertex(m, x2, y2, z1).color(r, g, b, a);
        buffer.vertex(m, x2, y1, z2).color(r, g, b, a);
        buffer.vertex(m, x2, y2, z2).color(r, g, b, a);
        buffer.vertex(m, x1, y1, z2).color(r, g, b, a);
        buffer.vertex(m, x1, y2, z2).color(r, g, b, a);
    }

    private Identifier getTexture() {
        return switch (this.view.get()) {
            case "Залупа" -> THOR_TEXTURE;
            case "Молния" -> LIGHTNING_TEXTURE;
            case "Снежинки" -> SNOW_TEXTURE;
            case "Снег" -> SNOW_NEW_TEXTURE;
            case "Снежок" -> SNOW_TEXTURE;
            case "Снежки" -> SNOW_BRICH_TEXTURE;
            case "Снегокак" -> SNOW_BLAST_TEXTURE;
            case "Еще снег" -> SNOW_BAG_TEXTURE;
            case "Орбизы" -> ORB_TEXTURE;
            case "Короны" -> CROWN_TEXTURE;
            case "Доллар" -> DOLLAR_TEXTURE;
            case "Скелеты" -> SKULL_TEXTURE;
            case "Звезда" -> STAR_TEXTURE;
            default -> HEART_TEXTURE;
        };
    }

    private boolean isFireflyMode() {
        return "Огоньки".equals(this.view.get());
    }

    private boolean is3DMode() {
        return "3D".equals(this.view.get());
    }

    private void spawnParticle() {
        float dynamicSpeed = this.fallMode.is("Отскоки") ? 0.1f : 0.4f;
        float x = (float)(FireFly.mc.player.getX() + (double)MathUtil.random(-48.0, 48.0));
        float y = (float)(FireFly.mc.player.getY() + (double)MathUtil.random(-20.0, 48.0));
        float z = (float)(FireFly.mc.player.getZ() + (double)MathUtil.random(-48.0, 48.0));
        float motionX = MathUtil.random(-dynamicSpeed, dynamicSpeed);
        float motionY = MathUtil.random(-0.1f, 0.1f);
        float motionZ = MathUtil.random(-dynamicSpeed, dynamicSpeed);
        this.particles.add(new Particle(x, y, z, motionX, motionY, motionZ));
    }

    private void spawnFirefly(Vec3d playerPos) {
        double distance = MathUtil.random(5.0, this.fireflyRadius.getCurrent());
        double yaw = Math.toRadians(MathUtil.random(0.0, 360.0));
        double xOffset = -Math.sin(yaw) * distance;
        double zOffset = Math.cos(yaw) * distance;
        double yOffset = MathUtil.random(-5.0, 10.0);
        double speedValue = this.fireflySpeed.getCurrent();
        double velocityYaw = Math.toRadians(MathUtil.random(0.0, 360.0));
        double velocityPitch = Math.toRadians(MathUtil.random(-30.0, 30.0));
        Vec3d velocity = new Vec3d(-Math.sin(velocityYaw) * Math.cos(velocityPitch) * speedValue, Math.sin(velocityPitch) * speedValue * 0.5, Math.cos(velocityYaw) * Math.cos(velocityPitch) * speedValue);
        ColorRGBA color = new ColorRGBA(ThreadLocalRandom.current().nextInt(256), ThreadLocalRandom.current().nextInt(256), ThreadLocalRandom.current().nextInt(256), 255);
        this.fireflies.add(new FireflyParticle(new Vec3d(playerPos.x + xOffset, playerPos.y + yOffset, playerPos.z + zOffset), velocity, this.fireflies.size(), color));
    }

    private boolean isOccluded(Vec3d from, Vec3d to) {
        BlockHitResult hit = FireFly.mc.world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)FireFly.mc.player));
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

    private ColorRGBA getThemeColor(int index) {
        if (this.customTheme.isEnabled()) {
            return this.primaryColor.getColor();
        }
        return this.themeManager.getCurrentTheme().getColor();
    }

    private final class Particle {
        private final long spawnTime;
        private float prevX;
        private float prevY;
        private float prevZ;
        private float posX;
        private float posY;
        private float posZ;
        private float motionX;
        private float motionY;
        private float motionZ;
        private float prevRotX;
        private float prevRotY;
        private float prevRotZ;
        private float rotX;
        private float rotY;
        private float rotZ;
        private float rotMotionX;
        private float rotMotionY;
        private float rotMotionZ;
        private int age;
        private float alpha;
        private final ColorRGBA randomColorValue;
        private long collisionTime = -1L;
        private boolean renderVisible;
        private float renderX;
        private float renderY;
        private float renderZ;
        private float renderRotX;
        private float renderRotY;
        private float renderRotZ;
        private float renderSize;
        private float renderAlpha;
        private float renderLifeScale;

        private Particle(float x, float y, float z, float motionX, float motionY, float motionZ) {
            this.posX = x;
            this.posY = y;
            this.posZ = z;
            this.prevX = x;
            this.prevY = y;
            this.prevZ = z;
            this.motionX = motionX;
            this.motionY = motionY;
            this.motionZ = motionZ;
            this.randomColorValue = new ColorRGBA(ThreadLocalRandom.current().nextInt(256), ThreadLocalRandom.current().nextInt(256), ThreadLocalRandom.current().nextInt(256), 255);
            this.rotX = MathUtil.random(-1.0, 1.0);
            this.rotY = MathUtil.random(-1.0, 1.0);
            this.rotZ = MathUtil.random(-1.0, 1.0);
            this.prevRotX = this.rotX;
            this.prevRotY = this.rotY;
            this.prevRotZ = this.rotZ;
            this.rotMotionX = MathUtil.random(-1.0, 1.0) * 0.04f;
            this.rotMotionY = MathUtil.random(-1.0, 1.0) * 0.04f;
            this.rotMotionZ = MathUtil.random(-1.0, 1.0) * 0.04f;
            this.spawnTime = System.currentTimeMillis();
            this.age = (int)MathUtil.random(120.0, 200.0);
        }

        private boolean tick() {
            if (IMinecraft.mc.player == null) {
                return true;
            }
            double distSq = IMinecraft.mc.player.squaredDistanceTo((double)this.posX, (double)this.posY, (double)this.posZ);
            this.age -= distSq > 4096.0 ? 8 : 1;
            if (this.age < 0) {
                return true;
            }
            this.prevX = this.posX;
            this.prevY = this.posY;
            this.prevZ = this.posZ;
            this.prevRotX = this.rotX;
            this.prevRotY = this.rotY;
            this.prevRotZ = this.rotZ;
            if (FireFly.this.fallMode.is("Отскоки")) {
                this.tickBounce();
            } else {
                this.posX += this.motionX;
                this.posY += this.motionY;
                this.posZ += this.motionZ;
                this.motionX *= 0.9f;
                this.motionY *= 0.9f;
                this.motionZ *= 0.9f;
                this.motionY -= 0.001f;
            }
            this.rotX += this.rotMotionX;
            this.rotY += this.rotMotionY;
            this.rotZ += this.rotMotionZ;
            this.rotMotionX *= 0.98f;
            this.rotMotionY *= 0.98f;
            this.rotMotionZ *= 0.98f;
            return false;
        }

        private void tickBounce() {
            this.motionX = 0.0f;
            this.motionZ = 0.0f;
            this.motionY -= 8.0E-4f;
            float newPosX = this.posX + this.motionX;
            float newPosY = this.posY + this.motionY;
            float newPosZ = this.posZ + this.motionZ;
            BlockPos particlePos = BlockPos.ofFloored((double)newPosX, (double)newPosY, (double)newPosZ);
            BlockState blockState = IMinecraft.mc.world.getBlockState(particlePos);
            if (!blockState.isAir()) {
                if (this.collisionTime == -1L) {
                    this.collisionTime = System.currentTimeMillis();
                }
                if (!IMinecraft.mc.world.getBlockState(BlockPos.ofFloored((double)(this.posX + this.motionX), (double)this.posY, (double)this.posZ)).isAir()) {
                    this.motionX = 0.0f;
                }
                if (!IMinecraft.mc.world.getBlockState(BlockPos.ofFloored((double)this.posX, (double)(this.posY + this.motionY), (double)this.posZ)).isAir()) {
                    this.motionY = -this.motionY * 0.8f;
                }
                if (!IMinecraft.mc.world.getBlockState(BlockPos.ofFloored((double)this.posX, (double)this.posY, (double)(this.posZ + this.motionZ))).isAir()) {
                    this.motionZ = 0.0f;
                }
                this.posX += this.motionX;
                this.posY += this.motionY;
                this.posZ += this.motionZ;
            } else {
                this.posX = newPosX;
                this.posY = newPosY;
                this.posZ = newPosZ;
            }
        }

        private void render(BufferBuilder buffer, MatrixStack matrices, Vec3d camPos, float tickDelta, long now) {
            float lifeScale = 1.0f - (float)(now - this.spawnTime) / 5000.0f;
            if (lifeScale <= 0.0f) {
                return;
            }
            this.updateAlpha(now);
            float size = 0.9f * lifeScale;
            if (size <= 0.0f) {
                return;
            }
            float x = MathHelper.lerp((float)tickDelta, (float)this.prevX, (float)this.posX);
            float y = MathHelper.lerp((float)tickDelta, (float)this.prevY, (float)this.posY);
            float z = MathHelper.lerp((float)tickDelta, (float)this.prevZ, (float)this.posZ);
            ColorRGBA base = FireFly.this.randomColor.isEnabled() ? this.randomColorValue : FireFly.this.getThemeColor(Math.max(0, this.age * 2));
            int color = base.withAlpha(FireFly.clamp255(this.alpha * 255.0f * lifeScale)).getRGB();
            matrices.push();
            matrices.translate((double)x - camPos.x, (double)y - camPos.y, (double)z - camPos.z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-IMinecraft.mc.getEntityRenderDispatcher().camera.getYaw()));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(IMinecraft.mc.getEntityRenderDispatcher().camera.getPitch()));
            Matrix4f matrix = matrices.peek().getPositionMatrix();
            buffer.vertex(matrix, 0.0f, -size, 0.0f).texture(0.0f, 1.0f).color(color);
            buffer.vertex(matrix, -size, -size, 0.0f).texture(1.0f, 1.0f).color(color);
            buffer.vertex(matrix, -size, 0.0f, 0.0f).texture(1.0f, 0.0f).color(color);
            buffer.vertex(matrix, 0.0f, 0.0f, 0.0f).texture(0.0f, 0.0f).color(color);
            matrices.pop();
        }

        private void prepareRender(float tickDelta, long now) {
            this.renderVisible = false;
            float lifeScale = 1.0f - (float)(now - this.spawnTime) / 5000.0f;
            if (lifeScale <= 0.0f) {
                return;
            }
            this.updateAlpha(now);
            float size = 0.9f * lifeScale;
            if (size <= 0.0f) {
                return;
            }
            this.renderX = MathHelper.lerp((float)tickDelta, (float)this.prevX, (float)this.posX);
            this.renderY = MathHelper.lerp((float)tickDelta, (float)this.prevY, (float)this.posY);
            this.renderZ = MathHelper.lerp((float)tickDelta, (float)this.prevZ, (float)this.posZ);
            this.renderRotX = MathHelper.lerp((float)tickDelta, (float)this.prevRotX, (float)this.rotX);
            this.renderRotY = MathHelper.lerp((float)tickDelta, (float)this.prevRotY, (float)this.rotY);
            this.renderRotZ = MathHelper.lerp((float)tickDelta, (float)this.prevRotZ, (float)this.rotZ);
            this.renderSize = size;
            this.renderAlpha = this.alpha;
            this.renderLifeScale = lifeScale;
            this.renderVisible = true;
        }

        private boolean isRenderVisible() {
            return this.renderVisible;
        }

        private void renderBloom(BufferBuilder buffer, MatrixStack matrices, Vec3d camPos) {
            ColorRGBA base = this.getBaseColor();
            float effectiveAlpha = this.renderAlpha * this.renderLifeScale;
            int bloomColor = base.withAlpha(FireFly.clamp255(255.0f * effectiveAlpha * 0.4f)).getRGB();
            float bigSize = 4.0f * this.renderSize;
            FireFly.this.drawBillboard(buffer, matrices, camPos, this.renderX, this.renderY, this.renderZ, bigSize, bloomColor);
        }

        private void renderBoxLines(BufferBuilder buffer, MatrixStack matrices, Vec3d camPos) {
            ColorRGBA base = this.getBaseColor();
            float effectiveAlpha = this.renderAlpha * this.renderLifeScale;
            int diagonalColor = base.withAlpha(FireFly.clamp255(255.0f * effectiveAlpha * 0.4f)).getRGB();
            int outlineColor = base.withAlpha(FireFly.clamp255(255.0f * effectiveAlpha * 0.8f)).getRGB();
            matrices.push();
            matrices.translate((double)this.renderX - camPos.x, (double)this.renderY - camPos.y, (double)this.renderZ - camPos.z);
            matrices.multiply(new Quaternionf().rotationXYZ(this.renderRotX, this.renderRotY, this.renderRotZ));
            matrices.scale(this.renderSize, this.renderSize, this.renderSize);
            Box box = new Box(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5);
            FireFly.this.renderBoxInternalDiagonals(matrices, buffer, box, diagonalColor);
            FireFly.this.renderOutlinedBox(matrices, buffer, box, outlineColor);
            matrices.pop();
        }

        private ColorRGBA getBaseColor() {
            return FireFly.this.randomColor.isEnabled() ? this.randomColorValue : FireFly.this.getThemeColor(Math.max(0, this.age * 2));
        }

        private void updateAlpha(long now) {
            if (FireFly.this.fallMode.is("Отскоки") && this.collisionTime != -1L) {
                long timeSinceCollision = now - this.collisionTime;
                this.alpha = Math.max(0.0f, 1.0f - (float)timeSinceCollision / 3000.0f);
            } else {
                this.alpha = MathHelper.clamp((float)(this.alpha + 0.1f), (float)0.0f, (float)1.0f);
            }
        }
    }

    private static final class FireflyParticle {
        private final int index;
        private final long spawnTime;
        private final ColorRGBA color;
        private final List<Vec3d> trail = new ArrayList<Vec3d>();
        private Vec3d position;
        private Vec3d velocity;

        private FireflyParticle(Vec3d position, Vec3d velocity, int index, ColorRGBA color) {
            this.position = position;
            this.velocity = velocity;
            this.index = index;
            this.color = color;
            this.spawnTime = System.currentTimeMillis();
            this.trail.add(position);
        }

        private void update(float speedValue, int maxTrail) {
            double randomness = 0.01;
            double vx = this.velocity.x + (Math.random() - 0.5) * randomness;
            double vy = this.velocity.y + (Math.random() - 0.5) * randomness;
            double vz = this.velocity.z + (Math.random() - 0.5) * randomness;
            double maxSpeed = (double)speedValue * 1.5;
            vx = MathHelper.clamp((double)vx, (double)(-maxSpeed), (double)maxSpeed);
            vy = MathHelper.clamp((double)vy, (double)(-maxSpeed), (double)maxSpeed);
            vz = MathHelper.clamp((double)vz, (double)(-maxSpeed), (double)maxSpeed);
            this.velocity = new Vec3d(vx, vy, vz);
            this.position = this.position.add(this.velocity);
            this.trail.add(this.position);
            while (this.trail.size() > maxTrail) {
                this.trail.remove(0);
            }
        }

        private boolean isExpired(long now) {
            return now - this.spawnTime >= 8000L;
        }

        private double distanceTo(Vec3d pos) {
            return this.position.distanceTo(pos);
        }

        private float getAlpha(long now) {
            long age = now - this.spawnTime;
            if (age <= 0L) {
                return 0.0f;
            }
            if (age < 500L) {
                return (float)age / 500.0f;
            }
            if (age > 7500L) {
                return Math.max(0.0f, (float)(8000L - age) / 500.0f);
            }
            return 1.0f;
        }

        private Vec3d getPosition() {
            return this.position;
        }

        private List<Vec3d> getTrail() {
            return this.trail;
        }

        private int getIndex() {
            return this.index;
        }

        private ColorRGBA getColor() {
            return this.color;
        }
    }
}

