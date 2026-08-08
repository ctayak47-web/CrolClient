package crol.client.ui.draggable;

import crol.client.util.math.MouseUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;

@Environment(EnvType.CLIENT)
public class Draggable implements IDraggable {
   public int x;
   public int y;
   public int weight;
   public int height;
   public int mouseX;
   public int mouseY;
   public int num;
   public int offsetX;
   public int offsetY;
   public int preX;
   private double mouseXStart;
   private double mouseYStart;
   private boolean drag;
   private String name;

   public Draggable(String name, int x, int y, int weight, int height) {
      this.x = x;
      this.y = y;
      this.weight = weight;
      this.height = height;
      this.name = name;
      this.preX = x;
   }

   public boolean canPush() {
      return true;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public int getHeight() {
      return this.height;
   }

   public int getWeight() {
      return this.weight;
   }

   public void setX(int x) {
      this.preX = this.x;
      this.x = x;
   }

   public void setY(int y) {
      this.y = y;
   }

   public void setHeight(int height) {
      this.height = height;
   }

   public void setWeight(int weight) {
      this.weight = weight;
   }

   public int getMouseX() {
      return this.mouseX;
   }

   public int getMouseY() {
      return this.mouseY;
   }

   public void setMouseX(int x) {
      this.mouseX = x;
   }

   public void setMouseY(int y) {
      this.mouseY = y;
   }

   public boolean drag() {
      return this.drag;
   }

   public void mouseClick() {
      if (MouseUtil.isHovered((double)this.x, (double)this.y, (double)this.weight, (double)this.height, (double)this.mouseX, (double)this.mouseY) && MinecraftClient.getInstance().currentScreen instanceof ChatScreen && this.num == 0) {
         this.drag = true;
      }

      this.offsetX = (int)(this.mouseXStart - (double)this.x);
      this.offsetY = (int)(this.mouseYStart - (double)this.y);
   }

   public void setNum(int button) {
      this.num = button;
   }

   public void endDrag() {
      this.drag = false;
   }

   public void setMouseXStart(double mouseXStart) {
      this.mouseXStart = mouseXStart;
   }

   public void setMouseYStart(double mouseYStart) {
      this.mouseYStart = mouseYStart;
   }

   public int getOffsetX() {
      return this.offsetX;
   }

   public int getOffsetY() {
      return this.offsetY;
   }

   public String getName() {
      return this.name;
   }
}
