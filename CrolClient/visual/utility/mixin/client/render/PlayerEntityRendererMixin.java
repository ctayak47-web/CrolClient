
package crol.client.utility.mixin.client.render;

import net.minecraft.LivingEntityRenderState;
import net.minecraft.PlayerEntityRenderState;
import net.minecraft.PlayerEntityRenderer;
import net.minecraft.LivingEntity;
import net.minecraft.Text;
import net.minecraft.MatrixStack;
import net.minecraft.VertexConsumer;
import net.minecraft.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import crol.client.modules.impl.render.NameF5;
import crol.client.modules.impl.render.ShaderHands;
import crol.client.utility.interfaces.IMinecraft;
import crol.client.utility.render.display.base.color.ColorRGBA;
import crol.client.utility.render.entity.RenderStateEntityCache;

@Mixin(value={PlayerEntityRenderer.class})
public abstract class PlayerEntityRendererMixin
implements IMinecraft {
    @ModifyVariable(method={"renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at=@At(value="HEAD"), argsOnly=true)
    private Text forceSelfName(Text text, PlayerEntityRenderState state) {
        Text displayName;
        if (!NameF5.INSTANCE.shouldShowName()) {
            return text;
        }
        LivingEntity entity = RenderStateEntityCache.get((LivingEntityRenderState)state);
        if (entity == PlayerEntityRendererMixin.mc.player && (displayName = entity.getDisplayName()) != null) {
            return displayName;
        }
        return text;
    }

    @Redirect(method={"renderArm(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/util/Identifier;Lnet/minecraft/client/model/ModelPart;Z)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V"))
    private void applyShaderHandsToFirstPersonArm(ModelPart part, MatrixStack matrices, VertexConsumer consumer, int light, int overlay) {
        ShaderHands shaderHands = ShaderHands.INSTANCE;
        if (!shaderHands.shouldShaderHands()) {
            part.render(matrices, consumer, light, overlay);
            return;
        }
        float alpha = shaderHands.getHandCombinedAlpha();
        float brightness = Math.max(0.0f, shaderHands.getHandBrightness());
        ColorRGBA handColor = shaderHands.getHandColor();
        int red = PlayerEntityRendererMixin.clampColor(Math.min(1.0f, (float)handColor.getRed() / 255.0f * brightness) * 255.0f);
        int green = PlayerEntityRendererMixin.clampColor(Math.min(1.0f, (float)handColor.getGreen() / 255.0f * brightness) * 255.0f);
        int blue = PlayerEntityRendererMixin.clampColor(Math.min(1.0f, (float)handColor.getBlue() / 255.0f * brightness) * 255.0f);
        int argb = PlayerEntityRendererMixin.clampColor(alpha * 255.0f) << 24 | red << 16 | green << 8 | blue;
        int adjustedLight = brightness > 1.0f ? 0xF000F0 : light;
        part.render(matrices, consumer, adjustedLight, overlay, argb);
    }

    private static int clampColor(float value) {
        return Math.max(0, Math.min(255, Math.round(value)));
    }
}

