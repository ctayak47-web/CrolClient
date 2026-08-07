
package vurst.visual.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import java.util.Locale;
import net.minecraft.Formatting;
import net.minecraft.Entity;
import net.minecraft.ItemEntity;
import net.minecraft.ItemStack;
import net.minecraft.Text;
import net.minecraft.Packet;
import net.minecraft.ItemPickupAnimationS2CPacket;
import net.minecraft.MutableText;
import net.minecraft.TextColor;
import net.minecraft.DataComponentTypes;
import vurst.visual.base.events.impl.server.EventPacket;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.BooleanSetting;

@ModuleAnnotation(name="ItemPickupLogger", category=Category.MOVEMENT, description="Пишет в чат о подобранных предметах.")
public final class ItemPickupLogger
extends Module {
    public static final ItemPickupLogger INSTANCE = new ItemPickupLogger();
    private static final String PREFIX_TITLE = "Vurst Visual";
    private static final String PICKUP_TEXT = "Поднят предмет: ";
    private static final String PICKUP_DONATE_TEXT = "Поднят донат предмет: ";
    private static final String[] HIDE_AMOUNT_KEYWORDS = new String[]{"талисман", "понож", "шлем", "ботин", "нагруд", "меч", "арбалет", "трезуб", "лук", "сфера"};
    private final BooleanSetting onlyDonateItems = new BooleanSetting("Только донат предметы", false);

    private ItemPickupLogger() {
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (!this.isEnabled() || !event.isReceive() || ItemPickupLogger.mc.player == null || ItemPickupLogger.mc.world == null) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (!(packet instanceof ItemPickupAnimationS2CPacket)) {
            return;
        }
        ItemPickupAnimationS2CPacket packet = (ItemPickupAnimationS2CPacket)packet;
        if (packet.getCollectorEntityId() != ItemPickupLogger.mc.player.getId()) {
            return;
        }
        Entity entity = ItemPickupLogger.mc.world.getEntityById(packet.getEntityId());
        if (!(entity instanceof ItemEntity)) {
            return;
        }
        ItemEntity itemEntity = (ItemEntity)entity;
        ItemStack stack = itemEntity.getStack().copy();
        if (stack.isEmpty()) {
            return;
        }
        int amount = Math.max(1, packet.getStackAmount());
        stack.setCount(amount);
        this.onLocalItemPickup(stack, amount);
    }

    public void onLocalItemPickup(ItemStack stack, int amount) {
        if (!this.isEnabled() || ItemPickupLogger.mc.player == null || stack == null || stack.isEmpty() || amount <= 0) {
            return;
        }
        boolean donateItem = this.isDonateItem(stack);
        if (this.onlyDonateItems.isEnabled() && !donateItem) {
            return;
        }
        MutableText message = Text.literal((String)"[Vurst Visual]").formatted(Formatting.BLUE).append((Text)Text.literal((String)" » ").formatted(Formatting.WHITE)).append((Text)Text.literal((String)(donateItem ? PICKUP_DONATE_TEXT : PICKUP_TEXT)).formatted(Formatting.WHITE)).append((Text)stack.getName().copy());
        if (!this.shouldHideAmount(stack)) {
            message = message.append((Text)Text.literal((String)(" x" + amount)).formatted(Formatting.GRAY));
        }
        if (ItemPickupLogger.mc.inGameHud != null && ItemPickupLogger.mc.inGameHud.getChatHud() != null) {
            ItemPickupLogger.mc.inGameHud.getChatHud().addMessage((Text)message);
        } else {
            ItemPickupLogger.mc.player.sendMessage((Text)message, false);
        }
    }

    private boolean isDonateItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (!stack.contains(DataComponentTypes.CUSTOM_NAME)) {
            return false;
        }
        return this.findTextColor(stack.getName()) != -1;
    }

    private int findTextColor(Text text) {
        if (text == null) {
            return -1;
        }
        TextColor color = text.getStyle().getColor();
        if (color != null) {
            return color.getRgb();
        }
        for (Text sibling : text.getSiblings()) {
            int rgb = this.findTextColor(sibling);
            if (rgb == -1) continue;
            return rgb;
        }
        return -1;
    }

    private boolean shouldHideAmount(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        String name = stack.getName().getString();
        if (name == null || name.isEmpty()) {
            return false;
        }
        String lowerName = name.toLowerCase(Locale.ROOT);
        for (String keyword : HIDE_AMOUNT_KEYWORDS) {
            if (!lowerName.contains(keyword)) continue;
            return true;
        }
        return false;
    }
}

