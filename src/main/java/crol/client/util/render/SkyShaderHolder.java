package crol.client.util.render;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.CompiledShader;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramDefinition;
import net.minecraft.client.gl.CompiledShader.Type;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class SkyShaderHolder {
   private static final Logger LOG = LoggerFactory.getLogger("SkyShaderHolder");
   private static final Identifier VERT_ID = Identifier.of("crol", "shaders/core/sky.vsh");
   private static final Identifier FRAG_ID = Identifier.of("crol", "shaders/core/sky.fsh");
   private static ShaderProgram program = null;
   private static GlUniform uSkyZenith;
   private static GlUniform uSkyHorizon;
   private static GlUniform uNebColor1;
   private static GlUniform uNebColor2;
   private static GlUniform uNebIntensity;
   private static GlUniform uStarColor;
   private static volatile boolean configDirty = true;

   public static void markDirty() {
      configDirty = true;
   }

   public static void reload(ResourceManager manager) {
      if (program != null) {
         program.close();
         program = null;
      }

      clearRefs();

      try {
         String vs = readResource(manager, VERT_ID);
         String fs = readResource(manager, FRAG_ID);
         CompiledShader vert = CompiledShader.compile(VERT_ID, Type.VERTEX, vs);
         CompiledShader frag = CompiledShader.compile(FRAG_ID, Type.FRAGMENT, fs);
         program = ShaderProgram.create(vert, frag, VertexFormats.POSITION);
         vert.close();
         frag.close();
         float[] id16 = new float[]{1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F};
         program.set(List.of(u("GameTime", "float", 1, List.of(0.0F)), u("FogColor", "float", 4, List.of(0.0F, 0.0F, 0.0F, 0.0F)), u("FogStart", "float", 1, List.of(0.0F)), u("FogEnd", "float", 1, List.of(192.0F)), u("InvProjMat", "matrix4x4", 16, fl(id16)), u("InvViewMat", "matrix4x4", 16, fl(id16)), u("SkyZenith", "float", 3, fl(SkyConfig.SKY_ZENITH)), u("SkyHorizon", "float", 3, fl(SkyConfig.SKY_HORIZON)), u("NebColor1", "float", 3, fl(SkyConfig.NEB_COLOR1)), u("NebColor2", "float", 3, fl(SkyConfig.NEB_COLOR2)), u("NebIntensity", "float", 1, List.of(SkyConfig.NEB_INTENSITY)), u("StarColor", "float", 3, fl(SkyConfig.STAR_COLOR))), List.of());
         uSkyZenith = program.getUniform("SkyZenith");
         uSkyHorizon = program.getUniform("SkyHorizon");
         uNebColor1 = program.getUniform("NebColor1");
         uNebColor2 = program.getUniform("NebColor2");
         uNebIntensity = program.getUniform("NebIntensity");
         uStarColor = program.getUniform("StarColor");
         configDirty = true;
         LOG.info("[SkyShader] Loaded OK");
      } catch (Exception e) {
         LOG.error("[SkyShader] Failed to load shader", e);
      }

   }

   public static void uploadConfigUniforms() {
      if (program != null && configDirty) {
         configDirty = false;
         set3(uSkyZenith, SkyConfig.SKY_ZENITH);
         set3(uSkyHorizon, SkyConfig.SKY_HORIZON);
         set3(uNebColor1, SkyConfig.NEB_COLOR1);
         set3(uNebColor2, SkyConfig.NEB_COLOR2);
         set3(uStarColor, SkyConfig.STAR_COLOR);
         if (uNebIntensity != null) {
            uNebIntensity.set(SkyConfig.NEB_INTENSITY);
         }

      }
   }

   public static ShaderProgram getProgram() {
      return program;
   }

   private static void set3(GlUniform u, float[] v) {
      if (u != null) {
         u.set(v[0], v[1], v[2]);
      }

   }

   private static void clearRefs() {
      uStarColor = null;
      uNebIntensity = null;
      uNebColor2 = null;
      uNebColor1 = null;
      uSkyHorizon = null;
      uSkyZenith = null;
   }

   private static ShaderProgramDefinition.Uniform u(String name, String type, int count, List<Float> values) {
      return new ShaderProgramDefinition.Uniform(name, type, count, values);
   }

   private static List<Float> fl(float[] arr) {
      List<Float> l = new ArrayList(arr.length);

      for(float f : arr) {
         l.add(f);
      }

      return l;
   }

   private static String readResource(ResourceManager manager, Identifier id) throws Exception {
      Optional<?> opt = manager.getResource(id);
      if (opt.isEmpty()) {
         throw new Exception("Resource not found: " + String.valueOf(id));
      } else {
         InputStream s = ((Resource)opt.get()).getInputStream();

         String var4;
         try {
            var4 = new String(s.readAllBytes(), StandardCharsets.UTF_8);
         } catch (Throwable var7) {
            if (s != null) {
               try {
                  s.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (s != null) {
            s.close();
         }

         return var4;
      }
   }
}
