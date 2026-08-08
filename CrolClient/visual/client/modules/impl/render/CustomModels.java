
package crol.client.modules.impl.render;

import net.minecraft.LivingEntity;
import net.minecraft.PlayerEntity;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.impl.ModeSetting;
import crol.client.modules.impl.render.CustomModelType;

@ModuleAnnotation(name="Custom Models", category=Category.MOVEMENT, description="Заменяет модели игроков на кастомные 3D.")
public final class CustomModels
extends Module {
    public static final CustomModels INSTANCE = new CustomModels();
    private final ModeSetting model = new ModeSetting("Модель", CustomModelType.names());

    private CustomModels() {
    }

    public CustomModelType getSelectedType() {
        return CustomModelType.fromDisplay(this.model.get());
    }

    public boolean shouldApplyTo(LivingEntity entity) {
        if (!this.isEnabled() || entity == null) {
            return false;
        }
        if (!(entity instanceof PlayerEntity)) {
            return false;
        }
        PlayerEntity player = (PlayerEntity)entity;
        return player == CustomModels.mc.player;
    }
}

