package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.event.classes.InputEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(KeyboardInput.class)
public class KeyboardInputMixin extends Input {

    @Shadow @Final private GameOptions settings;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        var forward = this.movementForward;
        var strafe = this.movementSideways;
        var jump = ((KeyboardInput) (Object) this).playerInput.jump();
        var sneak = ((KeyboardInput) (Object) this).playerInput.sneak();

        InputEvent event = new InputEvent(forward, strafe, this.settings.jumpKey.isPressed(), this.settings.sneakKey.isPressed());
        CrolClient.INSTANCE.getEventManager().hookEvent(event);

        if (event.isCancelled()) {
            this.movementForward = 0;
            this.movementSideways = 0;
            return;
        }

        this.movementForward = event.getForward();
        this.movementSideways = event.getStrafe();

        this.playerInput = new PlayerInput(
                event.getForward() > 0,
                event.getForward() < 0,
                event.getStrafe() > 0,
                event.getStrafe() < 0,
                event.isJump(),
                event.isSneak(),
                this.playerInput.sprint()
        );
    }
}