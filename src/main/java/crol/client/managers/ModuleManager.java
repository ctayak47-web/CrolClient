package crol.client.managers;

import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.impl.combat.AntiBot;
import crol.client.modules.impl.combat.Aura;
import crol.client.modules.impl.combat.AutoApple;
import crol.client.modules.impl.combat.AutoExplosion;
import crol.client.modules.impl.combat.AutoPotion;
import crol.client.modules.impl.combat.AutoSwap;
import crol.client.modules.impl.combat.AutoTotem;
import crol.client.modules.impl.combat.BowSpammer;
import crol.client.modules.impl.combat.HitBox;
import crol.client.modules.impl.combat.HitSound;
import crol.client.modules.impl.combat.NoFriendDamage;
import crol.client.modules.impl.combat.NoInteract;
import crol.client.modules.impl.combat.ShiftTap;
import crol.client.modules.impl.combat.SyncTps;
import crol.client.modules.impl.combat.TapeMouse;
import crol.client.modules.impl.combat.TriggerBot;
import crol.client.modules.impl.combat.Velocity;
import crol.client.modules.impl.movement.AirStuck;
import crol.client.modules.impl.movement.Blink;
import crol.client.modules.impl.movement.ElytraBooster;
import crol.client.modules.impl.movement.ElytraBounce;
import crol.client.modules.impl.movement.ElytraMotion;
import crol.client.modules.impl.movement.InventoryMove;
import crol.client.modules.impl.movement.NoJumpDelay;
import crol.client.modules.impl.movement.NoSlow;
import crol.client.modules.impl.movement.NoWeb;
import crol.client.modules.impl.movement.Parkour;
import crol.client.modules.impl.movement.SafeWalk;
import crol.client.modules.impl.movement.Speed;
import crol.client.modules.impl.movement.Sprint;
import crol.client.modules.impl.movement.Strafe;
import crol.client.modules.impl.movement.TargetStrafe;
import crol.client.modules.impl.player.AntiAfk;
import crol.client.modules.impl.player.AutoRespawn;
import crol.client.modules.impl.player.FastBreak;
import crol.client.modules.impl.player.FastUse;
import crol.client.modules.impl.player.FreeCam;
import crol.client.modules.impl.player.GodMode;
import crol.client.modules.impl.player.ItemScroller;
import crol.client.modules.impl.player.ItemSwapFix;
import crol.client.modules.impl.player.NameProtect;
import crol.client.modules.impl.player.NoEntityTrace;
import crol.client.modules.impl.player.NoPush;
import crol.client.modules.impl.player.OpenWall;
import crol.client.modules.impl.player.PearlBlockThrow;
import crol.client.modules.impl.render.Arrows;
import crol.client.modules.impl.render.BetterWorld;
import crol.client.modules.impl.render.BlockEsp;
import crol.client.modules.impl.render.BlockOutline;
import crol.client.modules.impl.render.ChinaHat;
import crol.client.modules.impl.render.Crosshair;
import crol.client.modules.impl.render.CustomModel;
import crol.client.modules.impl.render.GlyphLines;
import crol.client.modules.impl.render.GuiModule;
import crol.client.modules.impl.render.HitEffect;
import crol.client.modules.impl.render.HpAlert;
import crol.client.modules.impl.render.Interface;
import crol.client.modules.impl.render.ItemEsp;
import crol.client.modules.impl.render.ItemPhysics;
import crol.client.modules.impl.render.JumpCircle;
import crol.client.modules.impl.render.KillEffect;
import crol.client.modules.impl.render.Particles;
import crol.client.modules.impl.render.PlayerEsp;
import crol.client.modules.impl.render.Prediction;
import crol.client.modules.impl.render.SeeInvisible;
import crol.client.modules.impl.render.ShaderHand;
import crol.client.modules.impl.render.SmoothCamera;
import crol.client.modules.impl.render.SwingAnimation;
import crol.client.modules.impl.render.Wings;
import crol.client.modules.impl.util.AHHelper;
import crol.client.modules.impl.util.AutoDuel;
import crol.client.modules.impl.util.AutoLeave;
import crol.client.modules.impl.util.AutoTool;
import crol.client.modules.impl.util.AutoTpAccept;
import crol.client.modules.impl.util.BetterChat;
import crol.client.modules.impl.util.ChestStealer;
import crol.client.modules.impl.util.ClanUpgrader;
import crol.client.modules.impl.util.ClickFriend;
import crol.client.modules.impl.util.ClickPearl;
import crol.client.modules.impl.util.ClientSound;
import crol.client.modules.impl.util.CordDropper;
import crol.client.modules.impl.util.ElytraFix;
import crol.client.modules.impl.util.ElytraHelper;
import crol.client.modules.impl.util.FakePlayer;
import crol.client.modules.impl.util.LockSlot;
import crol.client.modules.impl.util.NoItemBreak;
import crol.client.modules.impl.util.NoRender;
import crol.client.modules.impl.util.NoRotation;
import crol.client.modules.impl.util.ReallyWorldHelper;
import crol.client.modules.impl.util.RpSpoof;
import crol.client.modules.impl.util.WebTrap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class ModuleManager {
   private final List<Module> modules = new ArrayList();
   private final HashMap<Class, Module> modules2 = new HashMap();

   public ModuleManager() {
      this.init();
   }

   @Compile
   private void init() {
      this.addModules(new Sprint(), new GuiModule(), new Interface(), new BetterWorld(), new RpSpoof(), new TriggerBot(), new InventoryMove(), new AutoTpAccept(), new SeeInvisible(), new NoRender(), new AutoTool(), new ItemSwapFix(), new ElytraBounce(), new SafeWalk(), new JumpCircle(), new Particles(), new PlayerEsp(), new SwingAnimation(), new Wings(), new NameProtect(), new NoFriendDamage(), new BowSpammer(), new AntiAfk(), new BlockOutline(), new ItemPhysics(), new TapeMouse(), new Prediction(), new FastBreak(), new NoEntityTrace(), new ShiftTap(), new AntiBot(), new Velocity(), new HitBox(), new NoInteract(), new NoWeb(), new AirStuck(), new NoSlow(), new ClickPearl(), new AutoRespawn(), new AutoLeave(), new ChestStealer(), new Crosshair(), new HitEffect(), new ChinaHat(), new KillEffect(), new GlyphLines(), new ShaderHand(), new SmoothCamera(), new HpAlert(), new Arrows(), new ClickFriend(), new Blink(), new ItemScroller(), new HitSound(), new FreeCam(), new NoPush(), new BlockEsp(), new ItemEsp(), new AutoTotem(), new Aura(), new ElytraHelper(), new AutoApple(), new FakePlayer(), new ClientSound(), new NoJumpDelay(), new AutoPotion(), new OpenWall(), new NoItemBreak(), new Speed(), new Strafe(), new ElytraBooster(), new ElytraMotion(), new TargetStrafe(), new CordDropper(), new Parkour(), new NoRotation(), new FastUse(), new AutoSwap(), new AutoDuel(), new AutoExplosion(), new SyncTps(), new BetterChat(), new WebTrap(), new LockSlot(), new ReallyWorldHelper(), new ElytraFix(), new GodMode(), new PearlBlockThrow(), new AHHelper(), new ClanUpgrader(), new CustomModel());
      this.modules.forEach((m) -> this.modules2.put(m.getClass(), m));
   }

   @Compile
   private void addModules(Module... modules) {
      this.modules.addAll(Arrays.asList(modules));
   }

   public List<Module> getModules() {
      return this.modules;
   }

   public Module getByClass(Class clasz) {
      return (Module)this.modules2.get(clasz);
   }

   public void initDesc() {
      this.modules.forEach(Module::initDesc);
   }

   public List<Module> getByCategory(Category category) {
      List<Module> modules1 = new ArrayList<>();
      this.modules.stream()
         .filter((module) -> module.getModuleInfo().category() == category)
         .forEach(modules1::add);
      return modules1;
   }

   public List<Module> search(String text) {
      String lower = text.toLowerCase();
      return (List)this.modules.stream().filter((module) -> module.getModuleInfo().name().toLowerCase().contains(lower)).collect(Collectors.toList());
   }
}
