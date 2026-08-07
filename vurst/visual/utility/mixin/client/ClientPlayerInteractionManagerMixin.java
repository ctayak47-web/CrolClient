
package vurst.visual.utility.mixin.client;

import com.darkmagician6.eventapi.EventManager;
import net.minecraft.Entity;
import net.minecraft.PlayerEntity;
import net.minecraft.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vurst.visual.base.events.impl.player.EventAttack;

@Mixin(value={ClientPlayerInteractionManager.class})
public class ClientPlayerInteractionManagerMixin {
    @Inject(method={"attackEntity"}, at={@At(value="HEAD")}, cancellable=true)
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        EventAttack event = new EventAttack(target, EventAttack.Action.PRE);
        EventManager.call(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"attackEntity"}, at={@At(value="RETURN")})
    private void onAttackEntityPost(PlayerEntity player, Entity target, CallbackInfo ci) {
        EventManager.call(new EventAttack(target, EventAttack.Action.POST));
    }
}

