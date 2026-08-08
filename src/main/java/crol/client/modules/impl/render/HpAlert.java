package crol.client.modules.impl.render;

import crol.client.event.classes.Render2DEvent;
import crol.client.event.interfaces.IRenderable2D;
import crol.client.managers.FontManager;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.util.render.builders.Builder;
import crol.client.util.render.msdf.MsdfFont;
import crol.client.util.render.renderers.impl.BuiltText;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class HpAlert extends Module implements IRenderable2D {
   private final FloatSetting hp = ((FloatSetting)(new FloatSetting()).name("Health")).minValue(3.0F).maxValue(18.0F).incriment(0.5F).value(5.0F);
   private float animProgress = 0.0F;
   private float pulsePhase = 0.0F;
   private long lastTime = 0L;

   public HpAlert() {
      super(new ModuleInfo("HpAlert", Category.RENDER, "Уведомляет о падении здоровья игрока ниже заданного значения"));
      this.addSetting(new Setting[]{this.hp});
   }

   public void onRender2D(Render2DEvent event) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player != null) {
         long now = System.nanoTime();
         float dt = this.lastTime == 0L ? 0.016F : (float)(now - this.lastTime) / 1.0E9F;
         dt = Math.min(dt, 0.1F);
         this.lastTime = now;
         boolean isAlert = mc.player.getHealth() <= this.hp.getValue();
         float target = isAlert ? 1.0F : 0.0F;
         this.animProgress += (target - this.animProgress) * Math.min(1.0F, dt * 5.0F);
         if (!(this.animProgress < 0.01F)) {
            this.pulsePhase += dt * 2.2F;
            float pulse = (float)Math.pow(Math.abs(Math.sin((double)this.pulsePhase)), (double)2.5F);
            DrawContext ctx = event.getDrawContext();
            int W = mc.getWindow().getScaledWidth();
            int H = mc.getWindow().getScaledHeight();
            float intensity = this.animProgress * (0.55F + 0.45F * pulse);

            for(int x = 0; x < W / 3; ++x) {
               float t = 1.0F - (float)x / ((float)W / 3.0F);
               float a = intensity * t * t * 0.85F;
               int alpha = MathHelper.clamp((int)(a * 255.0F), 0, 255);
               ctx.fill(x, 0, x + 1, H, alpha << 24 | 11141120);
            }

            for(int x = 0; x < W / 3; ++x) {
               float t = (float)x / ((float)W / 3.0F);
               float a = intensity * t * t * 0.85F;
               int alpha = MathHelper.clamp((int)(a * 255.0F), 0, 255);
               ctx.fill(W - W / 3 + x, 0, W - W / 3 + x + 1, H, alpha << 24 | 11141120);
            }

            for(int y = 0; y < H / 4; ++y) {
               float t = 1.0F - (float)y / ((float)H / 4.0F);
               float a = intensity * t * t * 0.4F;
               int alpha = MathHelper.clamp((int)(a * 255.0F), 0, 255);
               ctx.fill(0, y, W, y + 1, alpha << 24 | 11141120);
            }

            for(int y = 0; y < H / 4; ++y) {
               float t = (float)y / ((float)H / 4.0F);
               float a = intensity * t * t * 0.4F;
               int alpha = MathHelper.clamp((int)(a * 255.0F), 0, 255);
               ctx.fill(0, H - H / 4 + y, W, H - H / 4 + y + 1, alpha << 24 | 11141120);
            }

            float textAlpha = this.animProgress * (0.7F + 0.3F * pulse);
            int tA = MathHelper.clamp((int)(textAlpha * 255.0F), 0, 255);
            String title = "LOW HEALTH";
            float titleSize = 9.0F;
            float titleW = ((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(title, titleSize);
            float titleX = ((float)W - titleW) / 2.0F;
            float titleY = (float)H * 0.42F;
            int shadowA = MathHelper.clamp((int)(textAlpha * 0.45F * 255.0F), 0, 255);
            BuiltText shadow = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(title).color(shadowA << 24 | 3342336).size(titleSize).thickness(0.05F).build();
            shadow.render(ctx.getMatrices().peek().getPositionMatrix(), titleX + 1.0F, titleY + 1.0F);
            BuiltText main = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(title).color(tA << 24 | 16720418).size(titleSize).thickness(0.05F).build();
            main.render(ctx.getMatrices().peek().getPositionMatrix(), titleX, titleY);
            float fontHeight = 10.5F;
            int lineY = (int)(titleY + fontHeight + 4.0F);
            int lineW = (int)(titleW * 0.6F);
            int lineA = MathHelper.clamp((int)(textAlpha * 0.8F * 255.0F), 0, 255);
            ctx.fill(W / 2 - lineW / 2, lineY, W / 2 + lineW / 2, lineY + 1, lineA << 24 | 16720418);
            float currentHp = mc.player.getHealth();
            String hpStr = String.format("%.1f / %.0f", currentHp, mc.player.getMaxHealth());
            float hpSize = 5.5F;
            float hpW = ((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(hpStr, hpSize);
            float hpX = ((float)W - hpW) / 2.0F;
            float hpY = (float)(lineY + 5);
            int hpA = MathHelper.clamp((int)(textAlpha * 0.8F * 255.0F), 0, 255);
            BuiltText hpText = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(hpStr).color(hpA << 24 | 16746632).size(hpSize).thickness(0.05F).build();
            hpText.render(ctx.getMatrices().peek().getPositionMatrix(), hpX, hpY);
         }
      }
   }
}
