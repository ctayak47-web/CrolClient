package crol.client.util.other;

import crol.client.mixins.hooks.ItemStackAccessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class AutoBuyUtil {
   private static AutoBuyUtil inst;
   private Pattern patternFuntime = Pattern.compile("\\$\\s*([0-9][\\d,]*)");
   private Pattern patternHollyWorld = Pattern.compile("Цена:(?:.*?\\{\"text\":\"([\\d ]+)\")");
   private final Map<ItemStack, NbtCompound> nbtCompoundMap = new HashMap();
   public List<String> testBypass = new ArrayList();

   public int getPrice(String nbt) {
      Matcher matcher = this.patternFuntime.matcher(nbt);
      if (matcher.find()) {
         String priceWithCommas = matcher.group(1);
         String price = priceWithCommas.replace(",", "");
         return Integer.parseInt(price);
      } else {
         matcher = this.patternHollyWorld.matcher(nbt);
         if (matcher.find()) {
            String amount = matcher.group(1).replaceAll(" ", "");
            return Integer.parseInt(amount);
         } else {
            return Integer.MAX_VALUE;
         }
      }
   }

   public boolean checkDon(ItemStack itemStack) {
      return itemStack.getCustomName().getString().contains("★");
   }

   public int getPrice(ItemStack itemStack) {
      String nbt = this.getNBT(itemStack);
      return this.getPrice(nbt);
   }

   public String getNBT(ItemStack itemStack) {
      return this.getTag(itemStack).toString();
   }

   public String getKey(ItemStack itemStack) {
      System.out.println(this.getNBT(itemStack));
      NbtComponent customData = (NbtComponent)itemStack.get(DataComponentTypes.CUSTOM_DATA);
      if (customData != null) {
         System.out.println(customData.getNbt().getKeys());
         System.out.println(itemStack.getItem());
         if (customData.getNbt().contains("kringeItems")) {
            NbtElement customEnchants = customData.getNbt().get("kringeItems");
            MinecraftClient.getInstance().keyboard.setClipboard(customEnchants.toString());
            return customEnchants.toString();
         }
      }

      return "";
   }

   public NbtCompound getTag(ItemStack stack) {
      MergedComponentMap components = (MergedComponentMap) stack.getComponents();
      ComponentChanges changes = components.getChanges();
      World world = MinecraftClient.getInstance().world;
      return world == null ? new NbtCompound() : (NbtCompound)this.nbtCompoundMap.computeIfAbsent(stack, (itemStack) -> (NbtCompound)ComponentChanges.CODEC.encodeStart(world.getRegistryManager().getOps(NbtOps.INSTANCE), changes).getOrThrow());
   }

   public boolean isAuction(ScreenHandler handledScreen) {
      return handledScreen.slots.size() == 90 && handledScreen.getSlot(49).getStack().getItem() == Items.NETHER_STAR;
   }

   public boolean isWaitBuy(ScreenHandler handledScreen) {
      return handledScreen.slots.size() == 63 && handledScreen.getSlot(0).getStack().getItem() == Items.LIME_STAINED_GLASS_PANE;
   }

   public static void test(int slotId) {
   }

   public static AutoBuyUtil getInstance() {
      if (inst == null) {
         inst = new AutoBuyUtil();
      }

      return inst;
   }
}
