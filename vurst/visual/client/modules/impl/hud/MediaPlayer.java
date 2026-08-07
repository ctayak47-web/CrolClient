
package vurst.visual.client.modules.impl.hud;

import com.darkmagician6.eventapi.EventTarget;
import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaInfo;
import dev.redstones.mediaplayerinfo.MediaPlayerInfo;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.NativeImage;
import net.minecraft.NativeImageBackedTexture;
import net.minecraft.AbstractTexture;
import net.minecraft.Identifier;
import net.minecraft.ChatScreen;
import org.lwjgl.BufferUtils;
import vurst.visual.VurstVisual;
import vurst.visual.base.events.impl.input.EventMouse;
import vurst.visual.base.events.impl.player.EventUpdate;
import vurst.visual.base.events.impl.render.EventHudRender;
import vurst.visual.base.font.Font;
import vurst.visual.base.font.Fonts;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.impl.hud.HudModule;
import vurst.visual.client.screens.menu.MenuScreen;
import vurst.visual.utility.math.StopWatch;
import vurst.visual.utility.render.display.base.BorderRadius;
import vurst.visual.utility.render.display.base.CustomDrawContext;
import vurst.visual.utility.render.display.base.color.ColorRGBA;
import vurst.visual.utility.render.display.shader.DrawUtil;

@ModuleAnnotation(name="MediaPlayer", category=Category.HUD, description="Показывает текущий трек и позволяет управлять воспроизведением.")
public final class MediaPlayer
extends HudModule {
    public static final MediaPlayer INSTANCE = new MediaPlayer();
    private static final Identifier ARTWORK_TEXTURE = VurstVisual.id("hud/media_player_artwork");
    private static final long FETCH_INTERVAL_MS = 350L;
    private static final long KEEP_VISIBLE_MS = 5000L;
    private static final float CARD_WIDTH = 148.0f;
    private static final float CARD_HEIGHT = 47.0f;
    private static final float CARD_RADIUS = 6.5f;
    private static final float PADDING = 4.0f;
    private static final float ARTWORK_SIZE = 39.0f;
    private static final float CONTENT_GAP = 5.0f;
    private static final float PROGRESS_HEIGHT = 2.5f;
    private static final float BUTTON_WIDTH = 14.0f;
    private static final float BUTTON_HEIGHT = 10.0f;
    private static final float BUTTON_GAP = 3.5f;
    private static final float TITLE_Y = 6.0f;
    private static final float ARTIST_Y = 14.6f;
    private static final float PROGRESS_Y = 24.0f;
    private static final float BUTTONS_Y = 29.6f;
    private static final MediaInfo PLACEHOLDER_INFO = new MediaInfo("MediaPlayer", "Нет активного трека", new byte[0], 0L, 0L, false);
    private final StopWatch fetchTimer = new StopWatch();
    private final ExecutorService mediaExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread2 = new Thread(runnable, "VurstVisual-MediaPlayer");
        thread2.setDaemon(true);
        return thread2;
    });
    private final AtomicBoolean fetchInProgress = new AtomicBoolean(false);
    private final ControlButton previousButton = new ControlButton();
    private final ControlButton playPauseButton = new ControlButton();
    private final ControlButton nextButton = new ControlButton();
    private volatile MediaInfo mediaInfo = PLACEHOLDER_INFO;
    private volatile IMediaSession session;
    private volatile long lastMediaAt;
    private volatile boolean artworkRegistered;
    private volatile int artworkHash;
    private float displayedProgress;

    private MediaPlayer() {
        super(10.0f, 112.0f, 148.0f, 47.0f, true);
    }

    @Override
    public String[] getLegacyNames() {
        return new String[]{"Media Player", "Music Info"};
    }

    @Override
    public void onEnable() {
        this.fetchTimer.reset();
        this.displayedProgress = 0.0f;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.displayedProgress = 0.0f;
        super.onDisable();
    }

    @Override
    protected boolean isElementVisible() {
        return this.isHudEditContext() || this.hasRecentMedia();
    }

    @Override
    protected void onTick() {
        float targetProgress;
        if (this.fetchTimer.every(350L)) {
            this.requestMediaUpdate();
        }
        this.displayedProgress = Math.abs(this.displayedProgress - (targetProgress = this.getTargetProgress())) > 0.35f ? targetProgress : (this.displayedProgress += (targetProgress - this.displayedProgress) * 0.2f);
        this.displayedProgress = this.clamp01(this.displayedProgress);
    }

    @Override
    protected void draw(CustomDrawContext ctx) {
        float x = this.getX();
        float y = this.getY();
        float contentX = x + 4.0f + 39.0f + 5.0f;
        float contentWidth = 148.0f - (contentX - x) - 4.0f;
        Font titleFont = Fonts.ROUND_BOLD.getFont(6.7f);
        Font artistFont = Fonts.REGULAR.getFont(5.9f);
        Font metaFont = Fonts.REGULAR.getFont(5.2f);
        Font buttonFont = Fonts.ROUND_BOLD.getFont(5.1f);
        Font placeholderFont = Fonts.ROUND_BOLD.getFont(8.1f);
        MediaInfo info = this.getDisplayInfo();
        String title = this.trimToWidth(titleFont, this.sanitizeText(info.getTitle(), "Без названия"), contentWidth);
        String artist = this.trimToWidth(artistFont, this.sanitizeText(info.getArtist(), "Неизвестный исполнитель"), contentWidth);
        String playLabel = info.getPlaying() ? "||" : ">";
        Timeline timeline = this.resolveTimeline(info);
        String timeText = this.formatTime(timeline.positionSeconds()) + " / " + this.formatTime(timeline.durationSeconds());
        this.drawMediaCard(ctx, x, y, 148.0f, 47.0f, 6.5f, 1.0f);
        ctx.drawRoundedBorder(x, y, 148.0f, 47.0f, 1.0f, BorderRadius.all(6.5f), this.getBorderColor().mulAlpha(0.85f));
        float artworkX = x + 4.0f;
        float artworkY = y + 4.0f;
        BorderRadius artworkRadius = BorderRadius.all(5.5f);
        ctx.drawRoundedRect(artworkX, artworkY, 39.0f, 39.0f, artworkRadius, new ColorRGBA(255, 255, 255, 18));
        if (this.artworkRegistered) {
            DrawUtil.drawRoundedTexture(ctx.getMatrices(), ARTWORK_TEXTURE, artworkX, artworkY, 39.0f, 39.0f, artworkRadius, ColorRGBA.WHITE);
        } else {
            ColorRGBA accent = this.getAccentColor();
            ctx.drawRoundedBorder(artworkX, artworkY, 39.0f, 39.0f, 1.0f, artworkRadius, accent.mulAlpha(0.45f));
            String placeholder = "MP";
            ctx.drawText(placeholderFont, placeholder, artworkX + (39.0f - placeholderFont.width(placeholder)) / 2.0f, artworkY + (39.0f - placeholderFont.height()) / 2.0f + 0.1f, accent);
        }
        ctx.drawText(titleFont, title, contentX, y + 6.0f, this.getTextColor());
        ctx.drawText(artistFont, artist, contentX, y + 14.6f, this.getTextColor());
        float progressX = contentX;
        float progressY = y + 24.0f;
        float progressWidth = contentWidth;
        ctx.drawRoundedRect(progressX, progressY, progressWidth, 2.5f, BorderRadius.all(2.5f), new ColorRGBA(255, 255, 255, 32));
        float fillWidth = progressWidth * this.displayedProgress;
        if (fillWidth > 0.5f) {
            ctx.drawRoundedRect(progressX, progressY, fillWidth, 2.5f, BorderRadius.all(2.5f), this.getAccentColor());
        }
        float buttonsX = contentX;
        this.previousButton.set(buttonsX, y + 29.6f, 14.0f, 10.0f);
        this.playPauseButton.set(buttonsX + 14.0f + 3.5f, y + 29.6f, 14.0f, 10.0f);
        this.nextButton.set(buttonsX + 35.0f, y + 29.6f, 14.0f, 10.0f);
        boolean controlsActive = this.session != null;
        this.drawButton(ctx, buttonFont, this.previousButton, "<<", controlsActive, false);
        this.drawButton(ctx, buttonFont, this.playPauseButton, playLabel, controlsActive, true);
        this.drawButton(ctx, buttonFont, this.nextButton, ">>", controlsActive, false);
        float timeX = x + 148.0f - 4.0f - metaFont.width(timeText);
        float timeY = y + 29.6f + (10.0f - metaFont.height()) / 2.0f + 0.15f;
        ctx.drawText(metaFont, timeText, timeX, timeY, this.getTextColor().mulAlpha(this.hasRecentMedia() ? 0.9f : 0.7f));
        this.setBounds(148.0f, 47.0f);
    }

    private void drawButton(CustomDrawContext ctx, Font font, ControlButton button, String text, boolean active, boolean accent) {
        ColorRGBA fill;
        ColorRGBA colorRGBA = accent ? this.getAccentColor().mulAlpha(active ? 0.22f : 0.12f) : (fill = new ColorRGBA(255, 255, 255, active ? 18 : 10));
        ColorRGBA border = accent ? this.getAccentColor().mulAlpha(active ? 0.7f : 0.32f) : this.getBorderColor().mulAlpha(active ? 0.78f : 0.45f);
        ColorRGBA textColor = active ? this.getTextColor() : this.resolveTextColor(this.getHeaderColor().mulAlpha(0.65f));
        ctx.drawRoundedRect(button.x, button.y, button.width, button.height, BorderRadius.all(3.8f), fill);
        ctx.drawRoundedBorder(button.x, button.y, button.width, button.height, 0.9f, BorderRadius.all(3.8f), border);
        ctx.drawText(font, text, button.x + (button.width - font.width(text)) / 2.0f, button.y + (button.height - font.height()) / 2.0f + 0.15f, textColor);
    }

    private void requestMediaUpdate() {
        if (this.fetchInProgress.getAndSet(true)) {
            return;
        }
        this.mediaExecutor.execute(() -> {
            try {
                List<IMediaSession> sessions = MediaPlayerInfo.Instance.getMediaSessions();
                IMediaSession bestSession = this.selectBestSession(sessions);
                if (bestSession == null) {
                    this.session = null;
                    return;
                }
                MediaInfo info = bestSession.getMedia();
                if (!this.isUseful(info)) {
                    this.session = null;
                    return;
                }
                this.updateArtwork(info.getArtworkPng());
                this.mediaInfo = info;
                this.session = bestSession;
                this.lastMediaAt = System.currentTimeMillis();
            }
            catch (Throwable throwable) {
            }
            finally {
                this.fetchInProgress.set(false);
            }
        });
    }

    private IMediaSession selectBestSession(Collection<IMediaSession> sessions) {
        if (sessions == null || sessions.isEmpty()) {
            return null;
        }
        return sessions.stream().filter(Objects::nonNull).filter(session -> {
            try {
                return session.getMedia() != null;
            }
            catch (Throwable ignored) {
                return false;
            }
        }).max(Comparator.comparingInt(this::getSessionScore)).filter(session -> this.getSessionScore((IMediaSession)session) > 0).orElse(null);
    }

    private int getSessionScore(IMediaSession mediaSession) {
        try {
            MediaInfo info = mediaSession.getMedia();
            if (info == null) {
                return 0;
            }
            int score = 0;
            if (info.getPlaying()) {
                score += 100;
            }
            if (!this.sanitizeText(info.getTitle(), "").isEmpty()) {
                score += 40;
            }
            if (!this.sanitizeText(info.getArtist(), "").isEmpty()) {
                score += 25;
            }
            if (info.getDuration() > 0L) {
                score += 15;
            }
            if (info.getPosition() > 0L) {
                score += 8;
            }
            return score;
        }
        catch (Throwable ignored) {
            return 0;
        }
    }

    private boolean isUseful(MediaInfo info) {
        if (info == null) {
            return false;
        }
        return !this.sanitizeText(info.getTitle(), "").isEmpty() || !this.sanitizeText(info.getArtist(), "").isEmpty() || info.getDuration() > 0L || info.getPosition() > 0L || info.getPlaying();
    }

    private void updateArtwork(byte[] bytes) {
        int newHash;
        byte[] artworkBytes = bytes == null ? new byte[]{} : bytes;
        int n = newHash = artworkBytes.length == 0 ? 0 : Arrays.hashCode(artworkBytes);
        if (newHash == this.artworkHash) {
            return;
        }
        if (newHash == 0) {
            this.artworkHash = 0;
            this.artworkRegistered = false;
            mc.execute(() -> mc.getTextureManager().destroyTexture(ARTWORK_TEXTURE));
            return;
        }
        this.artworkHash = newHash;
        byte[] copy = (byte[])artworkBytes.clone();
        mc.execute(() -> {
            try {
                ByteBuffer buffer = BufferUtils.createByteBuffer((int)copy.length).put(copy);
                buffer.flip();
                NativeImageBackedTexture texture = new NativeImageBackedTexture(NativeImage.read((ByteBuffer)buffer));
                mc.getTextureManager().destroyTexture(ARTWORK_TEXTURE);
                mc.getTextureManager().registerTexture(ARTWORK_TEXTURE, (AbstractTexture)texture);
                this.artworkRegistered = true;
            }
            catch (Throwable ignored) {
                this.artworkRegistered = false;
                this.artworkHash = 0;
            }
        });
    }

    private MediaInfo getDisplayInfo() {
        MediaInfo current = this.mediaInfo;
        if (current == null || !this.hasRecentMedia() && !this.isHudEditContext()) {
            return PLACEHOLDER_INFO;
        }
        return current;
    }

    private float getTargetProgress() {
        MediaInfo info = this.mediaInfo;
        if (info == null || info.getDuration() <= 0L) {
            return 0.0f;
        }
        return this.clamp01((float)info.getPosition() / (float)info.getDuration());
    }

    private boolean hasRecentMedia() {
        return System.currentTimeMillis() - this.lastMediaAt <= 5000L;
    }

    private boolean isHudEditContext() {
        return MediaPlayer.mc.currentScreen instanceof ChatScreen || MediaPlayer.mc.currentScreen instanceof MenuScreen;
    }

    private String sanitizeText(String text, String fallback) {
        return text == null || text.isBlank() ? fallback : text.trim();
    }

    private String trimToWidth(Font font, String text, float maxWidth) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String trimmed = text;
        while (trimmed.length() > 3 && font.width(trimmed + "...") > maxWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed.length() > 3 ? trimmed + "..." : trimmed;
    }

    private Timeline resolveTimeline(MediaInfo info) {
        if (info == null) {
            return new Timeline(0L, 0L);
        }
        long position = this.normalizeTimelineValue(info.getPosition());
        long duration = this.normalizeTimelineValue(info.getDuration());
        if (duration > 0L && position > duration) {
            position = duration;
        }
        return new Timeline(position, duration);
    }

    private long normalizeTimelineValue(long rawValue) {
        if (rawValue <= 0L) {
            return 0L;
        }
        return rawValue > 100000L ? rawValue / 1000L : rawValue;
    }

    private String formatTime(long secondsValue) {
        if (secondsValue <= 0L) {
            return "0:00";
        }
        long hours = secondsValue / 3600L;
        long minutes = secondsValue % 3600L / 60L;
        long seconds = secondsValue % 60L;
        if (hours > 0L) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%d:%02d", minutes, seconds);
    }

    private float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        this.updateHud();
    }

    @EventTarget
    public void onRender(EventHudRender event) {
        this.renderHud(event.getContext());
    }

    @EventTarget
    public void onMouse(EventMouse event) {
        if (event.getButton() == 0 && event.getAction() == 1 && this.handleControlClick(this.getMouseX(), this.getMouseY())) {
            return;
        }
        this.handleMouse(event);
    }

    private boolean handleControlClick(float mouseX, float mouseY) {
        IMediaSession currentSession = this.session;
        if (currentSession == null || !this.isElementVisible()) {
            return false;
        }
        try {
            if (this.isInsideButton(this.previousButton, mouseX, mouseY)) {
                currentSession.previous();
                return true;
            }
            if (this.isInsideButton(this.playPauseButton, mouseX, mouseY)) {
                currentSession.playPause();
                return true;
            }
            if (this.isInsideButton(this.nextButton, mouseX, mouseY)) {
                currentSession.next();
                return true;
            }
        }
        catch (Throwable throwable) {
            
        }
        return false;
    }

    private boolean isInsideButton(ControlButton button, float mouseX, float mouseY) {
        float baseX = this.getX();
        float baseY = this.getY();
        float scaledX = baseX + (button.x - baseX) * this.getScaleX();
        float scaledY = baseY + (button.y - baseY) * this.getScaleY();
        float scaledWidth = button.width * this.getScaleX();
        float scaledHeight = button.height * this.getScaleY();
        return mouseX >= scaledX && mouseX <= scaledX + scaledWidth && mouseY >= scaledY && mouseY <= scaledY + scaledHeight;
    }

    private float getMouseX() {
        if (mc.getWindow() == null) {
            return (float)MediaPlayer.mc.mouse.getX();
        }
        return (float)(MediaPlayer.mc.mouse.getX() / mc.getWindow().getScaleFactor());
    }

    private float getMouseY() {
        if (mc.getWindow() == null) {
            return (float)MediaPlayer.mc.mouse.getY();
        }
        return (float)(MediaPlayer.mc.mouse.getY() / mc.getWindow().getScaleFactor());
    }

    private static final class ControlButton {
        private float x;
        private float y;
        private float width;
        private float height;

        private ControlButton() {
        }

        private void set(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private record Timeline(long positionSeconds, long durationSeconds) {
    }
}

