package ru.crolclient.api.system.shape;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class ShapeRenderer {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void drawRect(MatrixStack matrices, float x, float y, float width, float height, int color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float a = (color >> 24 & 0xFF) / 255.0F;
        float r = (color >> 16 & 0xFF) / 255.0F;
        float g = (color >> 8 & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(net.minecraft.client.gl.ShaderProgramKeys.POSITION_COLOR);

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x, y + height, 0.0F).color(r, g, b, a);
        bufferBuilder.vertex(matrix, x + width, y + height, 0.0F).color(r, g, b, a);
        bufferBuilder.vertex(matrix, x + width, y, 0.0F).color(r, g, b, a);
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(r, g, b, a);

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static void drawRectOutline(MatrixStack matrices, float x, float y, float width, float height, float thickness, int color) {
        drawRect(matrices, x, y, width, thickness, color);
        drawRect(matrices, x, y + height - thickness, width, thickness, color);
        drawRect(matrices, x, y, thickness, height, color);
        drawRect(matrices, x + width - thickness, y, thickness, height, color);
    }

    public static double[] project(double x, double y, double z) {
        if (mc.gameRenderer == null || mc.gameRenderer.getCamera() == null) return null;

        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();
        x -= camPos.x;
        y -= camPos.y;
        z -= camPos.z;

        Matrix4f projection = RenderSystem.getProjectionMatrix();
        Matrix4f modelView = RenderSystem.getModelViewMatrix();
        Vector4f vector = new Vector4f((float) x, (float) y, (float) z, 1.0f);
        vector.mul(modelView).mul(projection);

        if (vector.w <= 0.0f) return null;
        vector.mul(1.0f / vector.w);

        double screenX = (vector.x * 0.5f + 0.5f) * mc.getWindow().getScaledWidth();
        double screenY = (0.5f - vector.y * 0.5f) * mc.getWindow().getScaledHeight();
        return new double[]{screenX, screenY};
    }

    public static void drawRoundedRect(MatrixStack matrices, float x, float y, float width, float height, float radius, int color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float a = (color >> 24 & 0xFF) / 255.0F;
        float r = (color >> 16 & 0xFF) / 255.0F;
        float g = (color >> 8 & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        radius = Math.min(radius, Math.min(width, height) / 2);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(net.minecraft.client.gl.ShaderProgramKeys.POSITION_COLOR);

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x + radius, y, 0).color(r, g, b, a);
        bufferBuilder.vertex(matrix, x + width - radius, y, 0).color(r, g, b, a);
        bufferBuilder.vertex(matrix, x + width - radius, y + height, 0).color(r, g, b, a);
        bufferBuilder.vertex(matrix, x + radius, y + height, 0).color(r, g, b, a);
        bufferBuilder.vertex(matrix, x, y + radius, 0).color(r, g, b, a);
        bufferBuilder.vertex(matrix, x + radius, y + radius, 0).color(r, g, b, a);
        bufferBuilder.vertex(matrix, x + radius, y + height - radius, 0).color(r, g, b, a);
        bufferBuilder.vertex(matrix, x, y + height - radius, 0).color(r, g, b, a);
        bufferBuilder.vertex(matrix, x + width - radius, y + radius, 0).color(r, g, b, a);
        bufferBuilder.vertex(matrix, x + width, y + radius, 0).color(r, g, b, a);
        bufferBuilder.vertex(matrix, x + width, y + height - radius, 0).color(r, g, b, a);
        bufferBuilder.vertex(matrix, x + width - radius, y + height - radius, 0).color(r, g, b, a);

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        drawCorners(matrices, x, y, width, height, radius, r, g, b, a);
        RenderSystem.disableBlend();
    }

    private static void drawCorners(MatrixStack matrices, float x, float y, float width, float height, float radius, float r, float g, float b, float a) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        drawCorner(bufferBuilder, matrix, x + radius, y + radius, radius, 180, 270, r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        drawCorner(bufferBuilder, matrix, x + width - radius, y + radius, radius, 270, 360, r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        drawCorner(bufferBuilder, matrix, x + width - radius, y + height - radius, radius, 0, 90, r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        drawCorner(bufferBuilder, matrix, x + radius, y + height - radius, radius, 90, 180, r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    private static void drawCorner(BufferBuilder bufferBuilder, Matrix4f matrix, float x, float y, float radius, int startAngle, int endAngle, float r, float g, float b, float a) {
        bufferBuilder.vertex(matrix, x, y, 0).color(r, g, b, a);
        for (int i = startAngle; i <= endAngle; i += 5) {
            float angle = (float) Math.toRadians(i);
            bufferBuilder.vertex(matrix, x + (float) Math.cos(angle) * radius, y + (float) Math.sin(angle) * radius, 0).color(r, g, b, a);
        }
    }

    public static void drawRoundedRectOutline(MatrixStack matrices, float x, float y, float width, float height, float radius, float thickness, int color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float a = (color >> 24 & 0xFF) / 255.0F;
        float r = (color >> 16 & 0xFF) / 255.0F;
        float g = (color >> 8 & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        radius = Math.min(radius, Math.min(width, height) / 2);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(net.minecraft.client.gl.ShaderProgramKeys.POSITION_COLOR);

        drawRect(matrices, x + radius, y, width - 2 * radius, thickness, color);
        drawRect(matrices, x + radius, y + height - thickness, width - 2 * radius, thickness, color);
        drawRect(matrices, x, y + radius, thickness, height - 2 * radius, color);
        drawRect(matrices, x + width - thickness, y + radius, thickness, height - 2 * radius, color);

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        drawCornerOutline(bufferBuilder, matrix, x + radius, y + radius, radius, thickness, 180, 270, r, g, b, a);
        drawCornerOutline(bufferBuilder, matrix, x + width - radius, y + radius, radius, thickness, 270, 360, r, g, b, a);
        drawCornerOutline(bufferBuilder, matrix, x + width - radius, y + height - radius, radius, thickness, 0, 90, r, g, b, a);
        drawCornerOutline(bufferBuilder, matrix, x + radius, y + height - radius, radius, thickness, 90, 180, r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    private static void drawCornerOutline(BufferBuilder bufferBuilder, Matrix4f matrix, float x, float y, float radius, float thickness, int startAngle, int endAngle, float r, float g, float b, float a) {
        for (int i = startAngle; i <= endAngle; i += 5) {
            float angle = (float) Math.toRadians(i);
            float outerX = x + (float) Math.cos(angle) * radius;
            float outerY = y + (float) Math.sin(angle) * radius;
            float innerX = x + (float) Math.cos(angle) * (radius - thickness);
            float innerY = y + (float) Math.sin(angle) * (radius - thickness);
            bufferBuilder.vertex(matrix, outerX, outerY, 0).color(r, g, b, a);
            bufferBuilder.vertex(matrix, innerX, innerY, 0).color(r, g, b, a);
        }
    }
}