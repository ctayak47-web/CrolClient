
package crol.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ShaderProgramKeys;
import net.minecraft.ShaderProgramKey;
import net.minecraft.Entity;
import net.minecraft.Vec3d;
import net.minecraft.BufferRenderer;
import net.minecraft.BufferBuilder;
import net.minecraft.Tessellator;
import net.minecraft.VertexFormats;
import net.minecraft.VertexFormat;
import net.minecraft.MathHelper;
import net.minecraft.MatrixStack;
import net.minecraft.Perspective;
import net.minecraft.RotationAxis;
import net.minecraft.BuiltBuffer;
import org.joml.Matrix4f;
import crol.client.CrolClient;
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
import crol.client.modules.impl.render.BabyModel;
import crol.client.utility.render.display.base.color.ColorRGBA;

@ModuleAnnotation(name="China Hat", category=Category.RENDER, description="Рисует шляпу над головой.")
public final class ChinaHat
extends Module {
    public static final ChinaHat INSTANCE = new ChinaHat();
    private final ThemeManager themeManager = CrolClient.getInstance().getThemeManager();
    private final ModeSetting hatMode = new ModeSetting("Режим", "Китайская шляпа", "Нимб", "Новогодняя Шляпа");
    private final ModeSetting.Value modeNimbus = this.hatMode.getValues().get(1);
    private final ModeSetting.Value modeSanta = this.hatMode.getValues().get(2);
    private final BooleanSetting customTheme = new BooleanSetting("Клиентский", false);
    private final ColorSetting primaryColor = new ColorSetting("Первый цвет", Theme.DARK.getColor(), this.customTheme::isEnabled, Theme.DARK::getColor);
    private final ColorSetting secondaryColor = new ColorSetting("Второй цвет", Theme.DARK.getSecondColor(), this.customTheme::isEnabled, Theme.DARK::getSecondColor);
    private final NumberSetting radiusScale = new NumberSetting("Масштаб радиуса", 0.9f, 0.5f, 2.0f, 0.05f);
    private final NumberSetting heightOffset = new NumberSetting("Смещение по высоте", 0.08f, -0.2f, 0.5f, 0.01f);
    private static final ColorRGBA SANTA_RED_DARK = new ColorRGBA(156, 14, 14, 255);
    private static final ColorRGBA SANTA_RED_LIGHT = new ColorRGBA(196, 28, 28, 255);
    private static final ColorRGBA SANTA_RED_SHADOW = new ColorRGBA(112, 9, 9, 255);
    private static final ColorRGBA SANTA_WHITE = new ColorRGBA(255, 255, 255, 255);
    private static final ColorRGBA SANTA_BRIM_SHADOW = new ColorRGBA(214, 214, 214, 255);

    private ChinaHat() {
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        float pitch;
        if (ChinaHat.mc.player == null || ChinaHat.mc.world == null) {
            return;
        }
        if (ChinaHat.mc.options.getPerspective() == Perspective.FIRST_PERSON) {
            return;
        }
        float tickDelta = event.getPartialTicks();
        float visualScale = BabyModel.INSTANCE.getVisualScale((Entity)ChinaHat.mc.player);
        Vec3d pos = new Vec3d(MathHelper.lerp((double)tickDelta, (double)ChinaHat.mc.player.prevX, (double)ChinaHat.mc.player.getX()), MathHelper.lerp((double)tickDelta, (double)ChinaHat.mc.player.prevY, (double)ChinaHat.mc.player.getY()), MathHelper.lerp((double)tickDelta, (double)ChinaHat.mc.player.prevZ, (double)ChinaHat.mc.player.getZ()));
        float radius = ChinaHat.mc.player.getWidth() * this.radiusScale.getCurrent() * visualScale;
        float baseY = (float)(pos.y + (double)((ChinaHat.mc.player.getEyeHeight(ChinaHat.mc.player.getPose()) + 0.2f) * visualScale) + (double)this.heightOffset.getCurrent());
        float height = 0.25f * visualScale;
        float yaw = MathHelper.lerpAngleDegrees((float)tickDelta, (float)ChinaHat.mc.player.prevYaw, (float)ChinaHat.mc.player.getYaw());
        float f = pitch = ChinaHat.mc.player.isGliding() ? -90.0f - MathHelper.lerp((float)tickDelta, (float)ChinaHat.mc.player.prevPitch, (float)ChinaHat.mc.player.getPitch()) : 0.0f;
        if (this.hatMode.is(this.modeNimbus)) {
            this.renderNimbus(event.getMatrix(), pos.x, baseY, pos.z, radius, yaw, pitch);
            return;
        }
        if (this.hatMode.is(this.modeSanta)) {
            this.renderSantaHat(event.getMatrix(), pos.x, baseY, pos.z, radius, yaw, pitch);
            return;
        }
        this.renderHat(event.getMatrix(), pos.x, baseY, pos.z, radius, height, yaw, pitch);
    }

    private void renderHat(MatrixStack matrices, double x, double y, double z, float radius, float height, float yaw, float pitch) {
        Vec3d camPos = ChinaHat.mc.getEntityRenderDispatcher().camera.getPos();
        matrices.push();
        matrices.translate(x - camPos.x, y - camPos.y, z - camPos.z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
        if (pitch != 0.0f) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
        }
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask((boolean)true);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        int apexColor = this.getPrimaryColor().withAlpha(220).getRGB();
        buffer.vertex(matrix, 0.0f, height, 0.0f).color(apexColor);
        for (int i = 0; i <= 360; i += 5) {
            float angle = (float)Math.toRadians(i);
            float xOffset = MathHelper.sin((float)angle) * radius;
            float zOffset = -MathHelper.cos((float)angle) * radius;
            ColorRGBA ringColor = this.getRingColor(i * 4).withAlpha(200);
            buffer.vertex(matrix, xOffset, 0.0f, zOffset).color(ringColor.getRGB());
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        matrices.pop();
    }

    private void renderNimbus(MatrixStack matrices, double x, double y, double z, float radius, float yaw, float pitch) {
        float cos;
        float sin;
        float angle;
        int i;
        Vec3d camPos = ChinaHat.mc.getEntityRenderDispatcher().camera.getPos();
        matrices.push();
        matrices.translate(x - camPos.x, y - camPos.y, z - camPos.z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
        if (pitch != 0.0f) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
        }
        float innerRadius = radius * 0.55f;
        float midRadius = radius * 0.7f;
        float outerRadius = radius * 0.85f;
        float ringY = radius * 0.15f;
        float lift = radius * 0.08f;
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask((boolean)true);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        int color = this.getPrimaryColor().withAlpha(220).getRGB();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (i = 0; i <= 360; i += 5) {
            angle = (float)Math.toRadians(i);
            sin = MathHelper.sin((float)angle);
            cos = MathHelper.cos((float)angle);
            float xInner = sin * innerRadius;
            float zInner = -cos * innerRadius;
            float xMid = sin * midRadius;
            float zMid = -cos * midRadius;
            buffer.vertex(matrix, xInner, ringY + lift, zInner).color(color);
            buffer.vertex(matrix, xMid, ringY, zMid).color(color);
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (i = 0; i <= 360; i += 5) {
            angle = (float)Math.toRadians(i);
            sin = MathHelper.sin((float)angle);
            cos = MathHelper.cos((float)angle);
            float xMid = sin * midRadius;
            float zMid = -cos * midRadius;
            float xOuter = sin * outerRadius;
            float zOuter = -cos * outerRadius;
            buffer.vertex(matrix, xMid, ringY, zMid).color(color);
            buffer.vertex(matrix, xOuter, ringY + lift, zOuter).color(color);
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        matrices.pop();
    }

    private void renderSantaHat(MatrixStack matrices, double x, double y, double z, float radius, float yaw, float pitch) {
        Vec3d camPos = ChinaHat.mc.getEntityRenderDispatcher().camera.getPos();
        matrices.push();
        matrices.translate(x - camPos.x, y - camPos.y, z - camPos.z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
        if (pitch != 0.0f) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
        }
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-7.0f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(5.0f));
        float brimInnerRadius = radius * 0.52f;
        float brimOuterRadius = radius * 0.82f;
        float brimTopY = -radius * 0.03f;
        float brimBottomY = brimTopY - radius * 0.16f;
        float coneBaseRadius = brimInnerRadius * 1.03f;
        float coneBaseY = brimTopY + radius * 0.04f;
        float bendX = -radius * 0.1f;
        float bendY = radius * 0.62f;
        float bendZ = -radius * 0.11f;
        float tipX = -radius * 0.34f;
        float tipY = radius * 0.47f;
        float tipZ = -radius * 0.27f;
        float tailBaseRadius = radius * 0.15f;
        float pomRadius = radius * 0.19f;
        int segments = 56;
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask((boolean)true);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        int redLight = SANTA_RED_LIGHT.getRGB();
        int redDark = SANTA_RED_DARK.getRGB();
        int redShadow = SANTA_RED_SHADOW.getRGB();
        int white = SANTA_WHITE.getRGB();
        int brimShadow = SANTA_BRIM_SHADOW.getRGB();
        this.drawConeFan(matrix, segments, 0.0f, coneBaseY, 0.0f, coneBaseRadius, bendX, bendY, bendZ, redLight, redDark);
        this.drawConeFan(matrix, segments, bendX, bendY, bendZ, tailBaseRadius, tipX, tipY, tipZ, redLight, redShadow);
        this.drawConeFan(matrix, segments, bendX + radius * 0.01f, bendY + radius * 0.02f, bendZ - radius * 0.01f, tailBaseRadius * 0.45f, tipX + radius * 0.05f, tipY + radius * 0.03f, tipZ + radius * 0.02f, new ColorRGBA(255, 64, 64, 230).getRGB(), new ColorRGBA(208, 34, 34, 220).getRGB());
        this.drawAnnulus(matrix, segments, brimInnerRadius, brimOuterRadius, brimTopY, white, white);
        this.drawRingWall(matrix, segments, brimOuterRadius, brimTopY, brimBottomY, white, brimShadow);
        this.drawRingWall(matrix, segments, brimInnerRadius, brimTopY, brimBottomY, white, brimShadow);
        this.drawAnnulus(matrix, segments, brimInnerRadius, brimOuterRadius, brimBottomY, brimShadow, brimShadow);
        this.drawSphere(matrix, tipX, tipY, tipZ, pomRadius, 16, white, brimShadow);
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        matrices.pop();
    }

    private void drawConeFan(Matrix4f matrix, int segments, float baseCenterX, float baseY, float baseCenterZ, float baseRadius, float tipX, float tipY, float tipZ, int tipColor, int baseColor) {
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, tipX, tipY, tipZ).color(tipColor);
        for (int i = 0; i <= segments; ++i) {
            float angle = (float)((double)i * Math.PI * 2.0 / (double)segments);
            float xOffset = MathHelper.cos((float)angle) * baseRadius;
            float zOffset = MathHelper.sin((float)angle) * baseRadius;
            buffer.vertex(matrix, baseCenterX + xOffset, baseY, baseCenterZ + zOffset).color(baseColor);
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
    }

    private void drawAnnulus(Matrix4f matrix, int segments, float innerRadius, float outerRadius, float y, int innerColor, int outerColor) {
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= segments; ++i) {
            float angle = (float)((double)i * Math.PI * 2.0 / (double)segments);
            float cos = MathHelper.cos((float)angle);
            float sin = MathHelper.sin((float)angle);
            buffer.vertex(matrix, cos * outerRadius, y, sin * outerRadius).color(outerColor);
            buffer.vertex(matrix, cos * innerRadius, y, sin * innerRadius).color(innerColor);
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
    }

    private void drawRingWall(Matrix4f matrix, int segments, float radius, float topY, float bottomY, int topColor, int bottomColor) {
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= segments; ++i) {
            float angle = (float)((double)i * Math.PI * 2.0 / (double)segments);
            float cos = MathHelper.cos((float)angle);
            float sin = MathHelper.sin((float)angle);
            buffer.vertex(matrix, cos * radius, topY, sin * radius).color(topColor);
            buffer.vertex(matrix, cos * radius, bottomY, sin * radius).color(bottomColor);
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
    }

    private void drawSphere(Matrix4f matrix, float cx, float cy, float cz, float radius, int segments, int topColor, int bottomColor) {
        for (int i = 0; i < segments; ++i) {
            float lat0 = (float)(-1.5707963267948966 + Math.PI * (double)i / (double)segments);
            float lat1 = (float)(-1.5707963267948966 + Math.PI * (double)(i + 1) / (double)segments);
            float sin0 = MathHelper.sin((float)lat0);
            float cos0 = MathHelper.cos((float)lat0);
            float sin1 = MathHelper.sin((float)lat1);
            float cos1 = MathHelper.cos((float)lat1);
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            for (int j = 0; j <= segments; ++j) {
                float lng = (float)(Math.PI * 2 * (double)j / (double)segments);
                float cosLng = MathHelper.cos((float)lng);
                float sinLng = MathHelper.sin((float)lng);
                int color0 = sin0 > 0.0f ? topColor : bottomColor;
                int color1 = sin1 > 0.0f ? topColor : bottomColor;
                buffer.vertex(matrix, cx + cos0 * cosLng * radius, cy + sin0 * radius, cz + cos0 * sinLng * radius).color(color0);
                buffer.vertex(matrix, cx + cos1 * cosLng * radius, cy + sin1 * radius, cz + cos1 * sinLng * radius).color(color1);
            }
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        }
    }

    private ColorRGBA getPrimaryColor() {
        return this.customTheme.isEnabled() ? this.primaryColor.getColor() : this.themeManager.getCurrentTheme().getColor();
    }

    private ColorRGBA getSecondaryColor() {
        return this.getPrimaryColor();
    }

    private ColorRGBA getRingColor(int offset) {
        if (this.customTheme.isEnabled()) {
            return this.getPrimaryColor();
        }
        return this.themeManager.getCurrentTheme().getColor();
    }
}

