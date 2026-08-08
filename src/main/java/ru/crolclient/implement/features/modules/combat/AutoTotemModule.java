package ru.crolclient.implement.features.modules.combat;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import ru.crolclient.api.event.EventHandler;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.BooleanSetting;
import ru.crolclient.api.feature.module.setting.implement.ValueSetting;
import ru.crolclient.common.util.player.InventoryHandler;
import ru.crolclient.implement.events.player.TickEvent;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class AutoTotemModule extends Module {

    final ValueSetting healthThresholdSetting = new ValueSetting("Health Threshold", "HP value to trigger Totem swap.")
            .setValue(10.0F)
            .range(1.0F, 20.0F)
            .increment(0.5f);

    final BooleanSetting smartModeSetting = new BooleanSetting("Smart", "Considers additional risks and factors.")
            .setValue(false);

    final BooleanSetting priorityNoEnchantSetting = new BooleanSetting("Priority No Enchant", "Prioritize using Totem without enchantments.")
            .setValue(true);

    Item previousOffhandItem;
    boolean usingTotem = false;

    public AutoTotemModule() {
        super("AutoTotem", "Auto Totem", ModuleCategory.COMBAT);
        setup(healthThresholdSetting, smartModeSetting, priorityNoEnchantSetting);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (!isWorldLoaded()) {
            return;
        }

        ClientPlayerEntity player = mc.player;
        float health = player.getHealth() + player.getAbsorptionAmount();
        Item offhandItem = player.getOffHandStack().getItem();

        boolean needsTotem = health <= healthThresholdSetting.getValue()
                || (smartModeSetting.isValue() && shouldEquipTotemSmart());

        if (needsTotem && !usingTotem) {
            equipTotem(offhandItem);
        } else if (!needsTotem && usingTotem) {
            restorePreviousItem();
        }
    }

    private boolean shouldEquipTotemSmart() {
        ClientPlayerEntity player = mc.player;

        if (player == null) {
            return false;
        }

        if (isFallDamageFatal(player)) {
            return true;
        }

        if (isElytraCollisionFatal(player)) {
            return true;
        }

        if (isNearbyExplosive(player)) {
            return true;
        }

        if (isNearbyCreeper(player)) {
            return true;
        }

        return false;
    }

    private boolean isFallDamageFatal(ClientPlayerEntity player) {
        if (player.fallDistance > 3.0F) {
            float predictedFallDamage = calculateFallDamage(player.fallDistance);
            return predictedFallDamage >= player.getHealth() + player.getAbsorptionAmount();
        }
        return false;
    }

    private boolean isElytraCollisionFatal(ClientPlayerEntity player) {
        return player.isGliding() && player.horizontalCollision && player.getHealth() + player.getAbsorptionAmount() <= 20.0F;
    }

    private boolean isNearbyExplosive(ClientPlayerEntity player) {
        Box searchArea = new Box(player.getBlockPos()).expand(5.0);
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity && searchArea.contains(entity.getPos())) {
                return true;
            }
        }

        BlockPos playerPos = player.getBlockPos();
        for (BlockPos pos : BlockPos.iterateOutwards(playerPos, 5, 5, 5)) {
            if (mc.world.getBlockState(pos).isOf(Blocks.RESPAWN_ANCHOR)) {
                return true;
            }
        }

        return false;
    }

    private boolean isNearbyCreeper(ClientPlayerEntity player) {
        Box searchArea = new Box(player.getBlockPos()).expand(3.0);
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof CreeperEntity && searchArea.contains(entity.getPos())) {
                return true;
            }
        }
        return false;
    }

    private float calculateFallDamage(float fallDistance) {
        return Math.max(0, (fallDistance - 3.0F) * 2.0F);
    }

    private void equipTotem(Item currentOffhandItem) {
        int totemSlot = findTotemSlot();

        if (totemSlot == -1) {
            return;
        }
        previousOffhandItem = currentOffhandItem;
        moveItemToOffhand(totemSlot);
        usingTotem = true;
    }

    private void restorePreviousItem() {
        if (previousOffhandItem != null) {
            ClientPlayerEntity player = mc.player;
            if (player != null && player.getOffHandStack().getItem() != previousOffhandItem) {
                int previousItemSlot = InventoryHandler.findItemSlot(previousOffhandItem);

                if (previousItemSlot != -1) {
                    moveItemToOffhand(previousItemSlot);
                } else {
                    moveItemToOffhand(0);
                }
            }
        }

        usingTotem = false;
    }

    private void moveItemToOffhand(int slot) {
        ClientPlayerEntity player = mc.player;

        if (player == null || mc.interactionManager == null) {
            return;
        }

        int syncId = player.currentScreenHandler.syncId;

        mc.interactionManager.clickSlot(syncId, slot, 40, SlotActionType.SWAP, player);
    }

    private int findTotemSlot() {
        if (priorityNoEnchantSetting.isValue()) {
            int normalTotemSlot = InventoryHandler.findItemSlot(Items.TOTEM_OF_UNDYING);
            if (normalTotemSlot != -1) {
                return normalTotemSlot;
            }
        }

        return InventoryHandler.findItemSlot((Item) Items.TOTEM_OF_UNDYING);
    }

    private boolean isWorldLoaded() {
        return mc.world != null && mc.player != null && mc.interactionManager != null;
    }
}
