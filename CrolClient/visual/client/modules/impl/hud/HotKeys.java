
package crol.client.modules.impl.hud;

import com.darkmagician6.eventapi.EventTarget;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.Identifier;
import crol.client.CrolClient;
import crol.client.base.events.impl.input.EventMouse;
import crol.client.base.events.impl.player.EventUpdate;
import crol.client.base.events.impl.render.EventHudRender;
import crol.client.base.font.Font;
import crol.client.base.font.Fonts;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.impl.hud.HudModule;
import crol.client.modules.impl.render.Menu;
import crol.client.utility.game.player.PlayerIntersectionUtil;
import crol.client.utility.render.display.Keyboard;
import crol.client.utility.render.display.base.BorderRadius;
import crol.client.utility.render.display.base.CustomDrawContext;
import crol.client.utility.render.display.base.color.ColorRGBA;

@ModuleAnnotation(name="Hot Keys", category=Category.HUD, description="Показывает активные бинды.")
public final class HotKeys
extends HudModule {
    public static final HotKeys INSTANCE = new HotKeys();
    private static final Identifier HEADER_ICON = CrolClient.id("hud/hud/keybinds.png");
    private static final float HEADER_ICON_SIZE = 8.0f;
    private static final float HEADER_ICON_GAP = 4.0f;
    private static final String TITLE_TEXT = "Keybinds";
    private final Map<Module, Float> animMap = new HashMap<Module, Float>();
    private List<Module> modules = new ArrayList<Module>();

    private HotKeys() {
        super(300.0f, 10.0f, 90.0f, 22.0f, true);
    }

    @Override
    protected boolean isElementVisible() {
        return !this.animMap.isEmpty() || PlayerIntersectionUtil.isChat(HotKeys.mc.currentScreen);
    }

    @Override
    protected void onTick() {
        List<Module> allModules = CrolClient.getInstance().getModuleManager().getModules();
        this.modules = allModules.stream().filter(module -> module.getKeyCode() != -1 && module != Menu.INSTANCE && module.isEnabled()).sorted(Comparator.comparing(Module::getDisplayName)).toList();
        for (Module module2 : this.modules) {
            float current = this.animMap.getOrDefault(module2, Float.valueOf(0.0f)).floatValue();
            this.animMap.put(module2, Float.valueOf(current + (1.0f - current) * 0.15f));
        }
        ArrayList<Module> toRemove = new ArrayList<Module>();
        for (Map.Entry<Module, Float> entry : this.animMap.entrySet()) {
            Module module3 = entry.getKey();
            if (this.modules.contains(module3)) continue;
            toRemove.add(module3);
        }
        for (Module module4 : toRemove) {
            this.animMap.remove(module4);
        }
    }

    @Override
    protected void draw(CustomDrawContext ctx) {
        Font font = Fonts.MEDIUM.getFont(6.0f);
        float x = this.getX();
        float y = this.getY();
        float rowHeight = 11.0f;
        float padding = 3.5f;
        float titleWidth = font.width(TITLE_TEXT);
        float maxWidth = titleWidth + padding * 2.0f + 4.0f + 8.0f;
        ArrayList<Module> sortedVisible = new ArrayList<Module>();
        for (Module module : this.modules) {
            float anim = this.animMap.getOrDefault(module, Float.valueOf(0.0f)).floatValue();
            if (anim <= 0.05f) continue;
            sortedVisible.add(module);
            String keyName = Keyboard.getKeyName(module.getKeyCode());
            float rowWidth = font.width(module.getDisplayName()) + font.width(keyName) + padding * 4.5f;
            maxWidth = Math.max(maxWidth, rowWidth);
        }
        sortedVisible.sort((a, b) -> Float.compare(font.width(b.getDisplayName()) + font.width(Keyboard.getKeyName(b.getKeyCode())), font.width(a.getDisplayName()) + font.width(Keyboard.getKeyName(a.getKeyCode()))));
        this.drawCard(ctx, x, y, maxWidth, rowHeight, 1.0f, 3.0f);
        float headerTextY = y + (rowHeight - font.height()) / 2.0f + 0.35f;
        ctx.drawText(font, TITLE_TEXT, x + padding, headerTextY, this.getTextColor());
        float headerIconX = x + maxWidth - padding - 8.0f;
        float headerIconY = y + rowHeight / 2.0f - 4.0f;
        ctx.drawTexture(HEADER_ICON, headerIconX, headerIconY, 8.0f, 8.0f, ColorRGBA.WHITE);
        float currentY = y + rowHeight + 1.5f;
        for (Module module : sortedVisible) {
            float anim = this.animMap.getOrDefault(module, Float.valueOf(0.0f)).floatValue();
            float currentHeight = rowHeight * anim;
            int alpha = Math.max(0, Math.min(255, (int)(255.0f * anim)));
            this.drawCard(ctx, x, currentY, maxWidth, currentHeight, anim, 3.0f);
            float textY = currentY + Math.max(0.0f, (currentHeight - font.height()) / 2.0f) + 0.35f;
            if (anim > 0.62f) {
                ctx.drawText(font, module.getDisplayName(), x + padding, textY, this.resolveTextColor(ColorRGBA.WHITE.withAlpha(alpha)));
            }
            String keyName = Keyboard.getKeyName(module.getKeyCode());
            float keyWidth = font.width(keyName);
            float keyRectWidth = keyWidth + 2.5f;
            float keyRectHeight = 7.0f;
            float keyRectX = x + maxWidth - padding - keyRectWidth;
            float keyRectY = currentY + Math.max(0.0f, (currentHeight - keyRectHeight) / 2.0f) + 0.2f;
            float keyTextY = keyRectY + (keyRectHeight - font.height()) / 2.0f;
            if (anim > 0.78f) {
                ctx.drawRoundedRect(keyRectX, keyRectY, keyRectWidth, keyRectHeight, BorderRadius.all(1.7f), new ColorRGBA(0, 0, 0, (int)(120.0f * anim)));
                ctx.drawRoundedBorder(keyRectX, keyRectY, keyRectWidth, keyRectHeight, 0.5f, BorderRadius.all(1.7f), new ColorRGBA(100, 100, 100, alpha));
                ctx.drawText(font, keyName, keyRectX + (keyRectWidth - keyWidth) / 2.0f, keyTextY, this.resolveTextColor(ColorRGBA.WHITE.withAlpha(alpha)));
            }
            currentY += currentHeight + 1.0f;
        }
        this.setBounds(maxWidth, Math.max(rowHeight, currentY - y));
    }

    private void drawCard(CustomDrawContext ctx, float x, float y, float width, float height, float alphaMul, float radius) {
        this.drawMediaCard(ctx, x, y, width, height, radius, alphaMul);
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (HotKeys.mc.player == null || HotKeys.mc.world == null) {
            return;
        }
        this.updateHud();
    }

    @EventTarget
    public void onRender(EventHudRender event) {
        if (HotKeys.mc.player == null || HotKeys.mc.world == null) {
            return;
        }
        this.renderHud(event.getContext());
    }

    @EventTarget
    public void onMouse(EventMouse event) {
        if (HotKeys.mc.player == null || HotKeys.mc.world == null) {
            return;
        }
        this.handleMouse(event);
    }
}

