package ru.crolclient.api.feature.module;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import ru.crolclient.implement.features.modules.combat.*;
import ru.crolclient.implement.features.modules.misc.*;
import ru.crolclient.implement.features.modules.movement.*;
import ru.crolclient.implement.features.modules.player.*;
import ru.crolclient.implement.features.modules.render.*;

import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ModuleRepository {
    List<Module> modules = new ArrayList<>();

    public void setup() {
        register(
                new InterfaceModule(),
                //new AuctionHelperModule(),
                new PearlPredictionModule(),
                new AuraModule(),
                new AntiBot(),
                //new FlightModule(),
                new NoFriendDamageModule(),
                new HitBoxModule(),
                new BindSwapModule(),
                new AutoSprintModule(),
                new NoPushModule(),
                new ClickPearlModule(),
                new NoDelayModule(),
                new AutoRespawnModule(),
                new ScreenWalkModule(),
                new ServerRPSpooferModule(),
                new TriggerBotModule(),
                new AutoLeaveModule(),
                new AspectRatioModule(),
                new AutoGappleModule(),
                new AntiAFKModule(),
                new FishingModule(),
                new AutoEatModule(),
                new EffectCancelModule(),
                new ClearRenderModule(),
                new AutoTotemModule(),
                new PluginsModule(),
                new ElytraHelperModule(),
                new TwerkModule(),
                new SpeedModule(),
                new TargetESPModule(),
                new OptimizerModule(),
                new CustomCameraModule(),
                new AmbienceModule(),
                new CustomHandsModule(),
                new EdgeJumpModule(),
                new ZeroHitboxModule(),
                new MineHelperModule(),
                new AutoTpAcceptModule(),
                new ClickFriendModule(),
                new AutoPotionModule(),
                new ArrowsModule(),
                new ParticlesModule(),
                new ElytraTargetModule(),
                new DragonFlyModule(),
                new SpeedEatingSykaBlyat()
        );
    }

    public void register(Module... module) {
        modules.addAll(List.of(module));
    }

    public List<Module> modules() {
        return modules;
    }
}
