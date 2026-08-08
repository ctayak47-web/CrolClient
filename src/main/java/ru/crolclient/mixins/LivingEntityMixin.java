package ru.crolclient.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.crolclient.api.event.EventManager;
import ru.crolclient.implement.events.block.PushPlayerEvent;

@SuppressWarnings("all")
@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
    public void isPushable(CallbackInfoReturnable<Boolean> infoReturnable) {
        PushPlayerEvent pushPlayerEvent = new PushPlayerEvent();
        EventManager.callEvent(pushPlayerEvent);

        if ((Object) this instanceof ClientPlayerEntity && pushPlayerEvent.isCancelled()) {
            infoReturnable.setReturnValue(false);
        }
    }
}
