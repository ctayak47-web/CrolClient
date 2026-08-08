package crol.client.modules.impl.movement;

import crol.client.event.classes.ClickSlotEvent;
import crol.client.event.classes.InputEvent;
import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.IClickSlotable;
import crol.client.event.interfaces.IInputable;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.IEnableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.ui.gui.Gui;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class InventoryMove extends Module implements ITickable, IClickSlotable, IInputable, IUtil, IEnableable {
   private boolean click = false;
   private KeyBinding[] pressedKeys;

   public InventoryMove() {
      super(new ModuleInfo("InventoryMove", Category.MOVEMENT, "Позволяет ходить в гуи и инвентаре"));
   }

   public void onClickSlot(ClickSlotEvent event) {
      this.click = true;
      mc.player.input.movementSideways = 0.0F;
      mc.player.input.movementForward = 0.0F;
      this.click = false;
   }

   public void onInput(InputEvent event) {
      if (this.click) {
         event.setForward(0.0F);
         event.setStrafe(0.0F);
      }

   }

   @Compile
   public void onTick(TickEvent event) {
      if (mc.player != null && (mc.currentScreen instanceof InventoryScreen || mc.currentScreen instanceof Gui)) {
         this.updateKeyBindingState(this.pressedKeys);
      }

   }

   private void updateKeyBindingState(KeyBinding[] keyBindings) {
      long windowHandle = MinecraftClient.getInstance().getWindow().getHandle();

      for(KeyBinding keyBinding : keyBindings) {
         int keyCode = keyBinding.getDefaultKey().getCode();
         boolean isKeyPressed;
         if (keyCode < 0) {
            int mouseButton = -keyCode - 1;
            isKeyPressed = GLFW.glfwGetMouseButton(windowHandle, mouseButton) == 1;
         } else {
            isKeyPressed = GLFW.glfwGetKey(windowHandle, keyCode) == 1;
         }

         keyBinding.setPressed(isKeyPressed);
      }

   }

   public void onEnable() {
      this.pressedKeys = new KeyBinding[]{mc.options.forwardKey, mc.options.backKey, mc.options.leftKey, mc.options.rightKey, mc.options.jumpKey, mc.options.sprintKey};
   }
}
