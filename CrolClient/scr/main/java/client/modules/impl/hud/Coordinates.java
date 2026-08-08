
package crol.client.modules.impl.hud;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.MathHelper;
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

@ModuleAnnotation(name="Coordinates", category=Category.HUD, description="Показывает координаты игрока.")
public final class Coordinates
extends HudModule {
    public static final Coordinates INSTANCE = new Coordinates();

    private Coordinates() {
        super(10.0f, 28.0f, 95.0f, 15.0f, true);
    }

    @Override
    protected void draw(CustomDrawContext ctx) {
        if (Coordinates.mc.player == null) {
            return;
        }
        Font font = Fonts.MEDIUM.getFont(6.0f);
        float x = this.getX();
        float y = this.getY();
        float padding = 5.0f;
        float bgHeight = 15.0f;
        int coordX = MathHelper.floor((double)Coordinates.mc.player.getX());
        int coordY = MathHelper.floor((double)Coordinates.mc.player.getY());
        int coordZ = MathHelper.floor((double)Coordinates.mc.player.getZ());
        String coordsText = "XYZ: " + coordX + " " + coordY + " " + coordZ;
        float totalWidth = padding * 2.0f + font.width(coordsText);
        BorderRadius radius = BorderRadius.all(4.0f);
        this.drawMediaCard(ctx, x, y, totalWidth, bgHeight, 4.0f, 1.0f);
        ctx.drawRoundedBorder(x, y, totalWidth, bgHeight, 0.5f, radius, new ColorRGBA(50, 50, 55, 120));
        float textY = y + (bgHeight - font.height()) / 2.0f + 0.5f;
        ctx.drawText(font, coordsText, x + padding, textY, this.getTextColor());
        this.setBounds(totalWidth, bgHeight);
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (Coordinates.mc.player == null || Coordinates.mc.world == null) {
            return;
        }
        this.updateHud();
    }

    @EventTarget
    public void onRender(EventHudRender event) {
        if (Coordinates.mc.player == null || Coordinates.mc.world == null) {
            return;
        }
        this.renderHud(event.getContext());
    }

    @EventTarget
    public void onMouse(EventMouse event) {
        if (Coordinates.mc.player == null || Coordinates.mc.world == null) {
            return;
        }
        this.handleMouse(event);
    }
}

