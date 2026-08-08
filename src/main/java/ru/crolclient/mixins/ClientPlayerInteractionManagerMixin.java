package ru.crolclient.mixins;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.crolclient.api.event.EventManager;
import ru.crolclient.implement.events.player.AttackEvent;
import ru.crolclient.api.event.types.EventType;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Inject(method = "attackEntity", at = @At("HEAD"))
    public void attackEntityPre(PlayerEntity player, Entity target, CallbackInfo callbackInfo) {
        AttackEvent event = new AttackEvent(
                target,
                EventType.PRE
        );

        EventManager.callEvent(event);
    }

    @Inject(method = "attackEntity", at = @At("TAIL"))
    public void attackEntityPost(PlayerEntity player, Entity target, CallbackInfo callbackInfo) {
        AttackEvent event = new AttackEvent(
                target,
                EventType.POST
        );

        EventManager.callEvent(event);
    }
}
