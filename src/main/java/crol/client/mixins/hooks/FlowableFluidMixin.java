package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.modules.impl.player.NoPush;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction.Type;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({FlowableFluid.class})
public abstract class FlowableFluidMixin {
   @Shadow
   protected abstract boolean isFlowBlocked(BlockView var1, BlockPos var2, Direction var3);

   @Inject(
      method = {"getVelocity"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void getVelocityHook(BlockView world, BlockPos pos, FluidState state, CallbackInfoReturnable<Vec3d> cir) {
      NoPush noPush = (NoPush)CrolClient.INSTANCE.getModuleManager().getByClass(NoPush.class);
      if (noPush.isEnabled() && noPush.options.getValueByName("Water")) {
         double d = (double)0.0F;
         double e = (double)0.0F;
         BlockPos.Mutable mutable = new BlockPos.Mutable();
         Vec3d vec3d = new Vec3d(d, (double)0.0F, e);
         if ((Boolean)state.get(FlowableFluid.FALLING)) {
            for(Direction direction2 : Type.HORIZONTAL) {
               mutable.set(pos, direction2);
               if (this.isFlowBlocked(world, mutable, direction2) || this.isFlowBlocked(world, mutable.up(), direction2)) {
                  vec3d = vec3d.normalize().add((double)0.0F, (double)-6.0F, (double)0.0F);
                  break;
               }
            }
         }

         cir.setReturnValue(vec3d.normalize());
      }

   }
}
