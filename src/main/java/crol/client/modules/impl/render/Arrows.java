package crol.client.modules.impl.render;

import crol.client.CrolClient;
import crol.client.event.classes.Render2DEvent;
import crol.client.event.interfaces.IRenderable2D;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.modules.settings.impl.ModeSetting;
import crol.client.util.IUtil;
import crol.client.util.math.MathUtil;
import crol.client.util.player.MovementUtil;
import crol.client.util.render.builders.Builder;
import crol.client.util.render.builders.states.QuadColorState;
import crol.client.util.render.builders.states.QuadRadiusState;
import crol.client.util.render.builders.states.SizeState;
import crol.client.util.render.renderers.impl.BuiltTexture;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public class Arrows extends Module implements IRenderable2D, IUtil {
   private final ModeSetting mode = ((ModeSetting)(new ModeSetting() {
      public void onChangeState(String val) {
         Arrows.this.setCurrentMode(val);
      }
   }).name("Mode")).value("Type 1").modes("Type 1", "Type 2", "Type 3", "Type 4");
   private final FloatSetting size = ((FloatSetting)(new FloatSetting()).name("Size")).minValue(35.0F).maxValue(120.0F).value(40.0F).incriment(1.0F);
   public float animationStep;
   private float animatedYaw;
   private float animatedPitch;
   private String currentMode = "type1.jpg";

   public Arrows() {
      super(new ModuleInfo("Arrows", Category.RENDER, "Рисует стрелки вокруг прицела направленные на противников"));
      this.addSetting(new Setting[]{this.mode, this.size});
   }

   public void onRender2D(Render2DEvent event) {
      if (mc.player != null && mc.world != null) {
         this.animatedYaw = MathUtil.fast(this.animatedYaw, mc.player.input.movementSideways * 10.0F, 5.0F);
         this.animatedPitch = MathUtil.fast(this.animatedPitch, mc.player.input.movementForward * 10.0F, 5.0F);
         float size = this.size.getValue();
         if (mc.currentScreen instanceof InventoryScreen) {
            size += 80.0F;
         }

         if (MovementUtil.isMove()) {
            size += 10.0F;
         }

         this.animationStep = MathUtil.fast(this.animationStep, size, 10.0F);
         if (mc.options.getPerspective() == Perspective.FIRST_PERSON) {
            for(AbstractClientPlayerEntity player : mc.world.getPlayers()) {
               if (mc.player != player) {
                  double x = player.lastRenderX + (player.getX() - player.lastRenderX) - mc.getEntityRenderDispatcher().camera.getPos().x;
                  double z = player.lastRenderZ + (player.getZ() - player.lastRenderZ) - mc.getEntityRenderDispatcher().camera.getPos().z;
                  double cos = (double)MathHelper.cos((float)((double)mc.getEntityRenderDispatcher().camera.getYaw() * (Math.PI / 180D)));
                  double sin = (double)MathHelper.sin((float)((double)mc.getEntityRenderDispatcher().camera.getYaw() * (Math.PI / 180D)));
                  double rotY = -(z * cos - x * sin);
                  double rotX = -(x * cos + z * sin);
                  float angle = (float)(Math.atan2(rotY, rotX) * (double)180.0F / Math.PI);
                  double x2 = (double)(this.animationStep * MathHelper.cos((float)Math.toRadians((double)angle)) + (float)mc.getWindow().getScaledWidth() / 2.0F) - (double)4.5F;
                  double y2 = (double)(this.animationStep * MathHelper.sin((float)Math.toRadians((double)angle)) + (float)mc.getWindow().getScaledHeight() / 2.0F);
                  event.getDrawContext().getMatrices().push();
                  event.getDrawContext().getMatrices().translate(x2, y2, (double)0.0F);
                  this.rotateZ(angle, event.getDrawContext().getMatrices());
                  Matrix4f matrix = event.getDrawContext().getMatrices().peek().getPositionMatrix();
                  if (CrolClient.INSTANCE.getFriendManager().isFriend(player.getNameForScoreboard())) {
                     AbstractTexture abstractTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("crol", "images/arrows/" + this.currentMode));
                     BuiltTexture texture = (BuiltTexture)Builder.texture().size(new SizeState(18.0F, 18.0F)).radius(new QuadRadiusState(0.0F)).texture(0.0F, 0.0F, 1.0F, 1.0F, abstractTexture).color(new QuadColorState((new Color(4894801)).getRGB())).build();
                     texture.render2(matrix, -4.0F, -1.0F, 0.0F);
                  } else {
                     AbstractTexture abstractTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("crol", "images/arrows/" + this.currentMode));
                     BuiltTexture texture = (BuiltTexture)Builder.texture().size(new SizeState(18.0F, 18.0F)).radius(new QuadRadiusState(0.0F)).texture(0.0F, 0.0F, 1.0F, 1.0F, abstractTexture).color(new QuadColorState(CrolClient.INSTANCE.getThemeManager().getTheme().color())).build();
                     texture.render2(matrix, -4.0F, -1.0F, 0.0F);
                  }

                  event.getDrawContext().getMatrices().pop();
               }
            }
         }

      }
   }

   private void setCurrentMode(String mode) {
      switch (mode) {
         case "Type 1" -> this.currentMode = "type1.jpg";
         case "Type 2" -> this.currentMode = "type2.jpg";
         case "Type 3" -> this.currentMode = "type3.jpg";
         case "Type 4" -> this.currentMode = "type4.png";
      }

   }

   private void rotateZ(float angle, MatrixStack matrixStack) {
      Quaternionf quaternion = (new Quaternionf()).rotateZ((float)Math.toRadians((double)angle));
      matrixStack.multiply(quaternion);
   }
}
