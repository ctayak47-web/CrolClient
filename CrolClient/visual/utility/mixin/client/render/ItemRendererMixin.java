
package crol.client.utility.mixin.client.render;

import net.minecraft.MatrixStack;
import net.minecraft.VertexConsumer;
import net.minecraft.BakedQuad;
import net.minecraft.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import crol.client.utility.render.item.ShaderHandsRenderState;

@Mixin(value={ItemRenderer.class})
public abstract class ItemRendererMixin {
    @Redirect(method={"renderBakedItemQuads(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Ljava/util/List;[III)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/VertexConsumer;quad(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/model/BakedQuad;FFFFII)V"))
    private static void CrolClient$applyShaderHandsItemTint(VertexConsumer instance, MatrixStack.Entry matrixEntry, BakedQuad quad, float red, float green, float blue, float alpha, int light, int overlay) {
        if (!ShaderHandsRenderState.isActive()) {
            instance.quad(matrixEntry, quad, red, green, blue, alpha, light, overlay);
            return;
        }
        instance.quad(matrixEntry, quad, ShaderHandsRenderState.tintRed(red), ShaderHandsRenderState.tintGreen(green), ShaderHandsRenderState.tintBlue(blue), ShaderHandsRenderState.tintAlpha(alpha), light, overlay);
    }
}

