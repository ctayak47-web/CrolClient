package ru.crolclient.implement.screens.menu.components.implement.window.implement.module;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import ru.crolclient.api.system.font.FontRenderer;
import ru.crolclient.api.system.font.Fonts;
import ru.crolclient.api.system.shape.ShapeProperties;
import ru.crolclient.common.util.math.MathUtil;
import ru.crolclient.implement.screens.menu.components.implement.window.AbstractWindow;

public class ThemeWindow extends AbstractWindow {

    @Override
    public void drawWindow(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrices = context.getMatrices();
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

        renderImage("textures/about.png",
                positionMatrix,
                x,
                y,
                width,
                height
        );

        FontRenderer fontBold = Fonts.getSize(14, Fonts.Type.BOLD);
        fontBold.drawString(matrices, "Theme Settings", x + 13, y + 16, 0xFFD4D6E1);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        draggable(MathUtil.isHovered(mouseX, mouseY, x, y, width, 40));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderImage(String texturePath, Matrix4f positionMatrix, float x, float y, float width, float height) {
        image.setTexture(texturePath).render(
                ShapeProperties.create(positionMatrix, x, y, width, height)
                        .build()
        );
    }
}