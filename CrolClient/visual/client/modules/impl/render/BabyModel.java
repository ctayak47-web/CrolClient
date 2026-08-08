
package crol.client.modules.impl.render;

import net.minecraft.Entity;
import net.minecraft.MatrixStack;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;

@ModuleAnnotation(name="BabyMod", category=Category.RENDER, description="Уменьшает вашу модель только визуально.")
public final class BabyModel
extends Module {
    public static final BabyModel INSTANCE = new BabyModel();
    private static final float MODEL_SCALE = 0.5f;
    private static final double THIRD_PERSON_EYE_OFFSET = 0.81;

    private BabyModel() {
    }

    @Override
    public String[] getLegacyNames() {
        return new String[]{"BabyModel", "Babymodel"};
    }

    public boolean shouldApplyTo(Entity entity) {
        return this.isEnabled() && BabyModel.mc.player != null && entity == BabyModel.mc.player;
    }

    public void applyModelScale(MatrixStack matrices) {
        matrices.scale(0.5f, 0.5f, 0.5f);
    }

    public float getVisualScale(Entity entity) {
        return this.shouldApplyTo(entity) ? 0.5f : 1.0f;
    }

    public boolean shouldAdjustThirdPersonCamera(Entity focusedEntity, boolean thirdPerson) {
        return thirdPerson && this.shouldApplyTo(focusedEntity);
    }

    public double getThirdPersonEyeOffset() {
        return 0.81;
    }
}

