
package vurst.visual.utility.mixin.minecraft.network;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.PlaySoundFromEntityS2CPacket;
import net.minecraft.PlaySoundS2CPacket;
import net.minecraft.SoundEvent;
import net.minecraft.SoundEvents;
import net.minecraft.ClientPlayNetworkHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vurst.visual.VurstVisual;
import vurst.visual.client.modules.impl.utility.AntiBanChat;
import vurst.visual.client.modules.impl.utility.HitSound;
import vurst.visual.client.modules.impl.utility.PvpSave;

@Mixin(value={ClientPlayNetworkHandler.class})
public class ClientPlayNetworkHandlerMixin {
    @Unique
    private boolean vurstvisual$antiBanChatBypass;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Inject(method={"sendChatMessage"}, at={@At(value="HEAD")}, cancellable=true)
    private void sendChatMessageHook(@NotNull String message, CallbackInfo ci) {
        if (this.vurstvisual$antiBanChatBypass) {
            return;
        }
        if (PvpSave.INSTANCE.isEnabled() && message.startsWith("/") && PvpSave.INSTANCE.shouldBlockHubCommand(message)) {
            ci.cancel();
            return;
        }
        if (message.startsWith(VurstVisual.getInstance().getCommandManager().getPrefix())) {
            try {
                VurstVisual.getInstance().getCommandManager().getDispatcher().execute(message.substring(VurstVisual.getInstance().getCommandManager().getPrefix().length()), (Object)VurstVisual.getInstance().getCommandManager().getSource());
            }
            catch (CommandSyntaxException commandSyntaxException) {
                
            }
            ci.cancel();
            return;
        }
        String protectedMessage = AntiBanChat.INSTANCE.protectMessage(message);
        if (!protectedMessage.equals(message)) {
            this.vurstvisual$antiBanChatBypass = true;
            try {
                ((ClientPlayNetworkHandler)this).sendChatMessage(protectedMessage);
            }
            finally {
                this.vurstvisual$antiBanChatBypass = false;
            }
            AntiBanChat.INSTANCE.notifyProtectedMessage();
            ci.cancel();
        }
    }

    @Inject(method={"sendChatCommand"}, at={@At(value="HEAD")}, cancellable=true)
    private void sendChatCommandHook(@NotNull String command, CallbackInfo ci) {
        if (!PvpSave.INSTANCE.isEnabled()) {
            return;
        }
        if (PvpSave.INSTANCE.shouldBlockHubCommand(command)) {
            ci.cancel();
        }
    }

    @Inject(method={"onPlaySound"}, at={@At(value="HEAD")}, cancellable=true)
    private void onPlaySoundHook(PlaySoundS2CPacket packet, CallbackInfo ci) {
        if (!HitSound.INSTANCE.shouldSuppressDefaultHits()) {
            return;
        }
        if (this.isAttackSound((SoundEvent)packet.getSound().comp_349())) {
            HitSound.INSTANCE.captureSuppressedAttackSound((SoundEvent)packet.getSound().comp_349());
            ci.cancel();
        }
    }

    @Inject(method={"onPlaySoundFromEntity"}, at={@At(value="HEAD")}, cancellable=true)
    private void onPlaySoundFromEntityHook(PlaySoundFromEntityS2CPacket packet, CallbackInfo ci) {
        if (!HitSound.INSTANCE.shouldSuppressDefaultHits()) {
            return;
        }
        if (this.isAttackSound((SoundEvent)packet.getSound().comp_349())) {
            HitSound.INSTANCE.captureSuppressedAttackSound((SoundEvent)packet.getSound().comp_349());
            ci.cancel();
        }
    }

    private boolean isAttackSound(SoundEvent sound) {
        return sound == SoundEvents.ENTITY_PLAYER_ATTACK_STRONG || sound == SoundEvents.ENTITY_PLAYER_ATTACK_WEAK || sound == SoundEvents.ENTITY_PLAYER_ATTACK_CRIT || sound == SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP || sound == SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK || sound == SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE;
    }
}

