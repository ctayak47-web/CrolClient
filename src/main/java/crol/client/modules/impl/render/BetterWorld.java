package crol.client.modules.impl.render;

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
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.modules.settings.impl.ModeSetting;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class BetterWorld extends Module implements IReceivePacketable, ITickable, IDisableable, IUtil {
   public BooleanSetting changeTime = ((BooleanSetting)(new BooleanSetting()).name("Time Changer")).value(false);
   public FloatSetting time = ((FloatSetting)(new FloatSetting() {
      public boolean isVisible() {
         return BetterWorld.this.changeTime.getValue();
      }
   }).name("Time")).incriment(1.0F).minValue(1.0F).maxValue(40.0F).value(1.0F);
   public BooleanSetting fullBright = ((BooleanSetting)(new BooleanSetting() {
      public void preChangeState(boolean value) {
         if (!value) {
            MinecraftClient.getInstance().player.removeStatusEffect(StatusEffects.NIGHT_VISION);
         }

      }
   }).name("FullBright")).value(false);
   public BooleanSetting fog = ((BooleanSetting)(new BooleanSetting()).name("Fog")).value(false);
   public FloatSetting start = ((FloatSetting)(new FloatSetting() {
      public boolean isVisible() {
         return BetterWorld.this.fog.getValue();
      }
   }).name("StartFog")).minValue(0.0F).maxValue(20.0F).value(3.0F).incriment(1.0F);
   public FloatSetting end = ((FloatSetting)(new FloatSetting() {
      public boolean isVisible() {
         return BetterWorld.this.fog.getValue();
      }
   }).name("EndFog")).minValue(0.0F).maxValue(80.0F).value(50.0F).incriment(1.0F);
   public FloatSetting alphaFog = ((FloatSetting)(new FloatSetting() {
      public boolean isVisible() {
         return BetterWorld.this.fog.getValue();
      }
   }).name("AlphaFog")).minValue(30.0F).maxValue(100.0F).value(90.0F).incriment(5.0F);
   public final BooleanSetting skyShader = ((BooleanSetting)(new BooleanSetting() {
      public void onChangeState(boolean value) {
         MinecraftClient.getInstance().options.getBobView().setValue(false);
         super.onChangeState(value);
      }
   }).name("SkyShader")).value(false);
   public final BooleanSetting weather = ((BooleanSetting)(new BooleanSetting() {
   }).name("Weather")).value(false);
   public ModeSetting weatherMode = ((ModeSetting)(new ModeSetting() {
      public boolean isVisible() {
         return BetterWorld.this.weather.getValue();
      }
   }).name("WeatherMode")).value("Clear").modes("Clear", "Rain", "Storm");

   public BetterWorld() {
      super(new ModuleInfo("BetterWorld", Category.RENDER, "Изменяет отображение мира"));
      this.addSetting(new Setting[]{this.changeTime, this.time, this.fullBright, this.fog, this.start, this.end, this.alphaFog, this.skyShader, this.weather, this.weatherMode});
   }

   public void onReceivePacket(ReceivePacketEvent receivePacketEvent) {
      if (this.changeTime.getValue()) {
         Packet var8 = receivePacketEvent.getPacket();
         if (var8 instanceof WorldTimeUpdateS2CPacket) {
            WorldTimeUpdateS2CPacket var2 = (WorldTimeUpdateS2CPacket)var8;
            WorldTimeUpdateS2CPacket var10000 = var2;

            try {
               var10000.time();
            } catch (Throwable var15) {
               throw new MatchException(var15.toString(), var15);
            }

            if (true) {
               var10000 = var2;

               try {
                  var10000.timeOfDay();
               } catch (Throwable var14) {
                  throw new MatchException(var14.toString(), var14);
               }

               if (true) {
                  var10000 = var2;

                  try {
                     var10000.tickDayTime();
                  } catch (Throwable var13) {
                     throw new MatchException(var13.toString(), var13);
                  }

                  if (true) {
                     receivePacketEvent.cancel();
                  }
               }
            }
         }
      }

   }

   public void onTick(TickEvent event) {
      if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
         if (this.changeTime.getValue()) {
            MinecraftClient.getInstance().world.setTime(0L, (long)this.time.getValue() * 500L, false);
         }

         if (this.fullBright.getValue()) {
            StatusEffectInstance s = new StatusEffectInstance(StatusEffects.NIGHT_VISION, 400, 2);
            MinecraftClient.getInstance().player.addStatusEffect(s, MinecraftClient.getInstance().player);
         }

         if (this.skyShader.getValue()) {
            MinecraftClient.getInstance().options.getBobView().setValue(false);
         }

         if (this.weather.getValue()) {
            switch (this.weatherMode.getValue()) {
               case "Clear":
                  mc.world.getLevelProperties().setRaining(false);
                  mc.world.setRainGradient(0.0F);
                  mc.world.setThunderGradient(0.0F);
                  break;
               case "Rain":
                  mc.world.getLevelProperties().setRaining(true);
                  mc.world.setRainGradient(1.0F);
                  mc.world.setThunderGradient(0.0F);
                  break;
               case "Storm":
                  mc.world.getLevelProperties().setRaining(true);
                  mc.world.setRainGradient(1.0F);
                  mc.world.setThunderGradient(1.0F);
            }
         }
      }

   }

   @Compile
   public void onDisable() {
      if (this.fullBright.getValue()) {
         MinecraftClient.getInstance().player.removeStatusEffect(StatusEffects.NIGHT_VISION);
      }

      if (this.weather.getValue()) {
         mc.world.getLevelProperties().setRaining(mc.world.getLevelProperties().isRaining());
      }

   }
}
