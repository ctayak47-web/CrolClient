
package vurst.visual.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.PlayerEntity;
import net.minecraft.ScreenHandler;
import net.minecraft.SlotActionType;
import net.minecraft.Slot;
import net.minecraft.Items;
import net.minecraft.Packet;
import net.minecraft.ClientBossBar;
import net.minecraft.GameMessageS2CPacket;
import vurst.visual.VurstVisual;
import vurst.visual.base.events.impl.player.EventUpdate;
import vurst.visual.base.events.impl.server.EventPacket;
import vurst.visual.base.filemanager.impl.BlacklistManager;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.utility.game.other.MessageUtil;
import vurst.visual.utility.math.StopWatch;

@ModuleAnnotation(name="AutoDuel", category=Category.MOVEMENT, description="Автоматически ищет дуэли на Funtime.")
public final class AutoDuel
extends Module {
    public static final AutoDuel INSTANCE = new AutoDuel();
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("\\b[A-Za-z0-9_]{3,16}\\b");
    private static final long INITIAL_SEARCH_DELAY_MS = 250L;
    private static final long SEARCH_RETRY_DELAY_MS = 1200L;
    private static final long SEARCH_TO_GUI_DELAY_MS = 250L;
    private static final long GUI_CLICK_DELAY_MS = 200L;
    private static final long GUI_WAIT_TIMEOUT_MS = 3000L;
    private static final long CLICK_TO_CHAT_DELAY_MS = 150L;
    private static final long CHAT_WAIT_TIMEOUT_MS = 3500L;
    private static final long ACTIVE_SEARCH_GRACE_MS = 6000L;
    private static final long AUTO_LEAVE_COOLDOWN_MS = 1500L;
    private final StopWatch searchTimer = new StopWatch();
    private final StopWatch stateTimer = new StopWatch();
    private final StopWatch guiClickTimer = new StopWatch();
    private State state = State.IDLE;
    private boolean searchQueued;
    private long queuedSearchDelayMs;
    private long lastAutoLeaveAt;
    private long lastSearchSeenAt;
    private String lastAutoLeaveName;
    private String acceptedTargetName;

    private AutoDuel() {
    }

    @Override
    public void onEnable() {
        this.resetState();
        this.queueSearch(250L);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.resetState();
        super.onDisable();
    }

    @Override
    public String[] getLegacyNames() {
        return new String[]{"Duel Blacklist", "DuelBlacklist"};
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (AutoDuel.mc.player == null || AutoDuel.mc.world == null || mc.getNetworkHandler() == null) {
            return;
        }
        this.refreshSearchActivity();
        if ((this.state == State.MATCHMAKING || this.state == State.TARGET_ACCEPTED) && this.tryLeaveLoadedBlacklistedOpponent()) {
            return;
        }
        if (this.state == State.TARGET_ACCEPTED) {
            return;
        }
        if (this.searchQueued && this.state == State.IDLE && this.searchTimer.getElapsedTime() >= this.queuedSearchDelayMs) {
            this.sendSearchCommand();
            return;
        }
        if (this.state == State.SEARCH_SENT) {
            if (this.isSearchRecentlyActive()) {
                this.enterMatchmakingState();
                return;
            }
            if (this.stateTimer.getElapsedTime() >= 250L) {
                this.state = State.WAITING_GUI;
                this.stateTimer.reset();
                this.guiClickTimer.reset();
            }
            return;
        }
        if (this.state == State.WAITING_GUI) {
            if (this.isSearchRecentlyActive()) {
                this.enterMatchmakingState();
                return;
            }
            if (this.tryClickCompassInDuelGui()) {
                return;
            }
            if (this.stateTimer.getElapsedTime() >= 3000L) {
                this.restartSearch(1200L);
            }
            return;
        }
        if (this.state == State.COMPASS_CLICKED) {
            if (this.isSearchRecentlyActive()) {
                this.enterMatchmakingState();
                return;
            }
            if (this.stateTimer.getElapsedTime() >= 150L) {
                this.state = State.WAITING_TARGET_MESSAGE;
                this.stateTimer.reset();
            }
            return;
        }
        if (this.state == State.WAITING_TARGET_MESSAGE) {
            if (this.isSearchRecentlyActive()) {
                this.enterMatchmakingState();
                return;
            }
            if (this.stateTimer.getElapsedTime() >= 3500L) {
                this.restartSearch(1200L);
            }
            return;
        }
        if (this.state == State.MATCHMAKING && !this.isSearchRecentlyActive()) {
            this.restartSearch(1200L);
        }
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        boolean shouldParseNickname;
        Packet<?> packet;
        if (!event.isReceive() || !((packet = event.getPacket()) instanceof GameMessageS2CPacket)) {
            return;
        }
        GameMessageS2CPacket packet = (GameMessageS2CPacket)packet;
        if (AutoDuel.mc.player == null || mc.getNetworkHandler() == null) {
            return;
        }
        String message = this.cleanMessage(packet.comp_763().getString());
        if (message.isEmpty()) {
            return;
        }
        if (this.isActiveSearchMessage(message)) {
            this.markSearchActive();
        }
        if (this.isDuelStartingMessage(message)) {
            this.state = State.TARGET_ACCEPTED;
            this.searchQueued = false;
            this.queuedSearchDelayMs = 0L;
            this.stateTimer.reset();
        }
        boolean bl = shouldParseNickname = this.isDuelMessage(message) || this.isActiveSearchMessage(message) || this.state == State.COMPASS_CLICKED || this.state == State.WAITING_TARGET_MESSAGE || this.state == State.MATCHMAKING || this.state == State.TARGET_ACCEPTED;
        if (!shouldParseNickname) {
            return;
        }
        String nickname = this.extractNicknameFromSearchMessage(message);
        if (nickname == null) {
            return;
        }
        if (this.isBlacklisted(nickname)) {
            this.leaveDuelAndRestart(nickname);
            return;
        }
        if (this.state == State.MATCHMAKING || this.state == State.TARGET_ACCEPTED || this.isDuelStartingMessage(message)) {
            this.acceptTarget(nickname);
            return;
        }
        if (this.state != State.COMPASS_CLICKED && this.state != State.WAITING_TARGET_MESSAGE) {
            return;
        }
        PlayerEntity target = this.findPlayerByName(nickname);
        if (target == null) {
            if (this.isSearchRecentlyActive()) {
                this.acceptTarget(nickname);
                return;
            }
            this.restartSearch(1200L);
            return;
        }
        this.acceptTarget(nickname);
    }

    private void sendSearchCommand() {
        if (mc.getNetworkHandler() == null) {
            return;
        }
        if (this.hasActiveSearchBossBar()) {
            this.markSearchActive();
            return;
        }
        mc.getNetworkHandler().sendChatCommand("duel search");
        this.searchQueued = false;
        this.queuedSearchDelayMs = 0L;
        this.acceptedTargetName = null;
        this.state = State.SEARCH_SENT;
        this.stateTimer.reset();
        this.guiClickTimer.reset();
    }

    private boolean tryClickCompassInDuelGui() {
        if (AutoDuel.mc.currentScreen == null || AutoDuel.mc.interactionManager == null || AutoDuel.mc.player == null) {
            return false;
        }
        ScreenHandler handler = AutoDuel.mc.player.currentScreenHandler;
        if (handler == null || handler == AutoDuel.mc.player.playerScreenHandler) {
            return false;
        }
        String title = this.cleanMessage(AutoDuel.mc.currentScreen.getTitle().getString());
        if (!this.isDuelGuiTitle(title)) {
            return false;
        }
        if (this.guiClickTimer.getElapsedTime() < 200L) {
            return false;
        }
        for (Slot slot : handler.slots) {
            if (slot == null || !slot.hasStack() || slot.getStack().getItem() != Items.COMPASS) continue;
            AutoDuel.mc.interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.PICKUP, (PlayerEntity)AutoDuel.mc.player);
            this.state = State.COMPASS_CLICKED;
            this.stateTimer.reset();
            this.guiClickTimer.reset();
            return true;
        }
        return false;
    }

    private void queueSearch(long delayMs) {
        this.searchQueued = true;
        this.queuedSearchDelayMs = Math.max(0L, delayMs);
        this.searchTimer.reset();
    }

    private void restartSearch(long delayMs) {
        if (AutoDuel.mc.player != null && AutoDuel.mc.player.currentScreenHandler != null && AutoDuel.mc.player.currentScreenHandler != AutoDuel.mc.player.playerScreenHandler) {
            AutoDuel.mc.player.closeHandledScreen();
        }
        if (this.hasActiveSearchBossBar()) {
            this.markSearchActive();
            return;
        }
        this.acceptedTargetName = null;
        this.state = State.IDLE;
        this.queueSearch(delayMs);
        this.stateTimer.reset();
        this.guiClickTimer.reset();
    }

    private void leaveDuelAndRestart(String opponentName) {
        long now = System.currentTimeMillis();
        if (now - this.lastAutoLeaveAt < 1500L && opponentName.equalsIgnoreCase(this.lastAutoLeaveName)) {
            return;
        }
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendChatCommand("duel early");
        }
        this.lastAutoLeaveAt = now;
        this.lastAutoLeaveName = opponentName;
        this.lastSearchSeenAt = 0L;
        this.acceptedTargetName = null;
        MessageUtil.displayInfo("Автовыход из дуэли: " + opponentName + " находится в blacklist");
        this.restartSearch(1200L);
    }

    private void refreshSearchActivity() {
        if (this.hasActiveSearchBossBar()) {
            this.markSearchActive();
        }
    }

    private void markSearchActive() {
        this.lastSearchSeenAt = System.currentTimeMillis();
        this.searchQueued = false;
        this.queuedSearchDelayMs = 0L;
        if (this.state != State.TARGET_ACCEPTED) {
            this.state = State.MATCHMAKING;
        }
    }

    private void enterMatchmakingState() {
        this.state = State.MATCHMAKING;
        this.searchQueued = false;
        this.queuedSearchDelayMs = 0L;
        this.stateTimer.reset();
    }

    private void acceptTarget(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return;
        }
        this.acceptedTargetName = nickname;
        this.searchQueued = false;
        this.queuedSearchDelayMs = 0L;
        this.state = State.TARGET_ACCEPTED;
        this.stateTimer.reset();
    }

    private boolean isSearchRecentlyActive() {
        return this.lastSearchSeenAt > 0L && System.currentTimeMillis() - this.lastSearchSeenAt < 6000L;
    }

    private boolean hasActiveSearchBossBar() {
        if (AutoDuel.mc.inGameHud == null) {
            return false;
        }
        for (ClientBossBar bossBar : AutoDuel.mc.inGameHud.getBossBarHud().bossBars.values()) {
            if (bossBar == null || bossBar.getName() == null || !this.isActiveSearchMessage(this.cleanMessage(bossBar.getName().getString()))) continue;
            return true;
        }
        return false;
    }

    private boolean hasActiveDuelBossBar() {
        if (AutoDuel.mc.inGameHud == null) {
            return false;
        }
        for (ClientBossBar bossBar : AutoDuel.mc.inGameHud.getBossBarHud().bossBars.values()) {
            String lower;
            if (bossBar == null || bossBar.getName() == null || (lower = this.cleanMessage(bossBar.getName().getString()).toLowerCase(Locale.ROOT)).isEmpty() || this.containsSearchWord(lower) || !this.containsDuelWord(lower) && !this.containsPvpWord(lower)) continue;
            return true;
        }
        return false;
    }

    private boolean tryLeaveLoadedBlacklistedOpponent() {
        if (!this.hasActiveDuelBossBar() || AutoDuel.mc.world == null) {
            return false;
        }
        String selfName = this.getSelfName();
        for (PlayerEntity player : AutoDuel.mc.world.getPlayers()) {
            String name;
            if (player == null || player.getGameProfile() == null || (name = player.getGameProfile().getName()) == null || name.isBlank() || selfName != null && name.equalsIgnoreCase(selfName) || !this.isBlacklisted(name)) continue;
            this.leaveDuelAndRestart(name);
            return true;
        }
        return false;
    }

    private String extractNicknameFromSearchMessage(String message) {
        if (message == null || message.isEmpty()) {
            return null;
        }
        String selfName = this.getSelfName();
        Matcher matcher = NICKNAME_PATTERN.matcher(message);
        String fallback = null;
        while (matcher.find()) {
            String candidate = matcher.group();
            if (selfName != null && candidate.equalsIgnoreCase(selfName)) continue;
            if (this.findPlayerByName(candidate) != null) {
                return candidate;
            }
            if (fallback != null) continue;
            fallback = candidate;
        }
        return fallback;
    }

    private PlayerEntity findPlayerByName(String nickname) {
        if (nickname == null || AutoDuel.mc.world == null) {
            return null;
        }
        for (PlayerEntity player : AutoDuel.mc.world.getPlayers()) {
            String name;
            if (player == null || player.getGameProfile() == null || (name = player.getGameProfile().getName()) == null || !name.equalsIgnoreCase(nickname)) continue;
            return player;
        }
        return null;
    }

    private boolean isBlacklisted(String nickname) {
        BlacklistManager blacklistManager = VurstVisual.getInstance().getBlacklistManager();
        return blacklistManager != null && blacklistManager.isBlacklisted(nickname);
    }

    private String getSelfName() {
        if (AutoDuel.mc.player == null || AutoDuel.mc.player.getGameProfile() == null) {
            return null;
        }
        return AutoDuel.mc.player.getGameProfile().getName();
    }

    private String cleanMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        return message.replaceAll("§[0-9A-FK-ORa-fk-or]", "").replaceAll("&[0-9A-FK-ORa-fk-or]", "").replaceAll("[\\x00-\\x1F\\x7F]", "").replaceAll("\\p{C}", "").trim();
    }

    private boolean isDuelMessage(String message) {
        String lower = message.toLowerCase(Locale.ROOT);
        return this.containsDuelWord(lower) || lower.contains("поедин");
    }

    private boolean isDuelGuiTitle(String title) {
        return this.containsDuelWord(title.toLowerCase(Locale.ROOT));
    }

    private boolean isDuelStartingMessage(String message) {
        String lower = message.toLowerCase(Locale.ROOT);
        return lower.contains("начало") && lower.contains("через") && lower.contains("сек") || lower.contains("дуэль начинается") || lower.contains("поединок начинается") || lower.contains("телепортация на арену");
    }

    private boolean isActiveSearchMessage(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }
        String lower = message.toLowerCase(Locale.ROOT);
        return this.containsSearchWord(lower) && this.containsDuelWord(lower);
    }

    private boolean containsDuelWord(String lower) {
        return lower.contains("duel") || lower.contains("дуэл") || lower.contains("дуел");
    }

    private boolean containsSearchWord(String lower) {
        return lower.contains("search") || lower.contains("queue") || lower.contains("поиск") || lower.contains("подбор");
    }

    private boolean containsPvpWord(String lower) {
        return lower.contains("pvp") || lower.contains("пвп");
    }

    private void resetState() {
        this.searchQueued = false;
        this.queuedSearchDelayMs = 0L;
        this.lastAutoLeaveAt = 0L;
        this.lastSearchSeenAt = 0L;
        this.lastAutoLeaveName = null;
        this.acceptedTargetName = null;
        this.state = State.IDLE;
        this.searchTimer.reset();
        this.stateTimer.reset();
        this.guiClickTimer.reset();
    }

    private static enum State {
        IDLE,
        SEARCH_SENT,
        WAITING_GUI,
        COMPASS_CLICKED,
        WAITING_TARGET_MESSAGE,
        MATCHMAKING,
        TARGET_ACCEPTED;

    }
}

