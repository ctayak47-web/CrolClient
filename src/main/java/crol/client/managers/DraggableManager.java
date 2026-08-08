package crol.client.managers;

import crol.client.ui.draggable.Draggable;
import crol.client.util.math.MouseUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;

@Environment(EnvType.CLIENT)
public class DraggableManager {
   private final List<Draggable> draggables = new ArrayList();

   public DraggableManager() {
      this.init();
   }

   private void init() {
   }

   private void addDraggables(Draggable... draggables) {
      this.draggables.addAll(Arrays.asList(draggables));
   }

   public void addDraggable(Draggable draggable) {
      this.draggables.add(draggable);
   }

   public void draggableHook(int mouseX, int mouseY, DrawContext drawContext) {
      for(Draggable draggable : this.draggables) {
         draggable.setMouseY(mouseY);
         draggable.setMouseX(mouseX);
         if (draggable.drag() && mouseX - draggable.getOffsetX() > 0 && mouseY - draggable.getOffsetY() > 0 && mouseX - draggable.getOffsetX() + draggable.getWeight() < MinecraftClient.getInstance().getWindow().getScaledWidth() && mouseY - draggable.getOffsetY() + draggable.getHeight() < MinecraftClient.getInstance().getWindow().getScaledHeight()) {
            draggable.setX(mouseX - draggable.getOffsetX());
            draggable.setY(mouseY - draggable.getOffsetY());
         }
      }

   }

   public void mouseClickHook(int button, double mouseX, double mouseY) {
      for(Draggable draggable : this.draggables) {
         if (!draggable.drag()) {
            draggable.setMouseXStart(mouseX);
            draggable.setMouseYStart(mouseY);
            draggable.setNum(button);
            draggable.mouseClick();
            if (MouseUtil.isHovered((double)draggable.getX(), (double)draggable.getY(), (double)draggable.getWeight(), (double)draggable.getHeight(), mouseX, mouseY) && MinecraftClient.getInstance().currentScreen instanceof ChatScreen) {
               return;
            }
         }
      }

   }

   public void dragEnd() {
      for(Draggable draggable : this.draggables) {
         draggable.endDrag();
      }

   }

   public List<Draggable> getDraggables() {
      return this.draggables;
   }
}
