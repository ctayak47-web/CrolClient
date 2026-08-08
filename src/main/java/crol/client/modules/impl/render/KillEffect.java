package crol.client.modules.impl.render;

import crol.client.CrolClient;
import crol.client.event.classes.EntityDeathEvent;
import crol.client.event.classes.WorldRenderEvent;
import crol.client.event.interfaces.IEntityDeath;
import crol.client.event.interfaces.IWorldRender;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.modules.settings.impl.ModeSetting;
import crol.client.util.IUtil;
import crol.client.util.color.ColorUtil;
import crol.client.util.render.RenderUtil3D;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class KillEffect extends Module implements IUtil, IEntityDeath, IWorldRender {
   private final BooleanSetting mobs = ((BooleanSetting)(new BooleanSetting()).name("Mobs")).value(false);
   private final ModeSetting effectType = ((ModeSetting)(new ModeSetting()).name("Effect Type")).value("Soul").modes("Soul", "Shatter");
   private final Map<Entity, EntityRenderData> renderEntities = new ConcurrentHashMap();

   public KillEffect() {
      super(new ModuleInfo("KillEffect", Category.RENDER, "Добавляет анимацию гибели противника"));
      this.addSetting(new Setting[]{this.mobs, this.effectType});
   }

   public void onDeath(EntityDeathEvent event) {
      if (mc.world != null && mc.player != null) {
         Entity entity = event.getEntity();
         if (entity instanceof LivingEntity) {
            if (this.mobs.getValue() || entity instanceof PlayerEntity) {
               if (entity != mc.player && !this.renderEntities.containsKey(entity)) {
                  OtherClientPlayerEntity fakePlayer = null;
                  if (this.effectType.is("Soul") && entity instanceof PlayerEntity) {
                     fakePlayer = new OtherClientPlayerEntity(mc.world, ((PlayerEntity)entity).getGameProfile());
                     fakePlayer.setPitch(-15.0F);
                     fakePlayer.setYaw(entity.getYaw());
                     fakePlayer.headYaw = entity.getYaw();
                     fakePlayer.bodyYaw = entity.getYaw();
                     fakePlayer.setCustomNameVisible(false);
                     fakePlayer.setCustomName(Text.literal("Ghost_" + String.valueOf(((PlayerEntity)entity).getGameProfile().getId())));
                     mc.world.addEntity(fakePlayer);
                  }

                  this.renderEntities.put(entity, new EntityRenderData(System.currentTimeMillis(), entity.getYaw(), entity.getPos(), entity, fakePlayer));
               }
            }
         }
      }
   }

   public void onWorldRender(WorldRenderEvent event) {
      if (mc.world != null && mc.player != null) {
         MatrixStack stack = event.getMatrixStack();
         float tickDelta = event.getRenderTickCounter().getTickDelta(false);
         long dur = this.effectType.is("Shatter") ? 2500L : 4000L;
         List<Entity> dead = new ArrayList();
         this.renderEntities.forEach((entity, data) -> {
            long elapsed = System.currentTimeMillis() - data.startTime;
            if (elapsed > dur) {
               dead.add(entity);
               if (data.fakePlayer != null) {
                  mc.world.removeEntity(data.fakePlayer.getId(), RemovalReason.DISCARDED);
               }

            } else {
               float t = (float)elapsed / (float)dur;
               Color theme = CrolClient.INSTANCE.getThemeManager().getTheme().color();
               if (this.effectType.is("Soul")) {
                  this.renderSoul(data, t, elapsed, theme, stack, tickDelta);
               }

               if (this.effectType.is("Shatter")) {
                  this.renderShatter(data, t, elapsed, theme);
               }

            }
         });
         Map var10001 = this.renderEntities;
         Objects.requireNonNull(var10001);
         dead.forEach(var10001::remove);
      }
   }

   private void renderSoul(EntityRenderData data, float t, long elapsed, Color theme, MatrixStack stack, float tickDelta) {
      Vec3d base = data.pos;
      float rise = this.easeOutCubic(t) * 6.0F;
      float fadeOut = 1.0F - this.smoothstep(0.72F, 1.0F, t);
      float fadeIn = this.smoothstep(0.0F, 0.08F, t);
      int dnaSegs = 60;
      float dnaH = rise + 0.8F;
      double dnaPhase = (double)elapsed * 0.0025;

      for(int strand = 0; strand < 3; ++strand) {
         double strandOffset = (double)strand * 2.0943951023931953;

         for(int i = 0; i < dnaSegs - 1; ++i) {
            float fA = (float)i / (float)dnaSegs;
            float fB = (float)(i + 1) / (float)dnaSegs;
            float yA = fA * dnaH;
            float yB = fB * dnaH;
            double aA = (double)fA * Math.PI * (double)8.0F + dnaPhase + strandOffset;
            double aB = (double)fB * Math.PI * (double)8.0F + dnaPhase + strandOffset;
            float r = 0.4F;
            Vec3d pA = base.add(Math.cos(aA) * (double)r, (double)yA, Math.sin(aA) * (double)r);
            Vec3d pB = base.add(Math.cos(aB) * (double)r, (double)yB, Math.sin(aB) * (double)r);
            float edgeFade = (float)Math.sin((double)fA * Math.PI);
            float alpha = edgeFade * fadeOut * fadeIn * 0.85F;
            alpha *= (float)(0.7 + Math.sin((double)fA * Math.PI * (double)4.0F + (double)elapsed * 0.004) * 0.3);
            int hShift = strand * 30 + (int)(fA * 15.0F);
            int col = ColorUtil.colorAlpha(ColorUtil.shiftHue(theme, (float)hShift), (int)(210.0F * alpha)).getRGB();
            RenderUtil3D.getInstance().drawLine(pA, pB, col, 1.4F, true);
         }
      }

      for(int i = 0; i < dnaSegs; i += 4) {
         float fA = (float)i / (float)dnaSegs;
         float yA = fA * dnaH;
         double phase0 = (double)fA * Math.PI * (double)8.0F + dnaPhase;
         float r = 0.4F;

         for(int s = 0; s < 3; ++s) {
            double a1 = phase0 + (double)s * 2.0943951023931953;
            double a2 = phase0 + (double)(s + 1) * 2.0943951023931953;
            Vec3d p1 = base.add(Math.cos(a1) * (double)r, (double)yA, Math.sin(a1) * (double)r);
            Vec3d p2 = base.add(Math.cos(a2) * (double)r, (double)yA, Math.sin(a2) * (double)r);
            float edgeFade = (float)Math.sin((double)fA * Math.PI);
            int ca = (int)(90.0F * edgeFade * fadeOut * fadeIn);
            if (ca > 0) {
               RenderUtil3D.getInstance().drawLine(p1, p2, ColorUtil.colorAlpha(theme, ca).getRGB(), 0.7F, true);
            }
         }
      }

      for(int ring = 0; ring < 8; ++ring) {
         float delay = (float)ring * 0.055F;
         float rt = Math.max(0.0F, t - delay);
         float ringY = this.easeOutCubic(rt) * 6.0F;
         float ringT = 1.0F - this.smoothstep(0.62F, 1.0F, rt);
         float ringF = ringT * (1.0F - (float)ring / 9.0F);
         float radius = (0.45F + rt * 0.3F) * (1.0F - rt * 0.25F);
         int ra = (int)(140.0F * ringF * fadeIn);
         if (ra > 0) {
            Color rc = ColorUtil.colorAlpha(ColorUtil.shiftHue(theme, (float)(ring * 18)), ra);
            Color rc2 = ColorUtil.colorAlpha(ColorUtil.shiftHue(theme, (float)(ring * 18 + 12)), ra / 3);
            this.drawCircle(base.add((double)0.0F, (double)ringY, (double)0.0F), radius, 36, rc.getRGB(), 1.3F);
            this.drawCircle(base.add((double)0.0F, (double)ringY, (double)0.0F), radius * 1.4F, 36, rc2.getRGB(), 0.6F);
         }
      }

      for(int orbit = 0; orbit < 2; ++orbit) {
         int starCount = orbit == 0 ? 8 : 12;
         float orbitR = orbit == 0 ? 0.7F : 1.1F;
         float orbitSpd = orbit == 0 ? 0.001F : -6.0E-4F;

         for(int i = 0; i < starCount; ++i) {
            double angle = (Math.PI * 2D) / (double)starCount * (double)i + (double)((float)elapsed * orbitSpd);
            float starH = (float)((Math.sin((double)elapsed * 0.0018 + (double)i * 1.1 + (double)orbit) * (double)0.5F + (double)0.5F) * (double)rise);
            Vec3d sp = base.add(Math.cos(angle) * (double)orbitR, (double)starH, Math.sin(angle) * (double)orbitR);
            double tailAngle = angle - (double)0.25F * (double)Math.signum(orbitSpd);
            Vec3d tail = base.add(Math.cos(tailAngle) * (double)orbitR, (double)starH, Math.sin(tailAngle) * (double)orbitR);
            float sa = (float)((double)0.75F * (double)fadeOut * (double)fadeIn * (Math.sin((double)elapsed * 0.006 + (double)i * 1.4) * (double)0.25F + (double)0.75F));
            int sc = ColorUtil.colorAlpha(ColorUtil.shiftHue(theme, (float)(i * 15 + orbit * 40)), (int)(220.0F * sa)).getRGB();
            RenderUtil3D.getInstance().drawLine(sp, tail, sc, 2.0F, true);
            Vec3d tip = sp.add((double)0.0F, 0.04, (double)0.0F);
            RenderUtil3D.getInstance().drawLine(sp, tip, ColorUtil.colorAlpha(Color.WHITE, (int)(180.0F * sa)).getRGB(), 1.0F, true);
         }
      }

      Vec3d soulPos = base.add((double)0.0F, (double)rise, (double)0.0F);
      float flicker = (float)(Math.sin((double)elapsed / (double)80.0F) * 0.06 + 0.94);
      int soulAlpha = (int)(255.0F * flicker * fadeOut * fadeIn);
      Entity re = (Entity)(data.fakePlayer != null ? data.fakePlayer : data.entity);
      if (data.fakePlayer != null) {
         re.setPos(soulPos.x, soulPos.y, soulPos.z);
      }

      RenderUtil3D.getInstance().drawEntity(re, soulPos, data.yaw, soulAlpha, stack, tickDelta);
      Vec3d auraCenter = soulPos.add((double)0.0F, (double)1.0F, (double)0.0F);

      for(int a = 0; a < 3; ++a) {
         double pulsePhase = Math.sin((double)elapsed * 0.004 + (double)a * Math.PI * 0.66);
         float aR = 0.5F + (float)a * 0.22F + (float)pulsePhase * 0.08F;
         float aAlpha = (0.55F - (float)a * 0.14F) * fadeOut * fadeIn;
         int aa = (int)(170.0F * aAlpha);
         if (aa > 0) {
            this.drawCircle(auraCenter, aR, 40, ColorUtil.colorAlpha(ColorUtil.shiftHue(theme, (float)(a * 22)), aa).getRGB(), 2.2F - (float)a * 0.5F);
         }
      }

      if (t < 0.4F) {
         float bt = t / 0.4F;
         float beamAlpha = (float)Math.sin((double)bt * Math.PI) * fadeIn;
         int ba = (int)(120.0F * beamAlpha);
         float[] widths = new float[]{4.0F, 2.0F, 0.8F};
         float[] alphaMs = new float[]{0.25F, 0.55F, 1.0F};

         for(int l = 0; l < 3; ++l) {
            int lc = ColorUtil.colorAlpha(theme, (int)((float)ba * alphaMs[l])).getRGB();
            RenderUtil3D.getInstance().drawLine(base.add((double)0.0F, -0.3, (double)0.0F), base.add((double)0.0F, (double)rise + (double)2.5F, (double)0.0F), lc, widths[l], true);
         }
      }

   }

   private void renderShatter(EntityRenderData data, float t, long elapsed, Color theme) {
      Vec3d pos = data.pos;
      float fadeIn = this.smoothstep(0.0F, 0.06F, t);
      if (t < 0.25F) {
         float bt = t / 0.25F;
         float ease = this.easeOutExpo(bt);

         for(int ring = 0; ring < 4; ++ring) {
            float rr = ease * (0.8F + (float)ring * 0.5F);
            float ra = (1.0F - bt) * (1.0F - (float)ring * 0.2F);
            float lw = 3.5F - (float)ring * 0.6F;
            int rc = ColorUtil.colorAlpha(ColorUtil.shiftHue(theme, (float)(ring * 22)), (int)(240.0F * ra)).getRGB();
            this.drawCircle(pos.add((double)0.0F, 0.9, (double)0.0F), rr, 40, rc, lw);
         }

         float vba = 1.0F - bt;
         int vbc = ColorUtil.colorAlpha(theme, (int)(180.0F * vba)).getRGB();
         RenderUtil3D.getInstance().drawLine(pos.add((double)0.0F, (double)-0.5F, (double)0.0F), pos.add((double)0.0F, (double)3.5F, (double)0.0F), vbc, 3.0F, true);
         RenderUtil3D.getInstance().drawLine(pos.add((double)0.0F, (double)-0.5F, (double)0.0F), pos.add((double)0.0F, (double)3.5F, (double)0.0F), ColorUtil.colorAlpha(Color.WHITE, (int)(120.0F * vba)).getRGB(), 1.0F, true);
      }

      float gravity = t * t * 4.5F;
      float vortexPull = this.smoothstep(0.35F, 0.9F, t) * 2.2F;

      for(int i = 0; i < data.shards.size(); ++i) {
         ShardLine s = (ShardLine)data.shards.get(i);
         float ease = this.easeOutCubic(Math.min(t * 1.6F, 1.0F));
         double vx = s.dir.x * (double)s.speed * (double)ease * ((double)1.0F - (double)vortexPull * 0.45);
         double vz = s.dir.z * (double)s.speed * (double)ease * ((double)1.0F - (double)vortexPull * 0.45);
         double vy = s.dir.y * (double)s.speed * (double)ease - (double)(gravity * s.gravMult) + (double)vortexPull * (double)3.0F;
         Vec3d shardPos = s.origin.add(vx, vy, vz);
         s.rotAngle += s.rotSpeed * 0.016F * 60.0F;
         double rot = Math.toRadians((double)s.rotAngle);
         Vec3d perp = (new Vec3d(-s.dir.z, 0.2, s.dir.x)).normalize();
         Vec3d shardEnd = shardPos.add((Math.cos(rot) * s.dir.x + Math.sin(rot) * perp.x) * (double)s.length, Math.sin(rot * 0.7) * (double)s.length * 0.35, (Math.cos(rot) * s.dir.z + Math.sin(rot) * perp.z) * (double)s.length);
         float alphaF = (float)Math.pow((double)(1.0F - t), (double)2.0F);
         alphaF *= (float)(0.65 + Math.sin((double)elapsed * 0.009 + (double)i * 0.8) * 0.35);
         alphaF *= fadeIn;
         int a = (int)(235.0F * alphaF);
         if (a > 0) {
            float lw = 1.0F + (1.0F - t) * 2.2F;
            int col = ColorUtil.colorAlpha(ColorUtil.shiftHue(theme, (float)(s.hueShift + i % 3 * 15)), a).getRGB();
            RenderUtil3D.getInstance().drawLine(shardPos, shardEnd, col, lw, true);
            Vec3d tail1 = shardPos.subtract((new Vec3d(vx, vy, vz)).normalize().multiply((double)0.25F * (double)ease));
            Vec3d tail2 = shardPos.subtract((new Vec3d(vx, vy, vz)).normalize().multiply(0.12 * (double)ease));
            RenderUtil3D.getInstance().drawLine(shardPos, tail1, ColorUtil.colorAlpha(ColorUtil.shiftHue(theme, (float)(s.hueShift + 20)), a / 3).getRGB(), lw * 0.4F, true);
            RenderUtil3D.getInstance().drawLine(shardPos, tail2, ColorUtil.colorAlpha(Color.WHITE, a / 6).getRGB(), lw * 0.2F, true);
         }
      }

      float vortexT = this.smoothstep(0.28F, 0.88F, t);
      if (vortexT > 0.01F) {
         int spiralSegs = 48;
         float spiralH = 4.5F;
         double spiralPhase = (double)elapsed * 0.006;

         for(int i = 0; i < spiralSegs - 1; ++i) {
            float fA = (float)i / (float)spiralSegs;
            float fB = (float)(i + 1) / (float)spiralSegs;

            for(int strand = 0; strand < 2; ++strand) {
               double stOff = (double)strand * Math.PI;
               double aA = (double)fA * Math.PI * (double)10.0F + spiralPhase + stOff;
               double aB = (double)fB * Math.PI * (double)10.0F + spiralPhase + stOff;
               float rA = (0.9F - fA * 0.82F) * vortexT;
               float rB = (0.9F - fB * 0.82F) * vortexT;
               Vec3d pA = pos.add(Math.cos(aA) * (double)rA, (double)(fA * spiralH), Math.sin(aA) * (double)rA);
               Vec3d pB = pos.add(Math.cos(aB) * (double)rB, (double)(fB * spiralH), Math.sin(aB) * (double)rB);
               float edgeFade = (float)Math.sin((double)fA * Math.PI);
               int va = (int)(160.0F * vortexT * edgeFade * (1.0F - fA * 0.5F));
               if (va > 0) {
                  int vc = ColorUtil.colorAlpha(ColorUtil.shiftHue(theme, (float)(strand * 30 + (int)(fA * 25.0F))), va).getRGB();
                  RenderUtil3D.getInstance().drawLine(pA, pB, vc, 1.1F, true);
               }
            }
         }

         int ringCount = 7;

         for(int v = 0; v < ringCount; ++v) {
            float vf = (float)v / (float)ringCount;
            float vY = vf * spiralH;
            float vR = (0.9F - vf * 0.82F) * vortexT;
            double ph = (double)elapsed * 0.005 + (double)v * 0.6;
            int va = (int)(80.0F * vortexT * (1.0F - vf * 0.6F));
            if (va > 0) {
               this.drawRotatingRing(pos.add((double)0.0F, (double)vY, (double)0.0F), vR, 24, ph, ColorUtil.colorAlpha(ColorUtil.shiftHue(theme, (float)(v * 20)), va).getRGB(), 0.9F);
            }
         }
      }

      float sphereAlpha = (float)(Math.sin((double)elapsed * 0.008) * 0.3 + (double)0.5F) * (1.0F - t) * fadeIn;
      if (sphereAlpha > 0.02F) {
         float sr = 0.18F + (float)(Math.sin((double)elapsed * 0.01) * 0.06);

         for(int axis = 0; axis < 3; ++axis) {
            double phaseOff = (double)elapsed * 0.003 + (double)axis * Math.PI / (double)3.0F;
            int sa = (int)(180.0F * sphereAlpha);
            this.drawRotatingRing(pos.add((double)0.0F, 0.9, (double)0.0F), sr, 24, phaseOff, ColorUtil.colorAlpha(ColorUtil.shiftHue(theme, (float)(axis * 30)), sa).getRGB(), 1.5F);
         }
      }

      if (t > 0.85F) {
         float ft = (t - 0.85F) / 0.15F;
         float ease = this.easeOutExpo(ft);

         for(int ring = 0; ring < 3; ++ring) {
            float fr = ease * (1.0F + (float)ring * 0.6F);
            float fa = (1.0F - ft) * (1.0F - (float)ring * 0.3F);
            int fc = ColorUtil.colorAlpha(ColorUtil.shiftHue(theme, (float)(ring * 25)), (int)(200.0F * fa)).getRGB();
            this.drawCircle(pos.add((double)0.0F, 0.9, (double)0.0F), fr, 40, fc, 2.5F - (float)ring * 0.5F);
         }
      }

   }

   private void drawCircle(Vec3d center, float radius, int segments, int color, float width) {
      for(int i = 0; i < segments; ++i) {
         double a1 = (Math.PI * 2D) / (double)segments * (double)i;
         double a2 = (Math.PI * 2D) / (double)segments * (double)(i + 1);
         Vec3d p1 = center.add(Math.cos(a1) * (double)radius, (double)0.0F, Math.sin(a1) * (double)radius);
         Vec3d p2 = center.add(Math.cos(a2) * (double)radius, (double)0.0F, Math.sin(a2) * (double)radius);
         RenderUtil3D.getInstance().drawLine(p1, p2, color, width, true);
      }

   }

   private void drawRotatingRing(Vec3d center, float radius, int segments, double phase, int color, float width) {
      for(int i = 0; i < segments; ++i) {
         double a1 = (Math.PI * 2D) / (double)segments * (double)i + phase;
         double a2 = (Math.PI * 2D) / (double)segments * (double)(i + 1) + phase;
         Vec3d p1 = center.add(Math.cos(a1) * (double)radius, (double)0.0F, Math.sin(a1) * (double)radius);
         Vec3d p2 = center.add(Math.cos(a2) * (double)radius, (double)0.0F, Math.sin(a2) * (double)radius);
         RenderUtil3D.getInstance().drawLine(p1, p2, color, width, true);
      }

   }

   private float easeOutCubic(float t) {
      t = Math.min(t, 1.0F);
      return 1.0F - (float)Math.pow((double)(1.0F - t), (double)3.0F);
   }

   private float easeOutExpo(float t) {
      t = Math.min(t, 1.0F);
      return t == 1.0F ? 1.0F : 1.0F - (float)Math.pow((double)2.0F, (double)(-10.0F * t));
   }

   private float smoothstep(float edge0, float edge1, float x) {
      float v = Math.max(0.0F, Math.min(1.0F, (x - edge0) / (edge1 - edge0)));
      return v * v * (3.0F - 2.0F * v);
   }

   @Environment(EnvType.CLIENT)
   private static class ShardLine {
      final Vec3d origin;
      final Vec3d dir;
      final float speed;
      final float rotSpeed;
      final float length;
      final float gravMult;
      final int hueShift;
      float rotAngle;

      ShardLine(Vec3d origin, Vec3d dir, float speed, float rotSpeed, float length, float gravMult, int hueShift) {
         this.origin = origin;
         this.dir = dir;
         this.speed = speed;
         this.rotSpeed = rotSpeed;
         this.length = length;
         this.gravMult = gravMult;
         this.hueShift = hueShift;
         this.rotAngle = 0.0F;
      }
   }

   @Environment(EnvType.CLIENT)
   private static class EntityRenderData {
      final long startTime;
      final float yaw;
      final Vec3d pos;
      final Entity entity;
      final OtherClientPlayerEntity fakePlayer;
      final List<ShardLine> shards = new ArrayList();

      EntityRenderData(long startTime, float yaw, Vec3d pos, Entity entity, OtherClientPlayerEntity fakePlayer) {
         this.startTime = startTime;
         this.yaw = yaw;
         this.pos = pos;
         this.entity = entity;
         this.fakePlayer = fakePlayer;
         this.buildShards();
      }

      private void buildShards() {
         Random rng = new Random();
         int[] counts = new int[]{10, 16, 14, 8};
         float[] heights = new float[]{0.2F, 0.7F, 1.3F, 1.9F};
         int[] hues = new int[]{0, 20, -20, 40};

         for(int r = 0; r < counts.length; ++r) {
            for(int i = 0; i < counts[r]; ++i) {
               double angle = (Math.PI * 2D) / (double)counts[r] * (double)i + rng.nextDouble() * (double)0.5F;
               float spd = 1.2F + (float)rng.nextDouble() * 2.0F;
               double yVel = -0.1 + rng.nextDouble() * 0.8;
               Vec3d dir = (new Vec3d(Math.cos(angle), yVel, Math.sin(angle))).normalize();
               float rotSpd = (float)(rng.nextDouble() * (double)8.0F - (double)4.0F);
               float len = 0.2F + (float)rng.nextDouble() * 0.6F;
               float grav = 0.5F + (float)rng.nextDouble() * 1.0F;
               this.shards.add(new ShardLine(this.pos.add((double)0.0F, (double)heights[r], (double)0.0F), dir, spd, rotSpd, len, grav, hues[r]));
            }
         }

      }
   }
}
