package crol.client.modules.impl.combat;

import com.mojang.authlib.GameProfile;
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
import crol.client.modules.settings.impl.ModeSetting;
import crol.client.util.IUtil;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.util.Hand;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class AntiBot extends Module implements ITickable, IReceivePacketable, IUtil, IDisableable {
   Set<UUID> suspectSet = new HashSet();
   static Set<UUID> botSet = new HashSet();
   private final ModeSetting mode = ((ModeSetting)(new ModeSetting()).name("Mode")).value("Matrix").modes("Matrix", "ReallyWorld");
   public final BooleanSetting removeBot = ((BooleanSetting)(new BooleanSetting()).name("RemoveBot")).value(false);

   public AntiBot() {
      super(new ModuleInfo("AntiBot", Category.COMBAT, "Спасает от банов, определяя серверных ботов"));
      this.addSetting(new Setting[]{this.mode, this.removeBot});
   }

   public void onReceivePacket(ReceivePacketEvent receivePacketEvent) {
      Packet var10000 = receivePacketEvent.getPacket();
      Objects.requireNonNull(var10000);
      Packet var2 = var10000;
      if (var2 instanceof PlayerListS2CPacket list) {
         this.checkPlayerAfterSpawn(list);
      } else if (var2 instanceof PlayerRemoveS2CPacket remove) {
         this.removePlayerBecauseLeftServer(remove);
      }

   }

   @Compile
   public void onTick(TickEvent event) {
      if (!this.suspectSet.isEmpty()) {
         mc.world.getPlayers().stream().filter((p) -> this.suspectSet.contains(p.getUuid())).forEach(this::evaluateSuspectPlayer);
      }

      if (this.mode.is("Matrix")) {
         this.matrixMode();
      } else if (this.mode.is("ReallyWorld")) {
         this.ReallyWorldMode();
      }

      if (this.removeBot.getValue()) {
         List<Integer> toRemove = mc.world.getPlayers().stream().filter((p) -> p != null && p.getUuid() != null && botSet.contains(p.getUuid())).map((p) -> p.getId()).toList();
         toRemove.forEach((id) -> mc.world.removeEntity(id, RemovalReason.DISCARDED));
      }

   }

   private void checkPlayerAfterSpawn(PlayerListS2CPacket listS2CPacket) {
      listS2CPacket.getPlayerAdditionEntries().forEach((entry) -> {
         GameProfile profile = entry.profile();
         if (profile != null && !this.isRealPlayer(entry, profile)) {
            if (this.isDuplicateProfile(profile)) {
               botSet.add(profile.getId());
            } else {
               this.suspectSet.add(profile.getId());
            }

         }
      });
   }

   private void removePlayerBecauseLeftServer(PlayerRemoveS2CPacket removeS2CPacket) {
      removeS2CPacket.profileIds().forEach((uuid) -> {
         this.suspectSet.remove(uuid);
         botSet.remove(uuid);
      });
   }

   private boolean isRealPlayer(PlayerListS2CPacket.Entry entry, GameProfile profile) {
      return entry.latency() < 2 || profile.getProperties() != null && !profile.getProperties().isEmpty();
   }

   private void evaluateSuspectPlayer(PlayerEntity player) {
      Iterable<ItemStack> armor = null;
      if (!this.isFullyEquipped(player)) {
         armor = player.getArmorItems();
      }

      if (this.isFullyEquipped(player) || this.hasArmorChanged(player, armor)) {
         botSet.add(player.getUuid());
      }

      this.suspectSet.remove(player.getUuid());
   }

   private void matrixMode() {
      for(Iterator<UUID> iterator = this.suspectSet.iterator(); iterator.hasNext(); iterator.remove()) {
         UUID susPlayer = (UUID)iterator.next();
         PlayerEntity entity = mc.world.getPlayerByUuid(susPlayer);
         if (entity != null) {
            String playerName = entity.getName().getString();
            boolean isNameBot = playerName.startsWith("CIT-") && !playerName.contains("NPC") && !playerName.contains("[ZNPC]");
            int armorCount = 0;

            for(ItemStack item : entity.getArmorItems()) {
               if (!item.isEmpty()) {
                  ++armorCount;
               }
            }

            boolean isFullArmor = armorCount == 4;
            boolean isFakeUUID = !entity.getUuid().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes()));
            if (isFullArmor || isNameBot || isFakeUUID) {
               botSet.add(susPlayer);
            }
         }
      }

      if (mc.player.age % 100 == 0) {
         botSet.removeIf((uuid) -> mc.world.getPlayerByUuid(uuid) == null);
      }

   }

   private void ReallyWorldMode() {
      for(PlayerEntity entity : mc.world.getPlayers()) {
         if (!entity.getUuid().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + entity.getName().getString()).getBytes())) && !botSet.contains(entity.getUuid()) && !entity.getName().getString().contains("NPC") && !entity.getName().getString().startsWith("[ZNPC]")) {
            botSet.add(entity.getUuid());
         }
      }

   }

   private void newMatrixMode() {
      for(PlayerEntity entity : mc.world.getPlayers()) {
         if (entity != mc.player) {
            List<ItemStack> armorItems = StreamSupport.stream(entity.getArmorItems().spliterator(), false).toList();
            boolean allArmorValid = true;

            for(ItemStack item : armorItems) {
               if (item.isEmpty() || !item.isEnchantable() || item.getDamage() > 0) {
                  allArmorValid = false;
                  break;
               }
            }

            boolean hasSpecificArmor = false;

            for(ItemStack item : armorItems) {
               if (item.getItem() == Items.LEATHER_BOOTS || item.getItem() == Items.LEATHER_LEGGINGS || item.getItem() == Items.LEATHER_CHESTPLATE || item.getItem() == Items.LEATHER_HELMET || item.getItem() == Items.IRON_BOOTS || item.getItem() == Items.IRON_LEGGINGS || item.getItem() == Items.IRON_CHESTPLATE || item.getItem() == Items.IRON_HELMET) {
                  hasSpecificArmor = true;
                  break;
               }
            }

            if (allArmorValid && hasSpecificArmor && entity.getStackInHand(Hand.OFF_HAND).getItem() == Items.AIR && entity.getStackInHand(Hand.MAIN_HAND).getItem() != Items.AIR && entity.getHungerManager().getFoodLevel() == 20 && !entity.getName().getString().contains("NPC") && !entity.getName().getString().startsWith("[ZNPC]")) {
               botSet.add(entity.getUuid());
            } else {
               botSet.remove(entity.getUuid());
            }
         }
      }

   }

   public boolean isDuplicateProfile(GameProfile profile) {
      return ((ClientPlayNetworkHandler)Objects.requireNonNull(mc.getNetworkHandler())).getPlayerList().stream().filter((player) -> player.getProfile().getName().equals(profile.getName()) && !player.getProfile().getId().equals(profile.getId())).count() == 1L;
   }

   public boolean isFullyEquipped(PlayerEntity entity) {
      List<ItemStack> armor = (List<ItemStack>) entity.getArmorItems();
      if (armor == null || armor.size() < 4) {
         return false;
      }

      for (int i = 0; i < 4; i++) {
         ItemStack stack = armor.get(i);
         if (stack == null || stack.isEmpty()) {
            return false;
         }
         if (!(stack.getItem() instanceof ArmorItem)) {
            return false;
         }
         if (stack.hasEnchantments()) {
            return false;
         }
      }

      return true;
   }

   public boolean hasArmorChanged(PlayerEntity entity, Iterable<ItemStack> prevArmor) {
      if (prevArmor == null) {
         return true;
      } else {
         List<ItemStack> currentArmorList = StreamSupport.stream(entity.getArmorItems().spliterator(), false).toList();
         List<ItemStack> prevArmorList = StreamSupport.stream(prevArmor.spliterator(), false).toList();
         return !IntStream.range(0, Math.min(currentArmorList.size(), prevArmorList.size())).allMatch((i) -> ((ItemStack)currentArmorList.get(i)).equals(prevArmorList.get(i))) || currentArmorList.size() != prevArmorList.size();
      }
   }

   public boolean isBot(LivingEntity entity) {
      String playerName = entity.getName().getString();
      boolean isNameBot = playerName.startsWith("CIT-") && !playerName.contains("NPC") && !playerName.startsWith("[ZNPC]");
      boolean isMarkedBot = botSet.contains(entity.getUuid());
      return isNameBot || isMarkedBot || this.isBotU(entity);
   }

   public boolean isBot(UUID uuid) {
      return botSet.contains(uuid);
   }

   public boolean isBotU(Entity entity) {
      return !entity.getUuid().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + entity.getName().getString()).getBytes())) && entity.isInvisible() && !entity.getName().getString().contains("NPC") && !entity.getName().getString().startsWith("[ZNPC]");
   }

   public void reset() {
      this.suspectSet.clear();
      botSet.clear();
   }

   public void onDisable() {
      this.reset();
   }
}
