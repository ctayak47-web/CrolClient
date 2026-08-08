package ru.crolclient.implement.features.draggables;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import ru.crolclient.api.feature.draggable.AbstractDraggable;
import ru.crolclient.api.system.font.FontRenderer;
import ru.crolclient.api.system.font.Fonts;
import ru.crolclient.api.system.shape.ShapeProperties;
import ru.crolclient.api.system.shape.implement.Image;
import ru.crolclient.common.QuickImports;
import ru.crolclient.common.util.other.PingUtil;
import ru.crolclient.core.Extra;
import ru.crolclient.implement.features.modules.render.InterfaceModule;

import static ru.crolclient.api.system.font.Fonts.Type.BOLD;

public class WatermarkDraggable extends AbstractDraggable {

    public WatermarkDraggable() {
        super("Watermark", 6, 6, 92, 16);
    }

    @Override
    public boolean visible() {
        InterfaceModule interfaceModule = (InterfaceModule) Extra.getInstance().getModuleProvider().module("Interface");
        if (interfaceModule != null && interfaceModule.isState()) {
            return interfaceModule.getInterfaceSettings().isSelected("Watermark");
        }
        return false;
    }

    @Override
    public void drawDraggable(DrawContext context) {
        Matrix4f positionMatrix = context
                .getMatrices()
                .peek()
                .getPositionMatrix();

        InterfaceModule interfaceModule = (InterfaceModule) Extra.getInstance().getModuleProvider().module("Interface");
        float radius = interfaceModule.getCornerRadius().getValue();

        setHeight(14);

        rectangle.render(ShapeProperties.create(positionMatrix, getX(), getY(), getWidth(), getHeight())
                .round(radius)
                .softness(1)
                .thickness(2)
                .outlineColor(0xFF2D2E41)
                .color(0xF2141724)
                .build()
        );

        rectangle.render(ShapeProperties.create(positionMatrix, getX(), getY(), getWidth(), getHeight())
                .round(radius)
                .softness(1)
                .thickness(2)
                .outlineColor(0xFF2D2E41)
                .color(0xF2181A2A)
                .build()
        );

        Image image = QuickImports.image.setMatrixStack(context.getMatrices());
        image.setTexture("textures/icon_logo.png").render(ShapeProperties.create(positionMatrix, getX() + 3, getY() + 3.8, 6.5, 6.5)
                .build()
        );

        FontRenderer font = Fonts.getSize(14, BOLD);
        //String name = Extra.getInstance().getClientInfoProvider().clientName();
        String name = mc.player.getName().getString();

        String ms;
        if (mc.isInSingleplayer() || PingUtil.getPing() == 0) {
            ms = "localhost";
        } else {
            ms = PingUtil.getPing() + " ms";
        }

        String fps = mc.getCurrentFps() + " fps";

        image.setTexture("textures/ping.png").render(ShapeProperties.create(positionMatrix, getX() + font.getStringWidth(name) + 14, getY() + 2.5, 9, 9)
                .build()
        );

        image.setTexture("textures/frame.png").render(ShapeProperties.create(positionMatrix, getX() + font.getStringWidth(name) + 26.5 + font.getStringWidth(ms), getY() + 4, 6, 6)
                .build()
        );

        font.drawGradientString(context.getMatrices(), name, getX() + 11, getY() + 5.5, 0xFF8187FF, 0xFF4D5199);
        font.drawString(context.getMatrices(), ms, getX() + font.getStringWidth(name) + 24, getY() + 5.5, -1);
        font.drawString(context.getMatrices(), fps, getX() + font.getStringWidth(name) + 26 + font.getStringWidth(ms) + 8, getY() + 5.5, -1);

        setWidth((int) (font.getStringWidth(name + ms + fps) + 38));
    }
}
