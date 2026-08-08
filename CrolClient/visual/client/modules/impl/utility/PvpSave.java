
package crol.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import net.minecraft.Formatting;
import net.minecraft.BossBar;
import net.minecraft.Text;
import net.minecraft.Packet;
import net.minecraft.BossBarS2CPacket;
import net.minecraft.ClientBossBar;
import net.minecraft.PlayerListEntry;
import crol.client.CrolClient;
import crol.client.base.events.impl.player.EventUpdate;
import crol.client.base.events.impl.server.EventPacket;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;

@ModuleAnnotation(name="KT Save", category=Category.MOVEMENT, description="Блокирует выход, пока активен PvP.")
public final class PvpSave
extends Module {
    public static final PvpSave INSTANCE = new PvpSave();
    private static final List<String> PVP_WORDS = List.of("режим боя", "пвп", "pvp", "дуэль", "дуел", "duel");
    private static final Pattern END_TIME_PATTERN = Pattern.compile(".*\\b(?:0|1)\\b.*");
    private static final Pattern END_CLOCK_PATTERN = Pattern.compile(".*\\b(?:\\d+:)?0?0:0?[01]\\b.*");
    private static final int MIN_CLEAR_DELAY_MS = 200;
    private static final int MAX_CLEAR_DELAY_MS = 1500;
    private static final long HUB_CONFIRM_WINDOW_MS = 5000L;
    private boolean pvpActive;
    private UUID pvpUuid;
    private long clearAtMillis = -1L;
    private long hubConfirmUntil = -1L;

    private PvpSave() {
    }

    @Override
    public String[] getLegacyNames() {
        return new String[]{"PvpSave", "Pvp Save", "PvP Save", "KT Save", "Kt Save"};
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.resetState();
    }

    public boolean isPvpActive() {
        this.refreshPvpState();
        return this.pvpActive;
    }

    public boolean shouldBlockHubCommand(String command) {
        this.refreshPvpState();
        if (!this.pvpActive || command == null) {
            return false;
        }
        String trimmed = this.normalizeCommand(command);
        if (!this.isHubCommand(trimmed)) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (this.hubConfirmUntil > now) {
            this.hubConfirmUntil = -1L;
            return false;
        }
        this.hubConfirmUntil = now + 5000L;
        if (PvpSave.mc.player != null) {
            PvpSave.mc.player.sendMessage((Text)Text.literal((String)"(Crol Visual) ").formatted(Formatting.BLUE).append((Text)Text.literal((String)"Вы точно хотите выйти с PvP режима? Пропишите еще раз /hub")), false);
        }
        return true;
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (PvpSave.mc.player == null || PvpSave.mc.world == null) {
            this.resetState();
            return;
        }
        this.refreshPvpState();
        if (this.pvpActive && this.clearAtMillis > 0L && System.currentTimeMillis() >= this.clearAtMillis) {
            this.resetState();
        }
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        BossBarS2CPacket packet;
        block3: {
            block2: {
                Packet<?> packet;
                if (!event.isReceive() || !((packet = event.getPacket()) instanceof BossBarS2CPacket)) break block2;
                packet = (BossBarS2CPacket)packet;
                if (PvpSave.mc.player != null) break block3;
            }
            return;
        }
        packet.accept(new BossBarS2CPacket.Consumer(){

            public void add(UUID uuid, Text name, float percent, BossBar.Color color, BossBar.Style style, boolean darkenSky, boolean dragonMusic, boolean thickenFog) {
                PvpSave.this.checkPvpBar(name.getString(), uuid);
            }

            public void remove(UUID uuid) {
                if (uuid != null && uuid.equals(PvpSave.this.pvpUuid)) {
                    PvpSave.this.clearAtMillis = System.currentTimeMillis() + (long)PvpSave.this.getClearDelayMs();
                }
            }

            public void updateProgress(UUID uuid, float percent) {
            }

            public void updateName(UUID uuid, Text name) {
                PvpSave.this.checkPvpBar(name.getString(), uuid);
            }

            public void updateStyle(UUID uuid, BossBar.Color color, BossBar.Style style) {
            }

            public void updateProperties(UUID uuid, boolean darkenSky, boolean dragonMusic, boolean thickenFog) {
            }
        });
    }

    private void checkPvpBar(String bossBarName, UUID bossBarUuid) {
        if (!this.containsPvpWord(bossBarName)) {
            return;
        }
        if (this.isPvpEndState(bossBarName)) {
            this.resetState();
            return;
        }
        this.pvpUuid = bossBarUuid;
        this.pvpActive = true;
        this.clearAtMillis = -1L;
    }

    private void refreshPvpState() {
        if (!this.isEnabled()) {
            this.resetState();
            return;
        }
        if (PvpSave.mc.player == null || PvpSave.mc.world == null || PvpSave.mc.inGameHud == null) {
            this.resetState();
            return;
        }
        boolean found = false;
        UUID foundUuid = null;
        for (Map.Entry entry : PvpSave.mc.inGameHud.getBossBarHud().bossBars.entrySet()) {
            String bossBarName;
            ClientBossBar bossBar = (ClientBossBar)entry.getValue();
            if (bossBar == null || bossBar.getName() == null || !this.containsPvpWord(bossBarName = bossBar.getName().getString()) || this.isPvpEndState(bossBarName)) continue;
            found = true;
            foundUuid = (UUID)entry.getKey();
            break;
        }
        if (found) {
            this.pvpActive = true;
            this.pvpUuid = foundUuid;
            this.clearAtMillis = -1L;
            return;
        }
        if (!this.pvpActive) {
            this.hubConfirmUntil = -1L;
            this.clearAtMillis = -1L;
            return;
        }
        long now = System.currentTimeMillis();
        if (this.clearAtMillis <= 0L) {
            this.clearAtMillis = now + (long)this.getClearDelayMs();
            return;
        }
        if (now >= this.clearAtMillis) {
            this.resetState();
        }
    }

    private boolean containsPvpWord(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        String lower = name.toLowerCase(Locale.ROOT);
        return PVP_WORDS.stream().anyMatch(lower::contains);
    }

    private boolean isPvpEndState(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        String lower = name.toLowerCase(Locale.ROOT);
        if (!this.containsPvpWord(lower)) {
            return false;
        }
        if (CrolClient.getInstance().getServerHandler() != null && CrolClient.getInstance().getServerHandler().isPvpEnd()) {
            return true;
        }
        return lower.contains("0 сек") || lower.contains("1 сек") || lower.contains("0с") || lower.contains("1с") || lower.contains("0 sec") || lower.contains("1 sec") || lower.contains("0s") || lower.contains("1s") || END_CLOCK_PATTERN.matcher(lower).matches() || END_TIME_PATTERN.matcher(lower).matches();
    }

    private int getClearDelayMs() {
        int delay = 200;
        if (mc.getNetworkHandler() != null && PvpSave.mc.player != null) {
            int latency;
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(PvpSave.mc.player.getUuid());
            int n = latency = entry != null ? entry.getLatency() : -1;
            if (latency >= 0) {
                delay = latency * 2 + 150;
            }
        }
        if (delay < 200) {
            delay = 200;
        }
        if (delay > 1500) {
            delay = 1500;
        }
        return delay;
    }

    private void resetState() {
        this.pvpActive = false;
        this.pvpUuid = null;
        this.clearAtMillis = -1L;
        this.hubConfirmUntil = -1L;
    }

    private boolean isHubCommand(String command) {
        String lower = command.toLowerCase(Locale.ROOT);
        return lower.equals("hub") || lower.startsWith("hub ");
    }

    private String normalizeCommand(String command) {
        String trimmed = command.trim();
        if (trimmed.startsWith("/")) {
            return trimmed.substring(1).trim();
        }
        return trimmed;
    }
}

