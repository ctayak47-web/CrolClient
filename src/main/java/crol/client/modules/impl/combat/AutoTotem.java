package crol.client.modules.impl.combat;

import crol.client.event.classes.ReceivePacketEvent;
import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.IReceivePacketable;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.IDisableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.modules.settings.impl.MultiBoxSetting;
import crol.client.ui.gui.Gui;
import crol.client.util.IUtil;
import crol.client.util.player.inventory.InventoryResult;
import crol.client.util.player.inventory.InventoryToolkit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class AutoTotem extends Module implements ITickable, IReceivePacketable, IUtil, IDisableable {
   public final FloatSetting healthThreshold = ((FloatSetting)(new FloatSetting() {
   }).name("Health Threshold")).value(4.5F).minValue(1.0F).maxValue(20.0F).incriment(0.5F);
   public final FloatSetting elytraHealth = ((FloatSetting)(new FloatSetting() {
      public boolean isVisible() {
         return AutoTotem.this.modes.getValueByName("Elytra Health");
      }
   }).name("Elytra Health")).value(8.5F).minValue(1.0F).maxValue(20.0F).incriment(0.5F);
   public final FloatSetting crystalDistance = ((FloatSetting)(new FloatSetting() {
      public boolean isVisible() {
         return AutoTotem.this.modes.getValueByName("Crystals");
      }
   }).name("Crystal Distance")).value(4.0F).minValue(1.0F).maxValue(6.0F).incriment(0.5F);
   public final FloatSetting obsidianDistance = ((FloatSetting)(new FloatSetting() {
      public boolean isVisible() {
         return AutoTotem.this.modes.getValueByName("Obsidian");
      }
   }).name("Obsidian Distance")).value(4.0F).minValue(1.0F).maxValue(6.0F).incriment(0.5F);
   public final FloatSetting tntDistance = ((FloatSetting)(new FloatSetting() {
      public boolean isVisible() {
         return AutoTotem.this.modes.getValueByName("TNT");
      }
   }).name("TNT Distance")).value(10.0F).minValue(1.0F).maxValue(50.0F).incriment(1.0F);
   private final MultiBoxSetting settings = ((MultiBoxSetting)(new MultiBoxSetting()).name("Options")).booleanSettings(((BooleanSetting)(new BooleanSetting()).name("Save Enchanted")).value(true), ((BooleanSetting)(new BooleanSetting()).name("Return Item")).value(true), ((BooleanSetting)(new BooleanSetting()).name("Ignore If Skull")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Ignore While Eating")).value(false));
   private final MultiBoxSetting modes = ((MultiBoxSetting)(new MultiBoxSetting()).name("Conditions")).booleanSettings(((BooleanSetting)(new BooleanSetting()).name("Elytra Health")).value(true), ((BooleanSetting)(new BooleanSetting()).name("Fall")).value(true), ((BooleanSetting)(new BooleanSetting()).name("Absorption")).value(true), ((BooleanSetting)(new BooleanSetting()).name("Crystals")).value(true), ((BooleanSetting)(new BooleanSetting()).name("Obsidian")).value(true), ((BooleanSetting)(new BooleanSetting()).name("Mace")).value(true), ((BooleanSetting)(new BooleanSetting()).name("TNT")).value(false));
   private int savedSlot = -1;
   private int totemScreenSlot = -1;
   private long actionStartTime = 0L;
   private boolean keysOverridden = false;
   private boolean wasForwardPressed;
   private boolean wasBackPressed;
   private boolean wasLeftPressed;
   private boolean wasRightPressed;
   private boolean wasJumpPressed;
   private Phase phase;
   private ItemStack backItemStack;
   private int oldSlot;
   private boolean returningItem;
   private boolean swappingTotem;
   private long lastSwapAttempt;
   private long lastSuccessfulSwap;
   private boolean totemIsUsed;
   private long lastTotemUseTime;

   public void onReceivePacket(ReceivePacketEvent receivePacketEvent) {
      if (mc.world != null && mc.player != null) {
         Packet var3 = receivePacketEvent.getPacket();
         if (var3 instanceof EntityStatusS2CPacket) {
            EntityStatusS2CPacket pkt = (EntityStatusS2CPacket)var3;
            if (pkt.getStatus() == 35 && pkt.getEntity(mc.world) == mc.player) {
               this.totemIsUsed = true;
               this.lastTotemUseTime = System.currentTimeMillis();
            }
         }

      }
   }

   public AutoTotem() {
      super(new ModuleInfo("AutoTotem", Category.COMBAT, "Берет тотем во вторую руку при необходимости"));
      this.phase = AutoTotem.Phase.READY;
      this.backItemStack = ItemStack.EMPTY;
      this.oldSlot = -1;
      this.returningItem = false;
      this.swappingTotem = false;
      this.lastSwapAttempt = 0L;
      this.lastSuccessfulSwap = 0L;
      this.totemIsUsed = false;
      this.lastTotemUseTime = 0L;
      this.addSetting(new Setting[]{this.healthThreshold, this.elytraHealth, this.crystalDistance, this.obsidianDistance, this.tntDistance, this.settings, this.modes});
   }

   public void onTick(TickEvent event) {
      if (mc.player != null && mc.world != null && mc.interactionManager != null) {
         if (this.phase != AutoTotem.Phase.READY) {
            this.executeLegit();
         } else {
            float health = mc.player.getHealth();
            float effectiveHealth = health;
            if (this.modes.getValueByName("Absorption")) {
               effectiveHealth = health + mc.player.getAbsorptionAmount();
            }

            boolean shouldSwap = effectiveHealth <= this.healthThreshold.getValue() || this.totemIsUsed && this.getTotemCount() > 0 && System.currentTimeMillis() - this.lastTotemUseTime >= 500L;
            if (this.modes.getValueByName("Elytra Health") && mc.player.isGliding() && health <= this.elytraHealth.getValue()) {
               shouldSwap = true;
            }

            if (this.modes.getValueByName("Fall") && mc.player.fallDistance > 10.0F) {
               shouldSwap = true;
            }

            if (this.modes.getValueByName("Crystals") && this.getClosestCrystalDistance() <= (double)this.crystalDistance.getValue()) {
               if (this.settings.getValueByName("Ignore If Skull") && this.isHoldingSkull()) {
                  shouldSwap = effectiveHealth <= this.healthThreshold.getValue();
               } else {
                  shouldSwap = true;
               }
            }

            if (this.modes.getValueByName("Obsidian") && this.getClosestObsidianDistance() <= (double)this.obsidianDistance.getValue()) {
               if (this.settings.getValueByName("Ignore If Skull") && this.isHoldingSkull()) {
                  shouldSwap = effectiveHealth <= this.healthThreshold.getValue();
               } else {
                  shouldSwap = true;
               }
            }

            if (this.modes.getValueByName("TNT") && this.getClosestTntDistance() <= (double)this.tntDistance.getValue()) {
               shouldSwap = true;
            }

            if (this.modes.getValueByName("Mace") && this.checkForMaceInEnemyHand()) {
               shouldSwap = true;
            }

            if (this.settings.getValueByName("Ignore While Eating") && mc.player.isUsingItem() && mc.player.getActiveItem().contains(DataComponentTypes.FOOD)) {
               shouldSwap = false;
            }

            ItemStack offhandStack = mc.player.getOffHandStack();
            boolean isTotemInOffhand = this.isTotemInOffhand();
            boolean isEnchantedInOffhand = isTotemInOffhand && EnchantmentHelper.hasEnchantments(offhandStack);
            boolean hasNormalTotem = isTotemInOffhand && !isEnchantedInOffhand;
            boolean isOnCooldown = isTotemInOffhand && mc.player.getItemCooldownManager().isCoolingDown(Items.TOTEM_OF_UNDYING.getDefaultStack());
            if (isOnCooldown && this.backItemStack != ItemStack.EMPTY && this.oldSlot != -1 && this.settings.getValueByName("Return Item") && !this.returningItem) {
               this.tryReturnItem();
            } else {
               if (hasNormalTotem && !shouldSwap) {
                  this.swappingTotem = false;
               }

               boolean totemWasUsed = this.totemIsUsed && !this.isTotemInOffhand() && System.currentTimeMillis() - this.lastTotemUseTime > 200L;
               boolean canReturnAfterUse = totemWasUsed && mc.player.isOnGround() && mc.player.fallDistance < 6.0F;
               boolean shouldReturn = !shouldSwap && this.isTotemInOffhand() || canReturnAfterUse;
               if (shouldReturn && this.oldSlot != -1 && this.settings.getValueByName("Return Item")) {
                  this.tryReturnItem();
               } else {
                  if (this.returningItem && !shouldReturn) {
                     this.returningItem = false;
                  }

                  if (shouldSwap && (!hasNormalTotem || isEnchantedInOffhand)) {
                     if (hasNormalTotem) {
                        this.swappingTotem = false;
                        return;
                     }

                     if (this.swappingTotem) {
                        return;
                     }

                     long now = System.currentTimeMillis();
                     if (now - this.lastSwapAttempt < 100L || now - this.lastSuccessfulSwap < 200L) {
                        return;
                     }

                     if (mc.currentScreen != null && !(mc.currentScreen instanceof Gui)) {
                        return;
                     }

                     if (isOnCooldown) {
                        if (this.backItemStack != ItemStack.EMPTY && this.oldSlot != -1 && this.settings.getValueByName("Return Item") && !this.returningItem) {
                           this.tryReturnItem();
                        }

                        return;
                     }

                     if (this.isTotemInOffhand()) {
                        ItemStack foundTotem = this.findTotemStack();
                        if (foundTotem != null) {
                           boolean curEnch = EnchantmentHelper.hasEnchantments(offhandStack);
                           boolean fndEnch = EnchantmentHelper.hasEnchantments(foundTotem);
                           if (curEnch == fndEnch || !curEnch && fndEnch) {
                              return;
                           }
                        }
                     }

                     int slotId = this.findTotemSlotId();
                     if (slotId < 0) {
                        return;
                     }

                     this.savedSlot = mc.player.getInventory().selectedSlot;
                     this.lastSwapAttempt = now;
                     this.swappingTotem = true;
                     this.totemScreenSlot = slotId;
                     if (!offhandStack.isEmpty() && this.oldSlot == -1 && this.settings.getValueByName("Return Item")) {
                        this.oldSlot = slotId;
                        this.backItemStack = offhandStack.copy();
                     }

                     this.performClickSwap(this.totemScreenSlot);
                     this.totemIsUsed = false;
                     int slotToRestore = this.savedSlot;
                     this.resetPhase();
                     if (slotToRestore >= 0) {
                        mc.player.getInventory().selectedSlot = slotToRestore;
                     }

                     if (this.isTotemInOffhand() && !EnchantmentHelper.hasEnchantments(mc.player.getOffHandStack())) {
                        this.lastSuccessfulSwap = System.currentTimeMillis();
                     }

                     this.swappingTotem = false;
                  } else if (this.swappingTotem && hasNormalTotem) {
                     this.swappingTotem = false;
                  }

               }
            }
         }
      } else {
         this.resetState();
      }
   }

   private void performClickSwap(int screenSlot) {
      if (mc.interactionManager != null && mc.player.playerScreenHandler != null) {
         mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, screenSlot, 40, SlotActionType.SWAP, mc.player);
      }
   }

   private void tryReturnItem() {
      if (this.backItemStack != ItemStack.EMPTY && this.oldSlot != -1) {
         if (mc.player != null && mc.player.playerScreenHandler != null && mc.interactionManager != null) {
            ItemStack stackInSlot = this.getStackByScreenSlot(this.oldSlot);
            if (!stackInSlot.isEmpty() && stackInSlot.getItem() == this.backItemStack.getItem()) {
               if (!this.returningItem) {
                  this.returningItem = true;
                  this.performClickSwap(this.oldSlot);
                  this.backItemStack = ItemStack.EMPTY;
                  this.oldSlot = -1;
                  this.returningItem = false;
               }
            } else {
               InventoryResult found = InventoryToolkit.findInInventory((i) -> i.getItem() == this.backItemStack.getItem());
               if (found.found() && !this.returningItem) {
                  this.returningItem = true;
                  this.performClickSwap(found.slot());
                  this.backItemStack = ItemStack.EMPTY;
                  this.oldSlot = -1;
                  this.returningItem = false;
               } else {
                  this.oldSlot = -1;
                  this.backItemStack = ItemStack.EMPTY;
               }
            }

         }
      }
   }

   private ItemStack getStackByScreenSlot(int screenSlot) {
      int invSlot = screenSlot >= 36 && screenSlot <= 44 ? screenSlot - 36 : screenSlot;
      return invSlot >= 0 && invSlot < mc.player.getInventory().size() ? mc.player.getInventory().getStack(invSlot) : ItemStack.EMPTY;
   }

   private int findTotemSlotId() {
      if (this.settings.getValueByName("Save Enchanted")) {
         InventoryResult r = InventoryToolkit.findInInventory((i) -> i.getItem() == Items.TOTEM_OF_UNDYING && !EnchantmentHelper.hasEnchantments(i));
         if (r.found()) {
            return r.slot();
         }
      }

      InventoryResult r = InventoryToolkit.findItemInInventory(Items.TOTEM_OF_UNDYING);
      return r.found() ? r.slot() : -1;
   }

   private ItemStack findTotemStack() {
      InventoryResult r = InventoryToolkit.findItemInInventory(Items.TOTEM_OF_UNDYING);
      return r.found() ? r.stack() : null;
   }

   private void startLegitEquip() {
      long h = mc.getWindow().getHandle();
      this.wasForwardPressed = InputUtil.isKeyPressed(h, mc.options.forwardKey.getDefaultKey().getCode());
      this.wasBackPressed = InputUtil.isKeyPressed(h, mc.options.backKey.getDefaultKey().getCode());
      this.wasLeftPressed = InputUtil.isKeyPressed(h, mc.options.leftKey.getDefaultKey().getCode());
      this.wasRightPressed = InputUtil.isKeyPressed(h, mc.options.rightKey.getDefaultKey().getCode());
      this.wasJumpPressed = InputUtil.isKeyPressed(h, mc.options.jumpKey.getDefaultKey().getCode());
      this.phase = AutoTotem.Phase.SLOWING_DOWN;
      this.actionStartTime = System.currentTimeMillis();
      this.keysOverridden = false;
   }

   private void executeLegit() {
      if (mc.player != null && mc.currentScreen == null) {
         long elapsed = System.currentTimeMillis() - this.actionStartTime;
         switch (this.phase.ordinal()) {
            case 1:
               mc.player.input.movementForward = 0.0F;
               mc.player.input.movementSideways = 0.0F;
               if (mc.player.isSprinting()) {
                  mc.player.setSprinting(false);
               }

               if (!this.keysOverridden) {
                  mc.options.forwardKey.setPressed(false);
                  mc.options.backKey.setPressed(false);
                  mc.options.leftKey.setPressed(false);
                  mc.options.rightKey.setPressed(false);
                  mc.options.jumpKey.setPressed(false);
                  this.keysOverridden = true;
               }

               if (elapsed > 1L) {
                  this.phase = AutoTotem.Phase.SWAP_TOTEM;
                  this.actionStartTime = System.currentTimeMillis();
               }
               break;
            case 2:
               if (elapsed > 25L) {
                  if (this.totemScreenSlot < 0) {
                     this.resetState();
                     return;
                  }

                  this.performClickSwap(this.totemScreenSlot);
                  this.phase = AutoTotem.Phase.AWAIT_SWITCH;
                  this.actionStartTime = System.currentTimeMillis();
               }
               break;
            case 3:
               if (this.isTotemInOffhand() || elapsed > 50L) {
                  this.phase = AutoTotem.Phase.RESTORE_SLOT;
                  this.actionStartTime = System.currentTimeMillis();
               }
               break;
            case 4:
               if (elapsed > 25L) {
                  InventoryToolkit.switchTo(this.savedSlot);
                  if (this.keysOverridden) {
                     this.restoreKeyStates();
                  }

                  this.actionStartTime = System.currentTimeMillis();
                  this.phase = AutoTotem.Phase.SPEEDING_UP;
               }
               break;
            case 5:
               long sp = System.currentTimeMillis() - this.actionStartTime;
               float progress = Math.min(1.0F, (float)sp / 20.0F);
               if (mc.player.input != null) {
                  boolean fwd = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode());
                  mc.player.input.movementForward = this.lerp(mc.player.input.movementForward, (fwd ? 1.0F : 0.0F) * progress, 0.4F);
                  if (progress > 0.4F && fwd && !mc.player.isSprinting()) {
                     mc.player.setSprinting(true);
                  }
               }

               if (sp > 25L) {
                  this.phase = AutoTotem.Phase.FINISH;
               }
               break;
            case 6:
               if (this.isTotemInOffhand() && !EnchantmentHelper.hasEnchantments(mc.player.getOffHandStack())) {
                  this.lastSuccessfulSwap = System.currentTimeMillis();
               }

               this.swappingTotem = false;
               this.resetPhase();
         }

      } else {
         this.resetState();
      }
   }

   private float lerp(float a, float b, float t) {
      return a + (b - a) * t;
   }

   private void restoreKeyStates() {
      long h = mc.getWindow().getHandle();
      mc.options.forwardKey.setPressed(this.wasForwardPressed && InputUtil.isKeyPressed(h, mc.options.forwardKey.getDefaultKey().getCode()));
      mc.options.backKey.setPressed(this.wasBackPressed && InputUtil.isKeyPressed(h, mc.options.backKey.getDefaultKey().getCode()));
      mc.options.leftKey.setPressed(this.wasLeftPressed && InputUtil.isKeyPressed(h, mc.options.leftKey.getDefaultKey().getCode()));
      mc.options.rightKey.setPressed(this.wasRightPressed && InputUtil.isKeyPressed(h, mc.options.rightKey.getDefaultKey().getCode()));
      mc.options.jumpKey.setPressed(this.wasJumpPressed && InputUtil.isKeyPressed(h, mc.options.jumpKey.getDefaultKey().getCode()));
      this.keysOverridden = false;
   }

   private boolean isTotemInOffhand() {
      if (mc.player == null) {
         return false;
      } else {
         return mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING;
      }
   }

   private boolean isHoldingSkull() {
      return this.isSkull(mc.player.getMainHandStack()) || this.isSkull(mc.player.getOffHandStack());
   }

   private boolean isSkull(ItemStack s) {
      if (s.isEmpty()) {
         return false;
      } else {
         return s.getItem() == Items.SKELETON_SKULL || s.getItem() == Items.WITHER_SKELETON_SKULL || s.getItem() == Items.ZOMBIE_HEAD || s.getItem() == Items.PLAYER_HEAD || s.getItem() == Items.CREEPER_HEAD || s.getItem() == Items.DRAGON_HEAD || s.getItem() == Items.PIGLIN_HEAD;
      }
   }

   private boolean checkForMaceInEnemyHand() {
      Box box = mc.player.getBoundingBox().expand((double)30.0F);

      for(AbstractClientPlayerEntity e : mc.world.getEntitiesByClass(AbstractClientPlayerEntity.class, box, (x) -> true)) {
         if (e != mc.player && (e.getMainHandStack().getItem() == Items.MACE || e.getOffHandStack().getItem() == Items.MACE)) {
            return true;
         }
      }

      return false;
   }

   private double getClosestCrystalDistance() {
      double min = Double.MAX_VALUE;
      Vec3d pos = mc.player.getPos();

      for(EndCrystalEntity e : mc.world.getEntitiesByClass(EndCrystalEntity.class, mc.player.getBoundingBox().expand((double)this.crystalDistance.getValue()), (x) -> true)) {
         min = Math.min(min, pos.distanceTo(e.getPos()));
      }

      return min;
   }

   private double getClosestObsidianDistance() {
      double min = Double.MAX_VALUE;
      BlockPos o = mc.player.getBlockPos();
      int r = (int)Math.ceil((double)this.obsidianDistance.getValue());

      for(int x = -r; x <= r; ++x) {
         for(int y = -r; y <= r; ++y) {
            for(int z = -r; z <= r; ++z) {
               BlockPos p = o.add(x, y, z);
               if (mc.world.getBlockState(p).isOf(Blocks.OBSIDIAN)) {
                  min = Math.min(min, (double)MathHelper.sqrt((float)o.getSquaredDistance(p)));
               }
            }
         }
      }

      return min;
   }

   private double getClosestTntDistance() {
      double min = Double.MAX_VALUE;
      Vec3d pos = mc.player.getPos();

      for(TntEntity t : mc.world.getEntitiesByClass(TntEntity.class, mc.player.getBoundingBox().expand((double)this.tntDistance.getValue()), (x) -> true)) {
         min = Math.min(min, pos.distanceTo(t.getPos()));
      }

      return min;
   }

   private int getTotemCount() {
      return (int)mc.player.getInventory().main.stream().filter((s) -> s.getItem() == Items.TOTEM_OF_UNDYING).count();
   }

   private void resetPhase() {
      if (this.keysOverridden) {
         this.restoreKeyStates();
      }

      this.totemScreenSlot = -1;
      this.savedSlot = -1;
      this.actionStartTime = 0L;
      this.phase = AutoTotem.Phase.READY;
   }

   private void resetState() {
      this.resetPhase();
      this.backItemStack = ItemStack.EMPTY;
      this.oldSlot = -1;
      this.returningItem = false;
      this.swappingTotem = false;
      this.lastSwapAttempt = 0L;
      this.lastSuccessfulSwap = 0L;
      this.totemIsUsed = false;
      this.lastTotemUseTime = 0L;
   }

   public void onDisable() {
      this.resetState();
   }

   @Environment(EnvType.CLIENT)
   private static enum Phase {
      READY,
      SLOWING_DOWN,
      SWAP_TOTEM,
      AWAIT_SWITCH,
      RESTORE_SLOT,
      SPEEDING_UP,
      FINISH;

      // $FF: synthetic method
      private static Phase[] $values() {
         return new Phase[]{READY, SLOWING_DOWN, SWAP_TOTEM, AWAIT_SWITCH, RESTORE_SLOT, SPEEDING_UP, FINISH};
      }
   }
}
