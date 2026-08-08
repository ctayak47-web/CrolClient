package crol.client.ui.hud;

import crol.client.CrolClient;
import crol.client.managers.FontManager;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.ui.draggable.Draggable;
import crol.client.util.render.builders.Builder;
import crol.client.util.render.builders.states.QuadColorState;
import crol.client.util.render.builders.states.QuadRadiusState;
import crol.client.util.render.builders.states.SizeState;
import crol.client.util.render.msdf.MsdfFont;
import crol.client.util.render.renderers.impl.BuiltBlur;
import crol.client.util.render.renderers.impl.BuiltRectangle;
import crol.client.util.render.renderers.impl.BuiltText;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public abstract class HudElement {
   protected final Draggable draggable;
   protected final BooleanSetting blur;
   protected final FloatSetting streght;
   protected boolean visiblePane;
   private final String name;

   public HudElement(Draggable draggable, String name) {
      this.draggable = draggable;
      CrolClient.INSTANCE.getDraggableManager().addDraggable(draggable);
      this.blur = ((BooleanSetting)(new BooleanSetting()).name("Blur")).value(false);
      this.streght = ((FloatSetting)(new FloatSetting()).name("Streght")).incriment(0.5F).minValue(1.0F).maxValue(25.0F).value(12.0F);
      this.visiblePane = false;
      this.name = name;
   }

   public HudElement() {
      this.draggable = null;
      this.blur = ((BooleanSetting)(new BooleanSetting()).name("Blur")).value(false);
      this.streght = ((FloatSetting)(new FloatSetting()).name("Streght")).incriment(0.5F).minValue(1.0F).maxValue(25.0F).value(12.0F);
      this.visiblePane = false;
      this.name = "s";
   }

   public abstract void render(DrawContext var1);

   public BooleanSetting getBlur() {
      return this.blur;
   }

   public FloatSetting getStreght() {
      return this.streght;
   }

   public void setVisiblePane(boolean visiblePane) {
      this.visiblePane = visiblePane;
   }

   public void renderPanel(DrawContext drawContext) {
      Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();
      double x = (double)(this.draggable.x + this.draggable.getWeight());
      double y = (double)(this.draggable.y + this.draggable.getHeight());
      BuiltBlur blur = (BuiltBlur)Builder.blur().size(new SizeState(147.0F, 50.0F)).radius(new QuadRadiusState(8.0F)).blurRadius(12.0F).smoothness(1.0F).color(new QuadColorState(Color.white)).build();
      blur.render(matrix, x, y);
      BuiltRectangle rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(147.0F, 50.0F)).color(new QuadColorState(new Color(2048202266, true))).radius(new QuadRadiusState(8.0F)).smoothness(1.15F).build();
      rectangle.render(matrix, x, y);
      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(139.0F, 28.0F)).color(new QuadColorState(new Color(-1206446313, true))).radius(new QuadRadiusState(7.0F, 3.0F, 3.0F, 7.0F)).smoothness(1.15F).build();
      rectangle.render(matrix, x + (double)4.0F, y + (double)4.0F);
      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(18.0F, 18.0F)).color(new QuadColorState(CrolClient.INSTANCE.getThemeManager().getTheme().color())).radius(new QuadRadiusState(5.0F)).smoothness(1.15F).build();
      rectangle.render(matrix, x + (double)9.0F, y + (double)9.0F);
      BuiltText text = (BuiltText)Builder.text().font((MsdfFont)FontManager.CATEGORY.get()).text("E").color(Color.WHITE).size(10.0F).thickness(0.05F).build();
      text.render(matrix, x + (double)12.0F, y + (double)12.0F);
      BuiltText icon = (BuiltText)Builder.text().font((MsdfFont)FontManager.WILD.get()).text("A").color(new Color(-12829636, true)).size(7.0F).thickness(0.05F).build();
      icon.render(matrix, x + (double)34.0F, y + (double)14.0F);
      text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text("wildclient.org").color(Color.gray).size(7.4F).thickness(0.05F).build();
      text.render(matrix, x + (double)45.0F, y + (double)13.5F);
      text = (BuiltText)Builder.text().font((MsdfFont)FontManager.WILD.get()).text("T").color(new Color(1040187391, true)).size(5.4F).thickness(0.05F).build();
      text.render(matrix, x + (double)98.5F, y + (double)15.0F);
      text = (BuiltText)Builder.text().font((MsdfFont)FontManager.WILD.get()).text("C").color(CrolClient.INSTANCE.getThemeManager().getTheme().color()).size(7.0F).thickness(0.05F).build();
      text.render(matrix, x + (double)106.0F, y + (double)13.5F);
      text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text("Staff").color(Color.WHITE).size(8.0F).thickness(0.05F).build();
      text.render(matrix, x + (double)117.0F, y + (double)13.5F);
   }
}
