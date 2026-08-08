package crol.client.modules.impl.combat;

import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.ITickable;
import crol.client.mixins.other.IClientWorldMixin;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.util.IUtil;
import crol.client.util.player.PacketUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class BowSpammer extends Module implements ITickable, IUtil {
   public FloatSetting value = ((FloatSetting)(new FloatSetting()).name("Value")).minValue(1.0F).minValue(1.0F).maxValue(18.0F).incriment(1.0F).value(5.0F);

   public BowSpammer() {
      super(new ModuleInfo("BowSpammer", Category.COMBAT, "Спаммит выстрелами вблизи с лука"));
      this.addSetting(new Setting[]{this.value});
   }

   public void onTick(TickEvent event) {
      if ((mc.player.getOffHandStack().getItem() == Items.BOW || mc.player.getMainHandStack().getItem() == Items.BOW) && mc.player.isUsingItem() && (float)mc.player.getItemUseTime() >= this.value.getValue()) {
         PacketUtil.sendPacket(new PlayerActionC2SPacket(Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
         this.sendSequencedPacket((id) -> new PlayerInteractItemC2SPacket(mc.player.getOffHandStack().getItem() == Items.BOW ? Hand.OFF_HAND : Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
         mc.player.stopUsingItem();
      }

   }

   public void sendSequencedPacket(SequencedPacketCreator packetCreator) {
      if (mc.getNetworkHandler() != null && mc.world != null) {
         PendingUpdateManager pendingUpdateManager = ((IClientWorldMixin)mc.world).getPendingUpdateManager2().incrementSequence();

         try {
            int i = pendingUpdateManager.getSequence();
            mc.getNetworkHandler().sendPacket(packetCreator.predict(i));
         } catch (Throwable var6) {
            if (pendingUpdateManager != null) {
               try {
                  pendingUpdateManager.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (pendingUpdateManager != null) {
            pendingUpdateManager.close();
         }

      }
   }
}
