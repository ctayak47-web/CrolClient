
package vurst.visual.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ScoreboardObjective;
import net.minecraft.GameJoinS2CPacket;
import net.minecraft.Scoreboard;
import net.minecraft.ChatScreen;
import net.minecraft.GameMenuScreen;
import net.minecraft.HandledScreen;
import net.minecraft.ScoreboardDisplaySlot;
import vurst.visual.VurstVisual;
import vurst.visual.base.events.impl.player.EventUpdate;
import vurst.visual.base.events.impl.server.EventPacket;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;

@ModuleAnnotation(name="Auto Reconnect", category=Category.MOVEMENT, description="Автоматически возвращает на анархию после кика в хаб.")
public final class AutoReconnect
extends Module {
    public static final AutoReconnect INSTANCE = new AutoReconnect();
    private static final Pattern ANARCHY_PATTERN = Pattern.compile("Анархия-(\\d+)");
    private static final long HUB_STABILIZE_DELAY_MS = 900L;
    private static final long FIRST_RECONNECT_DELAY_MS = 1300L;
    private static final long RECONNECT_RETRY_MS = 2200L;
    private int lastKnownAnarchy = -1;
    private int reconnectTargetAnarchy = -1;
    private long nextReconnectAt = 0L;
    private long nonAnarchySince = 0L;
    private boolean reconnecting;
    private boolean reconnectNotified;

    private AutoReconnect() {
    }

    @Override
    public void onEnable() {
        this.reconnectTargetAnarchy = -1;
        this.nextReconnectAt = 0L;
        this.nonAnarchySince = 0L;
        this.reconnecting = false;
        this.reconnectNotified = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.resetReconnectState();
        this.lastKnownAnarchy = -1;
        this.nonAnarchySince = 0L;
        super.onDisable();
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (!event.isReceive() || !(event.getPacket() instanceof GameJoinS2CPacket) || this.lastKnownAnarchy <= 0) {
            return;
        }
        this.resetReconnectState();
        this.nonAnarchySince = System.currentTimeMillis();
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (AutoReconnect.mc.player == null || AutoReconnect.mc.world == null || mc.getNetworkHandler() == null) {
            return;
        }
        long now = System.currentTimeMillis();
        int currentAnarchy = this.resolveCurrentAnarchy();
        if (currentAnarchy > 0) {
            this.lastKnownAnarchy = currentAnarchy;
            this.nonAnarchySince = 0L;
            this.resetReconnectState();
            return;
        }
        if (!this.isSupportedServer() || this.lastKnownAnarchy <= 0) {
            this.nonAnarchySince = 0L;
            this.resetReconnectState();
            return;
        }
        if (this.nonAnarchySince == 0L) {
            this.nonAnarchySince = now;
        }
        if (this.isBlockingReconnectScreenOpen()) {
            return;
        }
        if (now - this.nonAnarchySince < 900L) {
            return;
        }
        if (!this.reconnecting || this.reconnectTargetAnarchy != this.lastKnownAnarchy) {
            this.reconnecting = true;
            this.reconnectTargetAnarchy = this.lastKnownAnarchy;
            this.nextReconnectAt = now + 1300L;
            this.reconnectNotified = false;
        }
        if (now < this.nextReconnectAt) {
            return;
        }
        this.sendReconnectCommand();
        this.nextReconnectAt = now + 2200L;
    }

    private int resolveCurrentAnarchy() {
        int trackedAnarchy;
        if (VurstVisual.getInstance().getServerHandler() != null && (trackedAnarchy = VurstVisual.getInstance().getServerHandler().getAnarchy()) > 0) {
            return trackedAnarchy;
        }
        Scoreboard scoreboard = AutoReconnect.mc.world.getScoreboard();
        if (scoreboard == null) {
            return -1;
        }
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null || objective.getDisplayName() == null) {
            return -1;
        }
        Matcher matcher = ANARCHY_PATTERN.matcher(objective.getDisplayName().getString());
        if (!matcher.find()) {
            return -1;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        }
        catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private boolean isSupportedServer() {
        if (VurstVisual.getInstance().getServerHandler() != null && VurstVisual.getInstance().getServerHandler().isCopyTime()) {
            return true;
        }
        if (mc.getNetworkHandler() == null || mc.getNetworkHandler().getServerInfo() == null) {
            return false;
        }
        String address = AutoReconnect.mc.getNetworkHandler().getServerInfo().address;
        if (address == null) {
            return false;
        }
        String lower = address.toLowerCase(Locale.ROOT);
        return lower.contains("funtime") || lower.contains("funsky") || lower.contains("skytime") || lower.contains("space-times");
    }

    private void sendReconnectCommand() {
        if (mc.getNetworkHandler() == null || this.reconnectTargetAnarchy <= 0) {
            return;
        }
        mc.getNetworkHandler().sendChatCommand("an" + this.reconnectTargetAnarchy);
        this.reconnectNotified = true;
    }

    private void resetReconnectState() {
        this.reconnectTargetAnarchy = -1;
        this.nextReconnectAt = 0L;
        this.reconnecting = false;
        this.reconnectNotified = false;
    }

    private boolean isBlockingReconnectScreenOpen() {
        if (AutoReconnect.mc.currentScreen == null) {
            return false;
        }
        return !(AutoReconnect.mc.currentScreen instanceof ChatScreen) && !(AutoReconnect.mc.currentScreen instanceof HandledScreen) && !(AutoReconnect.mc.currentScreen instanceof GameMenuScreen);
    }
}

