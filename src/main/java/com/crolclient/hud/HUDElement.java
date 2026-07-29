package com.crolclient.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public abstract class HUDElement {
    protected final String name;
    protected int x, y;
    protected boolean enabled;
    protected final MinecraftClient mc = MinecraftClient.getInstance();

    public HUDElement(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public String getName() { return name; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void toggle() { setEnabled(!enabled); }

    public abstract void render(DrawContext context, float tickDelta);
    public abstract int getWidth();
    public abstract int getHeight();

    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
}
