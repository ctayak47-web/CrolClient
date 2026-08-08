package ru.crolclient.api.system.shape.implement;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.gl.ShaderProgramKeys;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.joml.Vector4i;
import ru.crolclient.common.QuickImports;
import ru.crolclient.api.system.shape.Shape;
import ru.crolclient.api.system.shape.ShapeProperties;

import static ru.crolclient.api.system.shader.ShadersPool.ROUNDED_SHADER;
import static ru.crolclient.common.util.math.MathUtil.colorToArray;

public class Rectangle implements Shape, QuickImports {

    @Override
    public void render(ShapeProperties shapeProperties) {
        Vector4i gradientColor = shapeProperties.getColor();

        if (ROUNDED_SHADER == null) {
            drawFallback(shapeProperties, gradientColor);
            return;
        }

        ROUNDED_SHADER.use();

        if (ROUNDED_SHADER.size == null) {
            drawFallback(shapeProperties, gradientColor);
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        float softness = shapeProperties.getSoftness();
        Matrix4f matrix = shapeProperties.getMatrix4f();
        float x = shapeProperties.getX() - softness / 2;
        float y = shapeProperties.getY() - softness / 2;
        float width = shapeProperties.getWidth() + softness;
        float height = shapeProperties.getHeight() + softness;

        int scale = mc.options.getGuiScale()
                .getValue();

        ROUNDED_SHADER.size.set(shapeProperties.getWidth() * scale, shapeProperties.getHeight() * scale);
        ROUNDED_SHADER.location.set(shapeProperties.getX() * scale, mc.getWindow().getHeight() - (shapeProperties.getHeight() * scale) - (shapeProperties.getY() * scale));

        Vector4f round = shapeProperties.getRound();

        ROUNDED_SHADER.radius.set(
                round.x,
                round.y,
                round.z,
                round.w
        );

        ROUNDED_SHADER.softness.set(softness);
        ROUNDED_SHADER.thickness.set(shapeProperties.getThickness());

        ROUNDED_SHADER.color1.set(colorToArray(gradientColor.x));
        ROUNDED_SHADER.color2.set(colorToArray(gradientColor.y));
        ROUNDED_SHADER.color3.set(colorToArray(gradientColor.z));
        ROUNDED_SHADER.color4.set(colorToArray(gradientColor.w));

        ROUNDED_SHADER.outlineColor.set(colorToArray(shapeProperties.getOutlineColor()));

        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        buffer.vertex(matrix, x, y, 0);
        buffer.vertex(matrix, x, y + height, 0);
        buffer.vertex(matrix, x + width, y + height, 0);
        buffer.vertex(matrix, x + width, y, 0);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.disableBlend();
    }

    private void drawFallback(ShapeProperties shapeProperties, Vector4i gradientColor) {
        float x = shapeProperties.getX();
        float y = shapeProperties.getY();
        float w = shapeProperties.getWidth();
        float h = shapeProperties.getHeight();
        int color = gradientColor.x;

        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(x, y, 0.0F).color(r, g, b, a);
        buffer.vertex(x, y + h, 0.0F).color(r, g, b, a);
        buffer.vertex(x + w, y + h, 0.0F).color(r, g, b, a);
        buffer.vertex(x + w, y, 0.0F).color(r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
    }
}
