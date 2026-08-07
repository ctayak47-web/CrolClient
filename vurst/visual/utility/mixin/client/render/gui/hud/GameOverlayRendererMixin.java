
package vurst.visual.utility.mixin.client.render.gui.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Sprite;
import net.minecraft.MinecraftClient;
import net.minecraft.MatrixStack;
import net.minecraft.VertexConsumerProvider;
import net.minecraft.InGameOverlayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vurst.visual.client.modules.impl.render.NoFluid;
import vurst.visual.client.modules.impl.render.NoRender;

@Mixin(value={InGameOverlayRenderer.class})
public class GameOverlayRendererMixin {
    @Inject(method={"renderInWallOverlay"}, at={@At(value="HEAD")})
    private static void prepareInWallOverlay(Sprite sprite, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    @ModifyConstant(method={"renderInWallOverlay"}, constant={@Constant(floatValue=0.1f, ordinal=0)})
    private static float increaseInWallOverlayAlpha(float originalAlpha) {
        return 0.85f;
    }

    @Inject(method={"renderFireOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private static void removeFireOverlay(MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (NoRender.INSTANCE.isRemoveFire()) {
            ci.cancel();
        }
    }

    @Inject(method={"renderUnderwaterOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private static void removeUnderwaterOverlay(MinecraftClient client, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (NoFluid.INSTANCE.shouldRemoveOverlay()) {
            ci.cancel();
        }
    }
}

