
package crol.client.utility.mixin.client.render;

import net.minecraft.Entity;
import net.minecraft.Camera;
import net.minecraft.WorldRenderer;
import net.minecraft.FrameGraphBuilder;
import net.minecraft.Fog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import crol.client.modules.impl.render.CustomGlow;
import crol.client.modules.impl.render.CustomWorld;

@Mixin(value={WorldRenderer.class})
public abstract class WorldRendererMixin {
    @Inject(method={"renderSky(Lnet/minecraft/client/render/FrameGraphBuilder;Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/render/Fog;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void CrolClient$skipVanillaSkyForCustomWorld(FrameGraphBuilder frameGraphBuilder, Camera camera, float tickDelta, Fog fog, CallbackInfo ci) {
        if (CustomWorld.INSTANCE.shouldApplyWorldColoring()) {
            ci.cancel();
        }
    }

    @Redirect(method={"renderEntities(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/RenderTickCounter;Ljava/util/List;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;getTeamColorValue()I"))
    private int applyCustomGlowColor(Entity entity) {
        return CustomGlow.INSTANCE.getGlowColor(entity);
    }
}

