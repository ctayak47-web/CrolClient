package crol.client.modules.impl.player;

import com.mojang.authlib.GameProfile;
import crol.client.CrolClient;
import crol.client.event.classes.CameraPositionEvent;
import crol.client.event.classes.InputEvent;
import crol.client.event.classes.Render2DEvent;
import crol.client.event.classes.SendPacketEvent;
import crol.client.event.interfaces.ICameraPosable;
import crol.client.event.interfaces.IInputable;
import crol.client.event.interfaces.IRenderable2D;
import crol.client.event.interfaces.ISendPacketable;
import crol.client.modules.Category;
import crol.client.modules.IDisableable;
import crol.client.modules.IEnableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.util.IUtil;
import java.util.Objects;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class FreeCam extends Module implements IUtil, IRenderable2D, ISendPacketable, IEnableable, IDisableable, IInputable, ICameraPosable {
   private Vec3d pos;
   private Vec3d prevPos;
   private Vec3d velocity;
   private Vec3d move;
   private boolean isOnGround;
   private OtherClientPlayerEntity fakeModel;
   private float yaw;
   private float pitch;
   private final BooleanSetting freeze = ((BooleanSetting)(new BooleanSetting()).name("Freze")).value(false);
   private final FloatSetting speed = ((FloatSetting)(new FloatSetting()).name("Speed")).value(8.0F).minValue(0.5F).maxValue(20.0F).incriment(0.5F);

   public FreeCam() {
      super(new ModuleInfo("FreeCam", Category.PLAYER, "Позволяет отделить камеру от игрока"));
      this.addSetting(new Setting[]{this.freeze, this.speed});
   }

   public void onSendPacket(SendPacketEvent sendPacketEvent) {
      if (mc.world != null) {
         Packet var10000 = sendPacketEvent.getPacket();
         Objects.requireNonNull(var10000);
         Packet var2 = var10000;
         byte var3 = 0;

         if (var2 instanceof PlayerMoveC2SPacket) {
            if (this.freeze.getValue()) {
               sendPacketEvent.cancel();
            }
         } else if (var2 instanceof PlayerRespawnS2CPacket) {
            this.setEnabled(false);
         } else if (var2 instanceof GameJoinS2CPacket) {
            this.setEnabled(false);
         }
      }
   }

   @Compile
   public void onDisable() {
      if (mc.world != null && mc.player != null) {
         mc.world.removeEntity(1337, RemovalReason.DISCARDED);
         mc.player.setPitch(this.pitch);
         mc.player.setYaw(this.yaw);
      }
   }

   @Compile
   public void onEnable() {
      if (mc.world != null && mc.player != null) {
         NameProtect nameProtect = (NameProtect)CrolClient.INSTANCE.getModuleManager().getByClass(NameProtect.class);
         this.fakeModel = new OtherClientPlayerEntity(mc.world, new GameProfile(UUID.randomUUID(), nameProtect.replace(mc.player.getNameForScoreboard())));
         this.fakeModel.copyFrom(mc.player);
         this.fakeModel.headYaw = mc.player.headYaw;
         this.fakeModel.bodyYaw = mc.player.bodyYaw;
         this.fakeModel.setId(1337);
         mc.world.addEntity(this.fakeModel);
         this.pos = mc.player.getPos().add((double)0.0F, (double)2.0F, (double)0.0F);
         this.prevPos = this.pos;
         this.velocity = mc.player.getVelocity();
         this.move = mc.player.getMovement();
         this.isOnGround = mc.player.isOnGround();
         this.yaw = mc.player.getYaw();
         this.pitch = mc.player.getPitch();
      }
   }

   public void onRender2D(Render2DEvent event) {
      if (mc.player != null) {
         double x = mc.player.getX();
         double minY = mc.player.getBoundingBox().minY;
         double maxY = mc.player.getBoundingBox().maxY;
         double z = mc.player.getZ();
         mc.player.setBoundingBox(new Box(x, minY, z, x, maxY, z));
      }
   }

   public void onInput(InputEvent event) {
      if (this.pos != null) {
         float forward = event.getForward();
         float strafe = -event.getStrafe();
         double spd = (double)this.speed.getValue() * 0.1;
         if (forward != 0.0F && strafe != 0.0F) {
            forward *= 0.7071F;
            strafe *= 0.7071F;
         }

         double yawRad = Math.toRadians((double)mc.getCameraEntity().getYaw());
         double sinYaw = Math.sin(yawRad);
         double cosYaw = Math.cos(yawRad);
         double mx = -((double)strafe * cosYaw) - (double)forward * sinYaw;
         double mz = -((double)strafe * sinYaw) + (double)forward * cosYaw;
         double dy = event.isJump() ? spd : (event.isSneak() ? -spd : (double)0.0F);
         this.prevPos = this.pos;
         this.pos = this.pos.add(mx * spd, dy, mz * spd);
         event.inputNone();
      }
   }

   public void onCamera(CameraPositionEvent cameraPositionEvent) {
      if (this.prevPos != null && this.pos != null) {
         float tickDelta = mc.getRenderTickCounter().getTickDelta(false);
         double ix = MathHelper.lerp((double)tickDelta, this.prevPos.x, this.pos.x);
         double iy = MathHelper.lerp((double)tickDelta, this.prevPos.y, this.pos.y);
         double iz = MathHelper.lerp((double)tickDelta, this.prevPos.z, this.pos.z);
         cameraPositionEvent.setPos(new Vec3d(ix, iy, iz));
         mc.options.setPerspective(Perspective.FIRST_PERSON);
      }
   }
}
