
package crol.client.utility.mixin.minecraft.render;

import net.minecraft.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import crol.client.modules.impl.render.WorldTime;

@Mixin(value={ClientWorld.Properties.class})
public class ClientWorldPropertiesMixin {
    @Shadow
    private long timeOfDay;

    @Inject(method={"setTimeOfDay"}, at={@At(value="HEAD")}, cancellable=true)
    public void setTimeOfDayHook(long timeOfDay, CallbackInfo ci) {
        WorldTime worldTime = WorldTime.INSTANCE;
        if (worldTime.isEnabled()) {
            this.timeOfDay = (long)(worldTime.getTimeHours() * 1000.0f);
            ci.cancel();
        }
    }
}

