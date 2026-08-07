
package vurst.visual.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.Formatting;
import net.minecraft.Entity;
import net.minecraft.PlayerEntity;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.World;
import net.minecraft.Text;
import net.minecraft.Packet;
import net.minecraft.EntityStatusS2CPacket;
import net.minecraft.MutableText;
import vurst.visual.base.events.impl.server.EventPacket;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.NumberSetting;

@ModuleAnnotation(name="TotemTracker", category=Category.MOVEMENT, description="Отслеживает сносы тотемов игроков.")
public final class TotemTracker
extends Module {
    public static final TotemTracker INSTANCE = new TotemTracker();
    private static final byte TOTEM_STATUS = 35;
    private final NumberSetting radius = new NumberSetting("Радиус", 50.0f, 5.0f, 200.0f, 1.0f);

    private TotemTracker() {
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        double distance;
        if (!event.isReceive() || TotemTracker.mc.world == null) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (!(packet instanceof EntityStatusS2CPacket)) {
            return;
        }
        EntityStatusS2CPacket packet = (EntityStatusS2CPacket)packet;
        if (packet.getStatus() != 35) {
            return;
        }
        Entity entity = packet.getEntity((World)TotemTracker.mc.world);
        if (!(entity instanceof PlayerEntity)) {
            return;
        }
        PlayerEntity player = (PlayerEntity)entity;
        if (TotemTracker.mc.player == null || player == TotemTracker.mc.player) {
            return;
        }
        ItemStack main = player.getMainHandStack();
        ItemStack off = player.getOffHandStack();
        ItemStack totem = null;
        if (main.getItem() == Items.TOTEM_OF_UNDYING) {
            totem = main;
        } else if (off.getItem() == Items.TOTEM_OF_UNDYING) {
            totem = off;
        }
        if (totem == null) {
            return;
        }
        float maxRadius = Math.max(0.0f, this.radius.getCurrent());
        if (maxRadius > 0.0f && (distance = TotemTracker.mc.player.getPos().distanceTo(player.getPos())) > (double)maxRadius) {
            return;
        }
        boolean enchanted = totem.hasEnchantments();
        String typeText = enchanted ? "зачарованный" : "не зачарованный";
        Formatting typeColor = enchanted ? Formatting.GREEN : Formatting.RED;
        MutableText message = Text.literal((String)"Игрок ").formatted(Formatting.WHITE).append((Text)Text.literal((String)player.getName().getString()).formatted(Formatting.WHITE)).append((Text)Text.literal((String)" потерял ").formatted(Formatting.WHITE)).append((Text)Text.literal((String)typeText).formatted(typeColor)).append((Text)Text.literal((String)" талисман").formatted(Formatting.WHITE));
        TotemTracker.mc.player.sendMessage((Text)message, false);
    }
}

