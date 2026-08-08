package crol.client.mixins.hooks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import crol.client.CrolClient;
import crol.client.event.classes.DropItemEvent;
import crol.client.mixins.other.IMinecraftClientMixin;
import crol.client.modules.impl.combat.NoInteract;
import crol.client.modules.impl.render.PlayerEsp;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.session.Session;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin implements IMinecraftClientMixin {

    @Shadow private int itemUseCooldown;
    @Shadow @Nullable public ClientPlayerEntity player;
    @Shadow @Nullable public ClientPlayerInteractionManager interactionManager;
    @Shadow @Final public GameRenderer gameRenderer;

    @Mutable @Final @Shadow private Session session;

    @Shadow protected abstract boolean doAttack();

    // Кэшированные модули для FPS
    @Unique private NoInteract cachedNoInteract;
    @Unique private PlayerEsp cachedPlayerEsp;

    @Unique
    private NoInteract getNoInteract() {
        if (cachedNoInteract == null) cachedNoInteract = (NoInteract) CrolClient.INSTANCE.getModuleManager().getByClass(NoInteract.class);
        return cachedNoInteract;
    }

    @Unique
    private PlayerEsp getPlayerEsp() {
        if (cachedPlayerEsp == null) cachedPlayerEsp = (PlayerEsp) CrolClient.INSTANCE.getModuleManager().getByClass(PlayerEsp.class);
        return cachedPlayerEsp;
    }

    public void mouseClick() {
        this.doAttack();
    }

    @Inject(
            method = {"doItemUse"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Hand;values()[Lnet/minecraft/util/Hand;"
            )},
            cancellable = true
    )
    public void doItemUseHook(CallbackInfo ci) {
        if (CrolClient.INSTANCE.getModuleManager().getByClass(NoInteract.class).isEnabled()) {
            for(Hand hand : Hand.values()) {
                if (!this.player.getStackInHand(hand).isEmpty()) {
                    ActionResult result = this.player.interact(this.player, hand);
                    if (result.isAccepted()) {
                        if (result instanceof ActionResult.Success) {
                            ActionResult.Success success = (ActionResult.Success)result;
                            if (success.swingSource().equals(ActionResult.SwingSource.CLIENT)) {
                                this.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                                this.player.getStackInHand(hand);
                            }
                        }

                        ci.cancel();
                    }
                }
            }
        }

    }

    /**
     * Контроль свечения (Glow) игроков
     */
    @Inject(method = "hasOutline", at = @At("HEAD"), cancellable = true)
    private void onHasOutline(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        PlayerEsp esp = getPlayerEsp();
        if (esp != null && esp.isEnabled() && esp.options.getValueByName("Chams") && esp.chamsMode.is("Glow")) {
            if (entity instanceof PlayerEntity) {
                cir.setReturnValue(true);
            }
        }
    }

    @Unique
    @Override
    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public int getUseCooldown() {
        return this.itemUseCooldown;
    }

    @Override
    public void setUseCooldown(int val) {
        this.itemUseCooldown = val;
    }

    /**
     * Перехват выбрасывания предметов
     */
    @WrapOperation(
            method = "handleInputEvents",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;dropSelectedItem(Z)Z")
    )
    private boolean onDropSelectedItem(ClientPlayerEntity instance, boolean entireStack, Operation<Boolean> original) {
        DropItemEvent eventDropItem = new DropItemEvent();
        CrolClient.INSTANCE.getEventManager().hookEvent(eventDropItem);

        if (eventDropItem.isCancelled()) {
            return false;
        }

        return original.call(instance, entireStack);
    }
}