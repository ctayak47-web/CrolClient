
package vurst.visual.utility.mixin.client.render.gui.hud;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import vurst.visual.client.modules.impl.render.NoRender;

@Mixin(targets={"net.minecraft.client.gui.hud.InGameHud$HeartType"})
public abstract class InGameHudHeartTypeMixin {
    @ModifyExpressionValue(method={"fromPlayerState"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/player/PlayerEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z", ordinal=0)})
    private static boolean vv$disablePoisonHearts(boolean original) {
        if (NoRender.INSTANCE.isRemoveWitherHearts()) {
            return false;
        }
        return original;
    }

    @ModifyExpressionValue(method={"fromPlayerState"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/player/PlayerEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z", ordinal=1)})
    private static boolean vv$disableWitherHearts(boolean original) {
        if (NoRender.INSTANCE.isRemoveWitherHearts()) {
            return false;
        }
        return original;
    }
}

