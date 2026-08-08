package crol.client.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import crol.client.CrolClient;
import crol.client.mixins.other.IMinecraftClientMixin;
import crol.client.modules.Module;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.modules.settings.impl.ColorSetting;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.modules.settings.impl.KeySetting;
import crol.client.modules.settings.impl.ModeSetting;
import crol.client.modules.settings.impl.MultiBoxSetting;
import crol.client.modules.settings.impl.StringSetting;
import crol.client.ui.altmanager.NickName;
import crol.client.ui.draggable.Draggable;
import crol.client.ui.themes.Theme;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.Session.AccountType;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class ConfigManager {
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private final Path configDir;
   private final Path profilesDir;
   private final ModuleManager moduleManager;
   private final DraggableManager draggableManager;
   private final ThemeManager themeManager;

   public ConfigManager(ModuleManager moduleManager, DraggableManager draggableManager, ThemeManager themeManager) {
      this.moduleManager = moduleManager;
      this.configDir = this.resolveDir("configs");
      this.profilesDir = this.resolveDir("configs");
      this.draggableManager = draggableManager;
      this.themeManager = themeManager;
   }

   private Path resolveDir(String sub) {
      Path gameDir = MinecraftClient.getInstance().runDirectory.toPath();
      Path dir = gameDir.resolve("CrolClient").resolve(sub);

      try {
         Files.createDirectories(dir);
      } catch (IOException e) {
         e.printStackTrace();
      }

      return dir;
   }

   public void saveAll() {
      for(Module module : this.moduleManager.getModules()) {
         this.save(module);
      }

   }

   public void save(Module module) {
      String name = module.getModuleInfo().name();
      Path var10001 = this.configDir;
      String var10002 = this.sanitize(name);
      this.writeJson(var10001.resolve(var10002 + ".json"), this.buildModuleJson(module));
   }

   private JsonElement serializeSetting(Setting<?> setting) {
      if (setting instanceof BooleanSetting s) {
         return new JsonPrimitive(s.getValue());
      } else if (setting instanceof FloatSetting s) {
         return new JsonPrimitive(s.getValue());
      } else if (setting instanceof ModeSetting s) {
         return new JsonPrimitive(s.getValue());
      } else if (setting instanceof KeySetting s) {
         return new JsonPrimitive(s.getValue());
      } else if (setting instanceof StringSetting s) {
         return new JsonPrimitive(s.getValue());
      } else if (setting instanceof ColorSetting s) {
         JsonObject obj = new JsonObject();
         Color c = s.getDefColor();
         obj.addProperty("r", c.getRed());
         obj.addProperty("g", c.getGreen());
         obj.addProperty("elementCodec", c.getBlue());
         if (s.getAlpha() != null) {
            obj.addProperty("alpha", s.getAlpha().getValue());
         }

         return obj;
      } else if (!(setting instanceof MultiBoxSetting s)) {
         return null;
      } else {
         JsonObject obj = new JsonObject();

         for(BooleanSetting bs : s.getBooleanSettings()) {
            obj.addProperty(bs.getName(), bs.getValue());
         }

         return obj;
      }
   }

   public void loadAll() {
      for(Module module : this.moduleManager.getModules()) {
         this.load(module);
      }

   }

   @Compile
   public void load(Module module) {
      String name = module.getModuleInfo().name();
      Path var10000 = this.configDir;
      String var10001 = this.sanitize(name);
      Path file = var10000.resolve(var10001 + ".json");
      if (Files.exists(file, new LinkOption[0])) {
         try {
            Reader reader = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8);

            try {
               this.applyModuleJson(module, JsonParser.parseReader(reader).getAsJsonObject());
            } catch (Throwable var8) {
               try {
                  reader.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }

               throw var8;
            }

            reader.close();
         } catch (Exception e) {
            System.err.println("[ConfigManager] Failed to load " + name + ": " + e.getMessage());
         }

      }
   }

   @Compile
   private void deserializeSetting(Setting<?> setting, JsonElement element) {
      try {
         if (setting instanceof BooleanSetting s) {
            s.setValue(element.getAsBoolean());
         } else if (setting instanceof FloatSetting s) {
            s.setValue(element.getAsFloat());
         } else if (setting instanceof ModeSetting s) {
            s.setValue(element.getAsString());
         } else if (setting instanceof KeySetting s) {
            s.setValue(element.getAsInt());
         } else if (setting instanceof StringSetting s) {
            ;
         } else if (setting instanceof ColorSetting s) {
            JsonObject obj = element.getAsJsonObject();
            int r = obj.get("r").getAsInt();
            int g = obj.get("g").getAsInt();
            int b = obj.get("elementCodec").getAsInt();
            s.setColor(new Color(r, g, b));
            if (obj.has("alpha") && s.getAlpha() != null) {
               s.getAlpha().setValue(obj.get("alpha").getAsFloat());
            }
         } else if (setting instanceof MultiBoxSetting s) {
            JsonObject obj = element.getAsJsonObject();

            for(BooleanSetting bs : s.getBooleanSettings()) {
               if (obj.has(bs.getName())) {
                  bs.setValue(obj.get(bs.getName()).getAsBoolean());
               }
            }
         }
      } catch (Exception e) {
         PrintStream var10000 = System.err;
         String var10001 = setting.getName();
         var10000.println("[ConfigManager] Error deserializing setting '" + var10001 + "': " + e.getMessage());
      }

   }

   @Compile
   public void saveConfig(String configName) {
      JsonObject root = new JsonObject();

      for(Module module : this.moduleManager.getModules()) {
         root.add(module.getModuleInfo().name(), this.buildModuleJson(module));
      }

      Path var10001 = this.profilesDir;
      String var10002 = this.sanitize(configName);
      this.writeJson(var10001.resolve(var10002 + ".json"), root);
      System.out.println("[ConfigManager] Profile saved: " + configName);
   }

   @Compile
   public boolean loadConfig(String configName) {
      CrolClient.INSTANCE.getNotifyManager().setCanAddNotify(false);
      Path var10000 = this.profilesDir;
      String var10001 = this.sanitize(configName);
      Path file = var10000.resolve(var10001 + ".json");
      if (!Files.exists(file, new LinkOption[0])) {
         System.err.println("[ConfigManager] Profile not found: " + configName);
         return false;
      } else {
         try {
            Reader reader = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8);

            boolean var11;
            try {
               JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

               for(Module module : this.moduleManager.getModules()) {
                  String mName = module.getModuleInfo().name();
                  if (root.has(mName)) {
                     this.applyModuleJson(module, root.getAsJsonObject(mName));
                  }
               }

               CrolClient.INSTANCE.getNotifyManager().setCanAddNotify(true);
               System.out.println("[ConfigManager] Profile loaded: " + configName);
               var11 = true;
            } catch (Throwable var9) {
               try {
                  reader.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }

               throw var9;
            }

            reader.close();
            return var11;
         } catch (Exception e) {
            System.err.println("[ConfigManager] Failed to load profile '" + configName + "': " + e.getMessage());
            CrolClient.INSTANCE.getNotifyManager().setCanAddNotify(true);
            return false;
         }
      }
   }

   @Compile
   public boolean deleteConfig(String configName) {
      try {
         Path var10000 = this.profilesDir;
         String var10001 = this.sanitize(configName);
         return Files.deleteIfExists(var10000.resolve(var10001 + ".json"));
      } catch (IOException e) {
         System.err.println("[ConfigManager] Failed to delete profile '" + configName + "': " + e.getMessage());
         return false;
      }
   }

   public List<String> listConfigs() {
      List<String> names = new ArrayList();

      try {
         DirectoryStream<Path> stream = Files.newDirectoryStream(this.profilesDir, "*.json");

         try {
            for(Path p : stream) {
               String fname = p.getFileName().toString();
               names.add(fname.substring(0, fname.length() - 5));
            }
         } catch (Throwable var7) {
            if (stream != null) {
               try {
                  stream.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (stream != null) {
            stream.close();
         }
      } catch (IOException e) {
         System.err.println("[ConfigManager] Failed to list profiles: " + e.getMessage());
      }

      return names;
   }

   @Compile
   private JsonObject buildModuleJson(Module module) {
      JsonObject obj = new JsonObject();
      obj.addProperty("enabled", module.isEnabled());
      obj.addProperty("bind", module.getBind());
      obj.addProperty("binded", module.isBinded());
      JsonObject settingsObj = new JsonObject();

      for(Setting<?> setting : module.getSettings()) {
         JsonElement el = this.serializeSetting(setting);
         if (el != null) {
            settingsObj.add(setting.getName(), el);
         }
      }

      obj.add("settings", settingsObj);
      return obj;
   }

   @Compile
   private void applyModuleJson(Module module, JsonObject obj) {
      if (obj.has("bind")) {
         module.setBind(obj.get("bind").getAsInt());
      }

      if (obj.has("binded")) {
         module.setBinded(obj.get("binded").getAsBoolean());
      }

      boolean shouldEnable = obj.has("enabled") && obj.get("enabled").getAsBoolean();
      if (module.isEnabled()) {
         module.setEnabled(false);
      }

      if (obj.has("settings")) {
         JsonObject settingsObj = obj.getAsJsonObject("settings");

         for(Setting<?> setting : module.getSettings()) {
            if (settingsObj.has(setting.getName())) {
               this.deserializeSetting(setting, settingsObj.get(setting.getName()));
            }
         }
      }

      if (shouldEnable) {
         module.setEnabled(true);
      }

   }

   @Compile
   private void writeJson(Path path, JsonObject data) {
      try {
         Writer writer = new OutputStreamWriter(Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING), StandardCharsets.UTF_8);

         try {
            GSON.toJson(data, writer);
         } catch (Throwable var7) {
            try {
               writer.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }

            throw var7;
         }

         writer.close();
      } catch (IOException e) {
         System.err.println("[ConfigManager] Write error: " + e.getMessage());
      }

   }

   private String sanitize(String name) {
      return name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
   }

   @Compile
   public void saveDraggables() {
      if (this.draggableManager != null) {
         JsonObject root = new JsonObject();

         for(Draggable d : this.draggableManager.getDraggables()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("x", d.getX());
            obj.addProperty("y", d.getY());
            root.add(d.getName(), obj);
         }

         this.writeJson(this.configDir.resolve("draggables.json"), root);
      }
   }

   @Compile
   public void loadDraggables() {
      if (this.draggableManager != null) {
         Path file = this.configDir.resolve("draggables.json");
         if (Files.exists(file, new LinkOption[0])) {
            try {
               Reader reader = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8);

               try {
                  JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

                  for(Draggable d : this.draggableManager.getDraggables()) {
                     if (root.has(d.getName())) {
                        JsonObject obj = root.getAsJsonObject(d.getName());
                        if (obj.has("x")) {
                           d.setX(obj.get("x").getAsInt());
                        }

                        if (obj.has("y")) {
                           d.setY(obj.get("y").getAsInt());
                        }
                     }
                  }
               } catch (Throwable var8) {
                  try {
                     reader.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }

                  throw var8;
               }

               reader.close();
            } catch (Exception e) {
               System.err.println("[ConfigManager] Failed to load draggables: " + e.getMessage());
            }

         }
      }
   }

   @Compile
   public void saveTheme() {
      if (this.themeManager != null) {
         Color c = this.themeManager.getTheme().color();
         JsonObject obj = new JsonObject();
         obj.addProperty("r", c.getRed());
         obj.addProperty("g", c.getGreen());
         obj.addProperty("elementCodec", c.getBlue());
         obj.addProperty("keyCodec", c.getAlpha());
         this.writeJson(this.configDir.resolve("theme.json"), obj);
      }
   }

   @Compile
   public void loadTheme() {
      if (this.themeManager != null) {
         Path file = this.configDir.resolve("theme.json");
         if (Files.exists(file, new LinkOption[0])) {
            try {
               Reader reader = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8);

               try {
                  JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                  int r = obj.get("r").getAsInt();
                  int g = obj.get("g").getAsInt();
                  int b = obj.get("elementCodec").getAsInt();
                  int a = obj.has("keyCodec") ? obj.get("keyCodec").getAsInt() : 255;
                  this.themeManager.setTheme(new Theme(new Color(r, g, b, a)));
               } catch (Throwable var9) {
                  try {
                     reader.close();
                  } catch (Throwable var8) {
                     var9.addSuppressed(var8);
                  }

                  throw var9;
               }

               reader.close();
            } catch (Exception e) {
               System.err.println("[ConfigManager] Failed to load theme: " + e.getMessage());
            }

         }
      }
   }

   @Compile
   public void saveNickNames() {
      JsonObject root = new JsonObject();
      if (CrolClient.INSTANCE.getNickNameManager().getCurrentNickName() != null) {
         JsonObject current = new JsonObject();
         current.addProperty("nickname", CrolClient.INSTANCE.getNickNameManager().getCurrentNickName().getNickname());
         current.addProperty("tag", CrolClient.INSTANCE.getNickNameManager().getCurrentNickName().getTag());
         root.add("current", current);
      }

      JsonArray array = new JsonArray();

      for(NickName n : CrolClient.INSTANCE.getNickNameManager().getNickNames()) {
         JsonObject obj = new JsonObject();
         obj.addProperty("nickname", n.getNickname());
         obj.addProperty("tag", n.getTag());
         array.add(obj);
      }

      root.add("nicknames", array);
      this.writeJson(this.configDir.resolve("nicknames.json"), root);
   }

   @Compile
   public void loadNickNames() {
      Path file = this.configDir.resolve("nicknames.json");
      if (Files.exists(file, new LinkOption[0])) {
         try {
            Reader reader = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8);

            try {
               JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
               CrolClient.INSTANCE.getNickNameManager().getNickNames().clear();
               if (root.has("nicknames")) {
                  for(JsonElement el : root.getAsJsonArray("nicknames")) {
                     JsonObject obj = el.getAsJsonObject();
                     String nickname = obj.get("nickname").getAsString();
                     String tag = obj.has("tag") ? obj.get("tag").getAsString() : "";
                     CrolClient.INSTANCE.getNickNameManager().getNickNames().add(new NickName(nickname, tag));
                  }
               }

               if (root.has("current")) {
                  JsonObject cur = root.getAsJsonObject("current");
                  String nickname = cur.get("nickname").getAsString();
                  String tag = cur.has("tag") ? cur.get("tag").getAsString() : "";
                  CrolClient.INSTANCE.getNickNameManager().setCurrentNickName((NickName)CrolClient.INSTANCE.getNickNameManager().getNickNames().stream().filter((n) -> n.getNickname().equals(nickname) && n.getTag().equals(tag)).findFirst().orElse(new NickName(nickname, tag)));
                  if (!nickname.isEmpty()) {
                     IMinecraftClientMixin instance = (IMinecraftClientMixin)MinecraftClient.getInstance();
                     Session session = MinecraftClient.getInstance().getSession();
                     instance.setSession(new Session(nickname, UUID.randomUUID(), session.getAccessToken(), session.getXuid(), session.getClientId(), AccountType.LEGACY));
                  }
               }
            } catch (Throwable var10) {
               try {
                  reader.close();
               } catch (Throwable var9) {
                  var10.addSuppressed(var9);
               }

               throw var10;
            }

            reader.close();
         } catch (Exception e) {
            System.err.println("[NickNameManager] Failed to load nicknames: " + e.getMessage());
         }

      }
   }
}
