package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.mixins.other.IClientPlayNetworkHandlerMixin;
import java.time.Instant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.encryption.NetworkEncryptionUtils.SecureRandomUtil;
import net.minecraft.network.message.LastSeenMessagesCollector;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageChain;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({ClientPlayNetworkHandler.class})
public class ClientPlayNetworkHandlerMixin implements IClientPlayNetworkHandlerMixin {
   @Shadow
   private MessageChain.Packer messagePacker;
   @Shadow
   private LastSeenMessagesCollector lastSeenMessagesCollector;

   @Inject(
      method = {"sendChatMessage"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void sendChatMessage(String content, CallbackInfo ci) {
      boolean send = CrolClient.INSTANCE.getCommandManager().executeCommand(content);
      if (send) {
         Instant instant = Instant.now();
         long l = SecureRandomUtil.nextLong();
         LastSeenMessagesCollector.LastSeenMessages lastSeenMessages = this.lastSeenMessagesCollector.collect();
         MessageSignatureData messageSignatureData = this.messagePacker.pack(new MessageBody(content, instant, l, lastSeenMessages.lastSeen()));
         MinecraftClient.getInstance().player.networkHandler.sendPacket(new ChatMessageC2SPacket(content, instant, l, messageSignatureData, lastSeenMessages.update()));
      }

      ci.cancel();
   }

   public void sendMessageToServer(String msg) {
      Instant timestamp = Instant.now();
      long salt = SecureRandomUtil.nextLong();
      LastSeenMessagesCollector.LastSeenMessages lastSeenMessages = this.lastSeenMessagesCollector.collect();
      MessageBody messageBody = new MessageBody(msg, timestamp, salt, lastSeenMessages.lastSeen());
      MessageSignatureData signatureData = this.messagePacker.pack(messageBody);
      ChatMessageC2SPacket packet = new ChatMessageC2SPacket(msg, timestamp, salt, signatureData, lastSeenMessages.update());
      MinecraftClient.getInstance().player.networkHandler.sendPacket(packet);
   }
}
