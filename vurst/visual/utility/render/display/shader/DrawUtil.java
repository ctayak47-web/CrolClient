
package vurst.visual.utility.render.display.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import net.minecraft.ShaderProgramKeys;
import net.minecraft.ShaderProgramKey;
import net.minecraft.Vec2f;
import net.minecraft.BufferRenderer;
import net.minecraft.BufferBuilder;
import net.minecraft.Tessellator;
import net.minecraft.VertexFormats;
import net.minecraft.VertexFormat;
import net.minecraft.Identifier;
import net.minecraft.MatrixStack;
import net.minecraft.BuiltBuffer;
import org.joml.Matrix4f;
import vurst.visual.VurstVisual;
import vurst.visual.utility.interfaces.IWindow;
import vurst.visual.utility.math.MathUtil;
import vurst.visual.utility.render.display.Render2DUtil;
import vurst.visual.utility.render.display.base.BorderRadius;
import vurst.visual.utility.render.display.base.CustomSprite;
import vurst.visual.utility.render.display.base.Gradient;
import vurst.visual.utility.render.display.base.color.ColorRGBA;
import vurst.visual.utility.render.display.shader.BlurProgram;
import vurst.visual.utility.render.display.shader.CustomRenderTarget;
import vurst.visual.utility.render.display.shader.GlProgram;

public final class DrawUtil
implements IWindow {
    public static final float DEFAULT_SMOOTHNESS = 0.8f;
    private static final int DEFAULT_GLOW_RADIUS = 10;
    public static GlProgram rectangleProgram;
    private static GlProgram squircleProgram;
    private static GlProgram roundedTextureProgram;
    private static GlProgram squircleTextureProgram;
    private static GlProgram borderProgram;
    private static GlProgram figmaBorderProgram;
    private static GlProgram loadingProgram;
    private static GlProgram gradientRectangleProgram;
    public static BlurProgram blurProgram;
    private static final CustomRenderTarget buffer;

    private static boolean programReady(GlProgram program) {
        return program != null && program.isReady();
    }

    public static void initializeShaders() {
        rectangleProgram = new GlProgram(VurstVisual.id("rectangle/data"), VertexFormats.POSITION_COLOR);
        squircleProgram = new GlProgram(VurstVisual.id("squircle/data"), VertexFormats.POSITION_COLOR);
        squircleTextureProgram = new GlProgram(VurstVisual.id("squircle_texture/data"), VertexFormats.POSITION_TEXTURE_COLOR);
        roundedTextureProgram = new GlProgram(VurstVisual.id("texture/data"), VertexFormats.POSITION_TEXTURE_COLOR);
        borderProgram = new GlProgram(VurstVisual.id("border/data"), VertexFormats.POSITION_COLOR);
        figmaBorderProgram = new GlProgram(VurstVisual.id("corner/data"), VertexFormats.POSITION_COLOR);
        loadingProgram = new GlProgram(VurstVisual.id("loading/data"), VertexFormats.POSITION_COLOR);
        gradientRectangleProgram = new GlProgram(VurstVisual.id("gradient_rectangle/data"), VertexFormats.POSITION_COLOR);
        blurProgram = new BlurProgram();
        blurProgram.initShaders();
    }

    public static void updateBuffer() {
        buffer.setClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        buffer.setup();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        mc.getFramebuffer().beginRead();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (int)mc.getFramebuffer().getColorAttachment());
        DrawUtil.drawQuad(0.0f, 0.0f, mw.getScaledWidth(), mw.getScaledHeight(), true);
        mc.getFramebuffer().endRead();
        RenderSystem.disableBlend();
        mc.getFramebuffer().beginWrite(true);
        buffer.stop();
    }

    private static void drawQuad(float x, float y, float width, float height, boolean flip) {
        BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        int color = -1;
        float vTop = flip ? 0.0f : 1.0f;
        float vBottom = flip ? 1.0f : 0.0f;
        builder.vertex(x, y, 0.0f).texture(0.0f, vBottom).color(-1);
        builder.vertex(x, y + height, 0.0f).texture(0.0f, vTop).color(-1);
        builder.vertex(x + width, y + height, 0.0f).texture(1.0f, vTop).color(-1);
        builder.vertex(x + width, y, 0.0f).texture(1.0f, vBottom).color(-1);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void drawLine(MatrixStack matrices, Vec2f from, Vec2f to, ColorRGBA color) {
        matrices.push();
        try {
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
            RenderSystem.lineWidth((float)1.0f);
            DrawUtil.drawSetup();
            BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            builder.vertex(matrix4f, from.x, from.y, 0.0f).color(color.getRGB());
            builder.vertex(matrix4f, to.x, to.y, 0.0f).color(color.getRGB());
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
            DrawUtil.drawEnd();
        }
        finally {
            RenderSystem.disableBlend();
            RenderSystem.lineWidth((float)1.0f);
            matrices.pop();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void drawBezier(MatrixStack matrices, Vec2f p0, Vec2f p1, Vec2f p2, Vec2f p3, ColorRGBA color, int resolution) {
        matrices.push();
        try {
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
            RenderSystem.lineWidth((float)1.0f);
            DrawUtil.drawSetup();
            BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            for (int i = 0; i <= resolution; ++i) {
                float t = (float)i / (float)resolution;
                float x = (float)MathUtil.cubicBezier(t, p0.x, p1.x, p2.x, p3.x);
                float y = (float)MathUtil.cubicBezier(t, p0.y, p1.y, p2.y, p3.y);
                builder.vertex(matrix4f, x, y, 0.0f).color(color.getRGB());
            }
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
            DrawUtil.drawEnd();
        }
        finally {
            RenderSystem.disableBlend();
            RenderSystem.lineWidth((float)1.0f);
            matrices.pop();
        }
    }

    private static float cubicBezier(float t, float p0, float p1, float p2, float p3) {
        float u = 1.0f - t;
        float tt = t * t;
        float uu = u * u;
        return uu * u * p0 + 3.0f * uu * t * p1 + 3.0f * u * tt * p2 + tt * t * p3;
    }

    public static void drawRect(MatrixStack matrices, float x, float y, float width, float height, ColorRGBA color) {
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        DrawUtil.drawSetup();
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(matrix4f, x, y + height, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, x + width, y + height, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, x + width, y, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, x, y, 0.0f).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtil.drawEnd();
        matrices.pop();
    }

    public static void drawSquircle(MatrixStack matrices, float x, float y, float width, float height, float squirt, BorderRadius borderRadius, ColorRGBA color) {
        if (!DrawUtil.programReady(squircleProgram)) {
            DrawUtil.drawRect(matrices, x, y, width, height, color);
            return;
        }
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float smoothness = 0.8f;
        squircleProgram.use();
        squircleProgram.findUniform("Size").set(width, height);
        squircleProgram.findUniform("Radius").set(borderRadius.topLeftRadius() * squirt / 2.0f, borderRadius.bottomLeftRadius() * squirt / 2.0f, borderRadius.topRightRadius() * squirt / 2.0f, borderRadius.bottomRightRadius() * squirt / 2.0f);
        squircleProgram.findUniform("Smoothness").set(smoothness);
        squircleProgram.findUniform("CornerSmoothness").set(squirt);
        DrawUtil.drawSetup();
        float horizontalPadding = -smoothness / 2.0f + smoothness * 2.0f;
        float verticalPadding = smoothness / 2.0f + smoothness;
        float adjustedX = x - horizontalPadding / 2.0f;
        float adjustedY = y - verticalPadding / 2.0f;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(matrix4f, adjustedX, adjustedY, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtil.drawEnd();
        matrices.pop();
    }

    public static void drawLoadingRect(MatrixStack matrices, float x, float y, float width, float height, float progress, BorderRadius borderRadius, ColorRGBA color) {
        if (!DrawUtil.programReady(loadingProgram)) {
            float clamped = Math.max(0.0f, Math.min(1.0f, progress));
            DrawUtil.drawRect(matrices, x, y, width * clamped, height, color);
            return;
        }
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float smoothness = 0.8f;
        loadingProgram.use();
        loadingProgram.findUniform("Size").set(width, height);
        loadingProgram.findUniform("Radius").set(borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius());
        loadingProgram.findUniform("Smoothness").set(smoothness);
        loadingProgram.findUniform("Progress").set(progress);
        loadingProgram.findUniform("StripeWidth").set(0.0f);
        loadingProgram.findUniform("Fade").set(0.5f);
        DrawUtil.drawSetup();
        float horizontalPadding = -smoothness / 2.0f + smoothness * 2.0f;
        float verticalPadding = smoothness / 2.0f + smoothness;
        float adjustedX = x - horizontalPadding / 2.0f;
        float adjustedY = y - verticalPadding / 2.0f;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(matrix4f, adjustedX, adjustedY, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtil.drawEnd();
        matrices.pop();
    }

    public static void drawRoundedRect(MatrixStack matrices, float x, float y, float width, float height, BorderRadius borderRadius, ColorRGBA color) {
        if (!DrawUtil.programReady(rectangleProgram)) {
            DrawUtil.drawRect(matrices, x, y, width, height, color);
            return;
        }
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float smoothness = 0.8f;
        rectangleProgram.use();
        rectangleProgram.findUniform("Size").set(width, height);
        rectangleProgram.findUniform("Radius").set(borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius());
        rectangleProgram.findUniform("Smoothness").set(smoothness);
        DrawUtil.drawSetup();
        float horizontalPadding = -smoothness / 2.0f + smoothness * 2.0f;
        float verticalPadding = smoothness / 2.0f + smoothness;
        float adjustedX = x - horizontalPadding / 2.0f;
        float adjustedY = y - verticalPadding / 2.0f;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(matrix4f, adjustedX, adjustedY, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtil.drawEnd();
        matrices.pop();
    }

    public static void drawRoundedRect(MatrixStack matrices, float x, float y, float width, float height, BorderRadius borderRadius, ColorRGBA color1, ColorRGBA color2, ColorRGBA color3, ColorRGBA color4) {
        if (!DrawUtil.programReady(gradientRectangleProgram)) {
            DrawUtil.drawRoundedRect(matrices, x, y, width, height, borderRadius, color1);
            return;
        }
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float smoothness = 0.8f;
        gradientRectangleProgram.use();
        gradientRectangleProgram.findUniform("Size").set(width, height);
        gradientRectangleProgram.findUniform("Radius").set(borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius());
        gradientRectangleProgram.findUniform("Smoothness").set(smoothness);
        gradientRectangleProgram.findUniform("TopLeftColor").set((float)color1.getRed() / 255.0f, (float)color1.getGreen() / 255.0f, (float)color1.getBlue() / 255.0f, (float)color1.getAlpha() / 255.0f);
        gradientRectangleProgram.findUniform("BottomLeftColor").set((float)color2.getRed() / 255.0f, (float)color2.getGreen() / 255.0f, (float)color2.getBlue() / 255.0f, (float)color2.getAlpha() / 255.0f);
        gradientRectangleProgram.findUniform("BottomRightColor").set((float)color3.getRed() / 255.0f, (float)color3.getGreen() / 255.0f, (float)color3.getBlue() / 255.0f, (float)color3.getAlpha() / 255.0f);
        gradientRectangleProgram.findUniform("TopRightColor").set((float)color4.getRed() / 255.0f, (float)color4.getGreen() / 255.0f, (float)color4.getBlue() / 255.0f, (float)color4.getAlpha() / 255.0f);
        DrawUtil.drawSetup();
        float horizontalPadding = -smoothness / 2.0f + smoothness * 2.0f;
        float verticalPadding = smoothness / 2.0f + smoothness;
        float adjustedX = x - horizontalPadding / 2.0f;
        float adjustedY = y - verticalPadding / 2.0f;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(matrix4f, adjustedX, adjustedY, 0.0f).color(color1.getRGB());
        builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).color(color2.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).color(color3.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).color(color4.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtil.drawEnd();
        matrices.pop();
    }

    public static void drawRoundedRect(MatrixStack matrices, float x, float y, float width, float height, BorderRadius borderRadius, Gradient gradient) {
        DrawUtil.drawRoundedRect(matrices, x, y, width, height, borderRadius, gradient.getTopLeftColor(), gradient.getBottomLeftColor(), gradient.getBottomRightColor(), gradient.getTopRightColor());
    }

    public static void drawRoundedBorder(MatrixStack matrices, float x, float y, float width, float height, float borderThickness, BorderRadius borderRadius, ColorRGBA borderColor) {
        if (!DrawUtil.programReady(borderProgram)) {
            if (borderThickness <= 0.0f) {
                return;
            }
            DrawUtil.drawRect(matrices, x, y, width, borderThickness, borderColor);
            DrawUtil.drawRect(matrices, x, y + height - borderThickness, width, borderThickness, borderColor);
            DrawUtil.drawRect(matrices, x, y + borderThickness, borderThickness, Math.max(0.0f, height - borderThickness * 2.0f), borderColor);
            DrawUtil.drawRect(matrices, x + width - borderThickness, y + borderThickness, borderThickness, Math.max(0.0f, height - borderThickness * 2.0f), borderColor);
            return;
        }
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float internalSmoothness = 0.8f;
        float externalSmoothness = 1.0f;
        borderProgram.use();
        borderProgram.findUniform("Size").set(width, height);
        borderProgram.findUniform("Radius").set(borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius());
        borderProgram.findUniform("Smoothness").set(internalSmoothness, externalSmoothness);
        borderProgram.findUniform("Thickness").set(borderThickness);
        DrawUtil.drawSetup();
        float horizontalPadding = -externalSmoothness / 2.0f + externalSmoothness * 2.0f;
        float verticalPadding = externalSmoothness / 2.0f + externalSmoothness;
        float adjustedX = x - horizontalPadding / 2.0f;
        float adjustedY = y - verticalPadding / 2.0f;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(matrix4f, adjustedX, adjustedY, 0.0f).color(borderColor.getRGB());
        builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).color(borderColor.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).color(borderColor.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).color(borderColor.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtil.drawEnd();
        matrices.pop();
    }

    public static void drawRoundedCorner(MatrixStack matrices, float x, float y, float width, float height, float borderThikenes, float delta, ColorRGBA color, BorderRadius radius) {
        if (!VurstVisual.getInstance().getThemeManager().getCurrentTheme().isCorners()) {
            return;
        }
        DrawUtil.drawRoundedCornerOnly(matrices, x -= 0.3f, y -= 0.3f, delta, delta, borderThikenes, radius, color, 0.0f);
        DrawUtil.drawRoundedCornerOnly(matrices, x + (width += 0.6f) - delta, y, delta, delta, borderThikenes, radius, color, 1.0f);
        DrawUtil.drawRoundedCornerOnly(matrices, x, y + (height += 0.6f) - delta, delta, delta, borderThikenes, radius, color, 2.0f);
        DrawUtil.drawRoundedCornerOnly(matrices, x + width - delta, y + height - delta, delta, delta, borderThikenes, radius, color, 3.0f);
    }

    public static void drawRoundedCornerOnly(MatrixStack matrices, float x, float y, float width, float height, float borderThickness, BorderRadius borderRadius, ColorRGBA borderColor, float cornerIdex) {
        if (!DrawUtil.programReady(figmaBorderProgram)) {
            return;
        }
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float internalSmoothness = 0.8f;
        float externalSmoothness = 1.0f;
        figmaBorderProgram.use();
        figmaBorderProgram.findUniform("Size").set(width, height);
        figmaBorderProgram.findUniform("Radius").set(borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius());
        figmaBorderProgram.findUniform("Smoothness").set(internalSmoothness, externalSmoothness);
        figmaBorderProgram.findUniform("Thickness").set(borderThickness);
        figmaBorderProgram.findUniform("CornerIndex").set(cornerIdex);
        DrawUtil.drawSetup();
        float horizontalPadding = -externalSmoothness / 2.0f + externalSmoothness * 2.0f;
        float verticalPadding = externalSmoothness / 2.0f + externalSmoothness;
        float adjustedX = x - horizontalPadding / 2.0f;
        float adjustedY = y - verticalPadding / 2.0f;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(matrix4f, adjustedX, adjustedY, 0.0f).color(borderColor.getRGB());
        builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).color(borderColor.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).color(borderColor.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).color(borderColor.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtil.drawEnd();
        matrices.pop();
    }

    public static void drawTexture(MatrixStack matrices, Identifier identifier, float x, float y, float width, float height, ColorRGBA textureColor) {
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (Identifier)identifier);
        DrawUtil.drawSetup();
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix4f, x, y, 0.0f).texture(0.0f, 0.0f).color(textureColor.getRGB());
        builder.vertex(matrix4f, x, y + height, 0.0f).texture(0.0f, 1.0f).color(textureColor.getRGB());
        builder.vertex(matrix4f, x + width, y + height, 0.0f).texture(1.0f, 1.0f).color(textureColor.getRGB());
        builder.vertex(matrix4f, x + width, y, 0.0f).texture(1.0f, 0.0f).color(textureColor.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtil.drawEnd();
        RenderSystem.setShaderTexture((int)0, (int)0);
        matrices.pop();
    }

    public static void drawTexture(MatrixStack matrices, Identifier identifier, float x, float y, float width, float height, Gradient textureColor) {
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (Identifier)identifier);
        DrawUtil.drawSetup();
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix4f, x, y, 0.0f).texture(0.0f, 0.0f).color(textureColor.getTopLeftColor().getRGB());
        builder.vertex(matrix4f, x, y + height, 0.0f).texture(0.0f, 1.0f).color(textureColor.getBottomLeftColor().getRGB());
        builder.vertex(matrix4f, x + width, y + height, 0.0f).texture(1.0f, 1.0f).color(textureColor.getBottomRightColor().getRGB());
        builder.vertex(matrix4f, x + width, y, 0.0f).texture(1.0f, 0.0f).color(textureColor.getTopRightColor().getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtil.drawEnd();
        RenderSystem.setShaderTexture((int)0, (int)0);
        matrices.pop();
    }

    public static void drawTexture(MatrixStack matrices, Identifier identifier, float x, float y, float width, float height, float u1, float u2, float v1, float v2, ColorRGBA clor) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        matrices.push();
        int color = clor.getRGB();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float x2 = x + width;
        float y2 = y + height;
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (Identifier)identifier);
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix4f, x, y, 0.0f).texture(u1, v1).color(color);
        builder.vertex(matrix4f, x, y2, 0.0f).texture(u1, v2).color(color);
        builder.vertex(matrix4f, x2, y2, 0.0f).texture(u2, v2).color(color);
        builder.vertex(matrix4f, x2, y, 0.0f).texture(u2, v1).color(color);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtil.drawEnd();
        RenderSystem.setShaderTexture((int)0, (int)0);
        matrices.pop();
        RenderSystem.disableBlend();
    }

    public static void drawSprite(MatrixStack matrices, CustomSprite sprite, float x, float y, float width, float height, ColorRGBA color) {
        DrawUtil.drawTexture(matrices, sprite.getTexture(), x, y, width, height, 0.0f, 1.0f, 0.0f, 1.0f, color);
    }

    public static void drawRoundedTexture(MatrixStack matrices, Identifier identifier, float x, float y, float width, float height, BorderRadius borderRadius) {
        DrawUtil.drawRoundedTexture(matrices, identifier, x, y, width, height, borderRadius, ColorRGBA.WHITE);
    }

    public static void drawRoundedTexture(MatrixStack matrices, Identifier identifier, float x, float y, float width, float height, BorderRadius borderRadius, ColorRGBA color) {
        if (!DrawUtil.programReady(roundedTextureProgram)) {
            DrawUtil.drawTexture(matrices, identifier, x, y, width, height, color);
            return;
        }
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float smoothness = 0.8f;
        roundedTextureProgram.use();
        RenderSystem.setShaderTexture((int)0, (Identifier)identifier);
        roundedTextureProgram.findUniform("Size").set(width, height);
        roundedTextureProgram.findUniform("Radius").set(borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius());
        roundedTextureProgram.findUniform("Smoothness").set(smoothness);
        DrawUtil.drawSetup();
        float horizontalPadding = -smoothness / 2.0f + smoothness * 2.0f;
        float verticalPadding = smoothness / 2.0f + smoothness;
        float adjustedX = x - horizontalPadding / 2.0f;
        float adjustedY = y - verticalPadding / 2.0f;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix4f, adjustedX, adjustedY, 0.0f).texture(0.0f, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).texture(0.0f, 1.0f).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).texture(1.0f, 1.0f).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).texture(1.0f, 0.0f).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtil.drawEnd();
        RenderSystem.setShaderTexture((int)0, (int)0);
        matrices.pop();
    }

    public static void drawShadow(MatrixStack matrices, float x, float y, float width, float height, float softness, BorderRadius borderRadius, ColorRGBA color) {
        if (!DrawUtil.programReady(rectangleProgram)) {
            DrawUtil.drawRect(matrices, x, y, width, height, color);
            return;
        }
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        rectangleProgram.use();
        rectangleProgram.findUniform("Size").set(width, height);
        rectangleProgram.findUniform("Radius").set(borderRadius.topLeftRadius() * 3.0f, borderRadius.bottomLeftRadius() * 3.0f, borderRadius.topRightRadius() * 3.0f, borderRadius.bottomRightRadius() * 3.0f);
        rectangleProgram.findUniform("Smoothness").set(softness);
        DrawUtil.drawSetup();
        float horizontalPadding = -softness / 2.0f + softness * 2.0f;
        float verticalPadding = softness / 2.0f + softness;
        float adjustedX = x - horizontalPadding / 2.0f;
        float adjustedY = y - verticalPadding / 2.0f;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(matrix4f, adjustedX, adjustedY, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtil.drawEnd();
        matrices.pop();
    }

    public static void drawBlur(MatrixStack matrices, float x, float y, float width, float height, float blurRadius, float squirt, BorderRadius borderRadius, ColorRGBA color) {
        if (blurProgram == null || !blurProgram.isReady() || !DrawUtil.programReady(squircleTextureProgram)) {
            return;
        }
        if ((blurRadius /= 22.5f) <= 0.0f) {
            return;
        }
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float smoothness = 0.03f;
        blurProgram.setBlurRadius(2.0f);
        squircleTextureProgram.use();
        RenderSystem.setShaderTexture((int)0, (int)BlurProgram.getTexture());
        squircleTextureProgram.findUniform("Size").set(width, height);
        squircleTextureProgram.findUniform("Radius").set(borderRadius.topLeftRadius() * squirt / 2.0f, borderRadius.bottomLeftRadius() * squirt / 2.0f, borderRadius.topRightRadius() * squirt / 2.0f, borderRadius.bottomRightRadius() * squirt / 2.0f);
        squircleTextureProgram.findUniform("Smoothness").set(0.1f);
        squircleTextureProgram.findUniform("CornerSmoothness").set(squirt);
        DrawUtil.drawSetup();
        float horizontalPadding = -smoothness / 2.0f + smoothness * 2.0f;
        float verticalPadding = smoothness / 2.0f + smoothness;
        float adjustedX = x - horizontalPadding / 2.0f;
        float adjustedY = y - verticalPadding / 2.0f;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;
        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();
        float u = adjustedX / (float)screenWidth;
        float v = ((float)screenHeight - adjustedY - adjustedHeight) / (float)screenHeight;
        float texWidth = adjustedWidth / (float)screenWidth;
        float texHeight = adjustedHeight / (float)screenHeight;
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix4f, adjustedX, adjustedY, 0.0f).texture(u, v + texHeight).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).texture(u, v).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).texture(u + texWidth, v).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).texture(u + texWidth, v + texHeight).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtil.drawEnd();
        RenderSystem.setShaderTexture((int)0, (int)0);
        matrices.pop();
    }

    public static void drawBlurHud(MatrixStack matrices, float x, float y, float width, float height, float blurRadius, BorderRadius borderRadius, ColorRGBA color) {
        boolean blur = VurstVisual.getInstance().getThemeManager().getCurrentTheme().isBlur();
        boolean glow = VurstVisual.getInstance().getThemeManager().getCurrentTheme().isGlow();
        DrawUtil.drawBlurHudBooleanCheck(matrices, x, y, width, height, blurRadius, borderRadius, color, blur, glow);
    }

    public static void drawBlurHudBooleanCheck(MatrixStack matrices, float x, float y, float width, float height, float blurRadius, BorderRadius borderRadius, ColorRGBA color, boolean blur, boolean glow) {
        if (blur && blurProgram != null && blurProgram.isReady() && DrawUtil.programReady(roundedTextureProgram)) {
            if ((blurRadius /= 22.5f) <= 0.0f) {
                return;
            }
            matrices.push();
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            blurProgram.setBlurRadius(2.0f);
            roundedTextureProgram.use();
            RenderSystem.setShaderTexture((int)0, (int)BlurProgram.getTexture());
            roundedTextureProgram.findUniform("Size").set(width, height);
            roundedTextureProgram.findUniform("Radius").set(borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius());
            roundedTextureProgram.findUniform("Smoothness").set(0.01f);
            DrawUtil.drawSetup();
            int screenWidth = mc.getWindow().getScaledWidth();
            int screenHeight = mc.getWindow().getScaledHeight();
            float u = x / (float)screenWidth;
            float v = ((float)screenHeight - y - height) / (float)screenHeight;
            float texWidth = width / (float)screenWidth;
            float texHeight = height / (float)screenHeight;
            BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            builder.vertex(matrix4f, x, y, 0.0f).texture(u, v + texHeight).color(color.getRGB());
            builder.vertex(matrix4f, x, y + height, 0.0f).texture(u, v).color(color.getRGB());
            builder.vertex(matrix4f, x + width, y + height, 0.0f).texture(u + texWidth, v).color(color.getRGB());
            builder.vertex(matrix4f, x + width, y, 0.0f).texture(u + texWidth, v + texHeight).color(color.getRGB());
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
            DrawUtil.drawEnd();
            RenderSystem.setShaderTexture((int)0, (int)0);
            matrices.pop();
        }
        if (glow) {
            DrawUtil.drawGlow(matrices, x, y, width, height, 10);
        }
    }

    public static void drawGlow(MatrixStack matrixStack, float x, float y, float width, float height, int glowRadius) {
        Render2DUtil.drawGradientBlurredShadow(matrixStack, x, y, width, height, glowRadius, VurstVisual.getInstance().getThemeManager().getClientColor());
    }

    public static void drawBlur(MatrixStack matrices, float x, float y, float width, float height, float blurRadius, BorderRadius borderRadius, ColorRGBA color) {
        if (blurProgram == null || !blurProgram.isReady() || !DrawUtil.programReady(roundedTextureProgram)) {
            return;
        }
        if ((blurRadius /= 22.5f) <= 0.0f) {
            return;
        }
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        blurProgram.setBlurRadius(2.0f);
        roundedTextureProgram.use();
        RenderSystem.setShaderTexture((int)0, (int)BlurProgram.getTexture());
        roundedTextureProgram.findUniform("Size").set(width, height);
        roundedTextureProgram.findUniform("Radius").set(borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius());
        roundedTextureProgram.findUniform("Smoothness").set(0.01f);
        DrawUtil.drawSetup();
        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();
        float u = x / (float)screenWidth;
        float v = ((float)screenHeight - y - height) / (float)screenHeight;
        float texWidth = width / (float)screenWidth;
        float texHeight = height / (float)screenHeight;
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix4f, x, y, 0.0f).texture(u, v + texHeight).color(color.getRGB());
        builder.vertex(matrix4f, x, y + height, 0.0f).texture(u, v).color(color.getRGB());
        builder.vertex(matrix4f, x + width, y + height, 0.0f).texture(u + texWidth, v).color(color.getRGB());
        builder.vertex(matrix4f, x + width, y, 0.0f).texture(u + texWidth, v + texHeight).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtil.drawEnd();
        RenderSystem.setShaderTexture((int)0, (int)0);
        matrices.pop();
    }

    public static void drawImage(MatrixStack matrices, BufferBuilder builder, double x, double y, double z, double width, double height, ColorRGBA color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        builder.vertex(matrix, (float)x, (float)(y + height), (float)z).texture(0.0f, 1.0f).color(color.getRGB());
        builder.vertex(matrix, (float)(x + width), (float)(y + height), (float)z).texture(1.0f, 1.0f).color(color.getRGB());
        builder.vertex(matrix, (float)(x + width), (float)y, (float)z).texture(1.0f, 0.0f).color(color.getRGB());
        builder.vertex(matrix, (float)x, (float)y, (float)z).texture(0.0f, 0.0f).color(color.getRGB());
    }

    public static void drawImage(MatrixStack matrices, Identifier identifier, double x, double y, double z, double width, double height, ColorRGBA color) {
        RenderSystem.setShaderTexture((int)0, (Identifier)identifier);
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        builder.vertex(matrix, (float)x, (float)(y + height), (float)z).texture(0.0f, 1.0f).color(color.getRGB());
        builder.vertex(matrix, (float)(x + width), (float)(y + height), (float)z).texture(1.0f, 1.0f).color(color.getRGB());
        builder.vertex(matrix, (float)(x + width), (float)y, (float)z).texture(1.0f, 0.0f).color(color.getRGB());
        builder.vertex(matrix, (float)x, (float)y, (float)z).texture(0.0f, 0.0f).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
    }

    public static void drawPlayerHeadWithRoundedShader(MatrixStack matrices, Identifier skinTexture, float x, float y, float size, BorderRadius borderRadius, ColorRGBA color) {
        DrawUtil.drawRoundedTextureWithUV(matrices, skinTexture, x, y, size, size, borderRadius, color, 0.125f, 0.125f, 0.25f, 0.25f);
    }

    private static void drawPlayerHatLayerWithRoundedShader(MatrixStack matrices, Identifier skinTexture, float x, float y, float size, BorderRadius borderRadius, ColorRGBA color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        DrawUtil.drawRoundedTextureWithUV(matrices, skinTexture, x, y, size, size, borderRadius, color, 0.625f, 0.125f, 0.75f, 0.25f);
        RenderSystem.disableBlend();
    }

    public static void drawRoundedTextureWithUV(MatrixStack matrices, Identifier identifier, float x, float y, float width, float height, BorderRadius borderRadius, ColorRGBA color, float u1, float v1, float u2, float v2) {
        if (!DrawUtil.programReady(roundedTextureProgram)) {
            DrawUtil.drawTexture(matrices, identifier, x, y, width, height, color);
            return;
        }
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float smoothness = 0.8f;
        roundedTextureProgram.use();
        RenderSystem.setShaderTexture((int)0, (Identifier)identifier);
        roundedTextureProgram.findUniform("Size").set(width, height);
        roundedTextureProgram.findUniform("Radius").set(borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius());
        roundedTextureProgram.findUniform("Smoothness").set(smoothness);
        DrawUtil.drawSetup();
        float horizontalPadding = -smoothness / 2.0f + smoothness * 2.0f;
        float verticalPadding = smoothness / 2.0f + smoothness;
        float adjustedX = x - horizontalPadding / 2.0f;
        float adjustedY = y - verticalPadding / 2.0f;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix4f, adjustedX, adjustedY, 0.0f).texture(u1, v1).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).texture(u1, v2).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).texture(u2, v2).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).texture(u2, v1).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
        DrawUtil.drawEnd();
        RenderSystem.setShaderTexture((int)0, (int)0);
        matrices.pop();
    }

    public static void drawSetup() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public static void drawEnd() {
        RenderSystem.disableBlend();
    }

    @Generated
    private DrawUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    static {
        buffer = new CustomRenderTarget(false);
    }

    record HeadUV(float u1, float v1, float uSize, float vSize) {
    }
}

