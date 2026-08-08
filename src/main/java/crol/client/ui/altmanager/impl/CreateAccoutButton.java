package crol.client.ui.altmanager.impl;

import crol.client.managers.FontManager;
import crol.client.util.render.builders.Builder;
import crol.client.util.render.builders.states.QuadColorState;
import crol.client.util.render.builders.states.QuadRadiusState;
import crol.client.util.render.builders.states.SizeState;
import crol.client.util.render.msdf.MsdfFont;
import crol.client.util.render.renderers.impl.BuiltRectangle;
import crol.client.util.render.renderers.impl.BuiltText;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class CreateAccoutButton {
   private double x;
   private double y;
   private double width = (double)124.0F;
   private double height = (double)20.0F;
   private Runnable run;
   private String name;

   public CreateAccoutButton(String name, Runnable run) {
      this.run = run;
      this.name = name;
   }

   public void onClick() {
      this.run.run();
   }

   public void render(Matrix4f matrix4f) {
      ((BuiltRectangle)Builder.rectangle().size(new SizeState(this.width, this.height)).color(new QuadColorState(new Color(-12536065, true), new Color(-13466369, true), new Color(-12536065, true), new Color(-13466369, true))).radius(new QuadRadiusState(5.0F)).smoothness(1.15F).build()).render(matrix4f, this.x, this.y);
      double centerX = this.x + this.width / (double)2.0F - (double)(((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth("Create", 8.0F) / 2.0F);
      BuiltText text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text("Create").color(Color.WHITE).size(8.0F).thickness(0.05F).build();
      text.render(matrix4f, centerX + (double)5.0F, this.y + (double)5.0F);
      text = (BuiltText)Builder.text().font((MsdfFont)FontManager.MAINMENU.get()).text("A").color(Color.WHITE).size(8.0F).thickness(0.05F).build();
      text.render(matrix4f, centerX - (double)5.0F, this.y + (double)6.0F);
   }

   public void updatePos(double x, double y) {
      this.x = x;
      this.y = y;
   }

   public double getWidth() {
      return this.width;
   }

   public double getHeight() {
      return this.height;
   }

   public String getName() {
      return this.name;
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }
}
