package ru.crolclient.implement.features.modules.misc.minehelper;

import net.minecraft.block.AirBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import ru.crolclient.api.feature.module.setting.implement.GroupSetting;
import ru.crolclient.api.feature.module.setting.implement.MultiSelectSetting;

import java.util.ArrayList;
import java.util.List;

public class AutoTool {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final GroupSetting settings;

    private boolean swap;
    private long swapDelay;
    private final List<Integer> lastItem = new ArrayList<>();

    public AutoTool(GroupSetting settings) {
        this.settings = settings;
    }

    public void onTick() {
        if (!(mc.crosshairTarget instanceof BlockHitResult result))
            return;
        BlockPos pos = result.getBlockPos();
        if (mc.world.getBlockState(pos).isAir()) return;

        int tool = getTool(pos);
        if (tool != -1 && mc.options.attackKey.isPressed()) {
            lastItem.add(mc.player.getInventory().selectedSlot);
            mc.player.getInventory().selectedSlot = tool;
            swap = true;
            swapDelay = System.currentTimeMillis();
        } else if (swap && !lastItem.isEmpty() && System.currentTimeMillis() >= swapDelay + 300) {
            MultiSelectSetting options = (MultiSelectSetting) settings.getSubSetting("Options");
            if (options.isSelected("Swap Back")) {
                mc.player.getInventory().selectedSlot = lastItem.get(0);
                lastItem.clear();
                swap = false;
            }
        }
    }

    private int getTool(BlockPos pos) {
        int index = -1;
        float currentFastest = 1.0f;
        MultiSelectSetting options = (MultiSelectSetting) settings.getSubSetting("Options");

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack == ItemStack.EMPTY) continue;
            if (options.isSelected("Save Item") && stack.getMaxDamage() - stack.getDamage() <= 10) continue;

            float destroySpeed = stack.getMiningSpeedMultiplier(mc.world.getBlockState(pos));
            if (destroySpeed <= 1.0f) continue;

            RegistryEntry<Enchantment> efficiencyEntry = mc.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.EFFICIENCY.getValue()).orElseThrow();
            float digSpeed = EnchantmentHelper.getLevel(efficiencyEntry, stack);
            float totalSpeed = destroySpeed + (destroySpeed > 1.0f ? digSpeed * digSpeed + 1 : 0);

            if (mc.world.getBlockState(pos).getBlock() instanceof AirBlock) return -1;

            if (mc.world.getBlockState(pos).getBlock() instanceof EnderChestBlock && options.isSelected("Echest Silk")) {
                RegistryEntry<Enchantment> silkTouchEntry = mc.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.SILK_TOUCH.getValue()).orElseThrow();
                if (EnchantmentHelper.getLevel(silkTouchEntry, stack) > 0 && totalSpeed > currentFastest) {
                    currentFastest = totalSpeed;
                    index = i;
                }
            } else if (totalSpeed > currentFastest) {
                currentFastest = totalSpeed;
                index = i;
            }
        }
        return index;
    }
}