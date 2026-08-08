package crol.client.managers;

import crol.client.CrolClient;
import crol.client.event.classes.AttackEvent;
import crol.client.event.classes.BoundingBoxControlEvent;
import crol.client.event.classes.BreakEvent;
import crol.client.event.classes.CameraPositionEvent;
import crol.client.event.classes.ClickEvent;
import crol.client.event.classes.ClickSlotEvent;
import crol.client.event.classes.DamageEvent;
import crol.client.event.classes.DeathScreenEvent;
import crol.client.event.classes.DropItemEvent;
import crol.client.event.classes.EntityColorEvent;
import crol.client.event.classes.EntityDeathEvent;
import crol.client.event.classes.FireworkEvent;
import crol.client.event.classes.HandledScreenEvent;
import crol.client.event.classes.HudRenderEvent;
import crol.client.event.classes.InputEvent;
import crol.client.event.classes.MoveCorrectionEvent;
import crol.client.event.classes.MoveEvent;
import crol.client.event.classes.MoveOrEvent;
import crol.client.event.classes.PlaceBlockEvent;
import crol.client.event.classes.PostMoveEvent;
import crol.client.event.classes.ReceivePacketEvent;
import crol.client.event.classes.Render2DEvent;
import crol.client.event.classes.Render3DEvent;
import crol.client.event.classes.RotationEvent;
import crol.client.event.classes.SendPacketEvent;
import crol.client.event.classes.ShaderChamsEvent;
import crol.client.event.classes.ShaderEvent;
import crol.client.event.classes.ShaderHandEvent2D;
import crol.client.event.classes.TickEvent;
import crol.client.event.classes.TravelEvent;
import crol.client.event.classes.UsingItemEvent;
import crol.client.event.classes.WorldRenderEvent;
import crol.client.event.interfaces.IAttackable;
import crol.client.event.interfaces.IBoundingBoxControl;
import crol.client.event.interfaces.IBreakable;
import crol.client.event.interfaces.ICameraPosable;
import crol.client.event.interfaces.IClickSlotable;
import crol.client.event.interfaces.IClickaable;
import crol.client.event.interfaces.IDamageable;
import crol.client.event.interfaces.IDeathScreen;
import crol.client.event.interfaces.IDropable;
import crol.client.event.interfaces.IEntityColorable;
import crol.client.event.interfaces.IEntityDeath;
import crol.client.event.interfaces.IFireworkable;
import crol.client.event.interfaces.IHandledScreen;
import crol.client.event.interfaces.IHudRenderable;
import crol.client.event.interfaces.IInputable;
import crol.client.event.interfaces.IMoveCorrectionable;
import crol.client.event.interfaces.IMoveOrable;
import crol.client.event.interfaces.IMoveable;
import crol.client.event.interfaces.IPlaceBlockable;
import crol.client.event.interfaces.IPostMovaable;
import crol.client.event.interfaces.IReceivePacketable;
import crol.client.event.interfaces.IRenderable2D;
import crol.client.event.interfaces.IRenderable3D;
import crol.client.event.interfaces.IRotateable;
import crol.client.event.interfaces.ISendPacketable;
import crol.client.event.interfaces.IShader;
import crol.client.event.interfaces.IShaderChams;
import crol.client.event.interfaces.IShaderHandable;
import crol.client.event.interfaces.ITickable;
import crol.client.event.interfaces.ITravelable;
import crol.client.event.interfaces.IUsingItem;
import crol.client.event.interfaces.IWorldRender;
import crol.client.mixins.other.IPlayerMoveC2SPacket;
import crol.client.modules.Module;
import crol.client.ui.commands.impl.GpsCommand;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;

@Environment(EnvType.CLIENT)
public class EventManager {
   private final Map<Class<?>, List<Module>> listeners = new HashMap<>();
   private final WorldRenderEvent worldRenderEvent = new WorldRenderEvent();
   private final Render3DEvent render3DEvent = new Render3DEvent();
   private final Render2DEvent render2DEvent = new Render2DEvent();
   private final TickEvent tickEvent = new TickEvent();
   private final ShaderHandEvent2D shaderHandEvent2D = new ShaderHandEvent2D();
   private final HudRenderEvent hudRenderEvent = new HudRenderEvent();

   public void register(Module module) {
      if (module instanceof ITickable) {
         this.addListener(TickEvent.class, module);
      }

      if (module instanceof IShaderHandable) {
         this.addListener(ShaderHandEvent2D.class, module);
      }

      if (module instanceof IRenderable2D) {
         this.addListener(Render2DEvent.class, module);
      }

      if (module instanceof IRenderable3D) {
         this.addListener(Render3DEvent.class, module);
      }

      if (module instanceof IEntityColorable) {
         this.addListener(EntityColorEvent.class, module);
      }

      if (module instanceof IMoveCorrectionable) {
         this.addListener(MoveCorrectionEvent.class, module);
      }

      if (module instanceof ISendPacketable) {
         this.addListener(SendPacketEvent.class, module);
      }

      if (module instanceof IReceivePacketable) {
         this.addListener(ReceivePacketEvent.class, module);
      }

      if (module instanceof IInputable) {
         this.addListener(InputEvent.class, module);
      }

      if (module instanceof IClickSlotable) {
         this.addListener(ClickSlotEvent.class, module);
      }

      if (module instanceof IAttackable) {
         this.addListener(AttackEvent.class, module);
      }

      if (module instanceof IBreakable) {
         this.addListener(BreakEvent.class, module);
      }

      if (module instanceof IBoundingBoxControl) {
         this.addListener(BoundingBoxControlEvent.class, module);
      }

      if (module instanceof IUsingItem) {
         this.addListener(UsingItemEvent.class, module);
      }

      if (module instanceof IDeathScreen) {
         this.addListener(DeathScreenEvent.class, module);
      }

      if (module instanceof IShader) {
         this.addListener(ShaderEvent.class, module);
      }

      if (module instanceof IWorldRender) {
         this.addListener(WorldRenderEvent.class, module);
      }

      if (module instanceof IEntityDeath) {
         this.addListener(EntityDeathEvent.class, module);
      }

      if (module instanceof IShaderChams) {
         this.addListener(ShaderChamsEvent.class, module);
      }

      if (module instanceof IHandledScreen) {
         this.addListener(HandledScreenEvent.class, module);
      }

      if (module instanceof IRotateable) {
         this.addListener(RotationEvent.class, module);
      }

      if (module instanceof ITravelable) {
         this.addListener(TravelEvent.class, module);
      }

      if (module instanceof IFireworkable) {
         this.addListener(FireworkEvent.class, module);
      }

      if (module instanceof ICameraPosable) {
         this.addListener(CameraPositionEvent.class, module);
      }

      if (module instanceof IMoveable) {
         this.addListener(MoveEvent.class, module);
      }

      if (module instanceof IClickaable) {
         this.addListener(ClickEvent.class, module);
      }

      if (module instanceof IPlaceBlockable) {
         this.addListener(PlaceBlockEvent.class, module);
      }

      if (module instanceof IDropable) {
         this.addListener(DropItemEvent.class, module);
      }

      if (module instanceof IHudRenderable) {
         this.addListener(HudRenderEvent.class, module);
      }

      if (module instanceof IPostMovaable) {
         this.addListener(PostMoveEvent.class, module);
      }

      if (module instanceof IMoveOrable) {
         this.addListener(MoveOrEvent.class, module);
      }

      if (module instanceof IDamageable) {
         this.addListener(DamageEvent.class, module);
      }

   }

   public void unregister(Module module) {
      this.listeners.values().forEach((list) -> list.remove(module));
   }

   private void addListener(Class<?> eventClass, Module module) {
      this.listeners.computeIfAbsent(eventClass, (k) -> new ArrayList<>()).add(module);
   }

   public void hookEvent(Object event) {
      List<Module> targets = this.listeners.get(event.getClass());
      if (targets != null && !targets.isEmpty()) {
         if (event instanceof TickEvent) {
            CrolClient.INSTANCE.setBodyPitch(MinecraftClient.getInstance().player.getPitch());
         }

         for (Module module : new ArrayList<>(targets)) {
            if (module.isEnabled()) {
               Objects.requireNonNull(event);
               if (event instanceof TickEvent e) {
                  ((ITickable)module).onTick(e);
               } else if (event instanceof ShaderHandEvent2D e) {
                  ((IShaderHandable)module).onHandRender(e);
               } else if (event instanceof Render2DEvent e) {
                  ((IRenderable2D)module).onRender2D(e);
               } else if (event instanceof Render3DEvent e) {
                  ((IRenderable3D)module).onRender3D(e);
               } else if (event instanceof EntityColorEvent e) {
                  ((IEntityColorable)module).changeColor(e);
               } else if (event instanceof MoveCorrectionEvent e) {
                  ((IMoveCorrectionable)module).moveCorrection(e);
               } else if (event instanceof SendPacketEvent e) {
                  ((ISendPacketable)module).onSendPacket(e);
               } else if (event instanceof ReceivePacketEvent e) {
                  ((IReceivePacketable)module).onReceivePacket(e);
               } else if (event instanceof InputEvent e) {
                  ((IInputable)module).onInput(e);
               } else if (event instanceof ClickSlotEvent e) {
                  ((IClickSlotable)module).onClickSlot(e);
               } else if (event instanceof AttackEvent e) {
                  ((IAttackable)module).onAttack(e);
               } else if (event instanceof BreakEvent e) {
                  ((IBreakable)module).onBreak(e);
               } else if (event instanceof BoundingBoxControlEvent e) {
                  ((IBoundingBoxControl)module).onBoundingBoxControl(e);
               } else if (event instanceof UsingItemEvent e) {
                  ((IUsingItem)module).onUsing(e);
               } else if (event instanceof DeathScreenEvent e) {
                  ((IDeathScreen)module).onDeathScreen(e);
               } else if (event instanceof ShaderEvent e) {
                  ((IShader)module).onShader(e);
               } else if (event instanceof WorldRenderEvent e) {
                  ((IWorldRender)module).onWorldRender(e);
               } else if (event instanceof EntityDeathEvent e) {
                  ((IEntityDeath)module).onDeath(e);
               } else if (event instanceof ShaderChamsEvent e) {
                  ((IShaderChams)module).onShaderChams(e);
               } else if (event instanceof HandledScreenEvent e) {
                  ((IHandledScreen)module).onHandleScreen(e);
               } else if (event instanceof RotationEvent e) {
                  ((IRotateable)module).onRotate(e);
               } else if (event instanceof TravelEvent e) {
                  ((ITravelable)module).onTravel(e);
               } else if (event instanceof FireworkEvent e) {
                  ((IFireworkable)module).onFirework(e);
               } else if (event instanceof CameraPositionEvent e) {
                  ((ICameraPosable)module).onCamera(e);
               } else if (event instanceof MoveEvent e) {
                  ((IMoveable)module).onMove(e);
               } else if (event instanceof ClickEvent e) {
                  ((IClickaable)module).onClick(e);
               } else if (event instanceof PlaceBlockEvent e) {
                  ((IPlaceBlockable)module).onPlace(e);
               } else if (event instanceof DropItemEvent e) {
                  ((IDropable)module).onDrop(e);
               } else if (event instanceof HudRenderEvent e) {
                  ((IHudRenderable)module).onHudRender(e);
               } else if (event instanceof PostMoveEvent e) {
                  ((IPostMovaable)module).onPostMove(e);
               } else if (event instanceof MoveOrEvent e) {
                  ((IMoveOrable)module).onMoveOrable(e);
               } else if (event instanceof DamageEvent e) {
                  ((IDamageable)module).onDamage(e);
               }
            }
         }

         if (event instanceof SendPacketEvent) {
            SendPacketEvent sendPacketEvent = (SendPacketEvent)event;
            Packet var5 = sendPacketEvent.getPacket();
            if (var5 instanceof PlayerMoveC2SPacket) {
               PlayerMoveC2SPacket packet = (PlayerMoveC2SPacket)var5;
               CrolClient.INSTANCE.getRotationManager().update(((IPlayerMoveC2SPacket)packet).getYaw(), ((IPlayerMoveC2SPacket)packet).getPitch());
            }

            var5 = sendPacketEvent.getPacket();
            if (var5 instanceof ClientCommandC2SPacket) {
               ClientCommandC2SPacket c = (ClientCommandC2SPacket)var5;
               if (c.getMode() == Mode.START_SPRINTING || c.getMode() == Mode.STOP_SPRINTING) {
                  switch (c.getMode()) {
                     case START_SPRINTING -> CrolClient.INSTANCE.getRotationManager().setServerSprint(true);
                     case STOP_SPRINTING -> CrolClient.INSTANCE.getRotationManager().setServerSprint(false);
                  }
               }
            }
         }

         if (event instanceof Render2DEvent) {
            Render2DEvent render2DEvent = (Render2DEvent)event;
            if (GpsCommand.isEnabled) {
               GpsCommand.drawArrow(render2DEvent.getDrawContext());
            }
         }

      }
   }

   public WorldRenderEvent getWorldRenderEvent() {
      return this.worldRenderEvent;
   }

   public Render2DEvent getRender2DEvent() {
      return this.render2DEvent;
   }

   public Render3DEvent getRender3DEvent() {
      return this.render3DEvent;
   }

   public TickEvent getTickEvent() {
      return this.tickEvent;
   }

   public ShaderHandEvent2D getShaderHandEvent2D() {
      return this.shaderHandEvent2D;
   }

   public HudRenderEvent getHudRenderEvent() {
      return this.hudRenderEvent;
   }
}
