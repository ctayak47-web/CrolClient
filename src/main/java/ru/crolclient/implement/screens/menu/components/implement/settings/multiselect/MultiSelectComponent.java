package ru.crolclient.implement.screens.menu.components.implement.settings.multiselect;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import ru.crolclient.api.system.animation.Animation;
import ru.crolclient.api.system.animation.Direction;
import ru.crolclient.api.system.animation.implement.DecelerateAnimation;
import ru.crolclient.common.util.other.StringUtil;
import ru.crolclient.common.util.render.Stencil;
import ru.crolclient.core.Extra;
import ru.crolclient.api.feature.module.setting.implement.MultiSelectSetting;
import ru.crolclient.api.system.font.Fonts;
import ru.crolclient.api.system.shape.ShapeProperties;
import ru.crolclient.common.util.math.MathUtil;
import ru.crolclient.common.util.render.ScissorManager;
import ru.crolclient.implement.screens.menu.components.AbstractComponent;
import ru.crolclient.implement.screens.menu.components.implement.settings.AbstractSettingComponent;
import ru.crolclient.implement.screens.menu.components.implement.settings.select.SelectedButton;

import java.util.ArrayList;
import java.util.List;

import static ru.crolclient.api.system.font.Fonts.Type.BOLD;

public class MultiSelectComponent extends AbstractSettingComponent {
    private final List<MultiSelectedButton> multiSelectedButtons = new ArrayList<>();

    private final MultiSelectSetting setting;
    private boolean open;

    private float dropdownListX,
            dropDownListY,
            dropDownListWidth,
            dropDownListHeight;

    private final Animation alphaAnimation = new DecelerateAnimation()
            .setMs(300)
            .setValue(255);

    public MultiSelectComponent(MultiSelectSetting setting) {
        super(setting);
        this.setting = setting;

        alphaAnimation.setDirection(Animation.Direction.BACKWARDS);

        for (String s : setting.getList()) {
            multiSelectedButtons.add(new MultiSelectedButton(setting, s));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrices = context.getMatrices();
        Matrix4f positionMatrix = context
                .getMatrices()
                .peek()
                .getPositionMatrix();

        String wrapped = StringUtil.wrap(setting.getDescription(), 45, 12);
        height = (int) (18 + Fonts.getSize(12).getStringHeight(wrapped) / 3);

        List<String> fullSettingsList = setting.getList();

        this.dropdownListX = x + width - 75;
        this.dropDownListY = y + 20;
        this.dropDownListWidth = 66;
        this.dropDownListHeight = fullSettingsList.size() * 12 + 1.5F;

        alphaAnimation.setDirection(open
                ? Animation.Direction.FORWARDS
                : Animation.Direction.BACKWARDS
        );

        renderSelected(positionMatrix, matrices);
        renderSelectList(context, mouseX, mouseY, delta, positionMatrix);

        Fonts.getSize(14, BOLD).drawString(matrices, setting.getName(), x + 9, y + 6, 0xFFD4D6E1);
        Fonts.getSize(12).drawString(matrices, wrapped, x + 9, y + 15, 0xFF878894);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (MathUtil.isHovered(mouseX, mouseY, x + width - 75, y + 4, 66, 14)) {
                open = !open;
            } else if (open && !isHoveredList(mouseX, mouseY)) {
                open = false;
            }

            if (open) {
                multiSelectedButtons.forEach(selectedButton -> selectedButton.mouseClicked(mouseX, mouseY, button));
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        return open && isHoveredList(mouseX, mouseY);
    }

    private void renderSelected(Matrix4f positionMatrix, MatrixStack matrices) {
        rectangle.render(ShapeProperties.create(positionMatrix, x + width - 75, y + 4, 66, 14)
                .round(6)
                .thickness(2)
                .outlineColor(0x2D2D2E41)
                .color(0xFF161825)
                .build()
        );

        String selectedName = String.join(", ", setting.getSelected());
        
        Stencil.push();
        rectangle.render(ShapeProperties.create(positionMatrix, x + width - 75, y + 4, 64, 14)
                .build()
        );
        Stencil.read(1);
        Fonts.getSize(12, BOLD).drawString(matrices, selectedName, x + width - 75 + 3, y + 10, 0xFFD4D6E1);
        Stencil.pop();

        rectangle.render(ShapeProperties.create(positionMatrix, x + width - 74, y + 5, 64, 12)
                .round(6)
                .color(0x00161825, 0x00161825, 0xFF161825, 0xFF161825)
                .build()
        );
    }

    private void renderSelectList(DrawContext context, int mouseX, int mouseY, float delta, Matrix4f positionMatrix) {
        int opacity = alphaAnimation
                .getOutput()
                .intValue();

        rectangle.render(ShapeProperties.create(positionMatrix, dropdownListX, dropDownListY, dropDownListWidth, dropDownListHeight)
                .round(6)
                .thickness(2)
                .outlineColor(MathUtil.applyOpacity(0xFF2D2E41, (float) opacity / 5))
                .color(MathUtil.applyOpacity(0xFF161825, opacity))
                .build()
        );

        int offset = (int) dropDownListY + 1;

        for (MultiSelectedButton button : multiSelectedButtons) {
            button.x = dropdownListX;
            button.y = offset;
            button.width = dropDownListWidth;
            button.height = 12;

            button.setAlpha(opacity);

            button.render(context, mouseX, mouseY, delta);
            offset += 12;
        }
    }

    private boolean isHoveredList(double mouseX, double mouseY) {
        return MathUtil.isHovered(mouseX, mouseY, dropdownListX, dropDownListY - 16, dropDownListWidth, dropDownListHeight + 16);
    }
}

