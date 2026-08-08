
package crol.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.Screen;
import crol.client.CrolClient;
import crol.client.base.events.impl.render.EventRenderScreen;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.screens.menu.MenuScreen;
import crol.client.utility.render.display.base.UIContext;

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
        if (Menu.mc.currentScreen == CrolClient.getInstance().getMenuScreen()) {
            return;
        }
        mc.setScreen((Screen)CrolClient.getInstance().getMenuScreen());
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
        MenuScreen menuScreen = CrolClient.getInstance().getMenuScreen();
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

