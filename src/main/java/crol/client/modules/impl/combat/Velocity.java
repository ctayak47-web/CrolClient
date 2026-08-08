package crol.client.modules.impl.combat;

import crol.client.event.classes.ReceivePacketEvent;
import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.IReceivePacketable;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.IDisableable;
import crol.client.modules.IEnableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.ModeSetting;
import crol.client.util.IUtil;
import java.lang.reflect.Field;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class Velocity extends Module implements IReceivePacketable, IUtil, ITickable, IEnableable, IDisableable {
   private final ModeSetting mode = ((ModeSetting)(new ModeSetting()).name("Mode")).value("NewGrim").modes("NewGrim", "OldGrim", "Matrix", "Normal");
   private boolean flag;
   private int grimTicks;
   private int ccCooldown;

   public Velocity() {
      super(new ModuleInfo("Velocity", Category.COMBAT, "Убирает отдачу при получении урона"));
      this.addSetting(new Setting[]{this.mode});
   }

   public void onReceivePacket(ReceivePacketEvent receivePacketEvent) {
      if (mc.player != null && !mc.player.isTouchingWater() && !mc.player.isSubmergedInWater() && !mc.player.isInLava()) {
         if (this.ccCooldown > 0) {
            --this.ccCooldown;
         } else {
            Packet var3 = receivePacketEvent.getPacket();
            if (var3 instanceof EntityVelocityUpdateS2CPacket) {
               EntityVelocityUpdateS2CPacket pac = (EntityVelocityUpdateS2CPacket)var3;
               if (pac.getEntityId() == mc.player.getId()) {
                  switch (this.mode.getValue()) {
                     case "Matrix":
                        if (!this.flag) {
                           receivePacketEvent.cancel();
                           this.flag = true;
                        } else {
                           this.flag = false;
                           this.setVelocityX(pac, (int)(pac.getVelocityX() * -0.1));
                           this.setVelocityZ(pac, (int)(pac.getVelocityZ() * -0.1));
                        }
                        break;
                     case "Normal":
                        receivePacketEvent.cancel();
                        break;
                     case "OldGrim":
                        receivePacketEvent.cancel();
                        this.grimTicks = 6;
                        break;
                     case "NewGrim":
                        receivePacketEvent.cancel();
                        this.flag = true;
                  }
               }
            }

            if (this.mode.is("OldGrim") && receivePacketEvent.getPacket() instanceof CommonPingS2CPacket && this.grimTicks > 0) {
               receivePacketEvent.cancel();
               --this.grimTicks;
            }

            if (receivePacketEvent.getPacket() instanceof PlayerPositionLookS2CPacket && this.mode.is("NewGrim")) {
               this.ccCooldown = 5;
            }

         }
      }
   }

   @Compile
   public void onTick(TickEvent event) {
      if (mc.player != null && !mc.player.isTouchingWater() && !mc.player.isSubmergedInWater()) {
         if (this.mode.is("Matrix") && mc.player.hurtTime > 0 && !mc.player.isOnGround()) {
            double var3 = (double)(mc.player.getYaw() * ((float)Math.PI / 180F));
            double var5 = Math.sqrt(mc.player.getVelocity().x * mc.player.getVelocity().x + mc.player.getVelocity().z * mc.player.getVelocity().z);
            mc.player.setVelocity(-Math.sin(var3) * var5, mc.player.getVelocity().y, Math.cos(var3) * var5);
            mc.player.setSprinting(mc.player.age % 2 != 0);
         }

         if (this.mode.is("NewGrim") && this.flag) {
            if (this.ccCooldown <= 0) {
               mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround(), false));
               mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, BlockPos.ofFloored(mc.player.getPos()), Direction.DOWN));
            }

            this.flag = false;
         }

         if (this.grimTicks > 0) {
            --this.grimTicks;
         }

      }
   }

   public void onDisable() {
   }

   @Compile
   public void onEnable() {
      this.grimTicks = 0;
      this.flag = false;
      this.ccCooldown = 0;
   }

   @Compile
   private void setVelocityX(EntityVelocityUpdateS2CPacket packet, int value) {
      try {
         Field field = EntityVelocityUpdateS2CPacket.class.getDeclaredField("velocityX");
         field.setAccessible(true);
         field.setInt(packet, value);
      } catch (Exception ex) {
         ex.printStackTrace();
      }

   }

   @Compile
   private void setVelocityZ(EntityVelocityUpdateS2CPacket packet, int value) {
      try {
         Field field = EntityVelocityUpdateS2CPacket.class.getDeclaredField("velocityZ");
         field.setAccessible(true);
         field.setInt(packet, value);
      } catch (Exception ex) {
         ex.printStackTrace();
      }

   }
}
