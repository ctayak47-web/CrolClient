package crol.client.util.other;

import crol.client.util.IUtil;
import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.FloatControl.Type;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SoundUtil implements IUtil {
   private static Clip currentClip = null;

   public static void playSound(String sound, float value, boolean nonstop) {
      if (currentClip != null && currentClip.isRunning()) {
         currentClip.stop();
      }

      try {
         currentClip = AudioSystem.getClip();
         InputStream is = mc.getResourceManager().open(Identifier.of("crol", "sounds/" + sound));
         BufferedInputStream bis = new BufferedInputStream(is);
         AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bis);
         if (audioInputStream == null) {
            System.out.println("Sound not found!");
            return;
         }

         currentClip.open(audioInputStream);
         currentClip.start();
         FloatControl floatControl = (FloatControl)currentClip.getControl(Type.MASTER_GAIN);
         float min = floatControl.getMinimum();
         float max = floatControl.getMaximum();
         float volumeInDecibels = (float)((double)min * ((double)1.0F - (double)value / (double)100.0F) + (double)max * ((double)value / (double)100.0F));
         floatControl.setValue(volumeInDecibels);
         if (nonstop) {
            currentClip.addLineListener((event) -> {
               if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                  currentClip.setFramePosition(0);
                  currentClip.start();
               }

            });
         }
      } catch (Exception exception) {
         exception.printStackTrace();
      }

   }

   public static void playHitSound(String sound, float value, boolean nonstop) {
      if (currentClip != null && currentClip.isRunning()) {
         currentClip.stop();
      }

      try {
         currentClip = AudioSystem.getClip();
         InputStream is = mc.getResourceManager().open(Identifier.of("crol", "hitsounds/" + sound));
         BufferedInputStream bis = new BufferedInputStream(is);
         AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bis);
         if (audioInputStream == null) {
            System.out.println("Sound not found!");
            return;
         }

         currentClip.open(audioInputStream);
         currentClip.start();
         FloatControl floatControl = (FloatControl)currentClip.getControl(Type.MASTER_GAIN);
         float min = floatControl.getMinimum();
         float max = floatControl.getMaximum();
         float volumeInDecibels = (float)((double)min * ((double)1.0F - (double)value / (double)100.0F) + (double)max * ((double)value / (double)100.0F));
         floatControl.setValue(volumeInDecibels);
         if (nonstop) {
            currentClip.addLineListener((event) -> {
               if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                  currentClip.setFramePosition(0);
                  currentClip.start();
               }

            });
         }
      } catch (Exception exception) {
         exception.printStackTrace();
      }

   }
}
