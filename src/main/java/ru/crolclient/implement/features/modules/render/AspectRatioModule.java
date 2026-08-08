package ru.crolclient.implement.features.modules.render;

import ru.crolclient.api.event.EventManager;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.ValueSetting;
import ru.crolclient.implement.events.render.AspectRatioEvent;

public class AspectRatioModule extends Module {
    ValueSetting ratio = new ValueSetting("Ratio", "Sets the aspect ratio for the game")
            .setValue(1.5F).range(0.6F, 2.2F).increment(0.1f);
    AspectRatioEvent aspectRatioEvent = new AspectRatioEvent();

    public AspectRatioModule() {
        super("AspectRatio", "Aspect Ratio", ModuleCategory.RENDER);
        setup(ratio);
    }

    @Override
    public void activate() {
        super.activate();
        updateAspectRatio();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        aspectRatioEvent.setRatio(1.8f);
        EventManager.callEvent(aspectRatioEvent);
    }

    public void updateAspectRatio() {
        aspectRatioEvent.setRatio(ratio.getValue());
        EventManager.callEvent(aspectRatioEvent);
    }

    public float getRatio() {
        return ratio.getValue();
    }
}