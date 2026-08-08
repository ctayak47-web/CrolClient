package crol.client.utility.mixin.client.render.feature;

import net.minecraft.EquipmentRenderer;
import net.minecraft.LivingEntity;
import net.minecraft.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import crol.client.modules.impl.render.HitColor;
import crol.client.utility.render.entity.ArmorTintContext;
import crol.client.utility.render.entity.EntityDamageTracker;

@Mixin(value={EquipmentRenderer.class})
public class EquipmentRendererMixin {
    private static final long HIT_COLOR_DURATION_MS = 350L;

    @ModifyArg(method={"render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/util/Identifier;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/model/Model;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"), index=4)
    private int tintArmorColor(int color) {
        LivingEntity entity = ArmorTintContext.get();
        HitColor hitColor = HitColor.INSTANCE;
        if (entity != null && entity != MinecraftClient.getInstance().player && hitColor.isEnabled() && EntityDamageTracker.isRecentlyDamaged(entity, 350L) && hitColor.isFullColor()) {
            return hitColor.getColor().withAlpha(255).getRGB();
        }
        return color;
    }
}
