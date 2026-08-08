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

public class SpeedDraggable extends AbstractDraggable {

    public SpeedDraggable() {
        super("Speed", 3, 480, 72, 10);
    }

    @Override
    public boolean visible() {
        InterfaceModule interfaceModule = (InterfaceModule) Extra.getInstance().getModuleProvider().module("Interface");
        return interfaceModule != null && interfaceModule.isState() && interfaceModule.getInterfaceSettings().isSelected("Speed");
    }

    @Override
    public void drawDraggable(DrawContext context) {
        Matrix4f positionMatrix = context
                .getMatrices()
                .peek()
                .getPositionMatrix();

        int screenHeight = mc.getWindow().getScaledHeight();

        int bottomMargin = 15;

        int adjustedY = screenHeight - getHeight() - bottomMargin;

        int fixedX = 3;

        rectangle.render(ShapeProperties.create(positionMatrix, fixedX, adjustedY, getWidth(), getHeight())
                .round(6)
                .softness(1)
                .thickness(2)
                .outlineColor(0xFF2D2E41)
                .color(0xF2141724)
                .build()
        );

        rectangle.render(ShapeProperties.create(positionMatrix, fixedX + 11, adjustedY + 3, 0.8, 4)
                .color(0xFF2D2E41)
                .build()
        );

        double deltaX = mc.player.getX() - mc.player.prevX;
        double deltaY = mc.player.getY() - mc.player.prevY;
        double deltaZ = mc.player.getZ() - mc.player.prevZ;
        double speed = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) * 20;

        String speedLabel = "bps: ";
        String speedValue = String.format("%.2f", speed);

        Image image = QuickImports.image.setMatrixStack(context.getMatrices());
        image.setTexture("textures/running.png").render(ShapeProperties.create(positionMatrix, fixedX + 3.0, adjustedY + 2, 6, 6)
                .build()
        );

        FontRenderer fontRenderer = Fonts.getSize(12, BOLD);
        fontRenderer.drawString(context.getMatrices(), speedLabel, fixedX + 13.5, adjustedY + 4.5, 0xFF808080);
        fontRenderer.drawString(context.getMatrices(), speedValue, fixedX + 13.5 + fontRenderer.getStringWidth(speedLabel), adjustedY + 4.5, -1);

        setWidth((int) (fontRenderer.getStringWidth(speedLabel + speedValue) + 17));
    }
}
