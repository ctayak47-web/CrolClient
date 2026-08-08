package ru.crolclient.implement.screens.menu.components.implement.settings;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import ru.crolclient.api.feature.module.setting.implement.RangeSetting;
import ru.crolclient.api.system.font.Fonts;
import ru.crolclient.api.system.shape.ShapeProperties;
import ru.crolclient.common.util.other.StringUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static ru.crolclient.api.system.font.Fonts.Type.BOLD;
import static ru.crolclient.common.util.math.MathUtil.isHovered;

public class RangeComponent extends AbstractSettingComponent {
    public static final int SLIDER_WIDTH = 45;

    private final RangeSetting setting;

    private boolean draggingFirst;
    private boolean draggingSecond;
    private double firstAnimation;
    private double secondAnimation;

    public RangeComponent(RangeSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Matrix4f positionMatrix = context.getMatrices().peek().getPositionMatrix();

        String wrapped = StringUtil.wrap(setting.getDescription(), 75, 12);
        height = (int) (18 + Fonts.getSize(12).getStringHeight(wrapped) / 3);

        String value = String.format("%.1f - %.1f", setting.getFirstValue(), setting.getSecondValue())
                .replace(",", ".")
                .replace(".0", "");

        Fonts.getSize(12, BOLD).drawString(context.getMatrices(), value,
                x + width - 9 - Fonts.getSize(12).getStringWidth(value), y + 8, 0xFF8187FF);

        changeValues(getDifference(mouseX, positionMatrix));

        Fonts.getSize(14, BOLD).drawString(context.getMatrices(), setting.getName(), x + 9, y + 6, 0xFFD4D6E1);
        Fonts.getSize(12).drawString(context.getMatrices(), wrapped, x + 9, y + 15, 0xFF878894);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY, x + width - SLIDER_WIDTH - 9, y + 13, SLIDER_WIDTH, 4)) {
            float firstPos = getPositionOnSlider(setting.getFirstValue());
            float secondPos = getPositionOnSlider(setting.getSecondValue());

            float clickPos = (float) (mouseX - (x + width - SLIDER_WIDTH - 9));
            if (Math.abs(clickPos - firstPos) < Math.abs(clickPos - secondPos)) {
                draggingFirst = true;
            } else {
                draggingSecond = true;
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingFirst = draggingSecond = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private float getPositionOnSlider(float value) {
        return SLIDER_WIDTH * (value - setting.getMin()) / (setting.getMax() - setting.getMin());
    }

    private float getDifference(int mouseX, Matrix4f positionMatrix) {
        float firstPercentValue = getPositionOnSlider(setting.getFirstValue());
        float secondPercentValue = getPositionOnSlider(setting.getSecondValue());

        firstAnimation = MathHelper.lerp(0.9F, firstAnimation, firstPercentValue);
        secondAnimation = MathHelper.lerp(0.9F, secondAnimation, secondPercentValue);

        rectangle.render(ShapeProperties.create(positionMatrix,
                        x + width - SLIDER_WIDTH - 9, y + 15, SLIDER_WIDTH, 1)
                .color(0x2D2E414D)
                .build()
        );

        rectangle.render(ShapeProperties.create(positionMatrix,
                        x + width - SLIDER_WIDTH - 9 + (float)firstAnimation, y + 15,
                        (float)(secondAnimation - firstAnimation), 1)
                .color(0xFF8187FF, 0xFF8187FF, 0xFF4D5199, 0xFF4D5199)
                .build()
        );

        renderPoint(positionMatrix, firstAnimation);
        renderPoint(positionMatrix, secondAnimation);

        return MathHelper.clamp(mouseX - (x + width - SLIDER_WIDTH - 9), 0, SLIDER_WIDTH);
    }

    private void renderPoint(Matrix4f positionMatrix, double animation) {
        float v = MathHelper.clamp((float) (x + width - SLIDER_WIDTH - 9 + animation), 0, x + width - 4);

        rectangle.render(ShapeProperties.create(positionMatrix, v - 3, y + 12.5F, 5.5, 5.5)
                .round(6)
                .color(0xFF000000)
                .build()
        );

        rectangle.render(ShapeProperties.create(positionMatrix, v - 2, y + 13.5F, 3.5, 3.5)
                .round(4)
                .color(0xFF8187FF)
                .build()
        );
    }

    private void changeValues(float difference) {
        if (!draggingFirst && !draggingSecond) return;

        float range = setting.getMax() - setting.getMin();
        float newValue = (difference / SLIDER_WIDTH) * range + setting.getMin();

        BigDecimal bd = BigDecimal.valueOf(newValue).setScale(1, RoundingMode.HALF_UP);
        float value = bd.floatValue();

        if (draggingFirst) {
            setting.setValue(value, Math.max(value, setting.getSecondValue()));
        } else {
            setting.setValue(Math.min(value, setting.getFirstValue()), value);
        }
    }
}