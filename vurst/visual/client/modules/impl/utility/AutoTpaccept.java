
package vurst.visual.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.Packet;
import net.minecraft.GameMessageS2CPacket;
import vurst.visual.VurstVisual;
import vurst.visual.base.events.impl.server.EventPacket;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;

@ModuleAnnotation(name="AutoTpaccept", category=Category.MOVEMENT, description="Автоматически принимает запросы на телепортацию только от друзей.")
public final class AutoTpaccept
extends Module {
    public static final AutoTpaccept INSTANCE = new AutoTpaccept();
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("\\b[A-Za-z0-9_]{3,16}\\b");
    private static final long ACCEPT_COOLDOWN_MS = 1000L;
    private long lastAcceptAt;
    private String lastAcceptedName;

    private AutoTpaccept() {
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        Packet<?> packet;
        if (!event.isReceive() || !((packet = event.getPacket()) instanceof GameMessageS2CPacket)) {
            return;
        }
        GameMessageS2CPacket packet = (GameMessageS2CPacket)packet;
        if (AutoTpaccept.mc.player == null || AutoTpaccept.mc.player.networkHandler == null) {
            return;
        }
        String message = this.cleanMessage(packet.comp_763().getString());
        if (!this.isTeleportRequestMessage(message)) {
            return;
        }
        String requester = this.extractRequesterNickname(message);
        if (!this.isFriend(requester)) {
            return;
        }
        long now = System.currentTimeMillis();
        if (requester.equalsIgnoreCase(this.lastAcceptedName) && now - this.lastAcceptAt < 1000L) {
            return;
        }
        AutoTpaccept.mc.player.networkHandler.sendChatCommand("tpaccept");
        this.lastAcceptAt = now;
        this.lastAcceptedName = requester;
    }

    private boolean isFriend(String requester) {
        return requester != null && VurstVisual.getInstance().getFriendManager() != null && VurstVisual.getInstance().getFriendManager().isFriend(requester);
    }

    private boolean isTeleportRequestMessage(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }
        String lower = message.toLowerCase(Locale.ROOT);
        return lower.contains("телепортироваться") || lower.contains("телепортацию") || lower.contains("телепорт") || lower.contains("/tpaccept") || lower.contains("tpaccept");
    }

    private String extractRequesterNickname(String message) {
        if (message == null || message.isEmpty()) {
            return null;
        }
        String selfName = AutoTpaccept.mc.player != null && AutoTpaccept.mc.player.getGameProfile() != null ? AutoTpaccept.mc.player.getGameProfile().getName() : null;
        Matcher matcher = NICKNAME_PATTERN.matcher(message);
        while (matcher.find()) {
            String candidate = matcher.group();
            if (selfName != null && candidate.equalsIgnoreCase(selfName)) continue;
            return candidate;
        }
        return null;
    }

    private String cleanMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        return message.replaceAll("§[0-9A-FK-ORa-fk-or]", "").replaceAll("&[0-9A-FK-ORa-fk-or]", "").replaceAll("[\\x00-\\x1F\\x7F]", "").replaceAll("\\p{C}", "").trim();
    }
}

