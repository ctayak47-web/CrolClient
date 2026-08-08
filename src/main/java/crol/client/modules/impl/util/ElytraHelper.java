package crol.client.modules.impl.util;

import crol.client.CrolClient;
import crol.client.event.classes.ClickEvent;
import crol.client.event.classes.InputEvent;
import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.IClickaable;
import crol.client.event.interfaces.IInputable;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.IDisableable;
import crol.client.modules.IEnableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.impl.movement.Sprint;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.modules.settings.impl.KeySetting;
import crol.client.util.IUtil;
import crol.client.util.math.TimerUtil;
import crol.client.util.player.PacketUtil;
import crol.client.util.player.inventory.InventoryToolkit;
import crol.client.util.player.inventory.InventoryUtil;
import crol.client.util.player.inventory.SearchInvResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

@Environment(EnvType.CLIENT)
public class ElytraHelper extends Module implements IInputable, ITickable, IUtil, IDisableable, IClickaable, IEnableable {
   public KeySetting bind = ((KeySetting)(new KeySetting()).name("Swap")).value(0);
   public KeySetting firework = ((KeySetting)(new KeySetting()).name("Firework")).value(0);
   public BooleanSetting delay = ((BooleanSetting)(new BooleanSetting()).name("Delay")).value(false);
   public BooleanSetting autoFlight = ((BooleanSetting)(new BooleanSetting()).name("AutoFlight")).value(false);
   private boolean value = false;
   public boolean swapping = false;
   private boolean swap = false;
   private Sprint sprint;
   private final TimerUtil timerUtil = new TimerUtil();
   private final TimerUtil timerUtil2 = new TimerUtil();

   public ElytraHelper() {
      super(new ModuleInfo("ElytraSwap", Category.UTIL, "Чередует элитру с нагрудником по нажатию кнопки"));
      this.addSetting(new Setting[]{this.bind, this.firework, this.delay, this.autoFlight});
   }

   public void onInput(InputEvent event) {
      if (this.swap) {
         event.setStrafe(0.0F);
         event.setForward(0.0F);
      }

   }

   public void onEnable() {
      this.sprint = (Sprint)CrolClient.INSTANCE.getModuleManager().getByClass(Sprint.class);
   }

   public void onDisable() {
   }

   public void onClick(ClickEvent event) {
      if (mc.currentScreen == null) {
         if (event.getAction() == 1 && event.getKey() == this.bind.getValue()) {
            this.swap = true;
            this.sprint.setCanSprint(false);
            this.swapChest(false);
            this.sprint.getTimerUtil().reset();
            this.sprint.setCanSprint(true);
            this.timerUtil.reset();
            this.swap = false;
         }

         if (event.getAction() == 1 && event.getKey() == this.firework.getValue() && mc.player.isGliding()) {
            InventoryUtil.sendFireWork(CrolClient.INSTANCE.getRotationManager().getYaw(), CrolClient.INSTANCE.getRotationManager().getPitch());
            this.timerUtil2.reset();
         }
      }

   }

   public void onTick(TickEvent event) {
      if (this.autoFlight.getValue() && !mc.player.isGliding()) {
         if (mc.player.isOnGround() && mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
         }

         if (!mc.player.isOnGround() && this.timerUtil.isReached(5L) && mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
            mc.options.jumpKey.setPressed(this.value);
            this.value = !this.value;
            this.timerUtil.reset();
         }
      }

      if (this.autoFlight.getValue() && mc.player.isGliding()) {
         mc.options.jumpKey.setPressed(false);
      }

   }

   private void swapChest(boolean disable) {
      SearchInvResult result = InventoryUtil.findItemInInventory(Items.ELYTRA);
      if (mc.player.getInventory().getStack(38).getItem() == Items.ELYTRA) {
         int slot = this.getChestPlateSlot();
         if (slot == -1) {
            return;
         }

         if (this.delay.getValue()) {
            (new Thread(() -> {
               this.swapping = true;
               InventoryToolkit.clickSlot(slot);

               try {
                  Thread.sleep(200L);
               } catch (Exception var4) {
               }

               InventoryToolkit.clickSlot(6);

               try {
                  Thread.sleep(200L);
               } catch (Exception var3) {
               }

               InventoryToolkit.clickSlot(slot);
               PacketUtil.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
               this.swapping = false;
            })).start();
         } else {
            InventoryToolkit.clickSlot(slot);
            InventoryToolkit.clickSlot(6);
            InventoryToolkit.clickSlot(slot);
            PacketUtil.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
         }
      } else {
         if (!result.found()) {
            return;
         }

         if (this.delay.getValue()) {
            (new Thread(() -> {
               this.swapping = true;
               InventoryToolkit.clickSlot(result.slot());

               try {
                  Thread.sleep(200L);
               } catch (Exception var4) {
               }

               InventoryToolkit.clickSlot(6);

               try {
                  Thread.sleep(200L);
               } catch (Exception var3) {
               }

               InventoryToolkit.clickSlot(result.slot());
               PacketUtil.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
               this.swapping = false;
            })).start();
         } else {
            InventoryToolkit.clickSlot(result.slot());
            InventoryToolkit.clickSlot(6);
            InventoryToolkit.clickSlot(result.slot());
            PacketUtil.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
         }
      }

   }

   public int getChestPlateSlot() {
      Item[] items = new Item[]{Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.LEATHER_CHESTPLATE};

      for(Item item : items) {
         SearchInvResult slot = InventoryUtil.findItemInInventory(item);
         if (slot.found()) {
            return slot.slot();
         }
      }

      return -1;
   }
}
