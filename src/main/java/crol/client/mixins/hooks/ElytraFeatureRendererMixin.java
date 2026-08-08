package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.modules.impl.render.Wings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.client.render.entity.equipment.EquipmentModel.LayerType;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({ElytraFeatureRenderer.class})
public abstract class ElytraFeatureRendererMixin {
   @Shadow
   @Final
   private EquipmentRenderer equipmentRenderer;
   @Shadow
   @Final
   private ElytraEntityModel model;
   @Shadow
   @Final
   private ElytraEntityModel babyModel;

   @Inject(
      method = {"render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/BipedEntityRenderState;FF)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRender(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, BipedEntityRenderState bipedEntityRenderState, float limbAngle, float limbDistance, CallbackInfo ci) {
      ItemStack itemStack = bipedEntityRenderState.equippedChestStack;
      if (bipedEntityRenderState instanceof PlayerEntityRenderState playerEntityRenderState) {
         if (CrolClient.INSTANCE.getModuleManager().getByClass(Wings.class).isEnabled() && playerEntityRenderState.name.equalsIgnoreCase(MinecraftClient.getInstance().player.getNameForScoreboard())) {
            itemStack = Items.ELYTRA.getDefaultStack();
         }
      }

      EquippableComponent equippableComponent = (EquippableComponent)itemStack.get(DataComponentTypes.EQUIPPABLE);
      if (equippableComponent != null && !equippableComponent.assetId().isEmpty() || CrolClient.INSTANCE.getModuleManager().getByClass(Wings.class).isEnabled() && equippableComponent != null) {
         Identifier identifier = getTextureS(bipedEntityRenderState);
         ElytraEntityModel elytraEntityModel = bipedEntityRenderState.baby ? this.babyModel : this.model;
         matrices.push();
         matrices.translate(0.0F, 0.0F, 0.125F);
         elytraEntityModel.setAngles(bipedEntityRenderState);
         this.equipmentRenderer.render(LayerType.WINGS, (RegistryKey)equippableComponent.assetId().get(), elytraEntityModel, itemStack, matrices, vertexConsumers, light, identifier);
         matrices.pop();
      }

      ci.cancel();
   }

   @Unique
   private static Identifier getTextureS(BipedEntityRenderState state) {
      if (state instanceof PlayerEntityRenderState playerEntityRenderState) {
         SkinTextures skinTextures = playerEntityRenderState.skinTextures;
         Wings wings = (Wings)CrolClient.INSTANCE.getModuleManager().getByClass(Wings.class);
         if (wings.isEnabled()) {
            return wings.getMode();
         }

         if (skinTextures.elytraTexture() != null) {
            return skinTextures.elytraTexture();
         }

         if (skinTextures.capeTexture() != null && playerEntityRenderState.capeVisible) {
            return skinTextures.capeTexture();
         }
      }

      return null;
   }
}
