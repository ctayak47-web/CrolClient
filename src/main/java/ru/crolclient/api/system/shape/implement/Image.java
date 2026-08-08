package ru.crolclient.api.system.shape.implement;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import ru.crolclient.common.QuickImports;
import ru.crolclient.api.system.shape.Shape;
import ru.crolclient.api.system.shape.ShapeProperties;
import ru.crolclient.api.system.shader.ShadersPool;

@Setter
@Accessors(chain = true)
public class Image implements Shape, QuickImports {
    private MatrixStack matrixStack;
    private String texture;

    @Override
    public void render(ShapeProperties shapeProperties) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, 1);

        if (shapeProperties.isBloom()) {
            ShadersPool.BLOOM_SHADER.use();
            RenderSystem.blendFunc(770, 1);
        }

        RenderSystem.setShaderTexture(0, Identifier.of("crolclient/" + texture));

        float width = shapeProperties.getWidth();
        float x = shapeProperties.getX() + width;
        float y = shapeProperties.getY();

        matrixStack.push();
        matrixStack.translate(x, y, 0.0F);
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90));
        matrixStack.translate(-x, -y, 0.0F);

        drawEngine.quad(
                matrixStack.peek().getPositionMatrix(),
                x,
                y,
                shapeProperties.getHeight(),
                width,
                shapeProperties.getColor().x
        );

        matrixStack.pop();

        if (shapeProperties.isBloom()) {
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }

        RenderSystem.disableBlend();
    }
}