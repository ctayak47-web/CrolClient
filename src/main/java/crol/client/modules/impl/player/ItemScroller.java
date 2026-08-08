package crol.client.modules.impl.player;

import crol.client.event.classes.ClickSlotEvent;
import crol.client.event.classes.HandledScreenEvent;
import crol.client.event.interfaces.IClickSlotable;
import crol.client.event.interfaces.IHandledScreen;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.util.IUtil;
import crol.client.util.math.TimerUtil;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.Type;
import net.minecraft.item.Item;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class ItemScroller extends Module implements IUtil, IClickSlotable, IHandledScreen {
   private final FloatSetting delay = ((FloatSetting)(new FloatSetting()).name("Delay")).minValue(1.0F).maxValue(200.0F).incriment(5.0F).value(50.0F);
   private final TimerUtil timerUtil = new TimerUtil();

   public ItemScroller() {
      super(new ModuleInfo("ItemScroller", Category.PLAYER, "Убирает необходимость нажатия на каждый предмет для его перемещения"));
      this.addSetting(new Setting[]{this.delay});
   }

   public void onClickSlot(ClickSlotEvent e) {
      int slotId = e.getSlot();
      if (slotId >= 0 && slotId <= mc.player.currentScreenHandler.slots.size()) {
         Slot slot = mc.player.currentScreenHandler.getSlot(slotId);
         Item item = slot.getStack().getItem();
         if (item != null && this.isKey(mc.options.sneakKey) && this.timerUtil.every((double)50.0F)) {
            System.out.println("1");
            slots().filter((s) -> s.getStack().getItem().equals(item)).forEach((s) -> mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, s.id, 1, e.getSlotActionType(), mc.player));
         }

      }
   }

   public static Stream<Slot> slots() {
      return mc.player.currentScreenHandler.slots.stream();
   }

   public void onHandleScreen(HandledScreenEvent e) {
      Slot hoverSlot = e.getSlotHover();
      SlotActionType actionType = this.isKey(mc.options.dropKey) ? SlotActionType.THROW : (this.isKey(mc.options.attackKey) ? SlotActionType.QUICK_MOVE : null);
      if (this.isKey(mc.options.sneakKey) && !this.isKey(mc.options.sprintKey) && hoverSlot != null && hoverSlot.hasStack() && actionType != null && this.timerUtil.every((double)this.delay.getValue())) {
         mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, hoverSlot.id, actionType.equals(SlotActionType.THROW) ? 1 : 0, actionType, mc.player);
      }

   }

   public boolean isKey(KeyBinding key) {
      InputUtil.Key boundKey = key.getDefaultKey();
      int code = boundKey.getCode();
      if (boundKey.getCategory() == Type.KEYSYM) {
         return GLFW.glfwGetKey(mc.getWindow().getHandle(), code) == 1;
      } else if (boundKey.getCategory() == Type.MOUSE) {
         return GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), code) == 1;
      } else {
         return false;
      }
   }

   public boolean isKey(InputUtil.Type type, int keyCode) {
      if (keyCode != -1) {
         switch (type) {
            case KEYSYM -> {
               return GLFW.glfwGetKey(mc.getWindow().getHandle(), keyCode) == 1;
            }
            case MOUSE -> {
               return GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), keyCode) == 1;
            }
         }
      }

      return false;
   }
}
