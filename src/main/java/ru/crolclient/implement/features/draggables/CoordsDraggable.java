package ru.crolclient.implement.features.draggables;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import ru.crolclient.api.system.font.FontRenderer;
import ru.crolclient.api.system.shape.implement.Image;
import ru.crolclient.common.QuickImports;
import ru.crolclient.core.Extra;
import ru.crolclient.api.feature.draggable.AbstractDraggable;
import ru.crolclient.api.system.font.Fonts;
import ru.crolclient.api.system.shape.ShapeProperties;
import ru.crolclient.implement.features.modules.render.InterfaceModule;

import static ru.crolclient.api.system.font.Fonts.Type.*;

public class CoordsDraggable extends AbstractDraggable {

    public CoordsDraggable() {
        super("Coords", 3, 492, 72, 10);
    }

    @Override
    public boolean visible() {
        InterfaceModule interfaceModule = (InterfaceModule) Extra.getInstance().getModuleProvider().module("Interface");
        return interfaceModule != null && interfaceModule.isState() && interfaceModule.getInterfaceSettings().isSelected("Coords");
    }

    @Override
    public void drawDraggable(DrawContext context) {
        Matrix4f positionMatrix = context
                .getMatrices()
                .peek()
                .getPositionMatrix();

        int screenHeight = mc.getWindow().getScaledHeight();
        int bottomMargin = 3;
        int adjustedY = screenHeight - getHeight() - bottomMargin;

        rectangle.render(ShapeProperties.create(positionMatrix, getX(), adjustedY, getWidth(), getHeight())
                .round(6)
                .softness(1)
                .thickness(2)
                .outlineColor(0xFF2D2E41)
                .color(0xF2141724)
                .build()
        );

        rectangle.render(ShapeProperties.create(positionMatrix, getX() + 11, adjustedY + 3, 0.8, 4)
                .color(0xFF2D2E41)
                .build()
        );

        Image image = QuickImports.image.setMatrixStack(context.getMatrices());
        image.setTexture("textures/world.png").render(ShapeProperties.create(positionMatrix, getX() + 3.5, adjustedY + 2.5, 5, 5)
                .build()
        );

        FontRenderer fontRenderer = Fonts.getSize(12, BOLD);
        float currentX = getX() + 13;
        int gray = 0xFF808080;

        int xCoord = (int) mc.player.getX();
        int yCoord = (int) mc.player.getY();
        int zCoord = (int) mc.player.getZ();

        fontRenderer.drawString(context.getMatrices(), "x:", currentX + 0.5, adjustedY + 4.5f, gray);
        currentX += fontRenderer.getStringWidth("x:");
        fontRenderer.drawString(context.getMatrices(), String.valueOf(xCoord), currentX + 1.5, adjustedY + 4.5f, -1);
        currentX += fontRenderer.getStringWidth(String.valueOf(xCoord) + "  ");

        fontRenderer.drawString(context.getMatrices(), "y:", currentX + 2, adjustedY + 4.5f, gray);
        currentX += fontRenderer.getStringWidth("y:");
        fontRenderer.drawString(context.getMatrices(), String.valueOf(yCoord), currentX + 1.5, adjustedY + 4.5f, -1);
        currentX += fontRenderer.getStringWidth(String.valueOf(yCoord) + "  ");

        fontRenderer.drawString(context.getMatrices(), "z:", currentX + 2, adjustedY + 4.5f, gray);
        currentX += fontRenderer.getStringWidth("z:");
        fontRenderer.drawString(context.getMatrices(), String.valueOf(zCoord), currentX + 1.5, adjustedY + 4.5f, -1);

        setWidth((int) (currentX + fontRenderer.getStringWidth(String.valueOf(zCoord)) - getX() + 5));
    }
}