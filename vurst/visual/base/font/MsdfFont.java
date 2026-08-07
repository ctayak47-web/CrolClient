
package vurst.visual.base.font;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Generated;
import net.minecraft.AbstractTexture;
import net.minecraft.Text;
import net.minecraft.Identifier;
import net.minecraft.VertexConsumer;
import org.joml.Matrix4f;
import vurst.visual.VurstVisual;
import vurst.visual.base.font.Font;
import vurst.visual.base.font.FontData;
import vurst.visual.base.font.MsdfGlyph;
import vurst.visual.base.font.ResourceProvider;
import vurst.visual.utility.interfaces.IMinecraft;
import vurst.visual.utility.render.display.base.Gradient;

public final class MsdfFont
implements IMinecraft {
    private final String name;
    private final AbstractTexture texture;
    private final FontData.AtlasData atlas;
    private final FontData.MetricsData metrics;
    private final Map<Integer, MsdfGlyph> glyphs;
    private final Map<Integer, Map<Integer, Float>> kernings;

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

    public void applyGlyphs(Matrix4f matrix, VertexConsumer consumer, String text, float size, float thickness, float spacing, float x, float y, float z, int color) {
        this.texture.setFilter(true, true);
        text = text.replace("ᴀ", "A").replace("ʙ", "B").replace("ᴄ", "C").replace("ᴅ", "D").replace("ᴇ", "E").replace("ꜰ", "F").replace("ɢ", "G").replace("ʜ", "H").replace("ɪ", "I").replace("ᴊ", "J").replace("ᴋ", "K").replace("ʟ", "L").replace("ᴍ", "M").replace("ɴ", "N").replace("ᴏ", "O").replace("ᴘ", "P").replace("ʀ", "R").replace("ꜱ", "S").replace("ᴛ", "T").replace("ᴜ", "U").replace("ᴠ", "V").replace("ᴡ", "W").replace("ʏ", "Y").replace("ᴢ", "Z").replace("ǫ", "Q").replace("ʠ", "Q");
        int prevChar = -1;
        boolean skipNext = false;
        for (int i = 0; i < text.length(); ++i) {
            int c = text.charAt(i);
            if (c == 7424) {
                c = 1040;
            }
            if (skipNext) {
                skipNext = false;
                continue;
            }
            if (c == 167) {
                skipNext = true;
                continue;
            }
            MsdfGlyph glyph = this.glyphs.get(c);
            if (glyph == null) continue;
            Map<Integer, Float> kerning = this.kernings.get(prevChar);
            if (kerning != null) {
                x += kerning.getOrDefault(c, Float.valueOf(0.0f)).floatValue() * size;
            }
            x += glyph.apply(matrix, consumer, size, x, y, z, color) + thickness + spacing;
            prevChar = c;
        }
    }

    public void applyGlyphs(Matrix4f matrix, VertexConsumer consumer, String text, float size, float thickness, float spacing, float x, float y, float z, Gradient color) {
        this.texture.setFilter(true, true);
        text = text.replace("ᴀ", "A").replace("ʙ", "B").replace("ᴄ", "C").replace("ᴅ", "D").replace("ᴇ", "E").replace("ꜰ", "F").replace("ɢ", "G").replace("ʜ", "H").replace("ɪ", "I").replace("ᴊ", "J").replace("ᴋ", "K").replace("ʟ", "L").replace("ᴍ", "M").replace("ɴ", "N").replace("ᴏ", "O").replace("ᴘ", "P").replace("ʀ", "R").replace("ꜱ", "S").replace("ᴛ", "T").replace("ᴜ", "U").replace("ᴠ", "V").replace("ᴡ", "W").replace("ʏ", "Y").replace("ᴢ", "Z").replace("ǫ", "Q").replace("ʠ", "Q");
        int prevChar = -1;
        boolean skipNext = false;
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (skipNext) {
                skipNext = false;
                continue;
            }
            if (c == '§') {
                skipNext = true;
                continue;
            }
            MsdfGlyph glyph = this.glyphs.get(c);
            if (glyph == null) continue;
            Map<Integer, Float> kerning = this.kernings.get(prevChar);
            if (kerning != null) {
                x += kerning.getOrDefault(c, Float.valueOf(0.0f)).floatValue() * size;
            }
            x += glyph.apply(matrix, consumer, size, x, y, z, color) + thickness + spacing;
            prevChar = c;
        }
    }

    public float getWidth(String text, float size) {
        text = text.replace("ᴀ", "A").replace("ʙ", "B").replace("ᴄ", "C").replace("ᴅ", "D").replace("ᴇ", "E").replace("ꜰ", "F").replace("ɢ", "G").replace("ʜ", "H").replace("ɪ", "I").replace("ᴊ", "J").replace("ᴋ", "K").replace("ʟ", "L").replace("ᴍ", "M").replace("ɴ", "N").replace("ᴏ", "O").replace("ᴘ", "P").replace("ʀ", "R").replace("ꜱ", "S").replace("ᴛ", "T").replace("ᴜ", "U").replace("ᴠ", "V").replace("ᴡ", "W").replace("ʏ", "Y").replace("ᴢ", "Z").replace("ǫ", "Q").replace("ʠ", "Q");
        int prevChar = -1;
        float width = 0.0f;
        boolean skipNext = false;
        for (int i = 0; i < text.length(); ++i) {
            int c = text.charAt(i);
            if (c == 7424) {
                c = 1040;
            }
            if (skipNext) {
                skipNext = false;
                continue;
            }
            if (c == 167) {
                skipNext = true;
                continue;
            }
            MsdfGlyph glyph = this.glyphs.get(c);
            if (glyph == null) continue;
            Map<Integer, Float> kerning = this.kernings.get(prevChar);
            if (kerning != null) {
                width += kerning.getOrDefault(c, Float.valueOf(0.0f)).floatValue() * size;
            }
            width += glyph.getWidth(size);
            prevChar = c;
        }
        return width;
    }

    public float getTextWidth(Text text, float size) {
        return this.getWidth(text.getString(), size);
    }

    public Font getFont(float size) {
        return new Font(this, size);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public FontData.AtlasData getAtlas() {
        return this.atlas;
    }

    @Generated
    public FontData.MetricsData getMetrics() {
        return this.metrics;
    }

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
            this.dataIdentifer = VurstVisual.id("fonts/msdf/" + dataFileName + ".json");
            return this;
        }

        public Builder atlas(String atlasFileName) {
            this.atlasIdentifier = VurstVisual.id("fonts/msdf/" + atlasFileName + ".png");
            return this;
        }

        public MsdfFont build() {
            FontData data = ResourceProvider.fromJsonToInstance(this.dataIdentifer, FontData.class);
            AbstractTexture texture = IMinecraft.mc.getTextureManager().getTexture(this.atlasIdentifier);
            if (data == null) {
                throw new RuntimeException("Failed to read font data file: " + this.dataIdentifer.toString() + "; Are you sure this is json file? Try to check the correctness of its syntax.");
            }
            RenderSystem.recordRenderCall(() -> texture.setFilter(true, false));
            float aWidth = data.atlas().width();
            float aHeight = data.atlas().height();
            Map<Integer, MsdfGlyph> glyphs = data.glyphs().stream().collect(Collectors.toMap(FontData.GlyphData::unicode, glyphData -> new MsdfGlyph((FontData.GlyphData)glyphData, aWidth, aHeight)));
            HashMap<Integer, Map<Integer, Float>> kernings = new HashMap<Integer, Map<Integer, Float>>();
            data.kernings().forEach(kerning -> {
                Map map = kernings.computeIfAbsent(kerning.leftChar(), k -> new HashMap());
                map.put(kerning.rightChar(), Float.valueOf(kerning.advance()));
            });
            return new MsdfFont(this.name, texture, data.atlas(), data.metrics(), glyphs, kernings);
        }
    }
}

