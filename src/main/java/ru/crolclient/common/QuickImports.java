package ru.crolclient.common;

import com.google.gson.Gson;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.Window;
import ru.crolclient.api.system.draw.DrawEngine;
import ru.crolclient.api.system.draw.DrawEngineImpl;
import ru.crolclient.api.system.shape.implement.Image;
import ru.crolclient.api.system.shape.implement.Rectangle;
import ru.crolclient.implement.screens.menu.components.implement.window.WindowManager;

public interface QuickImports extends QuickLogger {
    MinecraftClient mc = MinecraftClient.getInstance();
    Window window = mc.getWindow();

    Tessellator tessellator = Tessellator.getInstance();

    DrawEngine drawEngine = new DrawEngineImpl();

    Rectangle rectangle = new Rectangle();
    Image image = new Image();

    Gson gson = new Gson();

    WindowManager windowManager = new WindowManager();
}
