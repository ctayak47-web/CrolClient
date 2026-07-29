package com.crolclient.config;

import com.google.gson.annotations.SerializedName;

public class CrolConfig {
    // Existing
    @SerializedName("fullbright_enabled") public boolean fullbrightEnabled = false;
    @SerializedName("auto_sprint_enabled") public boolean autoSprintEnabled = false;
    @SerializedName("auto_eat_enabled") public boolean autoEatEnabled = false;
    @SerializedName("auto_eat_threshold") public int autoEatThreshold = 6;
    @SerializedName("menu_blur_enabled") public boolean menuBlurEnabled = true;
    @SerializedName("glass_opacity") public float glassOpacity = 0.65f;

    // HUD
    @SerializedName("hud_watermark_enabled") public boolean hudWatermarkEnabled = true;
    @SerializedName("hud_arraylist_enabled") public boolean hudArraylistEnabled = true;
    @SerializedName("hud_coords_enabled") public boolean hudCoordsEnabled = true;
    @SerializedName("hud_fps_enabled") public boolean hudFpsEnabled = true;
    @SerializedName("hud_ping_enabled") public boolean hudPingEnabled = true;
    @SerializedName("hud_color") public int hudColor = 0xFF6C5CE7;

    // Visual
    @SerializedName("target_esp_enabled") public boolean targetEspEnabled = false;
    @SerializedName("target_esp_mode") public String targetEspMode = "Box";
    @SerializedName("custom_particles_enabled") public boolean customParticlesEnabled = false;
    @SerializedName("custom_particle_mode") public String customParticleMode = "heart";
    @SerializedName("custom_sky_enabled") public boolean customSkyEnabled = false;
    @SerializedName("custom_sky_mode") public String customSkyMode = "sky";
    @SerializedName("custom_cape_enabled") public boolean customCapeEnabled = false;
    @SerializedName("custom_cape_mode") public String customCapeMode = "cape";
    @SerializedName("custom_background_enabled") public boolean customBackgroundEnabled = false;
    @SerializedName("custom_background_mode") public String customBackgroundMode = "1";
    @SerializedName("custom_glow_enabled") public boolean customGlowEnabled = false;
    @SerializedName("custom_glow_mode") public String customGlowMode = "glow";
    @SerializedName("custom_trails_enabled") public boolean customTrailsEnabled = false;
    @SerializedName("custom_trail_mode") public String customTrailMode = "firefly";

    // Combat
    @SerializedName("custom_hit_sound_enabled") public boolean customHitSoundEnabled = false;
    @SerializedName("custom_hit_sound_mode") public String customHitSoundMode = "hit1";
    @SerializedName("custom_death_sound_enabled") public boolean customDeathSoundEnabled = false;
    @SerializedName("custom_death_sound_mode") public String customDeathSoundMode = "death";

    // Player
    @SerializedName("custom_skin_enabled") public boolean customSkinEnabled = false;
    @SerializedName("custom_skin_mode") public String customSkinMode = "amogus";
    @SerializedName("custom_arrow_enabled") public boolean customArrowEnabled = false;
    @SerializedName("custom_arrow_mode") public String customArrowMode = "default";
    @SerializedName("jump_effect_enabled") public boolean jumpEffectEnabled = false;

    // Sounds
    @SerializedName("ui_sounds_enabled") public boolean uiSoundsEnabled = true;
    @SerializedName("low_hp_sound_enabled") public boolean lowHpSoundEnabled = false;

    // ViewModel
    @SerializedName("viewmodel_enabled")
    public boolean viewmodelEnabled = false;
    @SerializedName("viewmodel_pos_x")
    public float viewmodelPosX = 0.0f;
    @SerializedName("viewmodel_pos_y")
    public float viewmodelPosY = 0.0f;
    @SerializedName("viewmodel_pos_z")
    public float viewmodelPosZ = 0.0f;
    @SerializedName("viewmodel_rot_x")
    public float viewmodelRotX = 0.0f;
    @SerializedName("viewmodel_rot_y")
    public float viewmodelRotY = 0.0f;
    @SerializedName("viewmodel_rot_z")
    public float viewmodelRotZ = 0.0f;
    @SerializedName("viewmodel_scale")
    public float viewmodelScale = 1.0f;

    // Swing Animation
    @SerializedName("swing_animation_enabled")
    public boolean swingAnimationEnabled = false;
    @SerializedName("swing_animation_mode")
    public String swingAnimationMode = "1.7";
}
