package crol.client.modules.impl.util;

import com.mojang.authlib.GameProfile;
import crol.client.event.classes.ReceivePacketEvent;
import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.IReceivePacketable;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.IEnableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Mode;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.ModeSetting;
import crol.client.util.IUtil;
import crol.client.util.math.TimerUtil;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.slot.SlotActionType;

@Environment(EnvType.CLIENT)
public class AutoDuel extends Module implements ITickable, IEnableable, IReceivePacketable {
   private final ModeSetting mode = ((ModeSetting)(new ModeSetting()).name("Mode")).value("Shield").modes("Shield", "Thorns", "Bow", "Totems", "NoDebuff", "Balls", "Classic", "CheatersParadise", "Netherite");
   private final ArrayList<Player> players = new ArrayList();
   private final TimerUtil timerUtil = new TimerUtil();
   private State state;
   private Player currentPlayer;
   private final TimerUtil stateTimer;

   public AutoDuel() {
      super(new ModuleInfo("AutoDuel", Category.UTIL, "Отправляет выбранные дуэли случайным противникам"));
      this.state = AutoDuel.State.IDLE;
      this.stateTimer = new TimerUtil();
      this.addSetting(new Setting[]{this.mode});
   }

   public void onReceivePacket(ReceivePacketEvent receivePacketEvent) {
      if (IUtil.mc.player != null && IUtil.mc.world != null) {
         Packet var3 = receivePacketEvent.getPacket();
         if (var3 instanceof GameMessageS2CPacket) {
            GameMessageS2CPacket gameMessageS2CPacket = (GameMessageS2CPacket)var3;
            String msg = gameMessageS2CPacket.content().getString();
            if (msg.contains("принял ваш запрос на поединок") && !msg.contains("не принял ваш запрос на поединок")) {
               this.setEnabled(false);
            }
         }

      }
   }

   public void onTick(TickEvent event) {
      int slot = this.getNum();
      switch (this.state.ordinal()) {
         case 0:
            if (this.timerUtil.isReached(500L)) {
               this.currentPlayer = (Player)this.players.stream().filter((p) -> !p.sended).findFirst().orElse((Player) null);
               if (this.currentPlayer != null) {
                  IUtil.mc.getNetworkHandler().sendChatCommand("duel " + this.currentPlayer.name);
                  this.stateTimer.reset();
                  this.state = AutoDuel.State.WAIT_AFTER_DUEL;
               }
            }
            break;
         case 1:
            if (this.stateTimer.isReached(300L)) {
               IUtil.mc.interactionManager.clickSlot(IUtil.mc.player.currentScreenHandler.syncId, slot, 1, SlotActionType.THROW, IUtil.mc.player);
               this.stateTimer.reset();
               this.state = AutoDuel.State.WAIT_AFTER_FIRST_CLICK;
            }
            break;
         case 2:
            if (this.stateTimer.isReached(300L)) {
               IUtil.mc.interactionManager.clickSlot(IUtil.mc.player.currentScreenHandler.syncId, 0, 1, SlotActionType.THROW, IUtil.mc.player);
               this.currentPlayer.sended = true;
               this.timerUtil.reset();
               this.state = AutoDuel.State.IDLE;
            }
      }

   }

   int getNum() {
      for(int i = 0; i < this.mode.getModes().size(); ++i) {
         if (((Mode)this.mode.getModes().get(i)).name().equals(this.mode.getValue())) {
            return i;
         }
      }

      return 0;
   }

   public List<String> getTabPlayerNames() {
      MinecraftClient client = MinecraftClient.getInstance();
      return client.player != null && client.player.networkHandler != null ? client.player.networkHandler.getPlayerList().stream().map(PlayerListEntry::getProfile).map(GameProfile::getName).toList() : List.of();
   }

   public void onEnable() {
      this.players.clear();
      this.getTabPlayerNames().forEach((s) -> this.players.add(new Player(s)));
   }

   @Environment(EnvType.CLIENT)
   private static enum State {
      IDLE,
      WAIT_AFTER_DUEL,
      WAIT_AFTER_FIRST_CLICK,
      DONE;

      // $FF: synthetic method
      private static State[] $values() {
         return new State[]{IDLE, WAIT_AFTER_DUEL, WAIT_AFTER_FIRST_CLICK, DONE};
      }
   }

   @Environment(EnvType.CLIENT)
   private class Player {
      String name;
      boolean sended;

      public Player(String name) {
         this.name = name;
         this.sended = false;
      }
   }
}
