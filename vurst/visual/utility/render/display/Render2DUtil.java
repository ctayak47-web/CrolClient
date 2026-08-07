
package vurst.visual.utility.render.display;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;
import lombok.Generated;
import net.minecraft.NativeImage;
import net.minecraft.ShaderProgramKeys;
import net.minecraft.ShaderProgramKey;
import net.minecraft.NativeImageBackedTexture;
import net.minecraft.AbstractTexture;
import net.minecraft.BufferRenderer;
import net.minecraft.BufferBuilder;
import net.minecraft.Tessellator;
import net.minecraft.VertexFormats;
import net.minecraft.VertexFormat;
import net.minecraft.DrawContext;
import net.minecraft.MathHelper;
import net.minecraft.MatrixStack;
import net.minecraft.BuiltBuffer;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import vurst.visual.utility.interfaces.IMinecraft;
import vurst.visual.utility.render.display.GaussianFilter;
import vurst.visual.utility.render.display.Texture;
import vurst.visual.utility.render.display.base.Gradient;
import vurst.visual.utility.render.display.base.color.ColorUtil;
import vurst.visual.utility.render.display.shader.DrawUtil;

public final class Render2DUtil
implements IMinecraft {
    public static HashMap<GlowKey, GlowRect> glowCache = new HashMap();
    public static HashMap<Integer, GlowRect> shadowCache1 = new HashMap();
    static final Stack<Rectangle> clipStack = new Stack();
    private static final List<Quad> QUAD = new ArrayList<Quad>();
    private static final ExecutorService GLOW_GENERATOR = Executors.newSingleThreadExecutor();

    public static void endScissor() {
        RenderSystem.disableScissor();
    }

    public static void setRectPoints(BufferBuilder bufferBuilder, Matrix4f matrix, float x, float y, float x1, float y1, Color c1, Color c2, Color c3, Color c4) {
        bufferBuilder.vertex(matrix, x, y1, 0.0f).color(c1.getRGB());
        bufferBuilder.vertex(matrix, x1, y1, 0.0f).color(c2.getRGB());
        bufferBuilder.vertex(matrix, x1, y, 0.0f).color(c3.getRGB());
        bufferBuilder.vertex(matrix, x, y, 0.0f).color(c4.getRGB());
    }

    public static boolean isHovered(double mouseX, double mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX - width <= x && mouseY >= y && mouseY - height <= y;
    }

    public static void drawBlurredShadow(MatrixStack matrices, float x, float y, float width, float height, int blurRadius, Color color) {
    }

    public static void onRender(DrawContext context) {
        MatrixStack matrix = context.getMatrices();
        Matrix4f matrix4f = matrix.peek().getPositionMatrix();
        if (!QUAD.isEmpty()) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            QUAD.forEach(quad -> Render2DUtil.quad(matrix4f, buffer, quad.x, quad.y, quad.width, quad.height, quad.color));
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
            RenderSystem.disableBlend();
            QUAD.clear();
        }
    }

    public static void drawQuad(float x, float y, float width, float height, int color) {
        QUAD.add(new Quad(x, y, width, height, ColorUtil.multAlpha(color, RenderSystem.getShaderColor()[3])));
    }

    public static void quad(Matrix4f matrix4f, BufferBuilder buffer, float x, float y, float width, float height) {
        buffer.vertex(matrix4f, x, y, 0.0f);
        buffer.vertex(matrix4f, x, y + height, 0.0f);
        buffer.vertex(matrix4f, x + width, y + height, 0.0f);
        buffer.vertex(matrix4f, x + width, y, 0.0f);
    }

    public static void quad(Matrix4f matrix4f, BufferBuilder buffer, float x, float y, float width, float height, int color) {
        buffer.vertex(matrix4f, x, y, 0.0f).color(color);
        buffer.vertex(matrix4f, x, y + height, 0.0f).color(color);
        buffer.vertex(matrix4f, x + width, y + height, 0.0f).color(color);
        buffer.vertex(matrix4f, x + width, y, 0.0f).color(color);
    }

    public static void quad(Matrix4f matrix4f, float x, float y, float width, float height, int color) {
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(matrix4f, x, y + height, 0.0f).texture(0.0f, 0.0f).color(color);
        buffer.vertex(matrix4f, x + width, y + height, 0.0f).texture(0.0f, 1.0f).color(color);
        buffer.vertex(matrix4f, x + width, y, 0.0f).texture(1.0f, 1.0f).color(color);
        buffer.vertex(matrix4f, x, y, 0.0f).texture(1.0f, 0.0f).color(color);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
    }

    public static void quadTexture(Matrix4f matrix4f, BufferBuilder buffer, float x, float y, float width, float height, int color) {
        buffer.vertex(matrix4f, x, y + height, 0.0f).texture(0.0f, 0.0f).color(color);
        buffer.vertex(matrix4f, x + width, y + height, 0.0f).texture(0.0f, 1.0f).color(color);
        buffer.vertex(matrix4f, x + width, y, 0.0f).texture(1.0f, 1.0f).color(color);
        buffer.vertex(matrix4f, x, y, 0.0f).texture(1.0f, 0.0f).color(color);
    }

    private static GlowKey findSimilarKey(int width, int height, int blurRadius) {
        GlowKey closest = null;
        int minDiff = Integer.MAX_VALUE;
        for (GlowKey k : glowCache.keySet()) {
            int diff = Math.abs(k.width() - width) + Math.abs(k.height() - height) + Math.abs(k.blurRadius() - blurRadius);
            if (diff >= minDiff) continue;
            minDiff = diff;
            closest = k;
        }
        return closest;
    }

    public static void drawGradientBlurredShadow(MatrixStack matrices, float x, float y, float width, float height, int blurRadius, Gradient gradient) {
        x -= (float)blurRadius;
        y -= (float)blurRadius;
        GlowKey existing = Render2DUtil.findSimilarKey((int)(width += (float)(blurRadius * 2)), (int)(height += (float)(blurRadius * 2)), blurRadius);
        if (existing == null || (int)(Math.abs((float)existing.width() - width) + Math.abs((float)existing.height() - height) + (float)Math.abs(existing.blurRadius() - blurRadius)) >= 5) {
            GlowKey key = new GlowKey((int)width, (int)height, blurRadius);
            mc.execute(() -> {
                BufferedImage original = new BufferedImage(key.width(), key.height(), 2);
                Graphics g = original.getGraphics();
                g.setColor(new Color(-1));
                g.fillRect(blurRadius, blurRadius, key.width() - blurRadius * 2, key.height() - blurRadius * 2);
                g.dispose();
                GaussianFilter op = new GaussianFilter(blurRadius);
                BufferedImage blurred = op.filter(original, null);
                glowCache.put(key, new GlowRect(blurred));
            });
        }
        GlowRect glowRect = glowCache.getOrDefault(existing, null);
        if (glowRect == null) {
            return;
        }
        glowRect.reset();
        DrawUtil.drawTexture(matrices, glowRect.id.getId(), x, y, width, height, gradient);
    }

    public static void registerBufferedImageTexture(Texture i, BufferedImage bi) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write((RenderedImage)bi, "png", baos);
            byte[] bytes = baos.toByteArray();
            Render2DUtil.registerTexture(i, bytes);
        }
        catch (Exception exception) {
            
        }
    }

    public static void registerTexture(Texture i, byte[] content) {
        try {
            ByteBuffer data = BufferUtils.createByteBuffer((int)content.length).put(content);
            data.flip();
            NativeImageBackedTexture tex = new NativeImageBackedTexture(NativeImage.read((ByteBuffer)data));
            mc.execute(() -> mc.getTextureManager().registerTexture(i.getId(), (AbstractTexture)tex));
        }
        catch (Exception exception) {
            
        }
    }

    public static void renderTexture(MatrixStack matrices, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight) {
        double x1 = x0 + width;
        double y1 = y0 + height;
        double z = 0.0;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(matrix, (float)x0, (float)y1, (float)z).texture(u / (float)textureWidth, (v + (float)regionHeight) / (float)textureHeight);
        buffer.vertex(matrix, (float)x1, (float)y1, (float)z).texture((u + (float)regionWidth) / (float)textureWidth, (v + (float)regionHeight) / (float)textureHeight);
        buffer.vertex(matrix, (float)x1, (float)y0, (float)z).texture((u + (float)regionWidth) / (float)textureWidth, v / (float)textureHeight);
        buffer.vertex(matrix, (float)x0, (float)y0, (float)z).texture(u / (float)textureWidth, (v + 0.0f) / (float)textureHeight);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
    }

    public static void renderGradientTexture(MatrixStack matrices, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight, Color c1, Color c2, Color c3, Color c4) {
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        Render2DUtil.renderGradientTextureInternal(buffer, matrices, x0, y0, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight, c1, c2, c3, c4);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
    }

    public static void renderGradientTextureInternal(BufferBuilder buff, MatrixStack matrices, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight, Color c1, Color c2, Color c3, Color c4) {
        double x1 = x0 + width;
        double y1 = y0 + height;
        double z = 0.0;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        buff.vertex(matrix, (float)x0, (float)y1, (float)z).texture(u / (float)textureWidth, (v + (float)regionHeight) / (float)textureHeight).color(c1.getRGB());
        buff.vertex(matrix, (float)x1, (float)y1, (float)z).texture((u + (float)regionWidth) / (float)textureWidth, (v + (float)regionHeight) / (float)textureHeight).color(c2.getRGB());
        buff.vertex(matrix, (float)x1, (float)y0, (float)z).texture((u + (float)regionWidth) / (float)textureWidth, v / (float)textureHeight).color(c3.getRGB());
        buff.vertex(matrix, (float)x0, (float)y0, (float)z).texture(u / (float)textureWidth, (v + 0.0f) / (float)textureHeight).color(c4.getRGB());
    }

    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public static void drawTracerPointer(MatrixStack matrices, float x, float y, float size, float tracerWidth, float downHeight, boolean down, boolean glow, int color) {
    }

    public static void drawNewArrow(MatrixStack matrices, float x, float y, float size, Color color) {
    }

    public static void drawDefaultArrow(MatrixStack matrices, float x, float y, float size, float tracerWidth, float downHeight, boolean down, boolean glow, int color) {
        if (glow) {
            Render2DUtil.drawBlurredShadow(matrices, x - size * tracerWidth, y, x + size * tracerWidth - (x - size * tracerWidth), size, 10, Render2DUtil.injectAlpha(new Color(color), 140));
        }
        matrices.push();
        Render2DUtil.setupRender();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x, y, 0.0f).color(color);
        bufferBuilder.vertex(matrix, x - size * tracerWidth, y + size, 0.0f).color(color);
        bufferBuilder.vertex(matrix, x, y + size - downHeight, 0.0f).color(color);
        bufferBuilder.vertex(matrix, x, y, 0.0f).color(color);
        color = Render2DUtil.darker(new Color(color), 0.8f).getRGB();
        bufferBuilder.vertex(matrix, x, y, 0.0f).color(color);
        bufferBuilder.vertex(matrix, x, y + size - downHeight, 0.0f).color(color);
        bufferBuilder.vertex(matrix, x + size * tracerWidth, y + size, 0.0f).color(color);
        bufferBuilder.vertex(matrix, x, y, 0.0f).color(color);
        if (down) {
            color = Render2DUtil.darker(new Color(color), 0.6f).getRGB();
            bufferBuilder.vertex(matrix, x - size * tracerWidth, y + size, 0.0f).color(color);
            bufferBuilder.vertex(matrix, x + size * tracerWidth, y + size, 0.0f).color(color);
            bufferBuilder.vertex(matrix, x, y + size - downHeight, 0.0f).color(color);
            bufferBuilder.vertex(matrix, x - size * tracerWidth, y + size, 0.0f).color(color);
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        Render2DUtil.endRender();
        matrices.pop();
    }

    public static void endRender() {
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public static float scrollAnimate(float endPoint, float current, float speed) {
        boolean shouldContinueAnimation;
        boolean bl = shouldContinueAnimation = endPoint > current;
        if (speed < 0.0f) {
            speed = 0.0f;
        } else if (speed > 1.0f) {
            speed = 1.0f;
        }
        float dif = Math.max(endPoint, current) - Math.min(endPoint, current);
        float factor = dif * speed;
        return current + (shouldContinueAnimation ? factor : -factor);
    }

    public static Color injectAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), MathHelper.clamp((int)alpha, (int)0, (int)255));
    }

    public static Color TwoColoreffect(Color cl1, Color cl2, double speed, double count) {
        int angle = (int)(((double)System.currentTimeMillis() / speed + count) % 360.0);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        return Render2DUtil.interpolateColorC(cl1, cl2, (float)angle / 360.0f);
    }

    public static Color astolfo(boolean clickgui, int yOffset) {
        float speed = clickgui ? 3500.0f : 3000.0f;
        float hue = System.currentTimeMillis() % (long)((int)speed) + (long)yOffset;
        if (hue > speed) {
            hue -= speed;
        }
        if ((hue /= speed) > 0.5f) {
            hue = 0.5f - (hue - 0.5f);
        }
        return Color.getHSBColor(hue += 0.5f, 0.4f, 1.0f);
    }

    public static Color rainbow(int delay, float saturation, float brightness) {
        double rainbow = Math.ceil((float)(System.currentTimeMillis() + (long)delay) / 16.0f);
        return Color.getHSBColor((float)((rainbow %= 360.0) / 360.0), saturation, brightness);
    }

    public static Color skyRainbow(int speed, int index) {
        int n;
        int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
        return Color.getHSBColor((double)((float)((double)n / 360.0)) < 0.5 ? -((float)((double)angle / 360.0)) : (float)((double)(angle %= 360) / 360.0), 0.5f, 1.0f);
    }

    public static Color getAnalogousColor(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float degree = 0.84f;
        float newHueSubtracted = hsb[0] - degree;
        return new Color(Color.HSBtoRGB(newHueSubtracted, hsb[1], hsb[2]));
    }

    public static Color applyOpacity(Color color, float opacity) {
        opacity = Math.min(1.0f, Math.max(0.0f, opacity));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)((float)color.getAlpha() * opacity));
    }

    public static int applyOpacity(int color_int, float opacity) {
        opacity = Math.min(1.0f, Math.max(0.0f, opacity));
        Color color = new Color(color_int);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)((float)color.getAlpha() * opacity)).getRGB();
    }

    public static Color darker(Color color, float factor) {
        return new Color(Math.max((int)((float)color.getRed() * factor), 0), Math.max((int)((float)color.getGreen() * factor), 0), Math.max((int)((float)color.getBlue() * factor), 0), color.getAlpha());
    }

    public static Color rainbow(int speed, int index, float saturation, float brightness, float opacity) {
        int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
        float hue = (float)angle / 360.0f;
        Color color = new Color(Color.HSBtoRGB(hue, saturation, brightness));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, (int)(opacity * 255.0f))));
    }

    public static Color interpolateColorsBackAndForth(int speed, int index, Color start, Color end, boolean trueColor) {
        int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        return trueColor ? Render2DUtil.interpolateColorHue(start, end, (float)angle / 360.0f) : Render2DUtil.interpolateColorC(start, end, (float)angle / 360.0f);
    }

    public static Color interpolateColorC(Color color1, Color color2, float amount) {
        amount = Math.min(1.0f, Math.max(0.0f, amount));
        return new Color(Render2DUtil.interpolateInt(color1.getRed(), color2.getRed(), amount), Render2DUtil.interpolateInt(color1.getGreen(), color2.getGreen(), amount), Render2DUtil.interpolateInt(color1.getBlue(), color2.getBlue(), amount), Render2DUtil.interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static Color interpolateColorHue(Color color1, Color color2, float amount) {
        amount = Math.min(1.0f, Math.max(0.0f, amount));
        float[] color1HSB = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
        float[] color2HSB = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);
        Color resultColor = Color.getHSBColor(Render2DUtil.interpolateFloat(color1HSB[0], color2HSB[0], amount), Render2DUtil.interpolateFloat(color1HSB[1], color2HSB[1], amount), Render2DUtil.interpolateFloat(color1HSB[2], color2HSB[2], amount));
        return new Color(resultColor.getRed(), resultColor.getGreen(), resultColor.getBlue(), Render2DUtil.interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static double interpolate(double oldValue, double newValue, double interpolationValue) {
        return oldValue + (newValue - oldValue) * interpolationValue;
    }

    public static float interpolateFloat(float oldValue, float newValue, double interpolationValue) {
        return (float)Render2DUtil.interpolate(oldValue, newValue, (float)interpolationValue);
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return (int)Render2DUtil.interpolate(oldValue, newValue, (float)interpolationValue);
    }

    public static BufferBuilder preShaderDraw(MatrixStack matrices, float x, float y, float width, float height) {
        Render2DUtil.setupRender();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        Render2DUtil.setRectanglePoints(buffer, matrix, x, y, x + width, y + height);
        return buffer;
    }

    public static void setRectanglePoints(BufferBuilder buffer, Matrix4f matrix, float x, float y, float x1, float y1) {
        buffer.vertex(matrix, x, y, 0.0f);
        buffer.vertex(matrix, x, y1, 0.0f);
        buffer.vertex(matrix, x1, y1, 0.0f);
        buffer.vertex(matrix, x1, y, 0.0f);
    }

    public static boolean isDark(Color color) {
        return Render2DUtil.isDark((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f);
    }

    public static boolean isDark(float r, float g, float b) {
        return Render2DUtil.colorDistance(r, g, b, 0.0f, 0.0f, 0.0f) < Render2DUtil.colorDistance(r, g, b, 1.0f, 1.0f, 1.0f);
    }

    public static float colorDistance(float r1, float g1, float b1, float r2, float g2, float b2) {
        float a = r2 - r1;
        float b = g2 - g1;
        float c = b2 - b1;
        return (float)Math.sqrt(a * a + b * b + c * c);
    }

    @NotNull
    public static Color getColor(@NotNull Color start, @NotNull Color end, float progress, boolean smooth) {
        if (!smooth) {
            return (double)progress >= 0.95 ? end : start;
        }
        int rDiff = end.getRed() - start.getRed();
        int gDiff = end.getGreen() - start.getGreen();
        int bDiff = end.getBlue() - start.getBlue();
        int aDiff = end.getAlpha() - start.getAlpha();
        return new Color(Render2DUtil.fixColorValue(start.getRed() + (int)((float)rDiff * progress)), Render2DUtil.fixColorValue(start.getGreen() + (int)((float)gDiff * progress)), Render2DUtil.fixColorValue(start.getBlue() + (int)((float)bDiff * progress)), Render2DUtil.fixColorValue(start.getAlpha() + (int)((float)aDiff * progress)));
    }

    private static int fixColorValue(int colorVal) {
        return colorVal > 255 ? 255 : Math.max(colorVal, 0);
    }

    public static void endBuilding(BufferBuilder bb) {
        BuiltBuffer builtBuffer = bb.endNullable();
        if (builtBuffer != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builtBuffer);
        }
    }

    @Generated
    private Render2DUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public record Quad(float x, float y, float width, float height, int color) {
    }

    record GlowKey(int width, int height, int blurRadius) {
    }

    public static class GlowRect {
        public final Texture id = new Texture("shadow_" + RandomStringUtils.randomAlphanumeric((int)8));
        private int ticksSinceUse = 0;

        public GlowRect(BufferedImage img) {
            Render2DUtil.registerBufferedImageTexture(this.id, img);
        }

        public void reset() {
            this.ticksSinceUse = 0;
        }

        public boolean tick() {
            return ++this.ticksSinceUse > 300;
        }

        public void destroy() {
            IMinecraft.mc.getTextureManager().destroyTexture(this.id.getId());
        }
    }

    public record Rectangle(float x, float y, float x1, float y1) {
        public boolean contains(double x, double y) {
            return x >= (double)this.x && x <= (double)this.x1 && y >= (double)this.y && y <= (double)this.y1;
        }
    }

    public static class GlowRect2 {
        public final int id;
        private int ticksSinceUse = 0;

        public GlowRect2(int id) {
            this.id = id;
        }

        public void reset() {
            this.ticksSinceUse = 0;
        }

        public boolean tick() {
            return ++this.ticksSinceUse > 4;
        }
    }
}

