package crol.client.ui.altmanager.impl;

import crol.client.managers.FontManager;
import crol.client.util.render.builders.Builder;
import crol.client.util.render.builders.states.QuadColorState;
import crol.client.util.render.builders.states.QuadRadiusState;
import crol.client.util.render.builders.states.SizeState;
import crol.client.util.render.msdf.MsdfFont;
import crol.client.util.render.renderers.impl.BuiltBorder;
import crol.client.util.render.renderers.impl.BuiltRectangle;
import crol.client.util.render.renderers.impl.BuiltText;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class GenerateAccoutButton {
   private double x;
   private double y;
   private double width = (double)20.0F;
   private double height = (double)20.0F;
   private Runnable run;
   private String name;

   public GenerateAccoutButton(String name, Runnable run) {
      this.run = run;
      this.name = name;
   }

   public void onClick() {
      this.run.run();
   }

   public void render(Matrix4f matrix4f) {
      ((BuiltRectangle)Builder.rectangle().size(new SizeState(this.width, this.height)).color(new QuadColorState(new Color(-14737108, true))).radius(new QuadRadiusState(4.0F)).smoothness(1.15F).build()).render(matrix4f, this.x, this.y);
      ((BuiltBorder)Builder.border().size(new SizeState(this.width, this.height)).color(new QuadColorState(new Color(2434866))).radius(new QuadRadiusState(4.0F)).thickness(0.01F).smoothness(0.6F, 0.6F).build()).render(matrix4f, this.x, this.y);
      BuiltText text = (BuiltText)Builder.text().font((MsdfFont)FontManager.ICONS4.get()).text("B").color(new Color(7040376)).size(8.0F).thickness(0.05F).build();
      text.render(matrix4f, this.x + (double)5.5F, this.y + (double)5.5F);
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
