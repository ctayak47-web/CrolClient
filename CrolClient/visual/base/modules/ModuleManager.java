
package crol.client.base.modules;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Generated;
import org.lwjgl.glfw.GLFW;
import crol.client.base.events.impl.input.EventKey;
import crol.client.modules.api.Module;
import crol.client.modules.impl.hud.Armor;
import crol.client.modules.impl.hud.CoolDownsHud;
import crol.client.modules.impl.hud.Coordinates;
import crol.client.modules.impl.hud.HotKeys;
import crol.client.modules.impl.hud.MediaPlayer;
import crol.client.modules.impl.hud.NotificationHud;
import crol.client.modules.impl.hud.Potions;
import crol.client.modules.impl.hud.TargetHud;
import crol.client.modules.impl.hud.Watermark;
import crol.client.modules.impl.movement.AutoSprint;
import crol.client.modules.impl.movement.ElytraSwap;
import crol.client.modules.impl.movement.HealingTracker;
import crol.client.modules.impl.movement.ShiftTap;
import crol.client.modules.impl.movement.TapeMouse;
import crol.client.modules.impl.movement.TrapTimer;
import crol.client.modules.impl.render.Animation;
import crol.client.modules.impl.render.AspectRatio;
import crol.client.modules.impl.render.BabyModel;
import crol.client.modules.impl.render.BlockOutline;
import crol.client.modules.impl.render.ChinaHat;
import crol.client.modules.impl.render.Crosshair;
import crol.client.modules.impl.render.CustomCape;
import crol.client.modules.impl.render.CustomFog;
import crol.client.modules.impl.render.CustomGlow;
import crol.client.modules.impl.render.CustomModels;
import crol.client.modules.impl.render.CustomWorld;
import crol.client.modules.impl.render.FireFly;
import crol.client.modules.impl.render.FullBright;
import crol.client.modules.impl.render.HitBoxCustomizer;
import crol.client.modules.impl.render.HitBubbles;
import crol.client.modules.impl.render.HitColor;
import crol.client.modules.impl.render.HitEffects;
import crol.client.modules.impl.render.HitParticles;
import crol.client.modules.impl.render.JumpCircle;
import crol.client.modules.impl.render.KillEffects;
import crol.client.modules.impl.render.NameF5;
import crol.client.modules.impl.render.NoFluid;
import crol.client.modules.impl.render.NoRender;
import crol.client.modules.impl.render.SaturationBar;
import crol.client.modules.impl.render.ShaderHands;
import crol.client.modules.impl.render.SwingAnimation;
import crol.client.modules.impl.render.TargetEsp;
import crol.client.modules.impl.render.Trails;
import crol.client.modules.impl.render.ViewModel;
import crol.client.modules.impl.render.WorldTime;
import crol.client.modules.impl.utility.AntiAfk;
import crol.client.modules.impl.utility.AntiBanChat;
import crol.client.modules.impl.utility.AucHelper;
import crol.client.modules.impl.utility.AutoDuel;
import crol.client.modules.impl.utility.AutoEat;
import crol.client.modules.impl.utility.AutoInviz;
import crol.client.modules.impl.utility.AutoReconnect;
import crol.client.modules.impl.utility.AutoResell;
import crol.client.modules.impl.utility.AutoRespawn;
import crol.client.modules.impl.utility.AutoSwap;
import crol.client.modules.impl.utility.AutoTpaccept;
import crol.client.modules.impl.utility.CoolDowns;
import crol.client.modules.impl.utility.EggMan;
import crol.client.modules.impl.utility.FakePlayer;
import crol.client.modules.impl.utility.FastExp;
import crol.client.modules.impl.utility.FastSwap;
import crol.client.modules.impl.utility.FreeLook;
import crol.client.modules.impl.utility.HealingHelper;
import crol.client.modules.impl.utility.HitSound;
import crol.client.modules.impl.utility.ItemHighliter;
import crol.client.modules.impl.utility.ItemPhysics;
import crol.client.modules.impl.utility.ItemPickupLogger;
import crol.client.modules.impl.utility.ItemScroller;
import crol.client.modules.impl.utility.LagHost;
import crol.client.modules.impl.utility.LockSlot;
import crol.client.modules.impl.utility.MineHelper;
import crol.client.modules.impl.utility.NameProtect;
import crol.client.modules.impl.utility.Predictions;
import crol.client.modules.impl.utility.PvpSave;
import crol.client.modules.impl.utility.RadiusHelper;
import crol.client.modules.impl.utility.SpecBind;
import crol.client.modules.impl.utility.TNTTimer;
import crol.client.modules.impl.utility.TotemTracker;
import crol.client.modules.impl.utility.Tracker;
import crol.client.modules.impl.utility.Zoom;
import crol.client.utility.interfaces.IMinecraft;

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

