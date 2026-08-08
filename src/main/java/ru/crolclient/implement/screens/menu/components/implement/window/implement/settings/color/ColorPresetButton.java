package ru.crolclient.implement.screens.menu.components.implement.window.implement.settings.color;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import ru.crolclient.api.feature.module.setting.implement.ColorSetting;
import ru.crolclient.api.system.shape.ShapeProperties;
import ru.crolclient.common.util.math.MathUtil;
import ru.crolclient.implement.screens.menu.components.AbstractComponent;

@RequiredArgsConstructor
public class ColorPresetButton extends AbstractComponent {
    private final ColorSetting setting;
    private final int color;

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Matrix4f positionMatrix = context
                .getMatrices()
                .peek()
                .getPositionMatrix();

        rectangle.render(ShapeProperties.create(positionMatrix, x, y, 8, 8)
                .round(4)
                .color(color)
                .build()
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, x, y, 8, 8) && button == 0) {
            setting.setColor(color);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
