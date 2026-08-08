package crol.client.mixins.hooks;

import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import crol.client.CrolClient;
import crol.client.modules.impl.render.BetterWorld;
import crol.client.util.render.SkyShaderHolder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.SkyRendering;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({SkyRendering.class})
public class SkyRenderingMixin {
   private static BetterWorld cachedModule = null;
   private static boolean moduleLookupDone = false;
   private static final Matrix4f sProjMat = new Matrix4f();
   private static final Matrix4f sViewMat = new Matrix4f();
   private static final Matrix4f sIdentity = new Matrix4f();
   private static boolean frameOverride = false;

   @Inject(
      method = {"renderSky"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void renderCustomSkyBackground(float r, float g, float b, CallbackInfo ci) {
      frameOverride = computeOverride();
      if (frameOverride) {
         ci.cancel();
         drawFullscreenSky();
      }
   }

   @Inject(
      method = {"renderSkyDark"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void cancelSkyDark(MatrixStack matrices, CallbackInfo ci) {
      if (frameOverride) {
         ci.cancel();
      }
   }

   private static boolean computeOverride() {
      BetterWorld mod = getModule();
      if (mod != null && mod.isEnabled() && mod.skyShader.getValue()) {
         if (SkyShaderHolder.getProgram() == null) {
            return false;
         } else {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.world == null) {
               return false;
            } else {
               float t = mc.world.getSkyAngle(mc.getRenderTickCounter().getTickDelta(false));
               return t >= 0.26F && t <= 0.74F;
            }
         }
      } else {
         return false;
      }
   }

   private static BetterWorld getModule() {
      if (!moduleLookupDone) {
         moduleLookupDone = true;

         try {
            cachedModule = (BetterWorld)CrolClient.INSTANCE.getModuleManager().getByClass(BetterWorld.class);
         } catch (Exception var1) {
            cachedModule = null;
         }
      }

      return cachedModule;
   }

   private static void drawFullscreenSky() {
      ShaderProgram prog = SkyShaderHolder.getProgram();
      if (prog != null) {
         MinecraftClient mc = MinecraftClient.getInstance();
         sProjMat.set(RenderSystem.getProjectionMatrix());
         sViewMat.set(RenderSystem.getModelViewMatrix());
         prog.initializeUniforms(DrawMode.QUADS, sViewMat, sProjMat, mc.getWindow());
         GlUniform uInvProj = prog.getUniform("InvProjMat");
         GlUniform uInvView = prog.getUniform("InvViewMat");
         if (uInvProj != null) {
            uInvProj.set((new Matrix4f(sProjMat)).invert());
         }

         if (uInvView != null) {
            uInvView.set((new Matrix4f(sViewMat)).invert());
         }

         SkyShaderHolder.uploadConfigUniforms();
         RenderSystem.depthMask(false);
         RenderSystem.disableDepthTest();
         RenderSystem.disableBlend();
         RenderSystem.disableCull();
         RenderSystem.setShader(prog);
         prog.bind();
         RenderSystem.backupProjectionMatrix();
         RenderSystem.setProjectionMatrix(sIdentity, ProjectionType.PERSPECTIVE);
         Matrix4fStack mvStack = RenderSystem.getModelViewStack();
         mvStack.pushMatrix();
         mvStack.identity();
         Tessellator tess = Tessellator.getInstance();
         BufferBuilder buf = tess.begin(DrawMode.QUADS, VertexFormats.POSITION);
         buf.vertex(-1.0F, -1.0F, 1.0F);
         buf.vertex(1.0F, -1.0F, 1.0F);
         buf.vertex(1.0F, 1.0F, 1.0F);
         buf.vertex(-1.0F, 1.0F, 1.0F);
         BufferRenderer.drawWithGlobalProgram(buf.end());
         mvStack.popMatrix();
         prog.unbind();
         RenderSystem.clearShader();
         RenderSystem.restoreProjectionMatrix();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(true);
         RenderSystem.enableCull();
      }
   }
}
