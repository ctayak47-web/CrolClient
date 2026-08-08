package crol.client.event.interfaces;

import crol.client.event.classes.PostMoveEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IPostMovaable {
   void onPostMove(PostMoveEvent var1);
}
