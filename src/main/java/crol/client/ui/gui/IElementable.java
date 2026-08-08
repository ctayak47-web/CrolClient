package crol.client.ui.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;

@Environment(EnvType.CLIENT)
public interface IElementable {
   void mouseClick(int var1);

   void render(double var1, double var3, double var5, double var7, double var9, double var11, DrawContext var13);

   void button(double var1, double var3, double var5, double var7, double var9, double var11, double var13, double var15, double var17, double var19, DrawContext var21, int var22);
}
