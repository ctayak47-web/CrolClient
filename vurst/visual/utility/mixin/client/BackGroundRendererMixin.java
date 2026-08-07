
package vurst.visual.utility.mixin.client;

import com.darkmagician6.eventapi.EventManager;
import net.minecraft.Camera;
import net.minecraft.CameraSubmersionType;
import net.minecraft.ClientWorld;
import net.minecraft.FogShape;
import net.minecraft.BackgroundRenderer;
import net.minecraft.Fog;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vurst.visual.base.events.impl.render.EventFog;
import vurst.visual.client.modules.impl.render.CustomWorld;
import vurst.visual.client.modules.impl.render.NoFluid;
import vurst.visual.utility.render.display.base.color.ColorRGBA;
import vurst.visual.utility.render.display.base.color.ColorUtil;

@Mixin(value={BackgroundRenderer.class})
public class BackGroundRendererMixin {
    @Inject(method={"getFogColor"}, at={@At(value="HEAD")}, cancellable=true)
    private static void getFogColorHook(Camera camera, float tickDelta, ClientWorld world, int clampedViewDistance, float skyDarkness, CallbackInfoReturnable<Vector4f> cir) {
        EventFog event = new EventFog();
        EventManager.call(event);
        if (event.isCancelled()) {
            int color = event.getColor();
            cir.setReturnValue((Object)new Vector4f(ColorUtil.redf(color), ColorUtil.greenf(color), ColorUtil.bluef(color), ColorUtil.alphaf(color)));
        }
    }

    @Inject(method={"getFogColor"}, at={@At(value="RETURN")}, cancellable=true)
    private static void vurstvisual$applyCustomWorldFogColor(Camera camera, float tickDelta, ClientWorld world, int clampedViewDistance, float skyDarkness, CallbackInfoReturnable<Vector4f> cir) {
        CameraSubmersionType submersionType = camera.getSubmersionType();
        if (NoFluid.INSTANCE.shouldRemoveFluidFog() && (submersionType == CameraSubmersionType.WATER || submersionType == CameraSubmersionType.LAVA)) {
            int color = world.getSkyColor(camera.getPos(), tickDelta);
            cir.setReturnValue((Object)new Vector4f(ColorUtil.redf(color), ColorUtil.greenf(color), ColorUtil.bluef(color), 1.0f));
            return;
        }
        if (!CustomWorld.INSTANCE.shouldApplyWorldColoring()) {
            return;
        }
        if (submersionType != CameraSubmersionType.NONE) {
            return;
        }
        ColorRGBA color = CustomWorld.INSTANCE.getColor();
        cir.setReturnValue((Object)new Vector4f((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, 1.0f));
    }

    @Inject(method={"applyFog"}, at={@At(value="HEAD")}, cancellable=true)
    private static void modifyFog(Camera camera, BackgroundRenderer.FogType fogType, Vector4f color, float viewDistance, boolean thickenFog, float tickDelta, CallbackInfoReturnable<Fog> cir) {
        CameraSubmersionType submersionType = camera.getSubmersionType();
        if (NoFluid.INSTANCE.shouldRemoveFluidFog() && (submersionType == CameraSubmersionType.WATER || submersionType == CameraSubmersionType.LAVA)) {
            cir.setReturnValue((Object)Fog.DUMMY);
            return;
        }
        EventFog event = new EventFog();
        EventManager.call(event);
        if (event.isCancelled()) {
            int color1 = event.getColor();
            cir.setReturnValue((Object)new Fog(2.0f, event.getDistance(), FogShape.CYLINDER, ColorUtil.redf(color1), ColorUtil.greenf(color1), ColorUtil.bluef(color1), ColorUtil.alphaf(color1)));
        }
    }
}

