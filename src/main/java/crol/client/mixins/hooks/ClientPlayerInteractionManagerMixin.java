package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.event.EventType;
import crol.client.event.classes.AttackEvent;
import crol.client.event.classes.BreakEvent;
import crol.client.event.classes.ClickSlotEvent;
import crol.client.event.classes.UsingItemEvent;
import crol.client.mixins.other.IClientPlayerInteractionManagerMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin implements IClientPlayerInteractionManagerMixin {
    @Shadow private float currentBreakingProgress;
    @Shadow protected abstract void sendSequencedPacket(ClientWorld world, SequencedPacketCreator packetCreator);

    @Inject(method = "attackEntity", at = @At("HEAD"))
    void attack(PlayerEntity player, Entity target, CallbackInfo ci) {
        CrolClient.INSTANCE.getEventManager().hookEvent(new AttackEvent(target));
    }

    @Inject(method = "clickSlot", at = @At("HEAD"), cancellable = true)
    public void clickSlotHook(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        ClickSlotEvent event = new ClickSlotEvent(actionType, slotId, button, syncId);
        CrolClient.INSTANCE.getEventManager().hookEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "attackBlock", at = @At("HEAD"))
    void onAttackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        CrolClient.INSTANCE.getEventManager().hookEvent(new BreakEvent(pos, direction));
    }

    @Override
    public float getBreakingProgress() {
        return this.currentBreakingProgress;
    }

    /**
     * ИСПРАВЛЕНО: Это HEAD ивента использования предмета.
     * Раньше ты ошибочно пытался инджектить это в getCurrentGameMode.
     */
    @Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
    private void onInteractItemHead(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        UsingItemEvent event = new UsingItemEvent(EventType.START);
        CrolClient.INSTANCE.getEventManager().hookEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Inject(method = "interactItem", at = @At("RETURN"))
    public void interactItemHook(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ActionResult result = cir.getReturnValue();
        // В 1.21.4 проверяем, было ли действие успешным
        if (result.isAccepted()) {
            UsingItemEvent event = new UsingItemEvent(EventType.PRE);
            CrolClient.INSTANCE.getEventManager().hookEvent(event);
        }
    }

    @Inject(method = "stopUsingItem", at = @At("HEAD"))
    public void stopUsingItemHook(PlayerEntity player, CallbackInfo ci) {
        UsingItemEvent event = new UsingItemEvent(EventType.POST);
        CrolClient.INSTANCE.getEventManager().hookEvent(event);
    }

    @Override
    public void invokeSendSequencedPacket(ClientWorld world, SequencedPacketCreator packetCreator) {
        this.sendSequencedPacket(world, packetCreator);
    }
}