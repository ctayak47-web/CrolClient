package crol.client.modules.impl.render;

import crol.client.CrolClient;
import crol.client.event.classes.AttackEvent;
import crol.client.event.classes.WorldRenderEvent;
import crol.client.event.interfaces.IAttackable;
import crol.client.event.interfaces.IWorldRender;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.util.IUtil;
import crol.client.util.color.ColorUtil;
import crol.client.util.render.RenderUtil3D;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShape;

@Environment(EnvType.CLIENT)
public class HitEffect extends Module implements IWorldRender, IUtil, IAttackable {
   final List<WaveEffect> waveEffects = new ArrayList();

   public HitEffect() {
      super(new ModuleInfo("HitEffect", Category.RENDER, "Добавляет анимацию попаданию по противнику"));
   }

   public void addWave(BlockPos pos) {
      if (mc.world != null) {
         this.waveEffects.add(new WaveEffect(pos, System.currentTimeMillis()));
      }

   }

   public void onWorldRender(WorldRenderEvent event) {
      if (!this.waveEffects.isEmpty() && mc.world != null) {
         Iterator<WaveEffect> iterator = this.waveEffects.iterator();

         while(iterator.hasNext()) {
            WaveEffect wave = (WaveEffect)iterator.next();
            if (wave.isExpired()) {
               iterator.remove();
            } else {
               wave.render();
            }
         }

      }
   }

   public void onAttack(AttackEvent event) {
      this.addWave(event.getEntity().getBlockPos().add(new Vec3i(0, 0, 0)));
   }

   @Environment(EnvType.CLIENT)
   private class WaveEffect {
      private final BlockPos centerPos;
      private final long startTime;
      private final long duration = 2000L;
      private final int maxRadius = 8;
      private final float ringThickness = 0.8F;

      public WaveEffect(BlockPos centerPos, long startTime) {
         this.centerPos = centerPos;
         this.startTime = startTime;
      }

      public boolean isExpired() {
         return System.currentTimeMillis() - this.startTime > 2000L;
      }

      public void render() {
         if (IUtil.mc.world != null) {
            long elapsed = System.currentTimeMillis() - this.startTime;
            float progress = (float)elapsed / 2000.0F;
            float currentRadius = this.easeOutCubic(progress) * 8.0F;
            float globalAlpha = this.smoothAlpha(progress);
            if (!(globalAlpha <= 0.01F)) {
               Color baseColor = CrolClient.INSTANCE.getThemeManager().getTheme().color();
               int rendered = 0;
               int maxPerFrame = 400;

               for(int x = -8; x <= 8; ++x) {
                  for(int z = -8; z <= 8; ++z) {
                     if (rendered >= maxPerFrame) {
                        return;
                     }

                     double distance = Math.sqrt((double)(x * x + z * z));
                     double diff = Math.abs(distance - (double)currentRadius);
                     if (!(diff > (double)0.8F)) {
                        BlockPos checkPos = this.centerPos.add(x, 0, z);
                        BlockPos renderPos = this.findBlock(checkPos);
                        if (renderPos != null) {
                           BlockState state = IUtil.mc.world.getBlockState(renderPos);
                           if (!state.isAir()) {
                              VoxelShape shape = state.getOutlineShape(IUtil.mc.world, renderPos);
                              if (!shape.isEmpty()) {
                                 ++rendered;
                                 float localAlpha = (float)(1.1 - diff / (double)0.8F);
                                 localAlpha *= localAlpha;
                                 localAlpha *= globalAlpha;
                                 if (!(localAlpha <= 0.015F)) {
                                    float lineWidth = 1.0F + localAlpha * 2.5F;
                                    Color color = ColorUtil.colorAlpha(baseColor, (int)(localAlpha * 250.0F));

                                    try {
                                       RenderUtil3D.getInstance().drawShapeAlternative(renderPos, shape, color.getRGB(), lineWidth, true, true);
                                    } catch (Exception var23) {
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }

            }
         }
      }

      private float easeOutCubic(float t) {
         return 1.0F - (float)Math.pow((double)(1.0F - t), (double)3.0F);
      }

      private float smoothAlpha(float progress) {
         float fadeIn = Math.min(1.0F, progress / 0.1F);
         fadeIn = fadeIn * fadeIn * (3.0F - 2.0F * fadeIn);
         float fadeOut = (float)Math.pow((double)(1.0F - progress), 4.8);
         return fadeIn * fadeOut;
      }

      private BlockPos findBlock(BlockPos start) {
         if (!IUtil.mc.world.isInBuildLimit(start)) {
            return null;
         } else {
            BlockState state = IUtil.mc.world.getBlockState(start);
            if (!state.isAir()) {
               return start;
            } else {
               for(int y = 1; y <= 10; ++y) {
                  BlockPos down = start.down(y);
                  if (IUtil.mc.world.isInBuildLimit(down) && !IUtil.mc.world.getBlockState(down).isAir()) {
                     return down;
                  }

                  BlockPos up = start.up(y);
                  if (IUtil.mc.world.isInBuildLimit(up) && !IUtil.mc.world.getBlockState(up).isAir()) {
                     return up;
                  }
               }

               return null;
            }
         }
      }
   }
}
