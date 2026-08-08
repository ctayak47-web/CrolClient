package crol.client.modules.impl.util;

import crol.client.event.classes.BreakEvent;
import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.IBreakable;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class AutoTool extends Module implements ITickable, IBreakable, IUtil {
   private int oldSlot = 0;
   private boolean change = false;

   public AutoTool() {
      super(new ModuleInfo("AutoTool", Category.UTIL, "Берет в руку нужный инструмент при ломании блока"));
   }

   @Compile
   public void onTick(TickEvent event) {
      if (!mc.options.attackKey.isPressed() || mc.crosshairTarget.getType() != Type.BLOCK) {
         if (this.change) {
            mc.player.getInventory().selectedSlot = this.oldSlot;
            this.change = false;
         } else {
            this.oldSlot = mc.player.getInventory().selectedSlot;
         }
      }

   }

   @Compile
   public void onBreak(BreakEvent event) {
      mc.player.getInventory().selectedSlot = this.getBetterSlot(event.getBlockPos());
      this.change = true;
   }

   @Compile
   public int getBetterSlot(BlockPos blockPos) {
      Block block = mc.world.getBlockState(new BlockPos(blockPos)).getBlock();
      if (Items.IRON_AXE.getMiningSpeed(Items.IRON_AXE.getDefaultStack(), block.getDefaultState()) > 1.0F) {
         return this.getAxe();
      } else if (Items.IRON_PICKAXE.getMiningSpeed(Items.IRON_PICKAXE.getDefaultStack(), block.getDefaultState()) > 1.0F) {
         return this.getPickaxe();
      } else {
         return Items.IRON_SHOVEL.getMiningSpeed(Items.IRON_SHOVEL.getDefaultStack(), block.getDefaultState()) > 1.0F ? this.getShovel() : mc.player.getInventory().selectedSlot;
      }
   }

   public int getAxe() {
      for(int i = 0; i < 9; ++i) {
         ItemStack itemStack = mc.player.getInventory().getStack(i);
         if (itemStack.getItem() instanceof AxeItem) {
            return i;
         }
      }

      return mc.player.getInventory().selectedSlot;
   }

   public int getPickaxe() {
      for(int i = 0; i < 9; ++i) {
         ItemStack itemStack = mc.player.getInventory().getStack(i);
         if (itemStack.getItem() instanceof PickaxeItem) {
            return i;
         }
      }

      return mc.player.getInventory().selectedSlot;
   }

   public int getShovel() {
      for(int i = 0; i < 9; ++i) {
         ItemStack itemStack = mc.player.getInventory().getStack(i);
         if (itemStack.getItem() instanceof ShovelItem) {
            return i;
         }
      }

      return mc.player.getInventory().selectedSlot;
   }
}
