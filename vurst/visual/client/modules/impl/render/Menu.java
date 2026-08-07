
package vurst.visual.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.Screen;
import vurst.visual.VurstVisual;
import vurst.visual.base.events.impl.render.EventRenderScreen;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.screens.menu.MenuScreen;
import vurst.visual.utility.render.display.base.UIContext;

@ModuleAnnotation(name="Menu", category=Category.RENDER, description="Открывает меню.")
public final class Menu
extends Module {
    public static final Menu INSTANCE = new Menu();

    private Menu() {
        this.setKeyCode(344);
    }

    @Override
    public void onEnable() {
        if (Menu.mc.world == null) {
            this.setEnabled(false);
            return;
        }
        if (Menu.mc.currentScreen == VurstVisual.getInstance().getMenuScreen()) {
            return;
        }
        mc.setScreen((Screen)VurstVisual.getInstance().getMenuScreen());
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void setKeyCode(int keyCode) {
        if (keyCode == -1) {
            return;
        }
        super.setKeyCode(keyCode);
    }

    @EventTarget
    public void render2d(EventRenderScreen eventRender2D) {
        MenuScreen menuScreen = VurstVisual.getInstance().getMenuScreen();
        if (menuScreen == null) {
            return;
        }
        if (Menu.mc.currentScreen != menuScreen) {
            UIContext uiContext = eventRender2D.getContext();
            menuScreen.renderTop(uiContext, uiContext.getMouseX(), uiContext.getMouseY());
        }
        if (menuScreen.isFinish()) {
            this.toggle();
        }
    }
}

