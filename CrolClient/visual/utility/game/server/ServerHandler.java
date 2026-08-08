
package crol.client.utility.game.server;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import lombok.Generated;
import net.minecraft.Text;
import net.minecraft.Packet;
import net.minecraft.ScoreboardObjective;
import net.minecraft.GameJoinS2CPacket;
import net.minecraft.Team;
import net.minecraft.Scoreboard;
import net.minecraft.AbstractTeam;
import net.minecraft.WorldTimeUpdateS2CPacket;
import net.minecraft.ClientCommandC2SPacket;
import net.minecraft.MathHelper;
import net.minecraft.ChatScreen;
import net.minecraft.GameMenuScreen;
import net.minecraft.Screen;
import net.minecraft.HandledScreen;
import net.minecraft.ScoreboardDisplaySlot;
import net.minecraft.ScoreboardEntry;
import org.apache.commons.lang3.StringUtils;
import crol.client.base.events.impl.player.EventUpdate;
import crol.client.base.events.impl.server.EventPacket;
import crol.client.screens.menu.MenuScreen;
import crol.client.utility.game.player.PlayerIntersectionUtil;
import crol.client.utility.interfaces.IMinecraft;
import crol.client.utility.math.StopWatch;

public class ServerHandler
implements IMinecraft {
    private static final long EVENT_DELAY_COMMAND_DELAY_MS = 800L;
    private static final long EVENT_DELAY_COMMAND_TIMEOUT_MS = 5000L;
    private static final long JOIN_SCREEN_CLOSE_DELAY_MS = 3000L;
    private static final long JOIN_SCREEN_GUARD_MS = 10000L;
    private final StopWatch pvpWatch = new StopWatch();
    private String server = "Vanilla";
    private float TPS = 20.0f;
    private long timestamp;
    private boolean serverSprint;
    private int anarchy;
    private boolean sendEventDelayOnJoin;
    private long sendEventDelayAt;
    private long joinScreenCloseAt;
    private long joinScreenGuardUntil;
    private boolean pvpEnd;

    public ServerHandler() {
        EventManager.register(this);
    }

    @EventTarget
    public void tick(EventUpdate eventUpdate) {
        this.server = this.updateServer();
        this.anarchy = this.getAnarchyMode();
        this.pvpEnd = this.inPvpEnd();
        if (this.inPvp()) {
            this.pvpWatch.reset();
        }
        this.trySendEventDelayCommand();
        this.tryCloseLingeringJoinScreen();
    }

    @EventTarget
    public void packet(EventPacket e) {
        if (e.isReceive() && e.getPacket() instanceof GameJoinS2CPacket) {
            long now = System.currentTimeMillis();
            this.sendEventDelayOnJoin = true;
            this.sendEventDelayAt = now + 800L;
            this.joinScreenCloseAt = now + 3000L;
            this.joinScreenGuardUntil = now + 10000L;
            return;
        }
        if (e.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            long nanoTime = System.nanoTime();
            float maxTPS = 20.0f;
            float rawTPS = maxTPS * (1.0E9f / (float)(nanoTime - this.timestamp));
            this.TPS = MathHelper.clamp((float)rawTPS, (float)0.0f, (float)maxTPS);
            this.timestamp = nanoTime;
        }
    }

    @EventTarget
    public void onPacket(EventPacket e) {
        Packet<?> packet = e.getPacket();
        if (packet instanceof ClientCommandC2SPacket) {
            ClientCommandC2SPacket command = (ClientCommandC2SPacket)packet;
            if (command.getMode().equals((Object)ClientCommandC2SPacket.Mode.START_SPRINTING)) {
                e.setCancelled(this.serverSprint);
                this.serverSprint = true;
            } else if (command.getMode().equals((Object)ClientCommandC2SPacket.Mode.STOP_SPRINTING)) {
                e.setCancelled(!this.serverSprint);
                this.serverSprint = false;
            }
        }
    }

    private String updateServer() {
        if (PlayerIntersectionUtil.nullCheck() || mc.getNetworkHandler() == null || mc.getNetworkHandler().getServerInfo() == null || mc.getNetworkHandler().getBrand() == null) {
            return "Vanilla";
        }
        String serverIp = ServerHandler.mc.getNetworkHandler().getServerInfo().address.toLowerCase();
        String brand = mc.getNetworkHandler().getBrand().toLowerCase();
        if (brand.contains("botfilter")) {
            return "FunTime";
        }
        if (serverIp.contains("funtime") || serverIp.contains("skytime") || serverIp.contains("space-times") || serverIp.contains("funsky")) {
            return "CopyTime";
        }
        if (brand.contains("holyworld") || brand.contains("leaf") || brand.contains("vk.com/idwok")) {
            return "HolyWorld";
        }
        if (serverIp.contains("reallyworld")) {
            return "ReallyWorld";
        }
        return "Vanilla";
    }

    private int getAnarchyMode() {
        Scoreboard scoreboard = ServerHandler.mc.world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        switch (this.server) {
            case "FunTime": {
                String[] string;
                if (objective == null || (string = objective.getDisplayName().getString().split("-")).length <= 1) break;
                return Integer.parseInt(string[1]);
            }
            case "HolyWorld": {
                for (ScoreboardEntry scoreboardEntry : scoreboard.getScoreboardEntries(objective)) {
                    String string;
                    String text = Team.decorateName((AbstractTeam)scoreboard.getScoreHolderTeam(scoreboardEntry.comp_2127()), (Text)scoreboardEntry.name()).getString();
                    if (text.isEmpty() || (string = StringUtils.substringBetween((String)text, (String)"#", (String)" -◆-")) == null || string.isEmpty()) continue;
                    return Integer.parseInt(string);
                }
                break;
            }
        }
        return -1;
    }

    public boolean isPvp() {
        return this.pvpWatch.getElapsedTime() < 250L;
    }

    private void trySendEventDelayCommand() {
        if (!this.sendEventDelayOnJoin) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now < this.sendEventDelayAt) {
            return;
        }
        if (ServerHandler.mc.player == null || mc.getNetworkHandler() == null) {
            return;
        }
        if (!this.isCopyTime()) {
            this.sendEventDelayOnJoin = false;
            return;
        }
        if (this.anarchy <= 0) {
            if (now - this.sendEventDelayAt >= 5000L) {
                this.sendEventDelayOnJoin = false;
            }
            return;
        }
        mc.getNetworkHandler().sendChatCommand("event delay");
        this.sendEventDelayOnJoin = false;
    }

    private void tryCloseLingeringJoinScreen() {
        if (this.joinScreenGuardUntil <= 0L) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now > this.joinScreenGuardUntil) {
            this.clearJoinScreenWatchdog();
            return;
        }
        if (now < this.joinScreenCloseAt) {
            return;
        }
        if (ServerHandler.mc.player == null || ServerHandler.mc.world == null || mc.getNetworkHandler() == null) {
            return;
        }
        Screen screen = ServerHandler.mc.currentScreen;
        if (!this.isLikelyJoinLoadingScreen(screen)) {
            if (screen == null) {
                this.clearJoinScreenWatchdog();
            }
            return;
        }
        mc.setScreen(null);
        this.clearJoinScreenWatchdog();
    }

    private void clearJoinScreenWatchdog() {
        this.joinScreenCloseAt = 0L;
        this.joinScreenGuardUntil = 0L;
    }

    private boolean isLikelyJoinLoadingScreen(Screen screen) {
        if (screen == null) {
            return false;
        }
        if (screen instanceof ChatScreen || screen instanceof HandledScreen || screen instanceof GameMenuScreen || screen instanceof MenuScreen) {
            return false;
        }
        if (screen.shouldPause()) {
            return false;
        }
        String className = screen.getClass().getSimpleName().toLowerCase();
        String title = screen.getTitle() == null ? "" : screen.getTitle().getString().toLowerCase();
        return className.contains("terrain") || className.contains("level") || title.contains("loading") || title.contains("joining") || title.contains("terrain") || title.contains("террит") || title.contains("загруз");
    }

    private boolean inPvp() {
        return ServerHandler.mc.inGameHud.getBossBarHud().bossBars.values().stream().map(c -> c.getName().getString().toLowerCase()).anyMatch(s -> s.contains("pvp") || s.contains("пвп"));
    }

    private boolean inPvpEnd() {
        return ServerHandler.mc.inGameHud.getBossBarHud().bossBars.values().stream().map(c -> c.getName().getString().toLowerCase()).anyMatch(s -> !(!s.contains("pvp") && !s.contains("пвп") || !s.contains("0") && !s.contains("1")));
    }

    public String getWorldType() {
        return ServerHandler.mc.world.getRegistryKey().getValue().getPath();
    }

    public boolean isCopyTime() {
        return this.server.equals("CopyTime") || this.server.equals("SpookyTime") || this.server.equals("FunTime");
    }

    public boolean isFunTime() {
        return this.server.equals("FunTime");
    }

    public boolean isReallyWorld() {
        return this.server.equals("ReallyWorld");
    }

    public boolean isHolyWorld() {
        return this.server.equals("HolyWorld");
    }

    public boolean isVanilla() {
        return this.server.equals("Vanilla");
    }

    @Generated
    public StopWatch getPvpWatch() {
        return this.pvpWatch;
    }

    @Generated
    public String getServer() {
        return this.server;
    }

    @Generated
    public float getTPS() {
        return this.TPS;
    }

    @Generated
    public long getTimestamp() {
        return this.timestamp;
    }

    @Generated
    public boolean isServerSprint() {
        return this.serverSprint;
    }

    @Generated
    public int getAnarchy() {
        return this.anarchy;
    }

    @Generated
    public boolean isSendEventDelayOnJoin() {
        return this.sendEventDelayOnJoin;
    }

    @Generated
    public long getSendEventDelayAt() {
        return this.sendEventDelayAt;
    }

    @Generated
    public long getJoinScreenCloseAt() {
        return this.joinScreenCloseAt;
    }

    @Generated
    public long getJoinScreenGuardUntil() {
        return this.joinScreenGuardUntil;
    }

    @Generated
    public boolean isPvpEnd() {
        return this.pvpEnd;
    }
}

