
package crol.client.modules.api.setting;

import com.google.gson.JsonObject;
import java.util.function.Supplier;
import lombok.Generated;
import crol.client.utility.text.UiTranslation;

public abstract class Setting {
    protected final String name;
    protected Supplier<Boolean> visible;

    public Setting(String name) {
        this.name = name;
        this.setVisible(() -> true);
    }

    public abstract void safe(JsonObject var1);

    public abstract void load(JsonObject var1);

    public boolean isVisible() {
        return this.visible.get();
    }

    public String getDisplayName() {
        return UiTranslation.translate(this.name);
    }

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public Supplier<Boolean> getVisible() {
        return this.visible;
    }

    @Generated
    public void setVisible(Supplier<Boolean> visible) {
        this.visible = visible;
    }
}

