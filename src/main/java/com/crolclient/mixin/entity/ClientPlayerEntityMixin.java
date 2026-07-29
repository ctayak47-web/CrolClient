package com.crolclient.mixin.entity;

import com.crolclient.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();

        // Auto Sprint
        if (ConfigManager.getConfig().autoSprintEnabled) {
            if (player.input.movementForward > 0
                    && !player.isSprinting()
                    && !player.isSneaking()
                    && !player.isUsingItem()) {
                player.setSprinting(true);
            }
        }

        // Auto Eat
        if (ConfigManager.getConfig().autoEatEnabled
                && player.getHungerManager().getFoodLevel() <= ConfigManager.getConfig().autoEatThreshold
                && !player.isUsingItem()
                && client.interactionManager != null) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = player.getInventory().getStack(i);
                if (stack.contains(DataComponentTypes.FOOD)) {
                    int prevSlot = player.getInventory().selectedSlot;
                    player.getInventory().selectedSlot = i;
                    client.interactionManager.interactItem(player, Hand.MAIN_HAND);
                    player.getInventory().selectedSlot = prevSlot;
                    break;
                }
            }
        }
    }
}
