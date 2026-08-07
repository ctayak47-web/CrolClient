
package vurst.visual.client.modules.impl.render;

import net.minecraft.LightmapTextureManager;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.ModeSetting;
import vurst.visual.utility.mixin.accessors.LightmapTextureManagerAccessor;

@ModuleAnnotation(name="FullBright", category=Category.RENDER, description="Повышает яркость мира.")
public final class FullBright
extends Module {
    public static final FullBright INSTANCE = new FullBright();
    private static final String MODE_GAMMA = "Гамма";
    private static final String MODE_NIGHT_VISION = "Ночное Зрение";
    private static final float FULL_GAMMA_BRIGHTNESS_FACTOR = 10000.0f;
    private final ModeSetting mode = new ModeSetting("Режим", "Гамма", "Ночное Зрение");

    private FullBright() {
    }

    public boolean isNightVisionMode() {
        return this.isEnabled() && this.mode.is(MODE_NIGHT_VISION);
    }

    public boolean isGammaMode() {
        return this.isEnabled() && this.mode.is(MODE_GAMMA);
    }

    public boolean shouldForceNightVisionStrength() {
        return this.isNightVisionMode();
    }

    public float getForcedGammaBrightnessFactor(float originalFactor) {
        if (!this.isGammaMode()) {
            return originalFactor;
        }
        return Math.max(originalFactor, 10000.0f);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.markLightmapDirty();
    }

    @Override
    public void onDisable() {
        this.markLightmapDirty();
        super.onDisable();
    }

    private void markLightmapDirty() {
        if (mc == null || FullBright.mc.gameRenderer == null) {
            return;
        }
        LightmapTextureManager lightmapTextureManager = FullBright.mc.gameRenderer.getLightmapTextureManager();
        if (lightmapTextureManager == null) {
            return;
        }
        ((LightmapTextureManagerAccessor)lightmapTextureManager).vurstvisual$setDirty(true);
    }
}

