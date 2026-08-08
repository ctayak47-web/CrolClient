package crol.client.util.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;

@Environment(EnvType.CLIENT)
public final class ItemRenderUtil {
   private static final MinecraftClient mc = MinecraftClient.getInstance();
   private static final ItemRenderState RENDER_STATE = new ItemRenderState();

   private ItemRenderUtil() {
   }

   public static void drawItemAlpha(MatrixStack matrices, ItemStack stack, int x, int y, float alpha) {
      if (!stack.isEmpty() && mc.world != null) {
         alpha = Math.max(0.0F, Math.min(1.0F, alpha));
         mc.getItemModelManager().update(RENDER_STATE, stack, ModelTransformationMode.GUI, false, mc.world, mc.player, stack.hashCode());
         matrices.push();
         matrices.translate((float)x + 8.0F, (float)y + 8.0F, 150.0F);
         matrices.scale(16.0F, -16.0F, 16.0F);
         boolean sideLit = RENDER_STATE.isSideLit();
         if (!sideLit) {
            DiffuseLighting.disableGuiDepthLighting();
         }

         VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
         RENDER_STATE.render(matrices, new AlphaProvider(immediate, alpha), 15728880, OverlayTexture.DEFAULT_UV);
         immediate.draw();
         if (!sideLit) {
            DiffuseLighting.enableGuiDepthLighting();
         }

         matrices.pop();
      }
   }

   @Environment(EnvType.CLIENT)
   private static record AlphaProvider(VertexConsumerProvider.Immediate delegate, float alpha) implements VertexConsumerProvider {
      public VertexConsumer getBuffer(RenderLayer layer) {
         return new AlphaConsumer(this.delegate.getBuffer(layer), this.alpha);
      }
   }

   @Environment(EnvType.CLIENT)
   private static record AlphaConsumer(VertexConsumer delegate, float alpha) implements VertexConsumer {
      public VertexConsumer vertex(float x, float y, float z) {
         this.delegate.vertex(x, y, z);
         return this;
      }

      public VertexConsumer color(int r, int g, int b, int a) {
         this.delegate.color(r, g, b, Math.round((float)a * this.alpha));
         return this;
      }

      public VertexConsumer texture(float u, float v) {
         this.delegate.texture(u, v);
         return this;
      }

      public VertexConsumer overlay(int u, int v) {
         this.delegate.overlay(u, v);
         return this;
      }

      public VertexConsumer light(int u, int v) {
         this.delegate.light(u, v);
         return this;
      }

      public VertexConsumer normal(float x, float y, float z) {
         this.delegate.normal(x, y, z);
         return this;
      }
   }
}
