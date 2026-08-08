package crol.client.modules.impl.render;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import crol.client.CrolClient;
import crol.client.event.classes.AttackEvent;
import crol.client.event.classes.Render3DEvent;
import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.IAttackable;
import crol.client.event.interfaces.IRenderable3D;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.IDisableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.modules.settings.impl.ModeSetting;
import crol.client.modules.settings.impl.MultiBoxSetting;
import crol.client.util.IUtil;
import crol.client.util.animations.Animation;
import crol.client.util.animations.Direction;
import crol.client.util.animations.impl.EaseBackIn;
import crol.client.util.math.TimerUtil;
import crol.client.util.player.MovementUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class Particles extends Module implements IRenderable3D, ITickable, IUtil, IDisableable, IAttackable {
   private static final int TYPE_COUNT = 9;
   private static final String[] TYPE_NAMES = new String[]{"Type 1", "Type 2", "Type 3", "Type 4", "Type 5", "Type 6", "Type 7", "Type 8", "Type 9"};
   private static final String TRIGGER_SKY = "Sky";
   private static final String TRIGGER_MOVE = "Move";
   private static final String TRIGGER_ATTACK = "Attack";
   private static final int MAX_SKY_PARTICLES = 100;
   private static final int MAX_MOVE_PARTICLES = 64;
   private static final int MAX_HIT_PARTICLES = 96;
   private static final int MAX_TOTAL_PARTICLES = 260;
   private static final int VISIBILITY_RECHECK_TICKS = 4;
   private static final int PARTICLE_INITIAL_CAPACITY = 256;
   private static final double MAX_RAYCAST_DIST_SQ = (double)16384.0F;
   private static final float BILLBOARD_SCALE = 0.6F;
   private static final float SKY_SPEED = 1.5F;
   private static final long MOVE_LIFETIME_MS = 500L;
   private static final long HIT_LIFETIME_MS = 5000L;
   private static final long SKY_SPAWN_INTERVAL_MS = 5L;
   private static final double LOG_035 = Math.log(0.35);
   private static final byte KIND_SKY = 0;
   private static final byte KIND_MOVE = 1;
   private static final byte KIND_HIT = 2;
   private static final Identifier[] PARTICLE_IDENTIFIERS = new Identifier[]{Identifier.of("crol", "images/particles/type1.jpg"), Identifier.of("crol", "images/particles/type2.jpg"), Identifier.of("crol", "images/particles/type3.jpg"), Identifier.of("crol", "images/particles/type4.png"), Identifier.of("crol", "images/particles/type5.png"), Identifier.of("crol", "images/particles/type6.png"), Identifier.of("crol", "images/particles/type7.png"), Identifier.of("crol", "images/particles/type8.png"), Identifier.of("crol", "images/particles/type9.png")};
   private final MultiBoxSetting particlesMode;
   private final ModeSetting moveMode;
   private final MultiBoxSetting triggers;
   private final List<Particle>[] batchLists;
   private final List<Particle> particles;
   private final TimerUtil skySpawnTimer;
   private final Random random;
   private final int[] enabledTypeIndices;
   private int enabledTypeCount;
   private final Quaternionf cameraRotation;
   private final Vector3f rightAxis;
   private final Vector3f upAxis;
   private long frameTimeMs;
   private long lastRenderTime;
   private int tickCounter;
   private double eyeX;
   private double eyeY;
   private double eyeZ;
   private boolean moveDown;
   private int skyParticleCount;
   private int moveParticleCount;
   private int hitParticleCount;

   public Particles() {
      super(new ModuleInfo("Particles", Category.RENDER, "Добавляет в мир частицы появляющиеся после выбранных действий"));
      this.particlesMode = ((MultiBoxSetting)(new MultiBoxSetting()).name("Particles")).booleanSettings(((BooleanSetting)(new BooleanSetting()).name(TYPE_NAMES[0])).value(true), ((BooleanSetting)(new BooleanSetting()).name(TYPE_NAMES[1])).value(true), ((BooleanSetting)(new BooleanSetting()).name(TYPE_NAMES[2])).value(true), ((BooleanSetting)(new BooleanSetting()).name(TYPE_NAMES[3])).value(true), ((BooleanSetting)(new BooleanSetting()).name(TYPE_NAMES[4])).value(true), ((BooleanSetting)(new BooleanSetting()).name(TYPE_NAMES[5])).value(true), ((BooleanSetting)(new BooleanSetting()).name(TYPE_NAMES[6])).value(true), ((BooleanSetting)(new BooleanSetting()).name(TYPE_NAMES[7])).value(true), ((BooleanSetting)(new BooleanSetting()).name(TYPE_NAMES[8])).value(true));
      this.moveMode = ((ModeSetting)(new ModeSetting()).name("Move Mode")).value("Down").modes("Up", "Down");
      this.triggers = ((MultiBoxSetting)(new MultiBoxSetting()).name("Triggers")).booleanSettings(((BooleanSetting)(new BooleanSetting()).name("Sky")).value(true), ((BooleanSetting)(new BooleanSetting()).name("Move")).value(true), ((BooleanSetting)(new BooleanSetting()).name("Attack")).value(true));
      this.batchLists = new List[]{new ArrayList(48), new ArrayList(48), new ArrayList(48), new ArrayList(48), new ArrayList(48), new ArrayList(48), new ArrayList(48), new ArrayList(48), new ArrayList(48)};
      this.particles = new ArrayList(256);
      this.skySpawnTimer = new TimerUtil();
      this.random = new Random();
      this.enabledTypeIndices = new int[9];
      this.cameraRotation = new Quaternionf();
      this.rightAxis = new Vector3f();
      this.upAxis = new Vector3f();
      this.lastRenderTime = System.nanoTime();
      this.moveDown = true;
      this.addSetting(new Setting[]{this.particlesMode, this.moveMode, this.triggers});
   }

   public void onRender3D(Render3DEvent event) {
      if (this.particles.isEmpty()) {
         this.lastRenderTime = System.nanoTime();
      } else {
         long nowNano = System.nanoTime();
         float deltaTime = Math.min((float)(nowNano - this.lastRenderTime) * 1.0E-9F, 0.05F);
         this.lastRenderTime = nowNano;
         this.frameTimeMs = System.currentTimeMillis();
         Camera camera = mc.gameRenderer.getCamera();
         Vec3d camPos = camera.getPos();
         double camX = camPos.x;
         double camY = camPos.y;
         double camZ = camPos.z;
         this.cameraRotation.set(camera.getRotation());
         this.rightAxis.set(0.6F, 0.0F, 0.0F).rotate(this.cameraRotation);
         this.upAxis.set(0.0F, 0.6F, 0.0F).rotate(this.cameraRotation);
         boolean firstPerson = mc.options.getPerspective() == Perspective.FIRST_PERSON;
         int themeRgb = CrolClient.INSTANCE.getThemeManager().getTheme().color().getRGB() & 16777215;
         MatrixStack matrices = event.getMatrixStack();
         Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
         this.clearBatches();
         this.fillBatches(deltaTime, firstPerson);
         RenderSystem.enableDepthTest();
         RenderSystem.depthFunc(515);
         RenderSystem.depthMask(true);
         RenderSystem.disableCull();
         RenderSystem.enableBlend();
         RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE, SrcFactor.ONE, DstFactor.ZERO);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);

         for(int textureIdx = 0; textureIdx < 9; ++textureIdx) {
            List<Particle> batch = this.batchLists[textureIdx];
            if (!batch.isEmpty()) {
               RenderSystem.setShaderTexture(0, PARTICLE_IDENTIFIERS[textureIdx]);
               BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
               int i = 0;

               for(int size = batch.size(); i < size; ++i) {
                  Particle particle = (Particle)batch.get(i);
                  this.appendParticleQuad(buffer, positionMatrix, particle, camX, camY, camZ, themeRgb);
               }

               BufferRenderer.drawWithGlobalProgram(buffer.end());
            }
         }

         RenderSystem.disableBlend();
         RenderSystem.enableCull();
         RenderSystem.depthMask(true);
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         this.pruneDeadParticles();
      }
   }

   public void onTick(TickEvent event) {
      if (mc.player != null && mc.world != null) {
         ++this.tickCounter;
         this.moveDown = this.moveMode.is("Down");
         this.eyeX = mc.player.getX();
         this.eyeY = mc.player.getEyeY();
         this.eyeZ = mc.player.getZ();
         this.rebuildEnabledTypes();
         this.updateParticleVisibility();
         if (this.skyParticleCount < 100 && this.triggers.getValueByName("Sky")) {
            this.spawnSkyParticle();
         }

         if (this.moveParticleCount < 64 && this.triggers.getValueByName("Move") && MovementUtil.isMotionMove()) {
            this.spawnMoveParticle(mc.player);
         }

      }
   }

   private void rebuildEnabledTypes() {
      int count = 0;

      for(int i = 0; i < 9; ++i) {
         if (this.particlesMode.getValueByName(TYPE_NAMES[i])) {
            this.enabledTypeIndices[count++] = i;
         }
      }

      this.enabledTypeCount = count;
   }

   private int nextTextureIndex() {
      return this.enabledTypeCount == 0 ? 0 : this.enabledTypeIndices[this.random.nextInt(this.enabledTypeCount)];
   }

   private void clearBatches() {
      for(int i = 0; i < 9; ++i) {
         this.batchLists[i].clear();
      }

   }

   private void fillBatches(float deltaTime, boolean firstPerson) {
      int i = 0;

      for(int size = this.particles.size(); i < size; ++i) {
         Particle particle = (Particle)this.particles.get(i);
         particle.update(deltaTime, this.moveDown, this.frameTimeMs);
         if (!particle.isMove() || !firstPerson) {
            float alpha = (float)particle.animation.getOutput();
            particle.renderAlpha = alpha;
            if (!(alpha <= 0.005F)) {
               this.batchLists[particle.textureIdx].add(particle);
            }
         }
      }

   }

   private void appendParticleQuad(BufferBuilder buffer, Matrix4f positionMatrix, Particle particle, double camX, double camY, double camZ, int themeRgb) {
      int colorArgb = ((int)(particle.renderAlpha * 255.0F) & 255) << 24 | themeRgb;
      float quadMax = particle.quadSize - 0.75F;
      float quadMin = -0.75F;
      float baseX = (float)(particle.x - camX);
      float baseY = (float)(particle.y - camY);
      float baseZ = (float)(particle.z - camZ);
      float x1 = baseX + this.rightAxis.x * -0.75F + this.upAxis.x * quadMax;
      float y1 = baseY + this.rightAxis.y * -0.75F + this.upAxis.y * quadMax;
      float z1 = baseZ + this.rightAxis.z * -0.75F + this.upAxis.z * quadMax;
      float x2 = baseX + this.rightAxis.x * quadMax + this.upAxis.x * quadMax;
      float y2 = baseY + this.rightAxis.y * quadMax + this.upAxis.y * quadMax;
      float z2 = baseZ + this.rightAxis.z * quadMax + this.upAxis.z * quadMax;
      float x3 = baseX + this.rightAxis.x * quadMax + this.upAxis.x * -0.75F;
      float y3 = baseY + this.rightAxis.y * quadMax + this.upAxis.y * -0.75F;
      float z3 = baseZ + this.rightAxis.z * quadMax + this.upAxis.z * -0.75F;
      float x4 = baseX + this.rightAxis.x * -0.75F + this.upAxis.x * -0.75F;
      float y4 = baseY + this.rightAxis.y * -0.75F + this.upAxis.y * -0.75F;
      float z4 = baseZ + this.rightAxis.z * -0.75F + this.upAxis.z * -0.75F;
      buffer.vertex(positionMatrix, x1, y1, z1).texture(0.0F, 0.0F).color(colorArgb);
      buffer.vertex(positionMatrix, x2, y2, z2).texture(1.0F, 0.0F).color(colorArgb);
      buffer.vertex(positionMatrix, x3, y3, z3).texture(1.0F, 1.0F).color(colorArgb);
      buffer.vertex(positionMatrix, x4, y4, z4).texture(0.0F, 1.0F).color(colorArgb);
   }

   private void updateParticleVisibility() {
      for(int i = this.particles.size() - 1; i >= 0; --i) {
         Particle particle = (Particle)this.particles.get(i);
         if (!this.moveDown && particle.startY + (double)15.0F < particle.y) {
            particle.animation.setDirection(Direction.BACKWARDS);
         } else if (particle.nextVisibilityTick <= this.tickCounter) {
            particle.nextVisibilityTick = this.tickCounter + 4;
            if (!this.canParticleBeSeen(particle.x, particle.y, particle.z)) {
               particle.animation.setDirection(Direction.BACKWARDS);
            }
         }
      }

   }

   private void spawnSkyParticle() {
      if (this.skySpawnTimer.isReached(5L)) {
         if (this.particles.size() < 260) {
            double px = mc.player.getX();
            double py = mc.player.getY();
            double pz = mc.player.getZ();
            double spawnX = px + (double)30.0F - (double)this.random.nextInt(60);
            double spawnY = this.moveDown ? py + (double)10.0F + (double)this.random.nextInt(25) : py + (double)1.0F + (double)this.random.nextInt(10);
            double spawnZ = pz + (double)30.0F - (double)this.random.nextInt(60);
            if (this.canParticleBeSeen(spawnX, spawnY, spawnZ)) {
               this.addParticle(new SkyParticle(spawnX, spawnY, spawnZ, this.nextTextureIndex(), this.tickCounter + this.random.nextInt(4)));
               this.skySpawnTimer.reset();
            }
         }
      }
   }

   private void spawnMoveParticle(Entity entity) {
      if (this.particles.size() < 260) {
         double sx = entity.getX() + (double)0.25F - this.random.nextDouble() * (double)0.5F;
         double sy = entity.getY() + (double)0.75F + this.random.nextDouble() * (double)0.75F;
         double sz = entity.getZ() + (double)0.25F - this.random.nextDouble() * (double)0.5F;
         this.addParticle(new MoveParticle(sx, sy, sz, this.nextTextureIndex(), this.frameTimeMs != 0L ? this.frameTimeMs : System.currentTimeMillis(), this.tickCounter + 1));
      }
   }

   private void spawnHitParticle(Entity entity) {
      if (this.particles.size() < 260 && this.hitParticleCount < 96) {
         double ex = entity.getX();
         double ey = entity.getY();
         double ez = entity.getZ();
         double sx = ex + (this.random.nextDouble() - (double)0.5F) * (double)0.75F;
         double sy = ey + this.random.nextDouble();
         double sz = ez + (this.random.nextDouble() - (double)0.5F) * (double)0.75F;
         double endX = ex - (double)8.0F + (double)this.random.nextInt(16);
         double endY = ey + (double)this.random.nextInt(10);
         double endZ = ez - (double)8.0F + (double)this.random.nextInt(16);
         this.addParticle(new HitParticle(sx, sy, sz, this.nextTextureIndex(), endX, endY, endZ, this.frameTimeMs != 0L ? this.frameTimeMs : System.currentTimeMillis(), this.tickCounter + 1));
      }
   }

   private void addParticle(Particle particle) {
      this.particles.add(particle);
      switch (particle.kind) {
         case 0 -> ++this.skyParticleCount;
         case 1 -> ++this.moveParticleCount;
         case 2 -> ++this.hitParticleCount;
      }

   }

   private void pruneDeadParticles() {
      for(int i = this.particles.size() - 1; i >= 0; --i) {
         if (((Particle)this.particles.get(i)).animation.finished(Direction.BACKWARDS)) {
            this.removeParticle(i);
         }
      }

   }

   private void removeParticle(int index) {
      int lastIndex = this.particles.size() - 1;
      Particle removed = (Particle)this.particles.get(index);
      if (index != lastIndex) {
         this.particles.set(index, (Particle)this.particles.get(lastIndex));
      }

      this.particles.remove(lastIndex);
      switch (removed.kind) {
         case 0 -> --this.skyParticleCount;
         case 1 -> --this.moveParticleCount;
         case 2 -> --this.hitParticleCount;
      }

   }

   private boolean canParticleBeSeen(double tx, double ty, double tz) {
      double adjustedY = ty - (this.moveDown ? (double)0.5F : (double)0.0F);
      double dx = tx - this.eyeX;
      double dy = adjustedY - this.eyeY;
      double dz = tz - this.eyeZ;
      if (dx * dx + dy * dy + dz * dz > (double)16384.0F) {
         return false;
      } else {
         Vec3d eye = new Vec3d(this.eyeX, this.eyeY, this.eyeZ);
         Vec3d target = new Vec3d(tx, adjustedY, tz);
         return mc.world.raycast(new RaycastContext(eye, target, ShapeType.COLLIDER, FluidHandling.NONE, mc.player)).getType() == Type.MISS;
      }
   }

   @Compile
   public void onDisable() {
      this.particles.clear();
      this.skyParticleCount = 0;
      this.moveParticleCount = 0;
      this.hitParticleCount = 0;
      this.frameTimeMs = 0L;
      this.tickCounter = 0;
      this.lastRenderTime = System.nanoTime();
      this.clearBatches();
   }

   public void onAttack(AttackEvent event) {
      if (this.triggers.getValueByName("Attack")) {
         this.spawnHitParticle(event.getEntity());
         this.spawnHitParticle(event.getEntity());
         this.spawnHitParticle(event.getEntity());
      }
   }

   @Environment(EnvType.CLIENT)
   private static class Particle {
      double x;
      double y;
      double z;
      final double startY;
      final int textureIdx;
      final byte kind;
      final float quadSize;
      final Animation animation;
      int nextVisibilityTick;
      float renderAlpha;

      Particle(double x, double y, double z, int textureIdx, byte kind, float quadSize, int nextVisibilityTick) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.startY = y;
         this.textureIdx = textureIdx;
         this.kind = kind;
         this.quadSize = quadSize;
         this.nextVisibilityTick = nextVisibilityTick;
         this.animation = new EaseBackIn(325, (double)1.0F, 0.1F, Direction.BACKWARDS);
         this.animation.setDirection(Direction.FORWARDS);
      }

      void update(float dt, boolean moveDown, long nowMs) {
         this.y += (double)((moveDown ? -1.5F : 1.5F) * dt);
      }

      boolean isMove() {
         return this.kind == 1;
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class SkyParticle extends Particle {
      SkyParticle(double x, double y, double z, int textureIdx, int nextVisibilityTick) {
         super(x, y, z, textureIdx, (byte)0, 1.5F, nextVisibilityTick);
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class MoveParticle extends Particle {
      private final long bornAtMs;

      MoveParticle(double x, double y, double z, int textureIdx, long bornAtMs, int nextVisibilityTick) {
         super(x, y, z, textureIdx, (byte)1, 0.75F, nextVisibilityTick);
         this.bornAtMs = bornAtMs;
      }

      void update(float dt, boolean moveDown, long nowMs) {
         if (nowMs - this.bornAtMs >= 500L) {
            this.animation.setDirection(Direction.BACKWARDS);
         }

      }
   }

   @Environment(EnvType.CLIENT)
   private static final class HitParticle extends Particle {
      private final double endX;
      private final double endY;
      private final double endZ;
      private final long bornAtMs;

      HitParticle(double x, double y, double z, int textureIdx, double endX, double endY, double endZ, long bornAtMs, int nextVisibilityTick) {
         super(x, y, z, textureIdx, (byte)2, 1.5F, nextVisibilityTick);
         this.endX = endX;
         this.endY = endY;
         this.endZ = endZ;
         this.bornAtMs = bornAtMs;
      }

      void update(float dt, boolean moveDown, long nowMs) {
         float lerpSpeed = 1.0F - (float)Math.exp(Particles.LOG_035 * (double)dt);
         this.x += (this.endX - this.x) * (double)lerpSpeed;
         this.y += (this.endY - this.y) * (double)lerpSpeed;
         this.z += (this.endZ - this.z) * (double)lerpSpeed;
         if (nowMs - this.bornAtMs >= 5000L) {
            this.animation.setDirection(Direction.BACKWARDS);
         }

      }
   }
}
