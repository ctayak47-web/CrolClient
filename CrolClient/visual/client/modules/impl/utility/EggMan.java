
package crol.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import com.google.gson.JsonObject;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import net.minecraft.LivingEntity;
import net.minecraft.PlayerEntity;
import net.minecraft.MathHelper;
import net.minecraft.MatrixStack;
import net.minecraft.InventoryScreen;
import crol.client.base.events.impl.player.EventUpdate;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.impl.BooleanSetting;
import crol.client.modules.api.setting.impl.NumberSetting;

@ModuleAnnotation(name="EggMan", category=Category.MOVEMENT, description="Эффект покачивания игроков с опциональной музыкой.")
public final class EggMan
extends Module {
    public static final EggMan INSTANCE = new EggMan();
    private static final String MUSIC_FILE = "eggman.wav";
    private static final long LOAD_RETRY_DELAY_MS = 3000L;
    private static final String SETTING_FLEX_MUSIC = "Флекс-музыка";
    private static final String SETTING_MUSIC_VOLUME = "Громкость музыки";
    private final BooleanSetting flexMusic = new BooleanSetting("Флекс-музыка", true);
    private final NumberSetting musicVolume = new NumberSetting("Громкость музыки", 25.0f, 0.0f, 100.0f, 1.0f, this.flexMusic::isEnabled);
    private Clip musicClip;
    private float prevVolume = -1.0f;
    private long nextLoadAttemptMs;

    private EggMan() {
    }

    @Override
    public void load(JsonObject object) {
        this.migrateLegacySettings(object);
        super.load(object);
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        this.updateMusic();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.updateMusic();
    }

    @Override
    public void onDisable() {
        this.stopMusic();
        super.onDisable();
    }

    public boolean shouldWobble(LivingEntity entity) {
        if (!this.isEnabled() || !(entity instanceof PlayerEntity)) {
            return false;
        }
        return entity != EggMan.mc.player || !(EggMan.mc.currentScreen instanceof InventoryScreen);
    }

    public void applyWobbleScale(LivingEntity entity, MatrixStack matrices) {
        if (!(entity instanceof PlayerEntity)) {
            return;
        }
        long now = System.currentTimeMillis() + (long)entity.getId() * 100L;
        float wobble = (float)(now % 400L) / 400.0f;
        wobble = (wobble > 0.5f ? 1.0f - wobble : wobble) * 2.0f;
        wobble = MathHelper.clamp((float)wobble, (float)0.0f, (float)1.0f);
        float xzScale = wobble * 2.0f + 1.0f;
        float yScale = 1.0f - 0.5f * wobble;
        matrices.scale(xzScale, yScale, xzScale);
    }

    private void updateMusic() {
        if (!this.isEnabled() || !this.flexMusic.isEnabled()) {
            this.stopMusic();
            return;
        }
        Clip clip = this.ensureClipLoaded();
        if (clip == null) {
            return;
        }
        this.applyVolume(clip);
        if (!clip.isRunning()) {
            clip.loop(-1);
            clip.start();
        }
    }

    /*
     * Exception decompiling
     */
    private Clip ensureClipLoaded() {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Started 2 blocks at once
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    private void applyVolume(Clip clip) {
        float dB;
        float currentVolume = this.musicVolume.getCurrent();
        if (Math.abs(currentVolume - this.prevVolume) < 1.0E-4f) {
            return;
        }
        this.prevVolume = currentVolume;
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl control = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
        float volumeLinear = Math.max(0.0f, Math.min(1.0f, currentVolume / 100.0f));
        float min = control.getMinimum();
        float max = control.getMaximum();
        float f = dB = volumeLinear <= 0.0f ? min : (float)(20.0 * Math.log10(volumeLinear));
        if (dB < min) {
            dB = min;
        }
        if (dB > max) {
            dB = max;
        }
        control.setValue(dB);
    }

    private void stopMusic() {
        if (this.musicClip == null) {
            return;
        }
        try {
            this.musicClip.stop();
            this.musicClip.flush();
            this.musicClip.close();
        }
        catch (Exception exception) {
        }
        finally {
            this.musicClip = null;
            this.prevVolume = -1.0f;
        }
    }

    private void migrateLegacySettings(JsonObject object) {
        if (object == null || !object.has("Settings") || !object.get("Settings").isJsonObject()) {
            return;
        }
        JsonObject settings = object.getAsJsonObject("Settings");
        this.migrateSettingKey(settings, "Flex Music", SETTING_FLEX_MUSIC);
        this.migrateSettingKey(settings, "Music Volume", SETTING_MUSIC_VOLUME);
    }

    private void migrateSettingKey(JsonObject settings, String legacyName, String newName) {
        if (!settings.has(newName) && settings.has(legacyName)) {
            settings.add(newName, settings.get(legacyName).deepCopy());
        }
    }
}

