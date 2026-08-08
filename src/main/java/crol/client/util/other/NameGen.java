package crol.client.util.other;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class NameGen {
   private SecureRandom secureRandom = new SecureRandom();
   private String[] names = new String[0];

   @Compile
   public String loadNames() throws IOException {
      String source = "";
      ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(((Resource)resourceManager.getResource(Identifier.of("crol", "names/names.txt")).get()).getInputStream()));
      source = (String)bufferedReader.lines().filter((str) -> !str.isEmpty()).map((str) -> str.replace("\t", "")).collect(Collectors.joining("\n"));
      this.names = source.split("\n");

      try {
         bufferedReader.close();
      } catch (IOException var5) {
      }

      return source;
   }

   @Compile
   public String generate() {
      try {
         this.loadNames();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }

      String var10000 = this.names[this.secureRandom.nextInt(this.names.length)];
      return var10000 + this.secureRandom.nextInt(9) + this.secureRandom.nextInt(9) + this.secureRandom.nextInt(9) + this.secureRandom.nextInt(9);
   }

   public String[] getNames() {
      return this.names;
   }
}
