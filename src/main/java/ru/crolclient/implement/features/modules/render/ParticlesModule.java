package ru.crolclient.implement.features.modules.render;

import ru.crolclient.api.event.EventHandler;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.*;
import ru.crolclient.implement.events.render.WorldRenderEvent;
import ru.crolclient.implement.features.modules.render.particles.ParticleInWorld;
import ru.crolclient.implement.features.modules.render.particles.ParticleTrails;

import java.util.ArrayList;
import java.util.List;

public class ParticlesModule extends Module {
    private final MultiSelectSetting particlesSetting = new MultiSelectSetting("Particles", "Select particle types")
            .value("Heart", "Lightning", "Point", "Rhombus", "Snow", "Star", "Cross", "Crown", "Line", "Dollar", "Triangle");

    private final List<ParticleTrails.ParticlePoint> particlePoints = new ArrayList<>();
    private final ParticleTrails particleTrails;

    private final GroupSetting trailsSettings = new GroupSetting("Trails", "Settings for particle trails")
            .settings(
                    new ValueSetting("Lifetime", "Particle lifetime in seconds").setValue(2.0F).range(0.5F, 5.0F).increment(0.1F),
                    new ValueSetting("Particle Count", "Number of particles to spawn").setValue(3).range(1, 10).increment(1),
                    new ValueSetting("Spread Strength", "Strength of particle spread").setValue(1.0F).range(0.0F, 3.0F).increment(0.1F),
                    new ValueSetting("Gravity Strength", "Strength of particle gravity").setValue(0.5F).range(0.0F, 2.0F).increment(0.1F),
                    new ColorSetting("Particle Color", "Color of the particles").presets(0x80FFFFFF, 0x80FF0000),
                    new BooleanSetting("Third Person Only", "Show particles only in third person view").setValue(false),
                    new BooleanSetting("Bloom", "Enable bloom/glow effect for particles").setValue(false)
            );

    private final GroupSetting worldSettings = new GroupSetting("In World", "Settings for particles in world")
            .settings(
                    new ValueSetting("Lifetime", "Particle lifetime in seconds").setValue(2.0F).range(0.5F, 5.0F).increment(0.1F),
                    new ValueSetting("Particle Count", "Number of particles to spawn").setValue(150).range(100, 1000).increment(50),
                    new ValueSetting("Spread Strength", "Strength of particle spread").setValue(1.0F).range(0.0F, 3.0F).increment(0.1F),
                    new ValueSetting("Gravity Strength", "Strength of particle gravity").setValue(0.5F).range(0.0F, 2.0F).increment(0.1F),
                    new ColorSetting("Particle Color", "Color of the particles").presets(0x80FFFFFF, 0x80FF0000),
                    new BooleanSetting("Bloom", "Enable bloom/glow effect for particles").setValue(false)
            );

    private final ParticleInWorld particleInWorld;

    public ParticlesModule() {
        super("Particles", "Particles", ModuleCategory.RENDER);
        setup(particlesSetting, trailsSettings, worldSettings);
        this.particleTrails = new ParticleTrails(this);
        this.particleInWorld = new ParticleInWorld(this);
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (mc.player == null) return;

        if (trailsSettings.isValue() &&
                !(((BooleanSetting)trailsSettings.getSubSetting("Third Person Only")).isValue()
                        && mc.options.getPerspective().isFirstPerson())) {
            particleTrails.onRender(event, particlePoints);
        }

        if (worldSettings.isValue()) {
            particleInWorld.onRender(event);
        }
    }

    public GroupSetting getWorldSettings() {
        return worldSettings;
    }

    @Override
    public void deactivate() {
        particlePoints.clear();
        particleTrails.reset();
        super.deactivate();
    }

    public MultiSelectSetting getParticlesSetting() {
        return particlesSetting;
    }
    public GroupSetting getTrailsSettings() {
        return trailsSettings;
    }
}