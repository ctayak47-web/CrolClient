package crol.client.modules.impl.player;

import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class OpenWall extends Module implements ITickable, IUtil {
   private final FloatSetting range = ((FloatSetting)(new FloatSetting()).name("Range")).value(6.0F).minValue(1.0F).maxValue(10.0F).incriment(0.5F);

   public OpenWall() {
      super(new ModuleInfo("OpenWall", Category.PLAYER, "Позволяет взаимодействовать с интерактивными блоками через стены"));
      this.addSetting(new Setting[]{this.range});
   }

   public void onTick(TickEvent event) {
      if (mc.player != null && mc.world != null && mc.interactionManager != null) {
         if (mc.currentScreen == null && mc.options.useKey.isPressed()) {
            ClientPlayerEntity player = mc.player;
            Vec3d eyePos = player.getEyePos();
            Vec3d lookVec = player.getRotationVec(1.0F).normalize();
            double reach = (double)this.range.getValue();
            double step = (double)0.25F;
            int steps = Math.max(1, (int)(reach / step));
            BlockPos lastPos = null;

            for(int i = 0; i <= steps; ++i) {
               Vec3d point = eyePos.add(lookVec.multiply((double)i * step));
               BlockPos pos = BlockPos.ofFloored(point);
               if (!pos.equals(lastPos)) {
                  lastPos = pos;
                  BlockState state = mc.world.getBlockState(pos);
                  if (!state.isAir()) {
                     NamedScreenHandlerFactory factory = state.createScreenHandlerFactory(mc.world, pos);
                     if (factory != null) {
                        Direction side = this.getHitSide(eyePos, pos);
                        Vec3d hitPos = Vec3d.ofCenter(pos).add(Vec3d.of(side.getVector()).multiply((double)0.5F));
                        BlockHitResult hitResult = new BlockHitResult(hitPos, side, pos, false);
                        mc.interactionManager.interactBlock(player, Hand.MAIN_HAND, hitResult);
                        player.swingHand(Hand.MAIN_HAND);
                        return;
                     }
                  }
               }
            }

         }
      }
   }

   private Direction getHitSide(Vec3d eyePos, BlockPos pos) {
      double dx = eyePos.x - ((double)pos.getX() + (double)0.5F);
      double dy = eyePos.y - ((double)pos.getY() + (double)0.5F);
      double dz = eyePos.z - ((double)pos.getZ() + (double)0.5F);
      double adx = Math.abs(dx);
      double ady = Math.abs(dy);
      double adz = Math.abs(dz);
      if (adx > ady && adx > adz) {
         return dx > (double)0.0F ? Direction.EAST : Direction.WEST;
      } else if (ady > adz) {
         return dy > (double)0.0F ? Direction.UP : Direction.DOWN;
      } else {
         return dz > (double)0.0F ? Direction.SOUTH : Direction.NORTH;
      }
   }
}
