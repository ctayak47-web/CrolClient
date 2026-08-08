package crol.client.util.render.esp;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.RenderLayer.MultiPhaseParameters;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class GlassEspRenderLayer {
   public static final ShaderProgramKey GLASS_ESP_KEY;
   public static final RenderLayer GLASS_ESP;

   static {
      GLASS_ESP_KEY = new ShaderProgramKey(Identifier.of("crol", "core/glassesp"), VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, Defines.EMPTY);
      GLASS_ESP = RenderLayer.of("wild_glass_esp", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, DrawMode.QUADS, 256, false, true, MultiPhaseParameters.builder().program(new RenderPhase.ShaderProgram(GLASS_ESP_KEY)).depthTest(RenderPhase.ALWAYS_DEPTH_TEST).cull(RenderPhase.DISABLE_CULLING).transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY).writeMaskState(RenderPhase.COLOR_MASK).build(false));
   }
}
