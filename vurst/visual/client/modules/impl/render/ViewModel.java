
package vurst.visual.client.modules.impl.render;

import net.minecraft.Arm;
import net.minecraft.MatrixStack;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.NumberSetting;

@ModuleAnnotation(name="View Model", category=Category.RENDER, description="Настраивает модель рук.")
public final class ViewModel
extends Module {
    public static final ViewModel INSTANCE = new ViewModel();
    public final NumberSetting leftX = new NumberSetting("Левая рука X", 0.0f, -1.0f, 1.0f, 0.1f);
    public final NumberSetting leftY = new NumberSetting("Левая рука Y", 0.0f, -1.0f, 1.0f, 0.1f);
    public final NumberSetting leftZ = new NumberSetting("Левая рука Z", 0.0f, -1.0f, 1.0f, 0.1f);
    public final NumberSetting leftScale = new NumberSetting("Масштаб левой руки", 1.0f, 0.5f, 1.5f, 0.05f);
    public final NumberSetting rightX = new NumberSetting("Правая рука X", 0.0f, -1.0f, 1.0f, 0.1f);
    public final NumberSetting rightY = new NumberSetting("Правая рука Y", 0.0f, -1.0f, 1.0f, 0.1f);
    public final NumberSetting rightZ = new NumberSetting("Правая рука Z", 0.0f, -1.0f, 1.0f, 0.1f);
    public final NumberSetting rightScale = new NumberSetting("Масштаб правой руки", 1.0f, 0.5f, 1.5f, 0.05f);

    private ViewModel() {
    }

    public void applyHandScale(MatrixStack matrices, Arm arm) {
        if (this.isEnabled()) {
            if (arm == Arm.RIGHT) {
                matrices.scale(this.rightScale.getCurrent(), this.rightScale.getCurrent(), this.rightScale.getCurrent());
            } else {
                matrices.scale(this.leftScale.getCurrent(), this.leftScale.getCurrent(), this.leftScale.getCurrent());
            }
        } else {
            matrices.scale(1.0f, 1.0f, 1.0f);
        }
    }

    public void applyHandPosition(MatrixStack matrices, Arm arm) {
        if (this.isEnabled()) {
            if (arm == Arm.RIGHT) {
                matrices.translate(this.rightX.getCurrent(), this.rightY.getCurrent(), this.rightZ.getCurrent());
            } else {
                matrices.translate(-this.leftX.getCurrent(), this.leftY.getCurrent(), this.leftZ.getCurrent());
            }
        } else {
            matrices.translate(0.0f, 0.0f, 0.0f);
        }
    }
}

