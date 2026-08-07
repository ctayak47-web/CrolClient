
package vurst.visual.utility.mixin.client.render;

import net.minecraft.Entity;
import net.minecraft.Frustum;
import net.minecraft.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vurst.visual.utility.culling.EntityCullingManager;

@Mixin(value={EntityRenderDispatcher.class})
public class EntityCullingMixin {
    @Inject(method={"shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/Frustum;DDD)Z"}, at={@At(value="HEAD")}, cancellable=true)
    private <T extends Entity> void vurstvisual$applyEntityCulling(T entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (EntityCullingManager.getInstance().shouldCull(entity, frustum)) {
            cir.setReturnValue((Object)false);
        }
    }
}

