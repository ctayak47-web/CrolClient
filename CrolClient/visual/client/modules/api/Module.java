
package crol.client.modules.api;

import com.darkmagician6.eventapi.EventManager;
import com.google.gson.JsonObject;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Generated;
import org.jetbrains.annotations.NotNull;
import crol.client.base.events.impl.other.EventModuleToggle;
import crol.client.modules.api.Category;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.Setting;
import crol.client.utility.interfaces.IClient;
import crol.client.utility.text.UiTranslation;

public class Module
implements IClient,
Comparable<Module> {
    protected ModuleAnnotation info = this.getClass().getAnnotation(ModuleAnnotation.class);
    private String name = this.info.name();
    private final Category category = this.info.category();
    private volatile boolean enabled = false;
    private int keyCode = -1;

    protected Module() {
    }

    public void setToggled(boolean state) {
        if (state) {
            this.onEnable();
        } else {
            this.onDisable();
        }
        this.enabled = state;
    }

    public void toggle() {
        boolean bl = this.enabled = !this.enabled;
        if (this.enabled) {
            this.onEnable();
        } else {
            this.onDisable();
        }
    }

    public void onEnable() {
        EventManager.register(this);
        EventManager.call(new EventModuleToggle(this, this.enabled));
    }

    public void onDisable() {
        EventManager.unregister(this);
        EventManager.call(new EventModuleToggle(this, this.enabled));
    }

    public List<Setting> getSettings() {
        ArrayList<Setting> settings = new ArrayList<Setting>();
        for (Class<?> clazz = this.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
                if (Modifier.isStatic(field.getModifiers())) {
                    return;
                }
                try {
                    Setting setting;
                    field.setAccessible(true);
                    Object value = field.get(this);
                    if (value instanceof Setting && !settings.contains(setting = (Setting)value)) {
                        settings.add(setting);
                    }
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        }
        return settings;
    }

    public JsonObject save() {
        JsonObject object = new JsonObject();
        object.addProperty("enabled", this.enabled);
        object.addProperty("keyCode", this.keyCode);
        JsonObject propertiesObject = new JsonObject();
        for (Setting setting : this.getSettings()) {
            setting.safe(propertiesObject);
        }
        object.add("Settings", propertiesObject);
        return object;
    }

    public void load(JsonObject object) {
        try {
            if (object != null) {
                if (object.has("enabled")) {
                    boolean enable = object.get("enabled").getAsBoolean();
                    if (enable && !this.isEnabled()) {
                        this.toggle();
                    }
                    if (!enable && this.isEnabled()) {
                        this.toggle();
                    }
                }
                if (object.has("keyCode")) {
                    this.keyCode = object.get("keyCode").getAsInt();
                }
                for (Setting setting : this.getSettings()) {
                    String valueOf = setting.getName();
                    JsonObject propertiesObject = object.getAsJsonObject("Settings");
                    if (propertiesObject == null || !propertiesObject.has(valueOf)) continue;
                    setting.load(propertiesObject);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int compareTo(@NotNull Module o) {
        return o.getName().compareTo(this.name);
    }

    public String[] getLegacyNames() {
        return new String[0];
    }

    public String getDisplayName() {
        return this.name;
    }

    public String getDisplayDescription() {
        return this.info == null ? "" : UiTranslation.translate(this.info.description());
    }

    @Generated
    public ModuleAnnotation getInfo() {
        return this.info;
    }

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public Category getCategory() {
        return this.category;
    }

    @Generated
    public boolean isEnabled() {
        return this.enabled;
    }

    @Generated
    public int getKeyCode() {
        return this.keyCode;
    }

    @Generated
    public void setInfo(ModuleAnnotation info) {
        this.info = info;
    }

    @Generated
    public void setName(String name) {
        this.name = name;
    }

    @Generated
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Generated
    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    @Generated
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Module)) {
            return false;
        }
        Module other = (Module)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.isEnabled() != other.isEnabled()) {
            return false;
        }
        if (this.getKeyCode() != other.getKeyCode()) {
            return false;
        }
        ModuleAnnotation this$info = this.getInfo();
        ModuleAnnotation other$info = other.getInfo();
        if (this$info == null ? other$info != null : !this$info.equals(other$info)) {
            return false;
        }
        String this$name = this.getName();
        String other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) {
            return false;
        }
        Category this$category = this.getCategory();
        Category other$category = other.getCategory();
        return !(this$category == null ? other$category != null : !((Object)((Object)this$category)).equals((Object)other$category));
    }

    @Generated
    protected boolean canEqual(Object other) {
        return other instanceof Module;
    }

    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + (this.isEnabled() ? 79 : 97);
        result = result * 59 + this.getKeyCode();
        ModuleAnnotation $info = this.getInfo();
        result = result * 59 + ($info == null ? 43 : $info.hashCode());
        String $name = this.getName();
        result = result * 59 + ($name == null ? 43 : $name.hashCode());
        Category $category = this.getCategory();
        result = result * 59 + ($category == null ? 43 : ((Object)((Object)$category)).hashCode());
        return result;
    }

    @Generated
    public String toString() {
        return "Module(info=" + String.valueOf(this.getInfo()) + ", name=" + this.getName() + ", category=" + String.valueOf((Object)this.getCategory()) + ", enabled=" + this.isEnabled() + ", keyCode=" + this.getKeyCode() + ")";
    }
}

