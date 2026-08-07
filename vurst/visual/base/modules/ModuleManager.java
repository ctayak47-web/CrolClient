
package vurst.visual.base.modules;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Generated;
import org.lwjgl.glfw.GLFW;
import vurst.visual.base.events.impl.input.EventKey;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.impl.hud.Armor;
import vurst.visual.client.modules.impl.hud.CoolDownsHud;
import vurst.visual.client.modules.impl.hud.Coordinates;
import vurst.visual.client.modules.impl.hud.HotKeys;
import vurst.visual.client.modules.impl.hud.MediaPlayer;
import vurst.visual.client.modules.impl.hud.NotificationHud;
import vurst.visual.client.modules.impl.hud.Potions;
import vurst.visual.client.modules.impl.hud.TargetHud;
import vurst.visual.client.modules.impl.hud.Watermark;
import vurst.visual.client.modules.impl.movement.AutoSprint;
import vurst.visual.client.modules.impl.movement.ElytraSwap;
import vurst.visual.client.modules.impl.movement.HealingTracker;
import vurst.visual.client.modules.impl.movement.ShiftTap;
import vurst.visual.client.modules.impl.movement.TapeMouse;
import vurst.visual.client.modules.impl.movement.TrapTimer;
import vurst.visual.client.modules.impl.render.Animation;
import vurst.visual.client.modules.impl.render.AspectRatio;
import vurst.visual.client.modules.impl.render.BabyModel;
import vurst.visual.client.modules.impl.render.BlockOutline;
import vurst.visual.client.modules.impl.render.ChinaHat;
import vurst.visual.client.modules.impl.render.Crosshair;
import vurst.visual.client.modules.impl.render.CustomCape;
import vurst.visual.client.modules.impl.render.CustomFog;
import vurst.visual.client.modules.impl.render.CustomGlow;
import vurst.visual.client.modules.impl.render.CustomModels;
import vurst.visual.client.modules.impl.render.CustomWorld;
import vurst.visual.client.modules.impl.render.FireFly;
import vurst.visual.client.modules.impl.render.FullBright;
import vurst.visual.client.modules.impl.render.HitBoxCustomizer;
import vurst.visual.client.modules.impl.render.HitBubbles;
import vurst.visual.client.modules.impl.render.HitColor;
import vurst.visual.client.modules.impl.render.HitEffects;
import vurst.visual.client.modules.impl.render.HitParticles;
import vurst.visual.client.modules.impl.render.JumpCircle;
import vurst.visual.client.modules.impl.render.KillEffects;
import vurst.visual.client.modules.impl.render.NameF5;
import vurst.visual.client.modules.impl.render.NoFluid;
import vurst.visual.client.modules.impl.render.NoRender;
import vurst.visual.client.modules.impl.render.SaturationBar;
import vurst.visual.client.modules.impl.render.ShaderHands;
import vurst.visual.client.modules.impl.render.SwingAnimation;
import vurst.visual.client.modules.impl.render.TargetEsp;
import vurst.visual.client.modules.impl.render.Trails;
import vurst.visual.client.modules.impl.render.ViewModel;
import vurst.visual.client.modules.impl.render.WorldTime;
import vurst.visual.client.modules.impl.utility.AntiAfk;
import vurst.visual.client.modules.impl.utility.AntiBanChat;
import vurst.visual.client.modules.impl.utility.AucHelper;
import vurst.visual.client.modules.impl.utility.AutoDuel;
import vurst.visual.client.modules.impl.utility.AutoEat;
import vurst.visual.client.modules.impl.utility.AutoInviz;
import vurst.visual.client.modules.impl.utility.AutoReconnect;
import vurst.visual.client.modules.impl.utility.AutoResell;
import vurst.visual.client.modules.impl.utility.AutoRespawn;
import vurst.visual.client.modules.impl.utility.AutoSwap;
import vurst.visual.client.modules.impl.utility.AutoTpaccept;
import vurst.visual.client.modules.impl.utility.CoolDowns;
import vurst.visual.client.modules.impl.utility.EggMan;
import vurst.visual.client.modules.impl.utility.FakePlayer;
import vurst.visual.client.modules.impl.utility.FastExp;
import vurst.visual.client.modules.impl.utility.FastSwap;
import vurst.visual.client.modules.impl.utility.FreeLook;
import vurst.visual.client.modules.impl.utility.HealingHelper;
import vurst.visual.client.modules.impl.utility.HitSound;
import vurst.visual.client.modules.impl.utility.ItemHighliter;
import vurst.visual.client.modules.impl.utility.ItemPhysics;
import vurst.visual.client.modules.impl.utility.ItemPickupLogger;
import vurst.visual.client.modules.impl.utility.ItemScroller;
import vurst.visual.client.modules.impl.utility.LagHost;
import vurst.visual.client.modules.impl.utility.LockSlot;
import vurst.visual.client.modules.impl.utility.MineHelper;
import vurst.visual.client.modules.impl.utility.NameProtect;
import vurst.visual.client.modules.impl.utility.Predictions;
import vurst.visual.client.modules.impl.utility.PvpSave;
import vurst.visual.client.modules.impl.utility.RadiusHelper;
import vurst.visual.client.modules.impl.utility.SpecBind;
import vurst.visual.client.modules.impl.utility.TNTTimer;
import vurst.visual.client.modules.impl.utility.TotemTracker;
import vurst.visual.client.modules.impl.utility.Tracker;
import vurst.visual.client.modules.impl.utility.Zoom;
import vurst.visual.utility.interfaces.IMinecraft;

public final class ModuleManager
implements IMinecraft {
    private final List<Module> modules = new ArrayList<Module>();

    public ModuleManager() {
        this.init();
        EventManager.register(this);
    }

    private void init() {
        this.registerCombat();
        this.registerMovement();
        this.registerRender();
        this.registerHud();
    }

    private void registerCombat() {
    }

    private void registerMovement() {
        this.registerModule(HealingTracker.INSTANCE);
        this.registerModule(AutoSprint.INSTANCE);
        this.registerModule(AntiAfk.INSTANCE);
        this.registerModule(AntiBanChat.INSTANCE);
        this.registerModule(ElytraSwap.INSTANCE);
        this.registerModule(ShiftTap.INSTANCE);
        this.registerModule(TrapTimer.INSTANCE);
        this.registerModule(TapeMouse.INSTANCE);
        this.registerModule(ItemScroller.INSTANCE);
        this.registerModule(ItemHighliter.INSTANCE);
        this.registerModule(ItemPhysics.INSTANCE);
        this.registerModule(ItemPickupLogger.INSTANCE);
        this.registerModule(LockSlot.INSTANCE);
        this.registerModule(AutoResell.INSTANCE);
        this.registerModule(AutoSwap.INSTANCE);
        this.registerModule(AutoEat.INSTANCE);
        this.registerModule(AutoInviz.INSTANCE);
        this.registerModule(AucHelper.INSTANCE);
        this.registerModule(AutoRespawn.INSTANCE);
        this.registerModule(AutoTpaccept.INSTANCE);
        this.registerModule(AutoReconnect.INSTANCE);
        this.registerModule(FastExp.INSTANCE);
        this.registerModule(FakePlayer.INSTANCE);
        this.registerModule(AutoDuel.INSTANCE);
        this.registerModule(CoolDowns.INSTANCE);
        this.registerModule(FreeLook.INSTANCE);
        this.registerModule(EggMan.INSTANCE);
        this.registerModule(HealingHelper.INSTANCE);
        this.registerModule(HitSound.INSTANCE);
        this.registerModule(FastSwap.INSTANCE);
        this.registerModule(MineHelper.INSTANCE);
        this.registerModule(Predictions.INSTANCE);
        this.registerModule(PvpSave.INSTANCE);
        this.registerModule(Tracker.INSTANCE);
        this.registerModule(SpecBind.INSTANCE);
        this.registerModule(Zoom.INSTANCE);
        this.registerModule(NameProtect.INSTANCE);
        this.registerModule(TotemTracker.INSTANCE);
        this.registerModule(TNTTimer.INSTANCE);
        this.registerModule(RadiusHelper.INSTANCE);
        this.registerModule(LagHost.INSTANCE);
    }

    private void registerRender() {
        this.registerModule(Animation.INSTANCE);
        this.registerModule(AspectRatio.INSTANCE);
        this.registerModule(BabyModel.INSTANCE);
        this.registerModule(NameF5.INSTANCE);
        this.registerModule(CustomCape.INSTANCE);
        this.registerModule(CustomGlow.INSTANCE);
        this.registerModule(NoFluid.INSTANCE);
        this.registerModule(NoRender.INSTANCE);
        this.registerModule(SwingAnimation.INSTANCE);
        this.registerModule(ChinaHat.INSTANCE);
        this.registerModule(Crosshair.INSTANCE);
        this.registerModule(BlockOutline.INSTANCE);
        this.registerModule(CustomModels.INSTANCE);
        this.registerModule(FireFly.INSTANCE);
        this.registerModule(HitBoxCustomizer.INSTANCE);
        this.registerModule(HitBubbles.INSTANCE);
        this.registerModule(HitEffects.INSTANCE);
        this.registerModule(HitParticles.INSTANCE);
        this.registerModule(KillEffects.INSTANCE);
        this.registerModule(HitColor.INSTANCE);
        this.registerModule(ViewModel.INSTANCE);
        this.registerModule(FullBright.INSTANCE);
        this.registerModule(CustomFog.INSTANCE);
        this.registerModule(CustomWorld.INSTANCE);
        this.registerModule(ShaderHands.INSTANCE);
        this.registerModule(SaturationBar.INSTANCE);
        this.registerModule(WorldTime.INSTANCE);
        this.registerModule(TargetEsp.INSTANCE);
        this.registerModule(Trails.INSTANCE);
        this.registerModule(JumpCircle.INSTANCE);
    }

    private void registerHud() {
        this.registerModule(Watermark.INSTANCE);
        this.registerModule(HotKeys.INSTANCE);
        this.registerModule(Potions.INSTANCE);
        this.registerModule(TargetHud.INSTANCE);
        this.registerModule(Armor.INSTANCE);
        this.registerModule(MediaPlayer.INSTANCE);
        this.registerModule(NotificationHud.INSTANCE);
        this.registerModule(CoolDownsHud.INSTANCE);
        this.registerModule(Coordinates.INSTANCE);
    }

    private void registerModule(Module module) {
        this.modules.add(module);
    }

    public Module getModule(String name) {
        return this.modules.stream().filter(module -> module.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Set<Module> getActiveModules() {
        HashSet<Module> active = new HashSet<Module>();
        for (Module module : this.modules) {
            if (!module.isEnabled()) continue;
            active.add(module);
        }
        return active;
    }

    @EventTarget
    public void onKey(EventKey event) {
        if (ModuleManager.mc.currentScreen != null || event.getAction() != 1) {
            return;
        }
        if (event.getKeyCode() == 66 && GLFW.glfwGetKey((long)mc.getWindow().getHandle(), (int)292) == 1) {
            HitBoxCustomizer.INSTANCE.toggle();
            return;
        }
        for (Module module : this.modules) {
            if (module.getKeyCode() != event.getKeyCode() || module.getKeyCode() == -1) continue;
            module.toggle();
        }
    }

    @Generated
    public List<Module> getModules() {
        return this.modules;
    }
}

