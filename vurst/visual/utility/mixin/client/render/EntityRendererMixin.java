
package vurst.visual.utility.mixin.client.render;

import net.minecraft.Entity;
import net.minecraft.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vurst.visual.client.modules.impl.render.NameF5;
import vurst.visual.utility.interfaces.IMinecraft;

@Mixin(value={EntityRenderer.class})
public abstract class EntityRendererMixin
implements IMinecraft {
    @Inject(method={"hasLabel"}, at={@At(value="HEAD")}, cancellable=true)
    private void showSelfLabel(Entity entity, double distance, CallbackInfoReturnable<Boolean> cir) {
        if (entity != EntityRendererMixin.mc.player) {
            return;
        }
        if (NameF5.INSTANCE.shouldShowName()) {
            cir.setReturnValue((Object)true);
        }
    }
}

