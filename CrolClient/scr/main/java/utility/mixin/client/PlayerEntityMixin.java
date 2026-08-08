
package crol.client.utility.mixin.client;

import net.minecraft.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import crol.client.modules.impl.render.SwingAnimation;

@Mixin(value={LivingEntity.class})
public abstract class PlayerEntityMixin {
    @Inject(method={"getHandSwingDuration"}, at={@At(value="HEAD")}, cancellable=true)
    private void getArmSwingAnimationEnd(CallbackInfoReturnable<Integer> info) {
        SwingAnimation swingAnimation = SwingAnimation.INSTANCE;
        if (swingAnimation.isEnabled()) {
            info.setReturnValue((Object)swingAnimation.getSwingDuration());
        }
    }
}

