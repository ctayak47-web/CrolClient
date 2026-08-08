package ru.crolclient.implement.screens.menu.components.implement.settings;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import org.joml.Vector4i;
import ru.crolclient.api.feature.module.setting.implement.ThemeSetting;
import ru.crolclient.api.system.font.Fonts;
import ru.crolclient.api.system.shape.ShapeProperties;

import static ru.crolclient.api.system.font.Fonts.Type.*;

public class ThemeComponent extends AbstractSettingComponent {
    private final ThemeSetting setting;

    public ThemeComponent(ThemeSetting setting) {
        super(setting);
        this.setting = setting;
        this.height = 24;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Matrix4f positionMatrix = context
                .getMatrices()
                .peek()
                .getPositionMatrix();

        rectangle.render(ShapeProperties.create(positionMatrix, x + 5, y + 4, width - 11, height - 6)
                .round(6)
                .softness(1)
                .thickness(2)
                .outlineColor(0xFF2D2E41)
                .color(0xF2141724)
                .build()
        );

        Fonts.getSize(14, BOLD).drawString(
                context.getMatrices(),
                setting.getName(),
                x + 9,
                y + 12,
                0xFFD4D6E1
        );

        Vector4i gradientColors = new Vector4i(
                setting.getEndColor(),
                setting.getEndColor(),
                setting.getStartColor(),
                setting.getStartColor()
        );

        rectangle.render(
                ShapeProperties.create(positionMatrix, x + width - 40, y + 8, 30, 10)
                        .round(6)
                        .color(gradientColors)
                        .build()
        );
    }
}