package crol.client.modules.impl.combat;

import crol.client.CrolClient;
import crol.client.event.classes.ReceivePacketEvent;
import crol.client.event.classes.Render2DEvent;
import crol.client.event.classes.RotationEvent;
import crol.client.event.classes.SendPacketEvent;
import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.IReceivePacketable;
import crol.client.event.interfaces.IRenderable2D;
import crol.client.event.interfaces.IRotateable;
import crol.client.event.interfaces.ISendPacketable;
import crol.client.event.interfaces.ITickable;
import crol.client.mixins.other.IPlayerMoveC2SPacket;
import crol.client.modules.Category;
import crol.client.modules.IEnableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.modules.settings.impl.ModeSetting;
import crol.client.modules.settings.impl.MultiBoxSetting;
import crol.client.util.IUtil;
import crol.client.util.math.MathUtil;
import crol.client.util.math.TimerUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;

@Environment(EnvType.CLIENT)
public class AutoPotion extends Module implements ITickable, IRenderable2D, IRotateable, IReceivePacketable, ISendPacketable, IEnableable, IUtil {
   private final ModeSetting mode = ((ModeSetting)(new ModeSetting()).name("Mode")).value("Legit").modes("Legit", "Instant");
   private final MultiBoxSetting potions = ((MultiBoxSetting)(new MultiBoxSetting()).name("Potions")).booleanSettings(((BooleanSetting)(new BooleanSetting()).name("Speed")).value(false), ((BooleanSetting)(new BooleanSetting()).name("NoFire")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Strength")).value(false));
   private Aura aura;
   public static boolean canSend = false;
   public static boolean lastRotated = true;
   private TimerUtil timerUtil = new TimerUtil();
   private TimerUtil timerUtil2 = new TimerUtil();
   private float lastPitch = 0.0F;
   public static float pitch = 0.0F;
   private boolean changeWorld = false;
   private boolean throwing = false;
   private RegistryEntry<StatusEffect> thrownEffect = null;
   private TimerUtil throwingTimeout = new TimerUtil();

   public AutoPotion() {
      super(new ModuleInfo("AutoPotion", Category.COMBAT, "Бросает полезные взрывные зелья под игрока"));
      this.addSetting(new Setting[]{this.mode, this.potions});
   }

   public void onReceivePacket(ReceivePacketEvent receivePacketEvent) {
      if (mc.currentScreen instanceof DownloadingTerrainScreen) {
         this.changeWorld = true;
      }

      if (!(mc.currentScreen instanceof DownloadingTerrainScreen) && this.changeWorld && mc.world != null) {
         this.timerUtil.reset();
         this.changeWorld = false;
      }

   }

   public void onRender2D(Render2DEvent event) {
      if (canSend) {
         pitch = MathUtil.fast(pitch, 90.0F, 10.0F);
      } else if (!lastRotated && !canSend) {
         pitch = MathUtil.fast(pitch, this.lastPitch, 10.0F);
         if (this.timerUtil2.isReached(400L)) {
            lastRotated = true;
         }
      }

   }

   public void onRotate(RotationEvent rotationEvent) {
      rotationEvent.setPitch(pitch);
      CrolClient.INSTANCE.setBodyPitch(pitch);
   }

   public void onSendPacket(SendPacketEvent sendPacketEvent) {
      Packet var3 = sendPacketEvent.getPacket();
      if (var3 instanceof PlayerMoveC2SPacket playerMoveC2SPacket) {
         ((IPlayerMoveC2SPacket)playerMoveC2SPacket).setPitch(pitch);
      }

      var3 = sendPacketEvent.getPacket();
      if (var3 instanceof PlayerInteractItemC2SPacket playerMoveC2SPacket) {
         ((IPlayerMoveC2SPacket)playerMoveC2SPacket).setPitch(pitch);
      }

   }

   public void onTick(TickEvent event) {
      canSend = false;
      if (this.throwing) {
         boolean effectArrived = this.thrownEffect != null && mc.player.hasStatusEffect(this.thrownEffect);
         boolean timedOut = this.throwingTimeout.isReached(1500L);
         if (!effectArrived && !timedOut) {
            canSend = true;
            return;
         }

         this.throwing = false;
         this.thrownEffect = null;
      }

      if (mc.player.isOnGround() && !mc.options.jumpKey.isPressed()) {
         for(int i = 0; i < 9; ++i) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            Item var5 = itemStack.getItem();
            if (var5 instanceof SplashPotionItem) {
               SplashPotionItem splashPotionItem = (SplashPotionItem)var5;
               if (this.canSend(itemStack) && !mc.options.useKey.isPressed()) {
                  lastRotated = false;
                  canSend = true;
                  if (pitch > 88.0F && this.timerUtil.isReached(this.mode.is("Legit") ? 200L : 60L)) {
                     mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(i));
                     mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, i, mc.player.getYaw(), mc.player.getPitch()));
                     mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
                     this.timerUtil.reset();
                     this.timerUtil2.reset();
                     this.throwing = true;
                     this.thrownEffect = this.getThrownEffect(itemStack);
                     this.throwingTimeout.reset();
                     break;
                  }
               }
            }
         }
      }

      if (!canSend && lastRotated) {
         this.lastPitch = mc.player.getPitch();
         pitch = this.aura.isEnabled() ? this.aura.getRotate().y : mc.player.getPitch();
      }

   }

   public void onEnable() {
      this.aura = (Aura)CrolClient.INSTANCE.getModuleManager().getByClass(Aura.class);
      canSend = false;
      lastRotated = true;
      this.throwing = false;
      this.thrownEffect = null;
      this.throwingTimeout.reset();
      this.timerUtil.reset();
      this.lastPitch = mc.player.getPitch();
      pitch = this.aura.isEnabled() ? this.aura.getRotate().y : mc.player.getPitch();
   }

   private RegistryEntry<StatusEffect> getThrownEffect(ItemStack itemStack) {
      PotionContentsComponent potionContentsComponent = (PotionContentsComponent)itemStack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);

      for(StatusEffectInstance statusEffect : potionContentsComponent.getEffects()) {
         if (this.potions.getValueByName("Speed") && statusEffect.getEffectType() == StatusEffects.SPEED) {
            return StatusEffects.SPEED;
         }

         if (this.potions.getValueByName("NoFire") && statusEffect.getEffectType() == StatusEffects.FIRE_RESISTANCE) {
            return StatusEffects.FIRE_RESISTANCE;
         }

         if (this.potions.getValueByName("Strength") && statusEffect.getEffectType() == StatusEffects.STRENGTH) {
            return StatusEffects.STRENGTH;
         }
      }

      return null;
   }

   private boolean canSend(ItemStack splashPotionItem) {
      boolean speed = false;
      boolean noFire = false;
      boolean strength = false;
      PotionContentsComponent potionContentsComponent = (PotionContentsComponent)splashPotionItem.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);

      for(StatusEffectInstance statusEffect : potionContentsComponent.getEffects()) {
         if (statusEffect.getEffectType() == StatusEffects.FIRE_RESISTANCE) {
            noFire = true;
         } else if (statusEffect.getEffectType() == StatusEffects.SPEED) {
            speed = true;
         } else if (statusEffect.getEffectType() == StatusEffects.STRENGTH) {
            strength = true;
         }
      }

      if (this.potions.getValueByName("Speed") && !mc.player.hasStatusEffect(StatusEffects.SPEED) && speed) {
         return true;
      } else if (this.potions.getValueByName("NoFire") && !mc.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE) && noFire) {
         return true;
      } else if (this.potions.getValueByName("Strength") && !mc.player.hasStatusEffect(StatusEffects.STRENGTH) && strength) {
         return true;
      } else {
         return false;
      }
   }

   public boolean isCanSend() {
      return canSend;
   }

   public boolean isThrowing() {
      return this.throwing;
   }
}
