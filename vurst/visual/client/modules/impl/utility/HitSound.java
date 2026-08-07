
package vurst.visual.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import com.google.gson.JsonObject;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import net.minecraft.StatusEffects;
import net.minecraft.LivingEntity;
import net.minecraft.Identifier;
import net.minecraft.Resource;
import net.minecraft.SoundEvent;
import net.minecraft.SoundEvents;
import vurst.visual.VurstVisual;
import vurst.visual.base.events.impl.player.EventAttack;
import vurst.visual.base.events.impl.player.EventUpdate;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.BooleanSetting;
import vurst.visual.client.modules.api.setting.impl.ModeSetting;
import vurst.visual.client.modules.api.setting.impl.NumberSetting;

@ModuleAnnotation(name="Hit Sound", category=Category.MOVEMENT, description="Проигрывает звук при каждом ударе.")
public final class HitSound
extends Module {
    public static final HitSound INSTANCE = new HitSound();
    private static final String NONE = "Нет";
    private static final String SOUND_ROOT = "hitsounds";
    private static final String SOUND_PREFIX = "hitsounds/";
    private static final String EXT = ".wav";
    private static final String NAMESPACE = VurstVisual.id("hitsounds").getNamespace();
    private static final String SOUND_HIT1 = "hit1";
    private static final String SOUND_HIT2 = "hit2";
    private static final String SOUND_HIT3 = "hit3";
    private static final String LABEL_HIT1 = "Криты V1";
    private static final String LABEL_HIT2 = "Хруст";
    private static final String LABEL_HIT3 = "Криты";
    private static final String LEGACY_HIT3_LABEL = "РљСЂРёС‚С‹";
    private static final Map<String, String> LABELS = Map.ofEntries(Map.entry("bell", "Дзиньк"), Map.entry("bonk", "Сахууур"), Map.entry("bubble", "Барабулька"), Map.entry("click1", "Клик мыши"), Map.entry("click3", "Пульк"), Map.entry("pop", "Польк"), Map.entry("hit1", "Криты V1"), Map.entry("hit2", "Хруст"), Map.entry("hit3", "Криты"), Map.entry("moan1", "Стоны"), Map.entry("moan2", "Стоны v3"), Map.entry("moan3", "Стоны v2"), Map.entry("moan4", "Стоны v4"), Map.entry("uwu", "Ювю"));
    private String pendingSoundSelection;
    private final ModeSetting sound = new ModeSetting("Звук", new String[]{"Нет"}){

        @Override
        public void load(JsonObject propertiesObject) {
            String savedValue = propertiesObject.get(this.getName()).getAsString();
            if ("None".equals(savedValue)) {
                savedValue = HitSound.NONE;
            }
            this.set(savedValue);
            HitSound.this.pendingSoundSelection = !savedValue.equals(this.get()) ? savedValue : null;
        }
    };
    private final NumberSetting volume = new NumberSetting("Громкость", 100.0f, 0.0f, 100.0f, 5.0f);
    private final BooleanSetting critOnly = new BooleanSetting("Только при крите", false);
    private final Map<String, String> labelToFile = new HashMap<String, String>();
    private final Map<Identifier, byte[]> soundCache = new HashMap<Identifier, byte[]>();
    private final ExecutorService soundExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread2 = new Thread(r, "HitSound");
        thread2.setDaemon(true);
        return thread2;
    });
    private static final long ATTACK_SOUND_CAPTURE_WINDOW_MS = 250L;
    private static final long CRIT_ONLY_RESOLVE_DELAY_MS = 125L;
    private boolean soundsLoaded;
    private boolean attackSoundCaptured;
    private boolean lastAttackWasCriticalSound;
    private long lastAttackSoundCapturedAt;
    private boolean pendingCritOnlyPlayback;
    private String pendingCritOnlySound;
    private long pendingCritOnlyStartedAt;

    private HitSound() {
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        this.ensureSoundsLoaded();
        this.resolvePendingCritOnlyPlayback();
    }

    @EventTarget
    public void onAttack(EventAttack event) {
        if (event.getAction() == EventAttack.Action.PRE) {
            this.clearCapturedAttackSoundState();
            return;
        }
        if (event.getAction() != EventAttack.Action.POST || HitSound.mc.player == null || HitSound.mc.world == null) {
            return;
        }
        this.ensureSoundsLoaded();
        if (!(event.getTarget() instanceof LivingEntity)) {
            return;
        }
        boolean criticalHit = this.wasLastAttackCritical();
        String selected = this.sound.get();
        if (selected == null || selected.isEmpty() || NONE.equals(selected)) {
            this.clearCapturedAttackSoundState();
            return;
        }
        if (this.critOnly.isEnabled()) {
            if (!this.attackSoundCaptured) {
                this.pendingCritOnlyPlayback = true;
                this.pendingCritOnlySound = selected;
                this.pendingCritOnlyStartedAt = System.currentTimeMillis();
                return;
            }
            this.clearCapturedAttackSoundState();
            if (!criticalHit) {
                return;
            }
        } else {
            this.clearCapturedAttackSoundState();
        }
        String fileName = this.labelToFile.getOrDefault(selected, selected);
        this.playWav(VurstVisual.id(SOUND_PREFIX + fileName + EXT));
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.ensureSoundsLoaded();
    }

    private void ensureSoundsLoaded() {
        if (this.soundsLoaded) {
            return;
        }
        if (mc == null || mc.getResourceManager() == null) {
            return;
        }
        List<String> files = this.loadSoundNames();
        if (files.isEmpty()) {
            return;
        }
        for (String file : files) {
            String label = this.resolveLabel(file);
            this.labelToFile.put(label, file);
            this.addSoundOption(label);
        }
        this.applyPendingSoundSelection();
        this.soundsLoaded = true;
    }

    private List<String> loadSoundNames() {
        if (mc == null || mc.getResourceManager() == null) {
            return List.of();
        }
        Map resources = mc.getResourceManager().findResources(SOUND_ROOT, id -> NAMESPACE.equals(id.getNamespace()) && id.getPath().endsWith(EXT));
        if (resources.isEmpty()) {
            return List.of();
        }
        ArrayList<String> names = new ArrayList<String>();
        for (Identifier id2 : resources.keySet()) {
            String path = id2.getPath();
            if (path.startsWith(SOUND_PREFIX)) {
                path = path.substring(SOUND_PREFIX.length());
            }
            if (path.endsWith(EXT)) {
                path = path.substring(0, path.length() - EXT.length());
            }
            if (path.isEmpty()) continue;
            names.add(path);
        }
        names.sort(Comparator.naturalOrder());
        return names;
    }

    private void addSoundOption(String name) {
        boolean exists = this.sound.getValues().stream().anyMatch(value -> value.getName().equals(name));
        if (!exists) {
            new ModeSetting.Value(this.sound, name);
        }
    }

    private void applyPendingSoundSelection() {
        if (this.pendingSoundSelection == null || this.pendingSoundSelection.isEmpty()) {
            return;
        }
        String migratedSelection = this.migrateLegacySelection(this.pendingSoundSelection);
        if (migratedSelection != null && this.trySelectSound(migratedSelection)) {
            this.pendingSoundSelection = null;
            return;
        }
        if (this.trySelectSound(this.pendingSoundSelection)) {
            this.pendingSoundSelection = null;
            return;
        }
        String label = this.findLabelByFileName(this.pendingSoundSelection);
        if (label != null && this.trySelectSound(label)) {
            this.pendingSoundSelection = null;
        }
    }

    private boolean trySelectSound(String value) {
        this.sound.set(value);
        return value.equals(this.sound.get());
    }

    private String findLabelByFileName(String fileName) {
        for (Map.Entry<String, String> entry : this.labelToFile.entrySet()) {
            if (!fileName.equals(entry.getValue())) continue;
            return entry.getKey();
        }
        return null;
    }

    private String resolveLabel(String fileName) {
        Object label = LABELS.getOrDefault(fileName, fileName);
        if (this.labelToFile.containsKey(label) && !fileName.equals(this.labelToFile.get(label))) {
            label = (String)label + " (" + fileName + ")";
        }
        return label;
    }

    private String migrateLegacySelection(String selection) {
        return switch (selection) {
            case SOUND_HIT2 -> LABEL_HIT2;
            case SOUND_HIT3, LEGACY_HIT3_LABEL -> LABEL_HIT3;
            default -> null;
        };
    }

    private void playWav(Identifier id) {
        this.soundExecutor.execute(() -> {
            try {
                byte[] data = this.getSoundBytes(id);
                if (data == null || data.length == 0) {
                    return;
                }
                try (AudioInputStream stream = AudioSystem.getAudioInputStream(new BufferedInputStream(new ByteArrayInputStream(data)));){
                    Clip clip = AudioSystem.getClip();
                    clip.open(stream);
                    this.applyVolume(clip);
                    clip.addLineListener(event -> {
                        if (event.getType() == LineEvent.Type.STOP) {
                            clip.close();
                        }
                    });
                    clip.start();
                }
            }
            catch (Exception exception) {
                
            }
        });
    }

    private byte[] getSoundBytes(Identifier id) {
        byte[] byArray;
        block10: {
            byte[] cached = this.soundCache.get(id);
            if (cached != null) {
                return cached;
            }
            Optional resource = mc.getResourceManager().getResource(id);
            if (resource.isEmpty()) {
                return null;
            }
            InputStream inputStream = ((Resource)resource.get()).getInputStream();
            try {
                byte[] data = inputStream.readAllBytes();
                this.soundCache.put(id, data);
                byArray = data;
                if (inputStream == null) break block10;
            }
            catch (Throwable throwable) {
                try {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Exception ignored) {
                    return null;
                }
            }
            inputStream.close();
        }
        return byArray;
    }

    private boolean isCriticalHit() {
        if (HitSound.mc.player == null) {
            return false;
        }
        if (HitSound.mc.player.getAttackCooldownProgress(0.5f) <= 0.9f) {
            return false;
        }
        if (HitSound.mc.player.isOnGround()) {
            return false;
        }
        if (HitSound.mc.player.isClimbing()) {
            return false;
        }
        if (HitSound.mc.player.isTouchingWater() || HitSound.mc.player.isSubmergedInWater()) {
            return false;
        }
        if (HitSound.mc.player.isInLava()) {
            return false;
        }
        if (HitSound.mc.player.hasVehicle()) {
            return false;
        }
        if (HitSound.mc.player.isSprinting()) {
            return false;
        }
        if (HitSound.mc.player.getAbilities().flying || HitSound.mc.player.isGliding()) {
            return false;
        }
        if (HitSound.mc.player.hasStatusEffect(StatusEffects.BLINDNESS)) {
            return false;
        }
        return HitSound.mc.player.fallDistance > 0.0f;
    }

    private boolean wasLastAttackCritical() {
        if (this.attackSoundCaptured && System.currentTimeMillis() - this.lastAttackSoundCapturedAt <= 250L) {
            return this.lastAttackWasCriticalSound;
        }
        return this.isCriticalHit();
    }

    private void resolvePendingCritOnlyPlayback() {
        if (!this.pendingCritOnlyPlayback) {
            return;
        }
        long elapsed = System.currentTimeMillis() - this.pendingCritOnlyStartedAt;
        if (!this.attackSoundCaptured && elapsed < 125L) {
            return;
        }
        String selected = this.pendingCritOnlySound;
        this.pendingCritOnlyPlayback = false;
        this.pendingCritOnlySound = null;
        this.pendingCritOnlyStartedAt = 0L;
        boolean criticalHit = this.wasLastAttackCritical();
        this.clearCapturedAttackSoundState();
        if (!criticalHit || selected == null || selected.isEmpty() || NONE.equals(selected)) {
            return;
        }
        String fileName = this.labelToFile.getOrDefault(selected, selected);
        this.playWav(VurstVisual.id(SOUND_PREFIX + fileName + EXT));
    }

    private void clearCapturedAttackSoundState() {
        this.attackSoundCaptured = false;
        this.lastAttackWasCriticalSound = false;
        this.lastAttackSoundCapturedAt = 0L;
        this.pendingCritOnlyPlayback = false;
        this.pendingCritOnlySound = null;
        this.pendingCritOnlyStartedAt = 0L;
    }

    public void captureSuppressedAttackSound(SoundEvent sound) {
        if (!this.isAttackSound(sound)) {
            return;
        }
        this.attackSoundCaptured = true;
        this.lastAttackSoundCapturedAt = System.currentTimeMillis();
        if (sound == SoundEvents.ENTITY_PLAYER_ATTACK_CRIT) {
            this.lastAttackWasCriticalSound = true;
        }
    }

    private boolean isAttackSound(SoundEvent sound) {
        return sound == SoundEvents.ENTITY_PLAYER_ATTACK_STRONG || sound == SoundEvents.ENTITY_PLAYER_ATTACK_WEAK || sound == SoundEvents.ENTITY_PLAYER_ATTACK_CRIT || sound == SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP || sound == SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK || sound == SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE;
    }

    private void applyVolume(Clip clip) {
        float dB;
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl control = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
        float volumePercent = this.volume.getCurrent();
        float volumeLinear = Math.max(0.0f, Math.min(1.0f, volumePercent / 100.0f));
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

    public boolean shouldSuppressDefaultHits() {
        return this.isEnabled();
    }
}

