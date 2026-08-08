package crol.client.util.aura;

import crol.client.modules.impl.combat.SyncTps;
import crol.client.util.IUtil;
import crol.client.util.player.inventory.InventoryUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

@Environment(EnvType.CLIENT)
public class AuraUtil implements IUtil {
   private static boolean isCrit() {
      boolean disableCrit = mc.player.isInLava() || mc.player.isSubmergedInWater() || mc.player.isGliding();
      if (!disableCrit) {
         return !mc.player.isOnGround() && mc.player.getMovement().y < -0.1 && mc.player.getVelocity().y < 0.08;
      } else {
         return true;
      }
   }

   private static boolean critCheck(boolean smartCrits) {
      if (smartCrits) {
         return mc.options.jumpKey.isPressed() ? isCrit() : true;
      } else {
         return isCrit();
      }
   }

   public static boolean canAttack(boolean smartCrits, SyncTps syncTps) {
      return (double)mc.player.getAttackCooldownProgress(syncTps.isEnabled() ? SyncTps.adjustTicks : 2.0F) >= 0.97 && critCheck(smartCrits);
   }

   public static void breakShield(LivingEntity target) {
      if (target.isBlocking() && target.getBlockingItem().getItem() == Items.SHIELD) {
         int invSlot = InventoryUtil.getAxeInInventoryOrHotbar(false);
         int hotBarSlot = InventoryUtil.getAxeInInventoryOrHotbar(true);
         if (hotBarSlot == -1 && invSlot != -1) {
            int bestSlot = InventoryUtil.findBestSlotInHotBar();
            mc.interactionManager.clickSlot(0, invSlot, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(0, bestSlot + 36, 0, SlotActionType.PICKUP, mc.player);
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(bestSlot));
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
            mc.interactionManager.clickSlot(0, bestSlot + 36, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(0, invSlot, 0, SlotActionType.PICKUP, mc.player);
         }

         if (hotBarSlot != -1) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(hotBarSlot));
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
         }
      }

   }

   private static double armorItem(ItemStack stack) {
      if (!(stack.getItem() instanceof ArmorItem)) {
         return (double)0.0F;
      } else if (mc.world == null) {
         return (double)0.0F;
      } else {
         double armorValue = (double)0.0F;
         AttributeModifiersComponent modifiers = (AttributeModifiersComponent)stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
         if (modifiers != null) {
            for(AttributeModifiersComponent.Entry entry : modifiers.modifiers()) {
               if (entry.attribute().value() == EntityAttributes.ARMOR) {
                  armorValue += entry.modifier().value();
               }
            }
         }

         ItemEnchantmentsComponent enchants = (ItemEnchantmentsComponent)stack.get(DataComponentTypes.ENCHANTMENTS);
         if (enchants != null) {
            for(Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : enchants.getEnchantmentEntries()) {
               if (((RegistryEntry)entry.getKey()).matchesKey(Enchantments.PROTECTION)) {
                  armorValue += (double)entry.getIntValue() * (double)0.25F;
               }
            }
         }

         return armorValue;
      }
   }

   public static double armor(LivingEntity entity) {
      double armor = (double)entity.getArmor();

      for(ItemStack item : entity.getArmorItems()) {
         armor += armorItem(item);
      }

      return armor;
   }

   public static double health(LivingEntity entity) {
      return (double)(entity.getHealth() + entity.getAbsorptionAmount());
   }

   public static double buffs(LivingEntity entity) {
      double buffs = (double)0.0F;

      for(StatusEffectInstance effect : entity.getStatusEffects()) {
         StatusEffect type = (StatusEffect)effect.getEffectType().value();
         if (type == StatusEffects.ABSORPTION.value()) {
            buffs += 1.2 * (double)(effect.getAmplifier() + 1);
         } else if (type == StatusEffects.RESISTANCE.value()) {
            buffs += (double)1.0F * (double)(effect.getAmplifier() + 1);
         } else if (type == StatusEffects.REGENERATION.value()) {
            buffs += 1.1 * (double)(effect.getAmplifier() + 1);
         }
      }

      return buffs;
   }

   public static double entity(LivingEntity entity, boolean health, boolean armor, boolean distance, double maxDistance, boolean buffs) {
      double a = (double)1.0F;
      double b = (double)1.0F;
      double c = (double)1.0F;
      double d = (double)1.0F;
      if (health) {
         a += health(entity);
      }

      if (armor) {
         b += armor(entity);
      }

      if (distance) {
         c += (double)entity.distanceTo(mc.player) / maxDistance;
      }

      if (buffs) {
         d += buffs(entity);
      }

      return a * b * d * c;
   }

   public static boolean canSeeEntityAtFov(Entity entityLiving, float scope, float yaw) {
      double diffX = entityLiving.getX() - mc.player.getX();
      double diffZ = entityLiving.getZ() - mc.player.getZ();
      float newYaw = (float)(Math.toDegrees(Math.atan2(diffZ, diffX)) - (double)90.0F);
      double d = (double)newYaw;
      double difference = angleDifference(d, (double)yaw);
      return difference <= (double)scope;
   }

   public static double angleDifference(double a, double b) {
      float yaw360 = (float)(Math.abs(a - b) % (double)360.0F);
      if (yaw360 > 180.0F) {
         yaw360 = 360.0F - yaw360;
      }

      return (double)yaw360;
   }
}
