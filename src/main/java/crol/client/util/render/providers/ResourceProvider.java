package crol.client.util.render.providers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class ResourceProvider {
   private static ResourceManager RESOURCE_MANAGER = MinecraftClient.getInstance().getResourceManager();
   private static final Gson GSON = new Gson();

   public static Identifier getShaderIdentifier(String name) {
      return Identifier.of("crol", "core/" + name);
   }

   public static JsonObject toJson(Identifier identifier) {
      return JsonParser.parseString(toString(identifier)).getAsJsonObject();
   }

   public static <T> T fromJsonToInstance(Identifier identifier, Class<T> clazz) {
      return (T)GSON.fromJson(toString(identifier), clazz);
   }

   public static String toString(Identifier identifier) {
      return toString(identifier, "\n");
   }

   public static String toString(Identifier identifier, String delimiter) {
      if (RESOURCE_MANAGER == null) {
         RESOURCE_MANAGER = MinecraftClient.getInstance().getResourceManager();
      }

      try {
         InputStream inputStream = RESOURCE_MANAGER.open(identifier);

         String var4;
         try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            try {
               var4 = (String)reader.lines().collect(Collectors.joining(delimiter));
            } catch (Throwable var8) {
               try {
                  reader.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }

               throw var8;
            }

            reader.close();
         } catch (Throwable var9) {
            if (inputStream != null) {
               try {
                  inputStream.close();
               } catch (Throwable var6) {
                  var9.addSuppressed(var6);
               }
            }

            throw var9;
         }

         if (inputStream != null) {
            inputStream.close();
         }

         return var4;
      } catch (IOException ex) {
         throw new RuntimeException(ex);
      }
   }
}
