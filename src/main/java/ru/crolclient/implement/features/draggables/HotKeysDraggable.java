package ru.crolclient.implement.features.draggables;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import org.joml.Matrix4f;
import ru.crolclient.api.feature.draggable.AbstractDraggable;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.system.font.Fonts;
import ru.crolclient.api.system.shape.ShapeProperties;
import ru.crolclient.api.system.shape.implement.Image;
import ru.crolclient.common.QuickImports;
import ru.crolclient.core.Extra;
import ru.crolclient.implement.features.modules.render.InterfaceModule;

import java.util.List;

import static ru.crolclient.api.system.font.Fonts.Type.BOLD;

public class HotKeysDraggable extends AbstractDraggable {
    private List<Module> key;
    private float animatedHeight = 18;
    private long lastUpdateTime;
    private static final float ANIMATION_SPEED = 0.02f;

    public HotKeysDraggable() {
        super("HotKeys", 420, 10, 80, 18);
        lastUpdateTime = System.currentTimeMillis();
    }

    @Override
    public boolean visible() {
        InterfaceModule interfaceModule = (InterfaceModule) Extra.getInstance().getModuleProvider().module("Interface");
        boolean isEmpty = key.isEmpty();
        return interfaceModule != null
                && interfaceModule.isState()
                && interfaceModule.getInterfaceSettings().isSelected("HotKeys")
                && (interfaceModule.getShowEmpty().isValue() || !isEmpty || mc.currentScreen instanceof ChatScreen);
    }

    @Override
    public void tick(float delta) {
        key = Extra.getInstance().getModuleProvider()
                .getModules()
                .stream()
                .filter(module -> module.isState() && module.getKey() != -1 && module.getCategory() != ModuleCategory.RENDER)
                .toList();

        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        float targetHeight = 20 + key.size() * 10;
        animatedHeight += (targetHeight - animatedHeight) * (ANIMATION_SPEED * deltaTime);
        setHeight(Math.round(animatedHeight));

        super.tick(delta);
    }

    @Override
    public void drawDraggable(DrawContext context) {
        Matrix4f positionMatrix = context
                .getMatrices()
                .peek()
                .getPositionMatrix();

        InterfaceModule interfaceModule = (InterfaceModule) Extra.getInstance().getModuleProvider().module("Interface");
        float radius = interfaceModule.getCornerRadius().getValue();

        rectangle.render(ShapeProperties.create(positionMatrix, getX(), getY(), getWidth(), getHeight())
                .round(radius)
                .softness(1)
                .thickness(2)
                .outlineColor(0xFF2D2E41)
                .color(0xCC141724)
                .build()
        );

        rectangle.render(ShapeProperties.create(positionMatrix, getX(), getY(), getWidth(), 16)
                .round(radius)
                .softness(1)
                .thickness(2)
                .outlineColor(0xFF2D2E41)
                .color(0xF2181A2A)
                .build()
        );

        Image image = QuickImports.image.setMatrixStack(context.getMatrices());

        image.setTexture("textures/keyboard.png").render(ShapeProperties.create(positionMatrix, getX() + getWidth() - 16, getY() + 5, 8, 8)
                .build()
        );

        Fonts.getSize(13, BOLD).drawString(context.getMatrices(), getName(), getX() + 8, getY() + 7, 0xFFD4D6E1);

        int offset = getY() + 21;
        for (Module module : key) {
            Fonts.getSize(11).drawString(context.getMatrices(), module.getName(), getX() + 8, offset, 0xFFD4D6E1);

            String keyName = String.valueOf((char)module.getKey()).toUpperCase();
            float keyWidth = Fonts.getSize(11).getStringWidth(keyName);
            Fonts.getSize(11).drawString(
                    context.getMatrices(),
                    keyName,
                    getX() + getWidth() - 14 + (8 - keyWidth) / 2,
                    offset + 1,
                    0xFFFFFFFF
            );

            offset += 10;
        }
    }
}