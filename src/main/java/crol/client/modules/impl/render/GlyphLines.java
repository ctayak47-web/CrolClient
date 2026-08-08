package crol.client.modules.impl.render;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import crol.client.CrolClient;
import crol.client.event.classes.Render3DEvent;
import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.IRenderable3D;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.IDisableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.util.IUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class GlyphLines extends Module implements IRenderable3D, ITickable, IUtil, IDisableable {
   public final FloatSetting quantity = ((FloatSetting)(new FloatSetting() {
   }).name("Quantity")).value(10.0F).minValue(3.0F).maxValue(60.0F).incriment(1.0F);
   public final BooleanSetting slow = ((BooleanSetting)(new BooleanSetting() {
      public void preChangeState(boolean value) {
         GlyphLines.this.glyphs.clear();
      }
   }).name("Slow")).value(true);
   private final Random random = new Random(93882L);
   private final List<GlyphVectorGenerator> glyphs = new ArrayList();
   private static final int RING_SEGS = 32;
   private static final int GLOW_SEGS = 24;
   private static final int SPARK_SEGS = 18;
   private static final float[] SIN_RING = new float[33];
   private static final float[] COS_RING = new float[33];
   private static final float[] SIN_GLOW = new float[25];
   private static final float[] COS_GLOW = new float[25];
   private static final float[] SIN_SPARK = new float[19];
   private static final float[] COS_SPARK = new float[19];
   private static final int RAY_COUNT = 8;
   private static final float[] RAY_COS = new float[8];
   private static final float[] RAY_SIN = new float[8];
   private static final int GLOW_LAYERS = 9;
   private static final int SPARK_RINGS = 6;
   private static final int RING_COUNT = 5;
   private static final float[] GLOW_POW = new float[9];
   private static final float[] SPARK_POW = new float[6];
   private static final float[] RING_POW = new float[5];
   private static final float[][] OFFSETS = new float[][]{{0.055F, 0.0F, 0.0F}, {-0.055F, 0.0F, 0.0F}, {0.0F, 0.055F, 0.0F}, {0.0F, -0.055F, 0.0F}, {0.0F, 0.0F, 0.055F}, {0.0F, 0.0F, -0.055F}};
   private static final float[] LAYER_A = new float[]{0.04F, 0.12F, 0.28F, 0.6F, 1.0F};
   private static final int PTS_BUF_SIZE = 32;
   private final double[] ptsX = new double[32];
   private final double[] ptsY = new double[32];
   private final double[] ptsZ = new double[32];
   private static final int[][] AXES;

   public GlyphLines() {
      super(new ModuleInfo("GlyphLines", Category.RENDER, "Добавляет в мир бегущие линии"));
      this.addSetting(new Setting[]{this.quantity, this.slow});
   }

   public void onDisable() {
      this.glyphs.clear();
   }

   public void onTick(TickEvent event) {
      if (mc.player != null) {
         for(int i = this.glyphs.size() - 1; i >= 0; --i) {
            GlyphVectorGenerator g = (GlyphVectorGenerator)this.glyphs.get(i);
            g.update();
            if (g.isExpired()) {
               this.glyphs.set(i, (GlyphVectorGenerator)this.glyphs.get(this.glyphs.size() - 1));
               this.glyphs.remove(this.glyphs.size() - 1);
            }
         }

         if (this.glyphs.size() < (int)this.quantity.getValue()) {
            this.tryAddGlyph();
         }

      }
   }

   public void onRender3D(Render3DEvent event) {
      if (!this.glyphs.isEmpty() && mc.player != null) {
         MatrixStack stack = event.getMatrixStack();
         Vec3d camVec = mc.getEntityRenderDispatcher().camera.getPos();
         double camX = camVec.x;
         double camY = camVec.y;
         double camZ = camVec.z;
         Matrix4f mat = stack.peek().getPositionMatrix();
         int rgb = CrolClient.INSTANCE.getThemeManager().getTheme().color().getRGB();
         int tr = rgb >> 16 & 255;
         int tg = rgb >> 8 & 255;
         int tb = rgb & 255;
         long now = System.currentTimeMillis();
         RenderSystem.disableCull();
         RenderSystem.depthMask(false);
         RenderSystem.enableDepthTest();
         RenderSystem.enableBlend();
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         Tessellator tess = Tessellator.getInstance();
         RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ONE, DstFactor.ZERO);
         int verts = 0;
         BufferBuilder buf = tess.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
         int gi = 0;

         for(int gn = this.glyphs.size(); gi < gn; ++gi) {
            GlyphVectorGenerator glyph = (GlyphVectorGenerator)this.glyphs.get(gi);
            float alpha = (float)glyph.getFadeOut();
            if (!(alpha < 0.01F)) {
               int total = glyph.fillPoints(this.ptsX, this.ptsY, this.ptsZ);
               if (total >= 2) {
                  float gp = this.smoothPulse(now, glyph.seed, 3000L);
                  float fp = this.smoothPulse(now, glyph.seed + (double)1.5F, 800L);
                  float pulse = 0.65F + gp * 0.25F + fp * 0.1F;
                  int tm1 = total - 1;

                  for(int oi = 0; oi < OFFSETS.length; ++oi) {
                     float ox = OFFSETS[oi][0];
                     float oy = OFFSETS[oi][1];
                     float oz = OFFSETS[oi][2];

                     for(int i = 0; i < tm1; ++i) {
                        float tA = (float)i / (float)tm1;
                        float tB = (float)(i + 1) / (float)tm1;
                        float aA = alpha * pulse * tA * tA * 0.05F;
                        float aB = alpha * pulse * tB * tB * 0.05F;
                        buf.vertex(mat, (float)(this.ptsX[i] - camX) + ox, (float)(this.ptsY[i] - camY) + oy, (float)(this.ptsZ[i] - camZ) + oz).color(tr, tg, tb, this.clamp(aA));
                        buf.vertex(mat, (float)(this.ptsX[i + 1] - camX) + ox, (float)(this.ptsY[i + 1] - camY) + oy, (float)(this.ptsZ[i + 1] - camZ) + oz).color(tr, tg, tb, this.clamp(aB));
                        verts += 2;
                     }
                  }
               }
            }
         }

         if (verts > 0) {
            BufferRenderer.drawWithGlobalProgram(buf.end());
         }

         RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE, SrcFactor.ONE, DstFactor.ZERO);
         verts = 0;
         buf = tess.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
         gi = 0;

         for(int gn = this.glyphs.size(); gi < gn; ++gi) {
            GlyphVectorGenerator glyph = (GlyphVectorGenerator)this.glyphs.get(gi);
            float alpha = (float)glyph.getFadeOut();
            if (!(alpha < 0.01F)) {
               int total = glyph.fillPoints(this.ptsX, this.ptsY, this.ptsZ);
               if (total >= 2) {
                  float gp = this.smoothPulse(now, glyph.seed, 3000L);
                  float fp = this.smoothPulse(now, glyph.seed + (double)1.5F, 800L);
                  float pulse = 0.6F + gp * 0.28F + fp * 0.12F;
                  int tm1 = total - 1;

                  for(int li = 0; li < LAYER_A.length; ++li) {
                     float la = LAYER_A[li];

                     for(int i = 0; i < tm1; ++i) {
                        float tA = (float)i / (float)tm1;
                        float tB = (float)(i + 1) / (float)tm1;
                        float shimA = 0.85F + this.smoothPulse(now, glyph.seed + (double)(tA * 4.0F), 600L) * 0.15F;
                        float shimB = 0.85F + this.smoothPulse(now, glyph.seed + (double)(tB * 4.0F), 600L) * 0.15F;
                        float aA = alpha * pulse * tA * tA * shimA * la;
                        float aB = alpha * pulse * tB * tB * shimB * la;
                        int iA = this.clamp(aA);
                        int iB = this.clamp(aB);
                        buf.vertex(mat, (float)(this.ptsX[i] - camX), (float)(this.ptsY[i] - camY), (float)(this.ptsZ[i] - camZ)).color(tr, tg, tb, iA);
                        buf.vertex(mat, (float)(this.ptsX[i + 1] - camX), (float)(this.ptsY[i + 1] - camY), (float)(this.ptsZ[i + 1] - camZ)).color(tr, tg, tb, iB);
                        verts += 2;
                     }
                  }
               }
            }
         }

         if (verts > 0) {
            BufferRenderer.drawWithGlobalProgram(buf.end());
         }

         verts = 0;

         for(int gn = this.glyphs.size(); verts < gn; ++verts) {
            GlyphVectorGenerator glyph = (GlyphVectorGenerator)this.glyphs.get(verts);
            float alpha = (float)glyph.getFadeOut();
            if (!(alpha < 0.01F)) {
               int total = glyph.fillPoints(this.ptsX, this.ptsY, this.ptsZ);
               if (total >= 2) {
                  float runT = (float)(((double)(now % 2500L) + glyph.seed * (double)400.0F) / (double)2500.0F) % 1.0F;
                  float runPulse = this.smoothPulse(now, glyph.seed + (double)3.0F, 500L);
                  float runBright = 0.75F + runPulse * 0.25F;
                  int runI = (int)(runT * (float)(total - 1));
                  float runF = runT * (float)(total - 1) - (float)runI;
                  if (runI < total - 1) {
                     double rx = this.ptsX[runI] + (this.ptsX[runI + 1] - this.ptsX[runI]) * (double)runF;
                     double ry = this.ptsY[runI] + (this.ptsY[runI + 1] - this.ptsY[runI]) * (double)runF;
                     double rz = this.ptsZ[runI] + (this.ptsZ[runI + 1] - this.ptsZ[runI]) * (double)runF;
                     this.renderSparkFlare(tess, mat, (float)(rx - camX), (float)(ry - camY), (float)(rz - camZ), tr, tg, tb, alpha * runBright * 0.9F, 0.1F);
                  }

                  float hx = (float)(this.ptsX[total - 1] - camX);
                  float hy = (float)(this.ptsY[total - 1] - camY);
                  float hz = (float)(this.ptsZ[total - 1] - camZ);
                  float hP1 = this.smoothPulse(now, glyph.seed * 2.1, 900L);
                  float hP2 = this.smoothPulse(now, glyph.seed * 1.3 + 0.7, 1400L);
                  float hPulse = 0.5F + hP1 * 0.3F + hP2 * 0.2F;
                  this.renderHeadFlare(tess, mat, hx, hy, hz, tr, tg, tb, alpha, hPulse, now, glyph.seed);
               }
            }
         }

         RenderSystem.depthMask(true);
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   private void renderHeadFlare(Tessellator tess, Matrix4f mat, float hx, float hy, float hz, int tr, int tg, int tb, float alpha, float hPulse, long now, double seed) {
      float glowMaxR = 0.11F + hPulse * 0.06F;
      float coreR = 0.011F + hPulse * 0.007F;
      int quadVerts = 0;
      BufferBuilder quads = tess.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);

      for(int g = 0; g < 9; ++g) {
         float gf = (float)g / 9.0F;
         float iR = glowMaxR * gf;
         float oR = glowMaxR * (gf + 0.11111111F);
         float ga = alpha * hPulse * GLOW_POW[g] * 0.42F;
         int gi = this.clamp(ga);
         if (gi >= 1) {
            int go = Math.max(0, gi / 18);

            for(int s = 0; s < 24; ++s) {
               float caA = COS_GLOW[s];
               float saA = SIN_GLOW[s];
               float caB = COS_GLOW[s + 1];
               float saB = SIN_GLOW[s + 1];
               quads.vertex(mat, hx + caA * oR, hy + saA * oR, hz).color(tr, tg, tb, go);
               quads.vertex(mat, hx + caA * iR, hy + saA * iR, hz).color(tr, tg, tb, gi);
               quads.vertex(mat, hx + caB * iR, hy + saB * iR, hz).color(tr, tg, tb, gi);
               quads.vertex(mat, hx + caB * oR, hy + saB * oR, hz).color(tr, tg, tb, go);
               quadVerts += 4;
            }
         }
      }

      int ci = this.clamp(alpha * hPulse);
      int ce = this.clamp(alpha * hPulse * 0.12F);
      if (ci > 0) {
         for(int s = 0; s < 24; ++s) {
            float caA = COS_GLOW[s];
            float saA = SIN_GLOW[s];
            float caB = COS_GLOW[s + 1];
            float saB = SIN_GLOW[s + 1];
            quads.vertex(mat, hx, hy, hz).color(tr, tg, tb, ci);
            quads.vertex(mat, hx + caA * coreR, hy + saA * coreR, hz).color(tr, tg, tb, ce);
            quads.vertex(mat, hx + caB * coreR, hy + saB * coreR, hz).color(tr, tg, tb, ce);
            quads.vertex(mat, hx, hy, hz).color(tr, tg, tb, ci);
            quadVerts += 4;
         }
      }

      if (quadVerts > 0) {
         BufferRenderer.drawWithGlobalProgram(quads.end());
      }

      float maxR = 0.048F + hPulse * 0.038F;
      int lineVerts = 0;
      BufferBuilder lines = tess.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

      for(int ring = 0; ring < 5; ++ring) {
         float rf = (float)ring / 5.0F;
         float ringR = maxR * (0.15F + rf * 0.85F);
         float ringA = alpha * hPulse * RING_POW[ring] * 0.88F;
         int ri = this.clamp(ringA);
         int ri2 = this.clamp(ringA * 0.58F);
         int ri3 = this.clamp(ringA * 0.38F);
         if (ri >= 2) {
            for(int s = 0; s < 32; ++s) {
               float caA = COS_RING[s];
               float saA = SIN_RING[s];
               float caB = COS_RING[s + 1];
               float saB = SIN_RING[s + 1];
               lines.vertex(mat, hx + caA * ringR, hy + saA * ringR, hz).color(tr, tg, tb, ri);
               lines.vertex(mat, hx + caB * ringR, hy + saB * ringR, hz).color(tr, tg, tb, ri);
               lines.vertex(mat, hx + caA * ringR, hy, hz + saA * ringR).color(tr, tg, tb, ri2);
               lines.vertex(mat, hx + caB * ringR, hy, hz + saB * ringR).color(tr, tg, tb, ri2);
               lines.vertex(mat, hx, hy + caA * ringR, hz + saA * ringR).color(tr, tg, tb, ri3);
               lines.vertex(mat, hx, hy + caB * ringR, hz + saB * ringR).color(tr, tg, tb, ri3);
               lineVerts += 6;
            }
         }
      }

      for(int i = 0; i < 8; ++i) {
         float rayPulse = this.smoothPulse(now, seed + (double)i * 0.8, 480L + (long)i * 65L);
         float rayLen = 0.048F + rayPulse * 0.075F;
         float rayA = alpha * (0.32F + rayPulse * 0.68F);
         float startR = coreR * 1.3F;
         float ca = RAY_COS[i];
         float sa = RAY_SIN[i];
         lines.vertex(mat, hx + ca * startR, hy + sa * startR, hz).color(tr, tg, tb, this.clamp(rayA));
         lines.vertex(mat, hx + ca * (startR + rayLen), hy + sa * (startR + rayLen), hz).color(tr, tg, tb, 0);
         lineVerts += 2;
      }

      if (lineVerts > 0) {
         BufferRenderer.drawWithGlobalProgram(lines.end());
      }

   }

   private void renderSparkFlare(Tessellator tess, Matrix4f mat, float sx, float sy, float sz, int tr, int tg, int tb, float alpha, float maxR) {
      int verts = 0;
      BufferBuilder glow = tess.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);

      for(int g = 0; g < 6; ++g) {
         float gf = (float)g / 6.0F;
         float iR = maxR * gf;
         float oR = maxR * (gf + 0.16666667F);
         float ga = alpha * SPARK_POW[g] * 0.88F;
         int gi = this.clamp(ga);
         if (gi >= 1) {
            int go = Math.max(0, gi / 20);

            for(int s = 0; s < 18; ++s) {
               float caA = COS_SPARK[s];
               float saA = SIN_SPARK[s];
               float caB = COS_SPARK[s + 1];
               float saB = SIN_SPARK[s + 1];
               glow.vertex(mat, sx + caA * oR, sy + saA * oR, sz).color(tr, tg, tb, go);
               glow.vertex(mat, sx + caA * iR, sy + saA * iR, sz).color(tr, tg, tb, gi);
               glow.vertex(mat, sx + caB * iR, sy + saB * iR, sz).color(tr, tg, tb, gi);
               glow.vertex(mat, sx + caB * oR, sy + saB * oR, sz).color(tr, tg, tb, go);
               verts += 4;
            }
         }
      }

      if (verts > 0) {
         BufferRenderer.drawWithGlobalProgram(glow.end());
      }

   }

   private float smoothPulse(long now, double seed, long periodMs) {
      float t = (float)((now + (long)(seed * (double)1000.0F)) % periodMs) / (float)periodMs;
      float s = (float)(Math.sin((double)t * Math.PI * (double)2.0F) * (double)0.5F + (double)0.5F);
      return s * s * (3.0F - 2.0F * s);
   }

   private int clamp(float a) {
      return Math.max(0, Math.min(255, (int)(a * 255.0F)));
   }

   private void tryAddGlyph() {
      if (mc.player != null) {
         float fov = (float)(Integer)mc.options.getFov().getValue();
         float baseYaw = mc.player.getYaw();
         int dist = this.random.nextInt(19) + 6;
         int yaw = this.random.nextInt((int)(fov * 1.5F) + 1) + (int)(baseYaw - fov * 0.75F);
         double rad = Math.toRadians((double)yaw);
         Vec3d eye = mc.player.getEyePos();
         double sx = eye.x - Math.sin(rad) * (double)dist;
         double sy = eye.y + (double)this.random.nextInt(25) - (double)12.0F;
         double sz = eye.z + Math.cos(rad) * (double)dist;
         this.glyphs.add(new GlyphVectorGenerator(sx, sy, sz, this.random.nextInt(6) + 7, this.random.nextDouble() * Math.PI * (double)2.0F));
      }
   }

   static {
      for(int i = 0; i <= 32; ++i) {
         double a = 0.19634954084936207 * (double)i;
         SIN_RING[i] = (float)Math.sin(a);
         COS_RING[i] = (float)Math.cos(a);
      }

      for(int i = 0; i <= 24; ++i) {
         double a = 0.2617993877991494 * (double)i;
         SIN_GLOW[i] = (float)Math.sin(a);
         COS_GLOW[i] = (float)Math.cos(a);
      }

      for(int i = 0; i <= 18; ++i) {
         double a = 0.3490658503988659 * (double)i;
         SIN_SPARK[i] = (float)Math.sin(a);
         COS_SPARK[i] = (float)Math.cos(a);
      }

      for(int i = 0; i < 8; ++i) {
         double a = (Math.PI / 4D) * (double)i;
         RAY_COS[i] = (float)Math.cos(a);
         RAY_SIN[i] = (float)Math.sin(a);
      }

      for(int g = 0; g < 9; ++g) {
         float f = 1.0F - (float)g / 9.0F;
         GLOW_POW[g] = (float)Math.pow((double)f, (double)2.8F);
      }

      for(int g = 0; g < 6; ++g) {
         float f = 1.0F - (float)g / 6.0F;
         SPARK_POW[g] = (float)Math.pow((double)f, (double)2.2F);
      }

      for(int r = 0; r < 5; ++r) {
         float f = 1.0F - (float)r / 5.0F;
         RING_POW[r] = (float)Math.pow((double)f, (double)1.6F);
      }

      AXES = new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
   }

   @Environment(EnvType.CLIENT)
   public class GlyphVectorGenerator {
      private static final int MAX_NODES = 20;
      private final double[] nodeX = new double[20];
      private final double[] nodeY = new double[20];
      private final double[] nodeZ = new double[20];
      private int nodeCount = 0;
      private int drawnNodes = 1;
      private double segProgress = (double)0.0F;
      private final double segSpeed;
      private byte dirX;
      private byte dirY;
      private byte dirZ;
      private boolean fadingOut = false;
      private long fadeStart = -1L;
      private static final long FADE_MS = 700L;
      private int tailNode = 0;
      private double tailProgress = (double)0.0F;
      private static final int MAX_VISIBLE_NODES = 14;
      final double seed;

      public GlyphVectorGenerator(double spawnX, double spawnY, double spawnZ, int maxSteps, double seed) {
         this.seed = seed;
         this.nodeX[0] = spawnX;
         this.nodeY[0] = spawnY;
         this.nodeZ[0] = spawnZ;
         this.nodeCount = 1;
         this.segSpeed = GlyphLines.this.slow.getValue() ? 0.04 : 0.1;
         this.randomAxisDir();
         this.buildPath(maxSteps);
      }

      private void buildPath(int steps) {
         for(int s = 0; s < steps && this.nodeCount < 20; ++s) {
            this.nextDir();
            int len = GlyphLines.this.random.nextInt(3) + 1;
            this.nodeX[this.nodeCount] = this.nodeX[this.nodeCount - 1] + (double)(this.dirX * len);
            this.nodeY[this.nodeCount] = this.nodeY[this.nodeCount - 1] + (double)(this.dirY * len);
            this.nodeZ[this.nodeCount] = this.nodeZ[this.nodeCount - 1] + (double)(this.dirZ * len);
            ++this.nodeCount;
         }

      }

      private void randomAxisDir() {
         int idx = GlyphLines.this.random.nextInt(GlyphLines.AXES.length);
         this.dirX = (byte)GlyphLines.AXES[idx][0];
         this.dirY = (byte)GlyphLines.AXES[idx][1];
         this.dirZ = (byte)GlyphLines.AXES[idx][2];
      }

      private void nextDir() {
         int count = 0;

         for(int[] axis : GlyphLines.AXES) {
            if (axis[0] != -this.dirX || axis[1] != -this.dirY || axis[2] != -this.dirZ) {
               ++count;
            }
         }

         int pick = GlyphLines.this.random.nextInt(count);

         for(int[] axis : GlyphLines.AXES) {
            if ((axis[0] != -this.dirX || axis[1] != -this.dirY || axis[2] != -this.dirZ) && pick-- == 0) {
               this.dirX = (byte)axis[0];
               this.dirY = (byte)axis[1];
               this.dirZ = (byte)axis[2];
               return;
            }
         }

      }

      public void update() {
         if (!this.fadingOut) {
            if (this.drawnNodes < this.nodeCount) {
               this.segProgress += this.segSpeed;
               if (this.segProgress >= (double)1.0F) {
                  --this.segProgress;
                  ++this.drawnNodes;
               }
            } else {
               this.fadingOut = true;
               this.fadeStart = System.currentTimeMillis();
            }

            if (this.drawnNodes - this.tailNode > 14) {
               this.tailProgress += this.segSpeed;
               if (this.tailProgress >= (double)1.0F) {
                  --this.tailProgress;
                  ++this.tailNode;
               }
            }

         }
      }

      public int fillPoints(double[] xs, double[] ys, double[] zs) {
         if (this.nodeCount < 2) {
            return 0;
         } else {
            int from = Math.min(this.tailNode, this.nodeCount - 1);
            int to = Math.min(this.drawnNodes, this.nodeCount - 1);
            int out = 0;
            if (from < this.nodeCount - 1) {
               double t = this.tailProgress;
               xs[out] = this.nodeX[from] + (this.nodeX[from + 1] - this.nodeX[from]) * t;
               ys[out] = this.nodeY[from] + (this.nodeY[from + 1] - this.nodeY[from]) * t;
               zs[out] = this.nodeZ[from] + (this.nodeZ[from + 1] - this.nodeZ[from]) * t;
            } else {
               xs[out] = this.nodeX[from];
               ys[out] = this.nodeY[from];
               zs[out] = this.nodeZ[from];
            }

            ++out;

            for(int i = from + 1; i <= to && out < xs.length; ++i) {
               xs[out] = this.nodeX[i];
               ys[out] = this.nodeY[i];
               zs[out] = this.nodeZ[i];
               ++out;
            }

            if (this.drawnNodes < this.nodeCount && out > 0) {
               --out;
               double t = this.segProgress;
               int dn = this.drawnNodes;
               xs[out] = this.nodeX[dn - 1] + (this.nodeX[dn] - this.nodeX[dn - 1]) * t;
               ys[out] = this.nodeY[dn - 1] + (this.nodeY[dn] - this.nodeY[dn - 1]) * t;
               zs[out] = this.nodeZ[dn - 1] + (this.nodeZ[dn] - this.nodeZ[dn - 1]) * t;
               ++out;
            }

            return out;
         }
      }

      public double getFadeOut() {
         if (!this.fadingOut) {
            return (double)1.0F;
         } else {
            float t = (float)(System.currentTimeMillis() - this.fadeStart) / 700.0F;
            return t >= 1.0F ? (double)0.0F : (double)1.0F - (double)(t * t * t);
         }
      }

      public boolean isExpired() {
         return this.fadingOut && this.getFadeOut() <= (double)0.0F;
      }
   }
}
