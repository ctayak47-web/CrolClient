
package crol.client.utility.mixin.client;

import com.darkmagician6.eventapi.EventManager;
import net.minecraft.PlayerInput;
import net.minecraft.GameOptions;
import net.minecraft.KeyboardInput;
import net.minecraft.Input;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import crol.client.base.events.impl.player.EventMoveInput;

@Mixin(value={KeyboardInput.class})
public abstract class KeyboardInputMixin
extends Input {
    @Shadow
    @Final
    private GameOptions settings;

    @Unique
    private float abobaGetMovementMultiplier(boolean positive, boolean negative) {
        if (positive == negative) {
            return 0.0f;
        }
        return positive ? 1.0f : -1.0f;
    }

    @Inject(method={"tick"}, at={@At(value="FIELD", target="Lnet/minecraft/client/input/KeyboardInput;playerInput:Lnet/minecraft/util/PlayerInput;", ordinal=0, shift=At.Shift.AFTER)}, cancellable=true)
    public void injectInputEvent(CallbackInfo ci) {
        EventMoveInput event = new EventMoveInput(this.playerInput, this.abobaGetMovementMultiplier(this.playerInput.comp_3159(), this.playerInput.comp_3160()), this.abobaGetMovementMultiplier(this.playerInput.comp_3161(), this.playerInput.comp_3162()));
        EventManager.call(event);
        if (event.isCancelled()) {
            return;
        }
        this.movementForward = event.getForward();
        this.movementSideways = event.getStrafe();
        this.playerInput = new PlayerInput(this.movementForward > 0.0f, this.movementForward < 0.0f, this.movementSideways > 0.0f, this.movementSideways < 0.0f, this.settings.jumpKey.isPressed(), this.settings.sneakKey.isPressed(), this.settings.sprintKey.isPressed());
        ci.cancel();
    }
}

