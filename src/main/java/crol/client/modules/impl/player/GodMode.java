package crol.client.modules.impl.player;

import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.IDisableable;
import crol.client.modules.IEnableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

@Environment(EnvType.CLIENT)
public class GodMode extends Module implements IUtil, IEnableable, IDisableable, ITickable {
   private boolean slot21Clicked = false;
   private long lastActionTime = 0L;
   private long enableTime = 0L;

   public GodMode() {
      super(new ModuleInfo("GodMode", Category.PLAYER, "NoDesc"));
   }

   public void onEnable() {
      this.enableTime = System.currentTimeMillis();
      this.lastActionTime = 0L;
      this.slot21Clicked = false;
      if (mc.player != null) {
         mc.player.networkHandler.sendCommand("menu");
      }

   }

   public void onDisable() {
      this.slot21Clicked = false;
   }

   public void onTick(TickEvent event) {
      if (mc.player != null) {
         ScreenHandler container = mc.player.currentScreenHandler;
         long now = System.currentTimeMillis();
         if (!this.slot21Clicked && container != null && container.slots.size() > 32) {
            mc.interactionManager.clickSlot(container.syncId, 32, 0, SlotActionType.QUICK_MOVE, mc.player);
            if (now - this.enableTime >= 500L) {
               this.slot21Clicked = true;
            }

            mc.setScreen((Screen)null);
         }

         if (container != null && container.slots.size() > 13 && now - this.enableTime >= 1000L && this.slot21Clicked) {
            mc.interactionManager.clickSlot(container.syncId, 13, 0, SlotActionType.QUICK_MOVE, mc.player);
            mc.interactionManager.clickSlot(container.syncId, 11, 0, SlotActionType.QUICK_MOVE, mc.player);
         }

      }
   }
}
