package crol.client.util.render.msdf;

import com.mojang.blaze3d.systems.RenderSystem;
import crol.client.CrolClient;
import crol.client.util.color.ColorUtil;
import crol.client.util.render.providers.ResourceProvider;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public final class MsdfFont {
   private final String name;
   private final AbstractTexture texture;
   private final FontData.AtlasData atlas;
   private final FontData.MetricsData metrics;
   private final Map<Integer, MsdfGlyph> glyphs;
   private final Map<Integer, Map<Integer, Float>> kernings;
   private float xGradient = 0.0F;

   private MsdfFont(String name, AbstractTexture texture, FontData.AtlasData atlas, FontData.MetricsData metrics, Map<Integer, MsdfGlyph> glyphs, Map<Integer, Map<Integer, Float>> kernings) {
      this.name = name;
      this.texture = texture;
      this.atlas = atlas;
      this.metrics = metrics;
      this.glyphs = glyphs;
      this.kernings = kernings;
   }

   public int getTextureId() {
      return this.texture.getGlId();
   }

   public void applyGlyphs(Matrix4f matrix, VertexConsumer consumer, String text, float size, float thickness, float spacing, float x, float y, float z, int color, boolean gradient, boolean alphaGradient, double alpha) {
      int prevChar = -1;
      this.xGradient = x;

      for(int i = 0; i < text.length(); ++i) {
         int _char = text.charAt(i);
         MsdfGlyph glyph = (MsdfGlyph)this.glyphs.get(_char);
         if (glyph != null) {
            Map<Integer, Float> kerning = (Map)this.kernings.get(prevChar);
            if (kerning != null) {
               x += (Float)kerning.getOrDefault(_char, 0.0F) * size;
            }

            x += glyph.apply(matrix, consumer, size, x, y, z, gradient ? (alphaGradient ? ColorUtil.setAlpha(alpha, new Color(CrolClient.INSTANCE.getGradientText().getColors()[(int)((x - this.xGradient) * 3.0F)])).getRGB() : (new Color(CrolClient.INSTANCE.getGradientText().getColors()[(int)((x - this.xGradient) * 3.0F)])).getRGB()) : color) + thickness + spacing;
            prevChar = _char;
         }
      }

   }

   public float getWidth(String text, float size) {
      int prevChar = -1;
      float width = 0.0F;

      for(int i = 0; i < text.length(); ++i) {
         int _char = text.charAt(i);
         MsdfGlyph glyph = (MsdfGlyph)this.glyphs.get(_char);
         if (glyph != null) {
            Map<Integer, Float> kerning = (Map)this.kernings.get(prevChar);
            if (kerning != null) {
               width += (Float)kerning.getOrDefault(_char, 0.0F) * size;
            }

            width += glyph.getWidth(size);
            prevChar = _char;
         }
      }

      return width;
   }

   public String getName() {
      return this.name;
   }

   public FontData.AtlasData getAtlas() {
      return this.atlas;
   }

   public FontData.MetricsData getMetrics() {
      return this.metrics;
   }

   public static Builder builder() {
      return new Builder();
   }

   @Environment(EnvType.CLIENT)
   public static class Builder {
      private String name = "?";
      private Identifier dataIdentifer;
      private Identifier atlasIdentifier;

      private Builder() {
      }

      public Builder name(String name) {
         this.name = name;
         return this;
      }

      public Builder data(String dataFileName) {
         this.dataIdentifer = Identifier.of("crol", "fonts/" + dataFileName + ".json");
         return this;
      }

      public Builder atlas(String atlasFileName) {
         this.atlasIdentifier = Identifier.of("crol", "fonts/" + atlasFileName + ".png");
         return this;
      }

      @Compile
      public MsdfFont build() {
         FontData data = (FontData)ResourceProvider.fromJsonToInstance(this.dataIdentifer, FontData.class);
         AbstractTexture texture = MinecraftClient.getInstance().getTextureManager().getTexture(this.atlasIdentifier);
         if (data == null) {
            throw new RuntimeException("Failed to read font data file: " + this.dataIdentifer.toString() + "; Are you sure this is json file? Try to check the correctness of its syntax.");
         } else {
            RenderSystem.recordRenderCall(() -> texture.setFilter(true, false));
            float aWidth = data.atlas().width();
            float aHeight = data.atlas().height();
            Map<Integer, MsdfGlyph> glyphs = (Map)data.glyphs().stream().collect(Collectors.toMap((glyphData) -> glyphData.unicode(), (glyphData) -> new MsdfGlyph(glyphData, aWidth, aHeight)));
            Map<Integer, Map<Integer, Float>> kernings = new HashMap();
            data.kernings().forEach((kerning) -> {
               Map<Integer, Float> map = (Map)kernings.get(kerning.leftChar());
               if (map == null) {
                  map = new HashMap();
                  kernings.put(kerning.leftChar(), map);
               }

               map.put(kerning.rightChar(), kerning.advance());
            });
            return new MsdfFont(this.name, texture, data.atlas(), data.metrics(), glyphs, kernings);
         }
      }
   }
}
