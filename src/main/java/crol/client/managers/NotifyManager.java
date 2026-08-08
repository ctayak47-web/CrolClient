package crol.client.managers;

import crol.client.ui.notify.Notify;
import crol.client.ui.notify.Status;
import crol.client.util.IUtil;
import crol.client.util.animations.Direction;
import crol.client.util.animations.impl.EaseBackIn;
import crol.client.util.math.TimerUtil;
import crol.client.util.render.msdf.MsdfFont;
import java.util.ArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.Window;

@Environment(EnvType.CLIENT)
public class NotifyManager implements IUtil {
   private final ArrayList<Notify> notifies = new ArrayList();
   private boolean canAddNotify = true;

   public void addNotify(String text, Status status) {
      if (this.canAddNotify) {
         Window window = mc.getWindow();
         float width = ((MsdfFont)FontManager.MONTSERRAT.get()).getWidth(text, 7.5F) + 42.0F;
         this.notifies.add(new Notify(text, (double)((float)(window.getScaledWidth() / 2) - width / 2.0F), (double)(window.getScaledHeight() / 2 + 31), (double)width, status, new EaseBackIn(400, (double)1.0F, 0.1F, Direction.FORWARDS), new TimerUtil()));
      }
   }

   public void updateNotify() {
      this.notifies.removeIf((notify) -> notify.getAnimation().finished(Direction.BACKWARDS));
   }

   public ArrayList<Notify> getNotifies() {
      return this.notifies;
   }

   public void setCanAddNotify(boolean canAddNotify) {
      this.canAddNotify = canAddNotify;
   }
}
