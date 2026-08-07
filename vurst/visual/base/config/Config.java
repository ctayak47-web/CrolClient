
package vurst.visual.base.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import lombok.Generated;
import vurst.visual.VurstVisual;
import vurst.visual.base.config.ConfigManager;
import vurst.visual.base.theme.Theme;
import vurst.visual.base.theme.ThemeManager;
import vurst.visual.client.modules.api.Module;
import vurst.visual.utility.culling.EntityCullingManager;
import vurst.visual.utility.render.display.base.color.ColorRGBA;

public class Config {
    private final String name;
    private final File file;

    public Config(String name) {
        this(name, ConfigManager.getConfigFile(name), true);
    }

    public Config(String name, File file, boolean create) {
        this.name = name;
        this.file = file;
        if (create && !file.exists()) {
            try {
                file.createNewFile();
            }
            catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public JsonObject save() {
        try {
            JsonObject root = new JsonObject();
            JsonObject modulesObject = new JsonObject();
            for (Module module : VurstVisual.getInstance().getModuleManager().getModules()) {
                modulesObject.add(module.getName(), module.save());
            }
            root.add("Modules", modulesObject);
            ThemeManager themeManager = VurstVisual.getInstance().getThemeManager();
            JsonObject themeObject = new JsonObject();
            themeObject.addProperty("selected", themeManager.getCurrentTheme().getName());
            themeObject.addProperty("columns", VurstVisual.getInstance().getMenuScreen().getColumns());
            JsonObject items = new JsonObject();
            for (Theme t : themeManager.getThemes()) {
                items.add(t.getName(), this.serializeTheme(t));
            }
            themeObject.add("items", items);
            root.add("Theme", themeObject);
            root.add("entityCulling", EntityCullingManager.getInstance().save());
            return root;
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public void load(JsonObject object) {
        if (object.has("Theme")) {
            JsonObject themeObject = object.getAsJsonObject("Theme");
            if (themeObject.has("selected")) {
                String selected = themeObject.get("selected").getAsString();
                VurstVisual.getInstance().getThemeManager().switchThemeByName(selected);
            }
            if (themeObject.has("columns")) {
                int columns = themeObject.get("columns").getAsInt();
                VurstVisual.getInstance().getMenuScreen().setColumns(columns);
            }
            ThemeManager themeManager = VurstVisual.getInstance().getThemeManager();
            if (themeObject.has("items")) {
                JsonObject items = themeObject.getAsJsonObject("items");
                for (Theme t : themeManager.getThemes()) {
                    if (!items.has(t.getName())) continue;
                    JsonObject tObj = items.getAsJsonObject(t.getName());
                    this.applyThemeFromJson(t, tObj);
                }
            }
        }
        if (object.has("entityCulling")) {
            EntityCullingManager.getInstance().load(object.getAsJsonObject("entityCulling"));
        }
        if (object.has("Modules")) {
            try {
                JsonObject modulesObject = object.getAsJsonObject("Modules");
                for (Module module : VurstVisual.getInstance().getModuleManager().getModules()) {
                    JsonObject moduleObject = modulesObject.getAsJsonObject(module.getName());
                    if (moduleObject == null) {
                        for (String legacy : module.getLegacyNames()) {
                            if (!modulesObject.has(legacy)) continue;
                            moduleObject = modulesObject.getAsJsonObject(legacy);
                            break;
                        }
                    }
                    module.load(moduleObject);
                }
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    private JsonObject serializeTheme(Theme theme) {
        JsonObject o = new JsonObject();
        this.putColor(o, "color", theme.getColor());
        this.putColor(o, "secondColor", theme.getSecondColorRaw());
        this.putColor(o, "friendColor", theme.getFriendColor());
        this.putColor(o, "gray", theme.getGray());
        this.putColor(o, "grayLight", theme.getGrayLight());
        this.putColor(o, "foregroundLight", theme.getForegroundLight());
        this.putColor(o, "whiteGray", theme.getWhiteGray());
        this.putColor(o, "foregroundGray", theme.getForegroundGray());
        this.putColor(o, "foregroundLightStroke", theme.getForegroundLightStroke());
        this.putColor(o, "foregroundColor", theme.getForegroundColor());
        this.putColor(o, "foregroundStroke", theme.getForegroundStroke());
        this.putColor(o, "foregroundDark", theme.getForegroundDark());
        this.putColor(o, "white", theme.getWhite());
        this.putColor(o, "backgroundColor", theme.getBackgroundColor());
        o.addProperty("glow", theme.isGlow());
        o.addProperty("blur", theme.isBlur());
        o.addProperty("corners", theme.isCorners());
        o.addProperty("useSecondColor", theme.isUseSecondColor());
        return o;
    }

    private void applyThemeFromJson(Theme theme, JsonObject obj) {
        ColorRGBA val = this.getColor(obj, "color");
        if (val != null) {
            theme.setColor(val);
        }
        if ((val = this.getColor(obj, "secondColor")) != null) {
            theme.setSecondColor(val);
        }
        if ((val = this.getColor(obj, "friendColor")) != null) {
            theme.setFriendColor(val);
        }
        if ((val = this.getColor(obj, "gray")) != null) {
            theme.setGray(val);
        }
        if ((val = this.getColor(obj, "grayLight")) != null) {
            theme.setGrayLight(val);
        }
        if ((val = this.getColor(obj, "foregroundLight")) != null) {
            theme.setForegroundLight(val);
        }
        if ((val = this.getColor(obj, "whiteGray")) != null) {
            theme.setWhiteGray(val);
        }
        if ((val = this.getColor(obj, "foregroundGray")) != null) {
            theme.setForegroundGray(val);
        }
        if ((val = this.getColor(obj, "foregroundLightStroke")) != null) {
            theme.setForegroundLightStroke(val);
        }
        if ((val = this.getColor(obj, "foregroundColor")) != null) {
            theme.setForegroundColor(val);
        }
        if ((val = this.getColor(obj, "foregroundStroke")) != null) {
            theme.setForegroundStroke(val);
        }
        if ((val = this.getColor(obj, "foregroundDark")) != null) {
            theme.setForegroundDark(val);
        }
        if ((val = this.getColor(obj, "white")) != null) {
            theme.setWhite(val);
        }
        if ((val = this.getColor(obj, "backgroundColor")) != null) {
            theme.setBackgroundColor(val);
        }
        if (obj.has("glow")) {
            theme.setGlow(this.getBool(obj, "glow", false));
        }
        if (obj.has("blur")) {
            theme.setBlur(this.getBool(obj, "blur", false));
        }
        if (obj.has("corners")) {
            theme.setCorners(this.getBool(obj, "corners", false));
        }
        if (obj.has("useSecondColor")) {
            theme.setUseSecondColor(this.getBool(obj, "useSecondColor", true));
        }
    }

    private boolean hasAnyLegacyThemeFields(JsonObject o) {
        return o.has("color") || o.has("secondColor") || o.has("backgroundColor");
    }

    private void putColor(JsonObject obj, String key, ColorRGBA color) {
        if (color != null) {
            obj.addProperty(key, color.getRGB());
        }
    }

    private ColorRGBA getColor(JsonObject obj, String key) {
        return obj.has(key) ? new ColorRGBA(obj.get(key).getAsInt()) : null;
    }

    private boolean getBool(JsonObject obj, String key, boolean def) {
        JsonElement el = obj.get(key);
        return el == null || el.isJsonNull() ? def : el.getAsBoolean();
    }

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public File getFile() {
        return this.file;
    }
}

