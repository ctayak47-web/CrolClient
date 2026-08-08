
package crol.client.modules.impl.hud;

import com.darkmagician6.eventapi.EventTarget;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ItemStack;
import net.minecraft.Identifier;
import net.minecraft.ChatScreen;
import crol.client.CrolClient;
import crol.client.base.events.impl.input.EventMouse;
import crol.client.base.events.impl.player.EventUpdate;
import crol.client.base.events.impl.render.EventHudRender;
import crol.client.base.font.Font;
import crol.client.base.font.Fonts;
import crol.client.modules.api.Category;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.impl.hud.HudModule;
import crol.client.utility.render.display.base.BorderRadius;
import crol.client.utility.render.display.base.CustomDrawContext;
import crol.client.utility.render.display.base.color.ColorRGBA;
import crol.client.utility.render.display.shader.DrawUtil;

@ModuleAnnotation(name="Notification", category=Category.HUD, description="Показывает уведомления о смене слотов.")
public final class NotificationHud
extends HudModule {
    public static final NotificationHud INSTANCE = new NotificationHud();
    private static final float FONT_SIZE = 7.8f;
    private static final float PADDING = 7.0f;
    private static final float PANEL_HEIGHT = 17.5f;
    private static final float CORNER_RADIUS = 6.6f;
    private static final float ICON_SIZE = 14.0f;
    private static final float TEXT_GAP = 7.0f;
    private static final float MAX_TEXT_WIDTH = 170.0f;
    private static final float GAP = 2.0f;
    private static final long FADE_IN_MS = 200L;
    private static final long FADE_OUT_MS = 300L;
    private final Identifier fallbackIcon = CrolClient.id("icons/avatar.png");
    private final List<NotificationEntry> notifications = new ArrayList<NotificationEntry>();

    private NotificationHud() {
        super(220.0f, 140.0f, 120.0f, 17.5f, true);
    }

    public void pushAutoSwap(String itemName, int contentColor, ItemStack icon) {
        if (!this.isEnabled()) {
            this.setToggled(true);
        }
        this.add("Свап", itemName, 3, contentColor, icon);
    }

    public void add(String title, String content, int seconds) {
        this.add(title, content, seconds, -4934476, ItemStack.EMPTY);
    }

    public void add(String title, String content, int seconds, int contentColor, ItemStack icon) {
        if (!this.isEnabled()) {
            return;
        }
        if (title == null || title.isBlank()) {
            return;
        }
        this.notifications.add(new NotificationEntry(title, content, seconds, contentColor, icon));
    }

    @Override
    protected boolean isElementVisible() {
        return !this.notifications.isEmpty() || NotificationHud.mc.currentScreen instanceof ChatScreen;
    }

    @Override
    protected void draw(CustomDrawContext ctx) {
        if (this.notifications.isEmpty()) {
            this.drawPlaceholder(ctx);
            return;
        }
        Font textFont = Fonts.REGULAR.getFont(7.8f);
        float maxWidth = 0.0f;
        float totalHeight = 0.0f;
        for (NotificationEntry entry : this.notifications) {
            float width = entry.getWidth(textFont);
            maxWidth = Math.max(maxWidth, width);
            totalHeight += 19.5f;
        }
        if (totalHeight > 0.0f) {
            totalHeight -= 2.0f;
        }
        this.setBounds(maxWidth, Math.max(17.5f, totalHeight));
        float baseX = this.getX();
        float baseY = this.getY();
        long now = System.currentTimeMillis();
        float yOffset = 0.0f;
        int i = 0;
        while (i < this.notifications.size()) {
            String delimiter;
            float delimiterWidth;
            float available;
            String content;
            float width;
            NotificationEntry entry = this.notifications.get(i);
            long elapsed = now - entry.startTime;
            if (elapsed >= entry.durationMs) {
                this.notifications.remove(i);
                continue;
            }
            float alpha = entry.getAlpha(elapsed);
            if (alpha <= 0.01f) {
                this.notifications.remove(i);
                continue;
            }
            float panelWidth = width = entry.getWidth(textFont);
            float targetY = baseY + yOffset;
            entry.y = entry.y < -900.0f ? targetY : (entry.y += (targetY - entry.y) * 0.25f);
            float x = baseX + (maxWidth - width) * 0.5f;
            float centerY = entry.y + 8.75f;
            if (!this.transparentBackground.isEnabled()) {
                if (this.getTheme().isBlur()) {
                    DrawUtil.drawBlur(ctx.getMatrices(), x, entry.y, panelWidth, 17.5f, 12.0f, BorderRadius.all(6.6f), new ColorRGBA(255, 255, 255, Math.round(200.0f * alpha)));
                }
                ctx.drawRoundedRect(x, entry.y, panelWidth, 17.5f, BorderRadius.all(6.6f), new ColorRGBA(0, 0, 0, Math.round(120.0f * alpha)));
            }
            float iconX = x + 7.0f;
            float iconY = centerY - 7.0f;
            if (entry.hasIcon()) {
                ctx.drawRoundedRect(iconX, iconY, 14.0f, 14.0f, BorderRadius.all(5.5f), new ColorRGBA(255, 255, 255, Math.round(20.0f * alpha)));
                float itemScale = 0.78f;
                float scaled = 16.0f * itemScale;
                float itemX = iconX + (14.0f - scaled) / 2.0f;
                float itemY = iconY + (14.0f - scaled) / 2.0f;
                this.drawItem(ctx, entry.icon, itemX, itemY, itemScale);
            } else {
                DrawUtil.drawRoundedTexture(ctx.getMatrices(), this.fallbackIcon, iconX, iconY, 14.0f, 14.0f, BorderRadius.all(5.5f), new ColorRGBA(255, 255, 255, Math.round(255.0f * alpha)));
            }
            float textX = iconX + 14.0f + 7.0f;
            float textY = centerY - textFont.height() / 2.0f + 0.2f;
            String title = this.trimToWidth(textFont, entry.title, 170.0f);
            float usedWidth = textFont.width(title);
            ctx.drawText(textFont, title, textX, textY, this.resolveTextColor(ColorRGBA.WHITE.withAlpha(Math.round(230.0f * alpha))));
            if (!entry.content.isEmpty() && !(content = this.trimToWidth(textFont, entry.content, available = Math.max(0.0f, 170.0f - usedWidth - (delimiterWidth = textFont.width(delimiter = " | "))))).isEmpty()) {
                ctx.drawText(textFont, delimiter, textX + usedWidth, textY, this.resolveTextColor(ColorRGBA.WHITE.withAlpha(Math.round(180.0f * alpha))));
                int contentColor = this.applyAlpha(entry.contentColor, Math.round(220.0f * alpha));
                ctx.drawText(textFont, content, textX + usedWidth + delimiterWidth, textY, this.resolveTextColor(new ColorRGBA(contentColor)));
            }
            if (!this.transparentBackground.isEnabled()) {
                float progress = Math.max(0.0f, Math.min(1.0f, 1.0f - (float)elapsed / (float)entry.durationMs));
                float barMaxWidth = Math.max(0.0f, panelWidth - (textX - x) - 7.0f);
                float barWidth = barMaxWidth * progress;
                if (barWidth > 0.5f) {
                    ctx.drawRoundedRect(textX, entry.y + 17.5f - 2.6f, barWidth, 1.2f, BorderRadius.all(1.0f), new ColorRGBA(255, 255, 255, Math.round(120.0f * alpha)));
                }
            }
            yOffset += 19.5f;
            ++i;
        }
    }

    private void drawPlaceholder(CustomDrawContext ctx) {
        float panelWidth;
        if (!(NotificationHud.mc.currentScreen instanceof ChatScreen)) {
            return;
        }
        Font textFont = Fonts.REGULAR.getFont(7.8f);
        String placeholder = "Уведомления";
        float width = panelWidth = Math.max(120.0f, 28.0f + textFont.width(placeholder) + 7.0f);
        this.setBounds(width, 17.5f);
        float x = this.getX();
        float y = this.getY();
        float centerY = y + 8.75f;
        if (!this.transparentBackground.isEnabled()) {
            if (this.getTheme().isBlur()) {
                DrawUtil.drawBlur(ctx.getMatrices(), x, y, panelWidth, 17.5f, 12.0f, BorderRadius.all(6.6f), new ColorRGBA(255, 255, 255, 200));
            }
            ctx.drawRoundedRect(x, y, panelWidth, 17.5f, BorderRadius.all(6.6f), new ColorRGBA(0, 0, 0, 120));
        }
        float iconX = x + 7.0f;
        float iconY = centerY - 7.0f;
        DrawUtil.drawRoundedTexture(ctx.getMatrices(), this.fallbackIcon, iconX, iconY, 14.0f, 14.0f, BorderRadius.all(5.5f), ColorRGBA.WHITE);
        ctx.drawText(textFont, placeholder, iconX + 14.0f + 7.0f, centerY - textFont.height() / 2.0f + 0.2f, this.resolveTextColor(ColorRGBA.WHITE.withAlpha(200)));
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (NotificationHud.mc.player == null || NotificationHud.mc.world == null) {
            return;
        }
        this.updateHud();
    }

    @EventTarget
    public void onRender(EventHudRender event) {
        if (NotificationHud.mc.player == null || NotificationHud.mc.world == null) {
            return;
        }
        this.renderHud(event.getContext());
    }

    @EventTarget
    public void onMouse(EventMouse event) {
        if (NotificationHud.mc.player == null || NotificationHud.mc.world == null) {
            return;
        }
        this.handleMouse(event);
    }

    @Override
    public void onDisable() {
        this.notifications.clear();
        super.onDisable();
    }

    private int applyAlpha(int color, int alpha) {
        int a = Math.max(0, Math.min(255, alpha));
        return a << 24 | color & 0xFFFFFF;
    }

    private String trimToWidth(Font font, String text, float maxWidth) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (maxWidth <= 0.0f) {
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

    private static final class NotificationEntry {
        private final String title;
        private final String content;
        private final long startTime;
        private final long durationMs;
        private final int contentColor;
        private final ItemStack icon;
        private float y = -999.0f;

        private NotificationEntry(String title, String content, int seconds, int contentColor, ItemStack icon) {
            this.title = title;
            this.content = content == null ? "" : content;
            this.startTime = System.currentTimeMillis();
            this.durationMs = Math.max(500L, (long)seconds * 1000L);
            this.contentColor = (contentColor & 0xFF000000) == 0 ? -4934476 : contentColor;
            this.icon = icon == null ? ItemStack.EMPTY : icon.copy();
        }

        private float getWidth(Font textFont) {
            String display = this.content.isEmpty() ? this.title : this.title + " | " + this.content;
            float textWidth = Math.min(textFont.width(display), 170.0f);
            float panelWidth = 28.0f + textWidth + 7.0f;
            panelWidth = Math.max(120.0f, panelWidth);
            return panelWidth;
        }

        private boolean hasIcon() {
            return this.icon != null && !this.icon.isEmpty();
        }

        private float getAlpha(long elapsed) {
            if (elapsed <= 0L) {
                return 0.0f;
            }
            if (elapsed < 200L) {
                return (float)elapsed / 200.0f;
            }
            long remaining = this.durationMs - elapsed;
            if (remaining < 300L) {
                return Math.max(0.0f, (float)remaining / 300.0f);
            }
            return 1.0f;
        }
    }
}

