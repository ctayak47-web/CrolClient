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
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.modules.settings.impl.ModeSetting;
import crol.client.modules.settings.impl.MultiBoxSetting;
import crol.client.util.IUtil;
import crol.client.util.animations.Animation;
import crol.client.util.animations.Direction;
import crol.client.util.animations.impl.EaseBackIn;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class JumpCircle extends Module implements IRenderable3D, ITickable, IUtil {
   private final List<Circle> circles = new ArrayList();
   private final List<PlayerEntity> cache = new CopyOnWriteArrayList();
   private final ModeSetting mode = ((ModeSetting)(new ModeSetting()).name("Mode")).value("Pulse").modes("Pulse", "Spiral", "Nova");
   private final FloatSetting radius = ((FloatSetting)(new FloatSetting()).name("Radius")).value(0.5F).minValue(0.3F).maxValue(1.0F).incriment(0.05F);
   private final FloatSetting time = ((FloatSetting)(new FloatSetting()).name("Time")).value(1000.0F).minValue(200.0F).maxValue(2000.0F).incriment(100.0F);
   private final FloatSetting animSpeed = ((FloatSetting)(new FloatSetting()).name("Anim")).value(500.0F).minValue(100.0F).maxValue(1000.0F).incriment(50.0F);
   private final MultiBoxSetting animations = ((MultiBoxSetting)(new MultiBoxSetting()).name("Animations")).booleanSettings(((BooleanSetting)(new BooleanSetting()).name("Size")).value(true), ((BooleanSetting)(new BooleanSetting()).name("Alpha")).value(true));
   private final FloatSetting animValue = ((FloatSetting)(new FloatSetting() {
      public boolean isVisible() {
         return JumpCircle.this.animations.getValueByName("Size");
      }
   }).name("AnimValue")).value(1.0F).minValue(0.05F).maxValue(1.0F).incriment(0.01F);
   private static final Quaternionf ROT_X_90;
   private static final int LUT_SIZE = 512;
   private static final float[] SIN_LUT;
   private static final float[] COS_LUT;
   private static final float[] PULSE_LAYER_A;
   private static final float[] PULSE_LEN_M;
   private static final float[] SPIRAL_LAYER_ALPHA;
   private final float[] orbitRsBuf = new float[2];
   private static final float[] ARC_SPEEDS;
   private static final float[] ARC_LEN;
   private static final float[] ARC_R_MULT;
   private static final int[] ORBIT_DOT_COUNTS;
   private static final float[] ORBIT_SPDS;
   private static final int[] ORBIT_DIRS;
   private static final float[] NOVA_LAYER_A;
   private static final float[] NOVA_LAYER_W;
   private static final double TWO_PI_ORBIT_0;
   private static final double TWO_PI_ORBIT_1;
   private static final double TWO_PI_ARC_6 = (Math.PI / 3D);
   private static final int SPOKE_SEGS = 10;
   private static final float[] SPOKE_EDGE;
   private static final float[] ARC_GLOW_POW;
   private static final float[] DOT_GLOW_POW;
   private static final float[] CENTER_GLOW_POW;
   private static final float[] HALO_POW;
   private static final int ARC_EDGE_LUT_SIZE = 64;
   private static final float[] ARC_EDGE_LUT;
   private final float[] orbitCos0;
   private final float[] orbitSin0;
   private final float[] orbitCos1;
   private final float[] orbitSin1;
   private final double[] arcStartA;
   private static final float[] NEB_POW;
   private static final float[] PULSE_CENTER_POW;
   private static final int TRAIL_SEGS = 40;
   private static final float[] TRAIL_EDGE;
   private static final float[] TRAIL_ANGLE_OFFSET;
   private static final float[] TRAIL_F;
   private static final float[] TRAIL_ANGLE_OFFSET_B;
   private static final float[] TRAIL_F_B;
   private static final int RAY_COUNT = 16;
   private static final double TWO_PI_RAY_16 = (Math.PI / 8D);
   private static final float[] SPIRAL_HALO_POW;
   private static final float[] SPIRAL_INNER_POW;
   private static final float[] SPIRAL_TIP_POW;
   private static final float[] TRAIL_COS_OFFSET;
   private static final float[] TRAIL_SIN_OFFSET;
   private static final float[] TRAIL_COS_OFFSET_B;
   private static final float[] TRAIL_SIN_OFFSET_B;
   private static final float TAIL_COS_DELTA;
   private static final float TAIL_SIN_DELTA;
   private static final double TWO_PI_SPIRAL_3 = 2.0943951023931953;
   private static final double TWO_PI_SPOKE_12 = (Math.PI / 6D);
   private static final int SPIRAL_SEGS = 80;
   private static final float[] SPIRAL_EDGE_POW07;
   private int cachedThemeRGB;
   private int transparentColor;

   public JumpCircle() {
      super(new ModuleInfo("JumpCircle", Category.RENDER, "Рисует под игроком круг при прыжке и приземлении"));
      this.orbitCos0 = new float[ORBIT_DOT_COUNTS[0]];
      this.orbitSin0 = new float[ORBIT_DOT_COUNTS[0]];
      this.orbitCos1 = new float[ORBIT_DOT_COUNTS[1]];
      this.orbitSin1 = new float[ORBIT_DOT_COUNTS[1]];
      this.arcStartA = new double[6];
      this.addSetting(new Setting[]{this.mode, this.radius, this.time, this.animSpeed, this.animations, this.animValue});
   }

   public void onRender3D(Render3DEvent event) {
      if (!this.circles.isEmpty()) {
         MatrixStack stack = event.getMatrixStack();
         RenderSystem.enableBlend();
         RenderSystem.disableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE, SrcFactor.ONE, DstFactor.ZERO);
         this.cachedThemeRGB = CrolClient.INSTANCE.getThemeManager().getTheme().color().getRGB();
         this.transparentColor = this.cachedThemeRGB & 16777215;
         Vec3d cp = mc.getEntityRenderDispatcher().camera.getPos();
         double camX = cp.x;
         double camY = cp.y;
         double camZ = cp.z;
         boolean animAlpha = this.animations.getValueByName("Alpha");
         boolean animSize = this.animations.getValueByName("Size");
         float animVal = this.animValue.getValue();
         float rad = this.radius.getValue();
         long timeoutMs = (long)(this.animSpeed.getValue() + this.time.getValue());
         long now = System.currentTimeMillis();
         String modeName = this.mode.getValue();
         int modeId = "Pulse".equals(modeName) ? 0 : ("Spiral".equals(modeName) ? 1 : 2);
         int ci = 0;

         for(int cn = this.circles.size(); ci < cn; ++ci) {
            Circle c = (Circle)this.circles.get(ci);
            double animOut = c.animation.getOutput();
            float alpha = animAlpha ? (float)animOut : 2.0F;
            float sizeF = animSize ? (float)((double)(1.0F - animVal) + animOut * (double)animVal) : 1.0F;
            float r = sizeF * rad;
            if (!(alpha < 0.01F)) {
               if (c.animation.finished(Direction.FORWARDS) && now - c.timerStart >= timeoutMs) {
                  c.animation.setDirection(Direction.BACKWARDS);
                  c.timerStart = now;
               }

               long elapsed = now - c.birthTime;
               stack.push();
               stack.translate(c.pos.x - camX, c.pos.y - camY, c.pos.z - camZ);
               stack.multiply(ROT_X_90);
               Matrix4f mat = stack.peek().getPositionMatrix();
               switch (modeId) {
                  case 0 -> this.drawPulse(mat, r, alpha, elapsed, this.cachedThemeRGB);
                  case 1 -> this.drawSpiral(mat, r, alpha, elapsed, this.cachedThemeRGB);
                  default -> this.drawNova(mat, r, alpha, elapsed, this.cachedThemeRGB);
               }

               stack.pop();
            }
         }

         RenderSystem.depthMask(true);
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   private void drawPulse(Matrix4f mat, float r, float alpha, long elapsed, int theme) {
      Tessellator tess = Tessellator.getInstance();
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      BufferBuilder fills = tess.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);

      for(int g = 0; g < 12; ++g) {
         float gf = (float)g / 12.0F;
         float grI = r * (1.0F + gf * 0.7F);
         float grO = r * (1.0F + (gf + 0.083333336F) * 0.7F);
         float ga = alpha * (1.0F - gf) * (1.0F - gf) * 0.18F;
         this.batchFilledRing(fills, mat, grI, grO, 48, this.col(theme, ga));
      }

      int nebSegs = 52;
      int nebBands = 7;
      double elapsedD = (double)elapsed * 0.0028;

      for(int b = 0; b < nebBands; ++b) {
         float bf = (float)b / (float)nebBands;
         float pulse = (float)(Math.sin(elapsedD + (double)b * 0.95) * (double)0.5F + (double)0.5F);
         float bandR = r * (0.15F + bf * 0.72F);
         float thick = r * (0.055F + pulse * 0.035F);
         float ba = alpha * NEB_POW[b] * (0.06F + pulse * 0.1F);
         if (!(ba < 0.004F)) {
            this.batchFilledRing(fills, mat, bandR - thick, bandR + thick, nebSegs, this.col(theme, ba));
         }
      }

      float trailRot = (float)((double)elapsed * 3.5E-4);
      double elapsedTrail = (double)elapsed * 0.003;
      float halfW = r * 0.03F;

      for(int arm = 0; arm < 2; ++arm) {
         float cosBase = (float)Math.cos((double)arm * Math.PI + (double)trailRot);
         float sinBase = (float)Math.sin((double)arm * Math.PI + (double)trailRot);

         for(int seg = 0; seg < 39; ++seg) {
            float edge = TRAIL_EDGE[seg];
            float fA = TRAIL_F[seg];
            float pulse = (float)(Math.sin(elapsedTrail + (double)(fA * 5.0F) + (double)arm * 2.1) * (double)0.25F + (double)0.75F);
            float sa = alpha * edge * pulse * 0.22F;
            if (!(sa < 0.004F)) {
               float coA = cosBase * TRAIL_COS_OFFSET[seg] - sinBase * TRAIL_SIN_OFFSET[seg];
               float siA = sinBase * TRAIL_COS_OFFSET[seg] + cosBase * TRAIL_SIN_OFFSET[seg];
               float coB = cosBase * TRAIL_COS_OFFSET_B[seg] - sinBase * TRAIL_SIN_OFFSET_B[seg];
               float siB = sinBase * TRAIL_COS_OFFSET_B[seg] + cosBase * TRAIL_SIN_OFFSET_B[seg];
               float dxA = -siA;
               float dxB = -siB;
               float fB = TRAIL_F_B[seg];
               float rA = fA * r * 0.88F;
               float rB = fB * r * 0.88F;
               fills.vertex(mat, coA * rA + dxA * halfW, siA * rA + coA * halfW, 0.0F).color(this.col(theme, sa));
               fills.vertex(mat, coA * rA - dxA * halfW, siA * rA - coA * halfW, 0.0F).color(this.col(theme, sa * 0.15F));
               fills.vertex(mat, coB * rB - dxB * halfW, siB * rB - coB * halfW, 0.0F).color(this.col(theme, sa * 0.15F));
               fills.vertex(mat, coB * rB + dxB * halfW, siB * rB + coB * halfW, 0.0F).color(this.col(theme, sa));
            }
         }
      }

      float cp = (float)(Math.sin((double)elapsed * 0.005) * (double)0.5F + (double)0.5F);
      float cpMod = 0.35F + cp * 0.3F;

      for(int g = 0; g < 9; ++g) {
         float gr = 0.006F + (float)g * 0.016F;
         float ga = alpha * PULSE_CENTER_POW[g] * cpMod;
         this.batchFilledRingOffset(fills, mat, gr * 0.25F, gr, 20, this.col(theme, ga), 0.0F, 0.0F);
      }

      BufferRenderer.drawWithGlobalProgram(fills.end());
      BufferBuilder lines = tess.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
      this.batchRing(lines, mat, r * 1.12F, 64, this.col(theme, alpha * 0.05F));
      this.batchRing(lines, mat, r * 1.06F, 64, this.col(theme, alpha * 0.14F));
      this.batchRing(lines, mat, r * 1.02F, 64, this.col(theme, alpha * 0.35F));
      this.batchRing(lines, mat, r * 1.0F, 64, this.col(theme, alpha * 0.8F));
      this.batchRing(lines, mat, r * 0.98F, 64, this.col(theme, alpha * 1.0F));
      double rot = (double)elapsed * 6.5E-4;
      double elapsedRay = (double)elapsed * 0.0045;

      for(int i = 0; i < 16; ++i) {
         double angle = (Math.PI / 8D) * (double)i + rot;
         float pulse = (float)(Math.sin(elapsedRay + (double)i * 0.6) * (double)0.5F + (double)0.5F);
         float rLen = r * (0.1F + pulse * 0.35F);
         float rS = r * 1.005F;
         float ca = (float)Math.cos(angle);
         float sa2 = (float)Math.sin(angle);
         float[] layerA = PULSE_LAYER_A;
         float[] lenM = PULSE_LEN_M;
         float pulsedAlphaBase = 0.2F + pulse * 0.6F;

         for(int l = 0; l < 3; ++l) {
            float la = alpha * pulsedAlphaBase * layerA[l];
            lines.vertex(mat, ca * rS, sa2 * rS, 0.0F).color(this.col(theme, la));
            lines.vertex(mat, ca * (rS + rLen * lenM[l]), sa2 * (rS + rLen * lenM[l]), 0.0F).color(this.transparentColor);
         }
      }

      for(int e = 0; e < 3; ++e) {
         float ep = (float)((elapsed + (long)e * 500L) % 1600L) / 1600.0F;
         float er = r * (1.0F + ep * 0.85F);
         float ea = alpha * (1.0F - ep) * (1.0F - ep) * 0.45F;
         if (!(ea < 0.004F)) {
            this.batchRing(lines, mat, er, 56, this.col(theme, ea * 0.4F));
            this.batchRing(lines, mat, er * 0.991F, 56, this.col(theme, ea * 0.8F));
            this.batchRing(lines, mat, er * 0.982F, 56, this.col(theme, ea));
         }
      }

      BufferRenderer.drawWithGlobalProgram(lines.end());
   }

   private void drawSpiral(Matrix4f mat, float r, float alpha, long elapsed, int theme) {
      Tessellator tess = Tessellator.getInstance();
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      BufferBuilder fills = tess.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      int gs = 44;

      for(int g = 0; g < 10; ++g) {
         float gf = (float)g / 10.0F;
         float gr = r * (1.0F + gf * 0.55F);
         float ga = alpha * SPIRAL_HALO_POW[g] * 0.16F;
         this.batchFilledRing(fills, mat, gr * 0.976F, gr, gs, this.col(theme, ga));
      }

      for(int i = 0; i < 10; ++i) {
         float rf = (float)i / 10.0F;
         float ia = alpha * SPIRAL_INNER_POW[i] * 0.22F;
         this.batchFilledRing(fills, mat, r * rf * 0.92F, r * (rf + 0.1F) * 0.92F, gs, this.col(theme, ia));
      }

      int spirals = 3;

      for(int s = 0; s < spirals; ++s) {
         double phase = (double)elapsed * 0.0013 * (s % 2 == 0 ? (double)1.0F : -1.15) + 2.0943951023931953 * (double)s;
         double tipA = 17.27875959474386 + phase;
         float tx = (float)(Math.cos(tipA) * (double)r);
         float ty = (float)(Math.sin(tipA) * (double)r);
         float pulse = (float)(Math.sin((double)elapsed * 0.006 + (double)s * 2.1) * (double)0.5F + (double)0.5F);
         float pBase = alpha * pulse * 0.75F;

         for(int g = 0; g < 7; ++g) {
            float gr = 0.015F + (float)g * 0.028F;
            float ga = pBase * SPIRAL_TIP_POW[g];
            this.batchFilledRingOffset(fills, mat, gr * 0.4F, gr, 20, this.col(theme, ga), tx, ty);
         }
      }

      BufferRenderer.drawWithGlobalProgram(fills.end());
      BufferBuilder lines = tess.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
      int segs = 80;

      for(int s = 0; s < spirals; ++s) {
         double dir = s % 2 == 0 ? (double)1.0F : -1.15;
         double phase = (double)elapsed * 0.0013 * dir + 2.0943951023931953 * (double)s;
         float[] layerAlpha = SPIRAL_LAYER_ALPHA;

         for(int i = 0; i < segs - 1; ++i) {
            float fA = (float)i / (float)segs;
            float fB = (float)(i + 1) / (float)segs;
            float edge = SPIRAL_EDGE_POW07[i];
            float pulse = (float)(Math.sin((double)elapsed * 0.004 + (double)(fA * 8.0F) + (double)s) * 0.06 + 0.94);
            float rA = fA * r * pulse;
            float rB = fB * r * pulse;
            double aA = (double)fA * Math.PI * (double)5.5F + phase;
            double aB = (double)fB * Math.PI * (double)5.5F + phase;
            float coA = (float)Math.cos(aA);
            float siA = (float)Math.sin(aA);
            float coB = (float)Math.cos(aB);
            float siB = (float)Math.sin(aB);
            float xA = coA * rA;
            float yA = siA * rA;
            float xB = coB * rB;
            float yB = siB * rB;

            for(int layer = 0; layer < 3; ++layer) {
               float sa = alpha * edge * layerAlpha[layer] * pulse;
               if (!(sa < 0.008F)) {
                  lines.vertex(mat, xA, yA, 0.0F).color(this.col(theme, sa));
                  lines.vertex(mat, xB, yB, 0.0F).color(this.col(theme, sa));
               }
            }
         }
      }

      this.batchRing(lines, mat, r * 1.1F, 64, this.col(theme, alpha * 0.05F));
      this.batchRing(lines, mat, r * 1.05F, 64, this.col(theme, alpha * 0.15F));
      this.batchRing(lines, mat, r * 1.01F, 64, this.col(theme, alpha * 0.4F));
      this.batchRing(lines, mat, r * 1.0F, 64, this.col(theme, alpha * 0.85F));
      this.batchRing(lines, mat, r * 0.97F, 64, this.col(theme, alpha * 0.5F));

      for(int e = 0; e < 2; ++e) {
         float ep = (float)((elapsed + (long)e * 700L) % 1400L) / 1400.0F;
         float er = r * (1.0F + ep * 0.7F);
         float ea = alpha * (1.0F - ep) * (1.0F - ep) * 0.38F;
         if (!(ea < 0.004F)) {
            this.batchRing(lines, mat, er, 56, this.col(theme, ea * 0.5F));
            this.batchRing(lines, mat, er * 0.989F, 56, this.col(theme, ea));
         }
      }

      BufferRenderer.drawWithGlobalProgram(lines.end());
   }

   private void drawNova(Matrix4f mat, float r, float alpha, long elapsed, int theme) {
      Tessellator tess = Tessellator.getInstance();
      int arcCount = 6;

      for(int a = 0; a < 6; ++a) {
         this.arcStartA[a] = (double)((float)elapsed * ARC_SPEEDS[a]) + (double)a * (Math.PI / 3D);
      }

      double orbitBase0 = (double)((float)elapsed * ORBIT_SPDS[0] * (float)ORBIT_DIRS[0]);
      double orbitBase1 = (double)((float)elapsed * ORBIT_SPDS[1] * (float)ORBIT_DIRS[1]);

      for(int i = 0; i < ORBIT_DOT_COUNTS[0]; ++i) {
         double a = TWO_PI_ORBIT_0 * (double)i + orbitBase0;
         this.orbitCos0[i] = (float)Math.cos(a);
         this.orbitSin0[i] = (float)Math.sin(a);
      }

      for(int i = 0; i < ORBIT_DOT_COUNTS[1]; ++i) {
         double a = TWO_PI_ORBIT_1 * (double)i + orbitBase1;
         this.orbitCos1[i] = (float)Math.cos(a);
         this.orbitSin1[i] = (float)Math.sin(a);
      }

      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      BufferBuilder fills = tess.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      int gs = 52;

      for(int g = 0; g < 14; ++g) {
         float gf = (float)g / 14.0F;
         float gr = r * (1.0F + gf * 0.9F);
         float ga = alpha * HALO_POW[g] * 0.2F;
         this.batchFilledRing(fills, mat, gr * 0.976F, gr, gs, this.col(theme, ga));
      }

      float webR = r * 0.88F;
      double webRot = (double)elapsed * 8.0E-4;

      for(int sp = 0; sp < 12; ++sp) {
         double angle = (Math.PI / 6D) * (double)sp + webRot;
         float pulse3 = (float)(Math.sin((double)elapsed * 0.005 + (double)sp * 0.8) * (double)0.5F + (double)0.5F);
         float pulseAlpha = 0.08F + pulse3 * 0.12F;
         float ca = (float)Math.cos(angle);
         float si = (float)Math.sin(angle);

         for(int seg = 0; seg < 9; ++seg) {
            float edgeA = SPOKE_EDGE[seg];
            float edgeB = SPOKE_EDGE[seg + 1];
            float sa = alpha * edgeA * pulseAlpha;
            float sb = alpha * edgeB * pulseAlpha;
            if (!(sa < 0.004F) || !(sb < 0.004F)) {
               float fA = (float)seg / 10.0F;
               float fB = (float)(seg + 1) / 10.0F;
               float rA = fA * webR;
               float rB = fB * webR;
               fills.vertex(mat, ca * rA + si * 0.025F, si * rA - ca * 0.025F, 0.0F).color(this.col(theme, sa));
               fills.vertex(mat, ca * rA - si * 0.025F, si * rA + ca * 0.025F, 0.0F).color(this.col(theme, sa));
               fills.vertex(mat, ca * rB - si * 0.025F, si * rB + ca * 0.025F, 0.0F).color(this.col(theme, sb));
               fills.vertex(mat, ca * rB + si * 0.025F, si * rB - ca * 0.025F, 0.0F).color(this.col(theme, sb));
            }
         }
      }

      for(int wr = 1; wr <= 4; ++wr) {
         float wrf = (float)wr / 4.0F;
         float wRad = webR * wrf;
         float pulse4 = (float)(Math.sin((double)elapsed * 0.004 + (double)wr * 1.2) * (double)0.5F + (double)0.5F);
         float wa = alpha * (1.0F - wrf * 0.5F) * (0.08F + pulse4 * 0.12F);
         this.batchFilledRing(fills, mat, wRad * 0.985F, wRad * 1.015F, 40, this.col(theme, wa));
      }

      for(int g = 0; g < 10; ++g) {
         float gr = 0.01F + (float)g * 0.02F;
         float ga = alpha * CENTER_GLOW_POW[g] * 0.65F;
         this.batchFilledRingOffset(fills, mat, gr * 0.25F, gr, 20, this.col(theme, ga), 0.0F, 0.0F);
      }

      for(int a = 0; a < 6; ++a) {
         float arcR = r * ARC_R_MULT[a];
         double tipA = this.arcStartA[a] + (Math.PI * 2D) * (double)ARC_LEN[a];
         float tx = (float)(Math.cos(tipA) * (double)arcR);
         float ty = (float)(Math.sin(tipA) * (double)arcR);
         float pulse = (float)(Math.sin((double)elapsed * 0.007 + (double)a * 1.7) * (double)0.5F + (double)0.5F);
         float pBase = alpha * pulse * 0.85F;

         for(int g = 0; g < 8; ++g) {
            float gr = 0.012F + (float)g * 0.022F;
            float ga = pBase * ARC_GLOW_POW[g];
            this.batchFilledRingOffset(fills, mat, gr * 0.35F, gr, 16, this.col(theme, ga), tx, ty);
         }
      }

      this.orbitRsBuf[0] = r * 1.28F;
      this.orbitRsBuf[1] = r * 1.48F;

      for(int o = 0; o < 2; ++o) {
         int dc = ORBIT_DOT_COUNTS[o];
         float or = this.orbitRsBuf[o];
         float[] oCos = o == 0 ? this.orbitCos0 : this.orbitCos1;
         float[] oSin = o == 0 ? this.orbitSin0 : this.orbitSin1;

         for(int i = 0; i < dc; ++i) {
            float dx = oCos[i] * or;
            float dy = oSin[i] * or;
            float pulse = (float)(Math.sin((double)elapsed * 0.008 + (double)i * 1.3 + (double)o) * (double)0.5F + (double)0.5F);
            float daB = alpha * (0.3F + pulse * 0.55F) * 0.7F;

            for(int g = 0; g < 5; ++g) {
               float gr = 0.01F + (float)g * 0.018F;
               float ga = daB * DOT_GLOW_POW[g];
               this.batchFilledRingOffset(fills, mat, gr * 0.3F, gr, 12, this.col(theme, ga), dx, dy);
            }
         }
      }

      BufferRenderer.drawWithGlobalProgram(fills.end());
      BufferBuilder lines = tess.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
      int arcSegs = 36;

      for(int a = 0; a < 6; ++a) {
         float arcR = r * ARC_R_MULT[a];
         int total = (int)((float)arcSegs * ARC_LEN[a]);
         float pulse = (float)(Math.sin((double)elapsed * 0.005 + (double)a * 1.3) * 0.35 + 0.65);
         int totalM1 = total - 1;
         double arcAngleSpan = (Math.PI * 2D) * (double)ARC_LEN[a];

         for(int layer = 0; layer < 3; ++layer) {
            float rW = arcR * (1.0F + NOVA_LAYER_W[layer] * 0.012F);
            float layerAlphaPulse = alpha * NOVA_LAYER_A[layer] * pulse;

            for(int seg = 0; seg < totalM1; ++seg) {
               int edgeIdx = seg * 63 / totalM1;
               float edge = ARC_EDGE_LUT[edgeIdx];
               float sa = layerAlphaPulse * edge;
               if (!(sa < 0.008F)) {
                  double aA = this.arcStartA[a] + (double)seg / (double)total * arcAngleSpan;
                  double aB = this.arcStartA[a] + (double)(seg + 1) / (double)total * arcAngleSpan;
                  lines.vertex(mat, (float)(Math.cos(aA) * (double)rW), (float)(Math.sin(aA) * (double)rW), 0.0F).color(this.col(theme, sa));
                  lines.vertex(mat, (float)(Math.cos(aB) * (double)rW), (float)(Math.sin(aB) * (double)rW), 0.0F).color(this.col(theme, sa));
               }
            }
         }
      }

      this.batchRing(lines, mat, r * 1.14F, 72, this.col(theme, alpha * 0.04F));
      this.batchRing(lines, mat, r * 1.08F, 72, this.col(theme, alpha * 0.1F));
      this.batchRing(lines, mat, r * 1.03F, 72, this.col(theme, alpha * 0.25F));
      this.batchRing(lines, mat, r * 1.0F, 72, this.col(theme, alpha * 0.75F));
      this.batchRing(lines, mat, r * 0.98F, 72, this.col(theme, alpha * 1.0F));
      this.batchRing(lines, mat, r * 0.95F, 72, this.col(theme, alpha * 0.4F));

      for(int o = 0; o < 2; ++o) {
         int dc = ORBIT_DOT_COUNTS[o];
         float or = this.orbitRsBuf[o];
         float[] oCos = o == 0 ? this.orbitCos0 : this.orbitCos1;
         float[] oSin = o == 0 ? this.orbitSin0 : this.orbitSin1;
         float cosDelta = TAIL_COS_DELTA;
         float sinDelta = o == 0 ? -TAIL_SIN_DELTA : TAIL_SIN_DELTA;

         for(int i = 0; i < dc; ++i) {
            float pulse = (float)(Math.sin((double)elapsed * 0.008 + (double)i * 1.3 + (double)o) * (double)0.5F + (double)0.5F);
            float da = alpha * (0.3F + pulse * 0.55F);
            float px = oCos[i] * or;
            float py = oSin[i] * or;

            for(int seg = 1; seg <= 3; ++seg) {
               float sa = da * (1.0F - (float)seg * 0.28F);
               float nx = px * cosDelta - py * sinDelta;
               float ny = px * sinDelta + py * cosDelta;
               if (sa >= 0.008F) {
                  lines.vertex(mat, px, py, 0.0F).color(this.col(theme, sa));
                  lines.vertex(mat, nx, ny, 0.0F).color(this.col(theme, sa));
               }

               px = nx;
               py = ny;
            }
         }
      }

      for(int e = 0; e < 3; ++e) {
         float ep = (float)((elapsed + (long)e * 550L) % 1800L) / 1800.0F;
         float er = r * (1.0F + ep);
         float ea = alpha * (1.0F - ep) * (1.0F - ep) * 0.48F;
         if (!(ea < 0.004F)) {
            this.batchRing(lines, mat, er, 72, this.col(theme, ea * 0.3F));
            this.batchRing(lines, mat, er * 0.993F, 72, this.col(theme, ea * 0.65F));
            this.batchRing(lines, mat, er * 0.985F, 72, this.col(theme, ea));
         }
      }

      BufferRenderer.drawWithGlobalProgram(lines.end());
   }

   private void batchRing(BufferBuilder buf, Matrix4f mat, float r, int segs, int color) {
      float step = 512.0F / (float)segs;

      for(int i = 0; i < segs; ++i) {
         int iA = (int)((float)i * step) & 511;
         int iB = (int)((float)(i + 1) * step) & 511;
         buf.vertex(mat, COS_LUT[iA] * r, SIN_LUT[iA] * r, 0.0F).color(color);
         buf.vertex(mat, COS_LUT[iB] * r, SIN_LUT[iB] * r, 0.0F).color(color);
      }

   }

   private void batchFilledRing(BufferBuilder buf, Matrix4f mat, float inner, float outer, int segs, int color) {
      this.batchFilledRingOffset(buf, mat, inner, outer, segs, color, 0.0F, 0.0F);
   }

   private void batchFilledRingOffset(BufferBuilder buf, Matrix4f mat, float inner, float outer, int segs, int color, float cx, float cy) {
      float step = 512.0F / (float)segs;

      for(int i = 0; i < segs; ++i) {
         int iA = (int)((float)i * step) & 511;
         int iB = (int)((float)(i + 1) * step) & 511;
         float coA = COS_LUT[iA];
         float siA = SIN_LUT[iA];
         float coB = COS_LUT[iB];
         float siB = SIN_LUT[iB];
         buf.vertex(mat, cx + coA * outer, cy + siA * outer, 0.0F).color(color);
         buf.vertex(mat, cx + coA * inner, cy + siA * inner, 0.0F).color(color);
         buf.vertex(mat, cx + coB * inner, cy + siB * inner, 0.0F).color(color);
         buf.vertex(mat, cx + coB * outer, cy + siB * outer, 0.0F).color(color);
      }

   }

   private int col(int themeRGB, float a) {
      int ai = Math.max(0, Math.min(255, (int)(a * 255.0F)));
      return themeRGB & 16777215 | ai << 24;
   }

   @Compile
   public void onTick(TickEvent event) {
      if (mc.player != null) {
         if (!this.cache.contains(mc.player) && mc.player.isOnGround()) {
            this.cache.add(mc.player);
         }

         for(int i = this.cache.size() - 1; i >= 0; --i) {
            PlayerEntity pl = (PlayerEntity)this.cache.get(i);
            if (pl != null && !pl.isOnGround()) {
               this.circles.add(new Circle(new Vec3d(pl.getX(), Math.floor(pl.getY()) + 0.05, pl.getZ())));
               this.cache.remove(i);
            }
         }

         for(int i = this.circles.size() - 1; i >= 0; --i) {
            if (((Circle)this.circles.get(i)).animation.finished(Direction.BACKWARDS)) {
               this.circles.remove(i);
            }
         }

         if (mc.currentScreen instanceof DeathScreen) {
            this.cache.clear();
         }

      }
   }

   static {
      ROT_X_90 = RotationAxis.POSITIVE_X.rotationDegrees(90.0F);
      SIN_LUT = new float[512];
      COS_LUT = new float[512];

      for(int i = 0; i < 512; ++i) {
         double a = 0.01227184630308513 * (double)i;
         SIN_LUT[i] = (float)Math.sin(a);
         COS_LUT[i] = (float)Math.cos(a);
      }

      PULSE_LAYER_A = new float[]{0.12F, 0.45F, 1.0F};
      PULSE_LEN_M = new float[]{1.5F, 1.0F, 0.65F};
      SPIRAL_LAYER_ALPHA = new float[]{0.1F, 0.4F, 0.9F};
      ARC_SPEEDS = new float[]{9.0E-4F, -7.0E-4F, 0.0013F, -0.0011F, 6.0E-4F, -0.0015F};
      ARC_LEN = new float[]{0.55F, 0.7F, 0.45F, 0.65F, 0.5F, 0.6F};
      ARC_R_MULT = new float[]{1.0F, 0.96F, 1.03F, 0.98F, 1.01F, 0.95F};
      ORBIT_DOT_COUNTS = new int[]{16, 24};
      ORBIT_SPDS = new float[]{0.001F, 6.0E-4F};
      ORBIT_DIRS = new int[]{1, -1};
      NOVA_LAYER_A = new float[]{0.08F, 0.35F, 0.9F};
      NOVA_LAYER_W = new float[]{3.0F, 1.5F, 0.6F};
      TWO_PI_ORBIT_0 = (Math.PI * 2D) / (double)ORBIT_DOT_COUNTS[0];
      TWO_PI_ORBIT_1 = (Math.PI * 2D) / (double)ORBIT_DOT_COUNTS[1];
      SPOKE_EDGE = new float[11];

      for(int i = 0; i <= 10; ++i) {
         SPOKE_EDGE[i] = (float)Math.sin((double)((float)i / 10.0F) * Math.PI);
      }

      ARC_GLOW_POW = new float[8];

      for(int g = 0; g < 8; ++g) {
         float v = 1.0F - (float)g / 8.0F;
         ARC_GLOW_POW[g] = v * v;
      }

      DOT_GLOW_POW = new float[5];

      for(int g = 0; g < 5; ++g) {
         float v = 1.0F - (float)g / 5.0F;
         DOT_GLOW_POW[g] = v * v;
      }

      CENTER_GLOW_POW = new float[10];

      for(int g = 0; g < 10; ++g) {
         float v = 1.0F - (float)g / 10.0F;
         CENTER_GLOW_POW[g] = v * v * v;
      }

      HALO_POW = new float[14];

      for(int g = 0; g < 14; ++g) {
         float gf = (float)g / 14.0F;
         HALO_POW[g] = (float)Math.pow((double)(1.0F - gf), (double)2.2F);
      }

      ARC_EDGE_LUT = new float[64];

      for(int i = 0; i < 64; ++i) {
         ARC_EDGE_LUT[i] = (float)Math.sin((double)((float)i / 63.0F) * Math.PI);
      }

      NEB_POW = new float[7];

      for(int b = 0; b < 7; ++b) {
         float v = 1.0F - (float)b / 7.0F;
         NEB_POW[b] = (float)Math.pow((double)v, (double)1.4F);
      }

      PULSE_CENTER_POW = new float[9];

      for(int g = 0; g < 9; ++g) {
         float v = 1.0F - (float)g / 9.0F;
         PULSE_CENTER_POW[g] = v * v * v;
      }

      TRAIL_EDGE = new float[40];
      TRAIL_ANGLE_OFFSET = new float[40];
      TRAIL_F = new float[40];

      for(int seg = 0; seg < 40; ++seg) {
         float fA = (float)seg / 40.0F;
         TRAIL_EDGE[seg] = (float)Math.sin((double)fA * Math.PI);
         TRAIL_ANGLE_OFFSET[seg] = (float)((double)fA * Math.PI * 1.6);
         TRAIL_F[seg] = fA;
      }

      TRAIL_ANGLE_OFFSET_B = new float[40];
      TRAIL_F_B = new float[40];

      for(int seg = 0; seg < 40; ++seg) {
         float fB = (float)(seg + 1) / 40.0F;
         TRAIL_ANGLE_OFFSET_B[seg] = (float)((double)fB * Math.PI * 1.6);
         TRAIL_F_B[seg] = fB;
      }

      SPIRAL_HALO_POW = new float[10];

      for(int g = 0; g < 10; ++g) {
         float v = 1.0F - (float)g / 10.0F;
         SPIRAL_HALO_POW[g] = v * v;
      }

      SPIRAL_INNER_POW = new float[10];

      for(int i = 0; i < 10; ++i) {
         float v = 1.0F - (float)i / 10.0F;
         SPIRAL_INNER_POW[i] = (float)Math.pow((double)v, (double)2.5F);
      }

      SPIRAL_TIP_POW = new float[7];

      for(int g = 0; g < 7; ++g) {
         float v = 1.0F - (float)g / 7.0F;
         SPIRAL_TIP_POW[g] = v * v;
      }

      TRAIL_COS_OFFSET = new float[40];
      TRAIL_SIN_OFFSET = new float[40];
      TRAIL_COS_OFFSET_B = new float[40];
      TRAIL_SIN_OFFSET_B = new float[40];

      for(int seg = 0; seg < 40; ++seg) {
         TRAIL_COS_OFFSET[seg] = (float)Math.cos((double)TRAIL_ANGLE_OFFSET[seg]);
         TRAIL_SIN_OFFSET[seg] = (float)Math.sin((double)TRAIL_ANGLE_OFFSET[seg]);
         TRAIL_COS_OFFSET_B[seg] = (float)Math.cos((double)TRAIL_ANGLE_OFFSET_B[seg]);
         TRAIL_SIN_OFFSET_B[seg] = (float)Math.sin((double)TRAIL_ANGLE_OFFSET_B[seg]);
      }

      TAIL_COS_DELTA = (float)Math.cos(0.1);
      TAIL_SIN_DELTA = (float)Math.sin(0.1);
      SPIRAL_EDGE_POW07 = new float[80];

      for(int i = 0; i < 80; ++i) {
         float fA = (float)i / 80.0F;
         SPIRAL_EDGE_POW07[i] = (float)Math.pow(Math.sin((double)fA * Math.PI), (double)0.7F);
      }

   }

   @Environment(EnvType.CLIENT)
   public class Circle {
      final Vec3d pos;
      final long birthTime = System.currentTimeMillis();
      long timerStart;
      final Animation animation;

      Circle(Vec3d pos) {
         this.timerStart = this.birthTime;
         this.pos = pos;
         this.animation = new EaseBackIn((int)JumpCircle.this.animSpeed.getValue(), (double)1.5F, 0.1F, Direction.FORWARDS);
      }
   }
}
