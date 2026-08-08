
package crol.client.utility.mixin.client;

import com.darkmagician6.eventapi.EventManager;
import net.minecraft.Entity;
import net.minecraft.Vec3d;
import net.minecraft.ClientWorld;
import net.minecraft.ColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import crol.client.base.events.impl.other.EventSpawnEntity;
import crol.client.modules.impl.render.CustomWorld;
import crol.client.utility.render.display.base.color.ColorRGBA;

@Mixin(value={ClientWorld.class})
public class ClientWorldMixin {
    @Inject(method={"addEntity"}, at={@At(value="RETURN")})
    public void injectAddEntity(Entity entity, CallbackInfo ci) {
        EventSpawnEntity eventSpawnEntity = new EventSpawnEntity(entity);
        EventManager.call(eventSpawnEntity);
    }

    @Inject(method={"getSkyColor"}, at={@At(value="HEAD")}, cancellable=true)
    private void CrolClient$customSkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Integer> cir) {
        if (!CustomWorld.INSTANCE.shouldApplyWorldColoring()) {
            return;
        }
        ColorRGBA color = CustomWorld.INSTANCE.getColor();
        cir.setReturnValue((Object)ColorHelper.getArgb((int)color.getRed(), (int)color.getGreen(), (int)color.getBlue()));
    }

    @Inject(method={"getCloudsColor"}, at={@At(value="HEAD")}, cancellable=true)
    private void CrolClient$customCloudsColor(float tickDelta, CallbackInfoReturnable<Integer> cir) {
        if (!CustomWorld.INSTANCE.shouldApplyWorldColoring()) {
            return;
        }
        ColorRGBA color = CustomWorld.INSTANCE.getColor();
        cir.setReturnValue((Object)ColorHelper.getArgb((int)color.getRed(), (int)color.getGreen(), (int)color.getBlue()));
    }
}

