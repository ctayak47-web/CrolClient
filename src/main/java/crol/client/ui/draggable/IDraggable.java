package crol.client.ui.draggable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IDraggable {
   int getX();

   int getY();

   int getHeight();

   int getWeight();

   int getMouseX();

   int getMouseY();

   void setX(int var1);

   void setY(int var1);

   void setHeight(int var1);

   void setWeight(int var1);

   void setMouseX(int var1);

   void setMouseY(int var1);

   boolean drag();

   void mouseClick();

   void setNum(int var1);

   void endDrag();
}
