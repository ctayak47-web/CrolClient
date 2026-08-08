
package crol.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.Text;
import net.minecraft.Packet;
import net.minecraft.GameJoinS2CPacket;
import net.minecraft.Identifier;
import net.minecraft.MathHelper;
import net.minecraft.GameMessageS2CPacket;
import net.minecraft.RotationAxis;
import crol.client.CrolClient;
import crol.client.base.events.impl.render.EventHudRender;
import crol.client.base.events.impl.server.EventPacket;
import crol.client.base.font.Font;
import crol.client.base.font.Fonts;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.impl.BooleanSetting;
import crol.client.modules.api.setting.impl.NumberSetting;
import crol.client.utility.render.display.base.CustomDrawContext;
import crol.client.utility.render.display.base.color.ColorRGBA;

@ModuleAnnotation(name="GPS", description="Показывает стрелку к заданным координатам", category=Category.MOVEMENT)
public final class GPS
extends Module {
    public static final GPS INSTANCE = new GPS();
    private static final Identifier ARROW_TEXTURE = CrolClient.id("icons/gps_arrow.png");
    private static final long AUTO_TARGET_LIFETIME_MS = 360000L;
    private final NumberSetting distanceFromCenter = new NumberSetting("Дистанция от центра", 50.0f, 30.0f, 150.0f, 5.0f);
    private final NumberSetting arrowSize = new NumberSetting("Размер стрелки", 16.0f, 16.0f, 64.0f, 2.0f);
    private final NumberSetting markerSize = new NumberSetting("Размер метки 3D", 1.5f, 0.5f, 3.0f, 0.1f);
    private final BooleanSetting showDistance = new BooleanSetting("Показывать дистанцию", true);
    private static double targetX = 0.0;
    private static double targetY = 0.0;
    private static double targetZ = 0.0;
    private static boolean hasTarget = false;
    private static boolean autoTarget = false;
    private static long autoTargetExpireAtMs = 0L;
    private static String lastEventName = "";
    private float smoothAngle = 0.0f;
    private double smoothArrowX = 0.0;
    private double smoothArrowY = 0.0;
    private boolean smoothInitialized = false;
    private static final Pattern METEOR_PATTERN = Pattern.compile("Метеоритный дождь.*?\\[(-?\\d+)\\s+(\\d+)\\s+(-?\\d+)\\]");
    private static final Pattern VOLCANO_PATTERN = Pattern.compile("Вулкан.*?(-?\\d+)\\s+(\\d+)\\s+(-?\\d+)");
    private static final Pattern KILLER_BEACON_PATTERN = Pattern.compile("Маяк убийца.*?(-?\\d+)\\s+(\\d+)\\s+(-?\\d+)");
    private static final Pattern DEATH_CHEST_PATTERN = Pattern.compile("Сундук Смерти.*?Появится уже через (\\d+) минут");
    private static final Pattern EVENT_STARTED_PATTERN = Pattern.compile("(Портал на резню активирован|Идёт страшный бой)");

    private GPS() {
    }

    public static void setTarget(double x, double y, double z) {
        GPS.setTarget(x, y, z, "");
    }

    public static void setTarget(double x, double y, double z, String eventName) {
        GPS.setTargetInternal(x, y, z, eventName, false);
    }

    public static void setAutoTarget(double x, double y, double z, String eventName) {
        GPS.setTargetInternal(x, y, z, eventName, true);
    }

    private static void setTargetInternal(double x, double y, double z, String eventName, boolean auto) {
        targetX = x;
        targetY = y;
        targetZ = z;
        hasTarget = true;
        autoTarget = auto;
        autoTargetExpireAtMs = auto ? System.currentTimeMillis() + 360000L : 0L;
        lastEventName = eventName;
        INSTANCE.resetSmooth();
    }

    public static void clearTarget() {
        hasTarget = false;
        autoTarget = false;
        autoTargetExpireAtMs = 0L;
        lastEventName = "";
        INSTANCE.resetSmooth();
    }

    public static boolean hasTarget() {
        return hasTarget;
    }

    public static String getLastEventName() {
        return lastEventName;
    }

    public static double getTargetX() {
        return targetX;
    }

    public static double getTargetY() {
        return targetY;
    }

    public static double getTargetZ() {
        return targetZ;
    }

    @EventTarget
    public void onHudRender(EventHudRender event) {
        float angleDiff;
        if (GPS.mc.player == null || GPS.mc.world == null) {
            return;
        }
        if (this.isAutoTargetExpired()) {
            GPS.clearTarget();
            GPS.mc.player.sendMessage((Text)Text.literal((String)"GPS auto marker expired after 6 minutes"), true);
            return;
        }
        if (!hasTarget) {
            return;
        }
        CustomDrawContext ctx = event.getContext();
        Font font = Fonts.MEDIUM.getFont(5.0f);
        float centerX = (float)mc.getWindow().getScaledWidth() / 2.0f;
        float centerY = (float)mc.getWindow().getScaledHeight() / 2.0f;
        double playerX = GPS.mc.player.getX();
        double playerZ = GPS.mc.player.getZ();
        double deltaX = targetX - playerX;
        double deltaZ = targetZ - playerZ;
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float yaw = GPS.mc.player.getYaw();
        double yawRad = Math.toRadians(yaw);
        double cos = Math.cos(yawRad);
        double sin = Math.sin(yawRad);
        double rotY = -(deltaZ * cos - deltaX * sin);
        double rotX = -(deltaX * cos + deltaZ * sin);
        if (Math.abs(rotX) < 0.01 && Math.abs(rotY) < 0.01) {
            return;
        }
        float targetAngle = (float)(Math.atan2(rotY, rotX) * 180.0 / Math.PI);
        for (angleDiff = targetAngle - this.smoothAngle; angleDiff > 180.0f; angleDiff -= 360.0f) {
        }
        while (angleDiff < -180.0f) {
            angleDiff += 360.0f;
        }
        this.smoothAngle += angleDiff * 0.3f;
        float radius = this.distanceFromCenter.getCurrent();
        double targetArrowX = radius * MathHelper.cos((float)((float)Math.toRadians(targetAngle))) + centerX;
        double targetArrowY = radius * MathHelper.sin((float)((float)Math.toRadians(targetAngle))) + centerY;
        if (!this.smoothInitialized) {
            this.smoothArrowX = targetArrowX;
            this.smoothArrowY = targetArrowY;
            this.smoothAngle = targetAngle;
            this.smoothInitialized = true;
        } else {
            this.smoothArrowX += (targetArrowX - this.smoothArrowX) * 0.3;
            this.smoothArrowY += (targetArrowY - this.smoothArrowY) * 0.3;
        }
        float size = this.arrowSize.getCurrent();
        ColorRGBA arrowColor = this.getColor();
        ctx.getMatrices().push();
        ctx.getMatrices().translate(this.smoothArrowX, this.smoothArrowY, 0.0);
        ctx.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(this.smoothAngle + 90.0f));
        ctx.getMatrices().translate((double)(-size / 2.0f), (double)(-size / 2.0f), 0.0);
        ctx.drawTexture(ARROW_TEXTURE, 0.0f, 0.0f, size, size, arrowColor);
        ctx.getMatrices().pop();
        if (this.showDistance.isEnabled()) {
            String distanceText = String.format("%.0f", distance);
            float textWidth = font.width(distanceText);
            float textAngleRad = (float)Math.toRadians(this.smoothAngle);
            float textX = (float)this.smoothArrowX + MathHelper.cos((float)textAngleRad) * (size / 2.0f + 5.0f);
            float textY = (float)this.smoothArrowY + MathHelper.sin((float)textAngleRad) * (size / 2.0f + 5.0f);
            ctx.drawText(font, distanceText, textX - textWidth / 2.0f, textY - font.height() / 2.0f, arrowColor);
        }
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (!event.isReceive()) {
            return;
        }
        if (event.getPacket() instanceof GameJoinS2CPacket) {
            GPS.clearTarget();
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof GameMessageS2CPacket) {
            GameMessageS2CPacket packet = (GameMessageS2CPacket)packet;
            this.processMessage(packet.comp_763().getString());
        }
    }

    private ColorRGBA getColor() {
        return ColorRGBA.WHITE;
    }

    private void resetSmooth() {
        this.smoothAngle = 0.0f;
        this.smoothArrowX = 0.0;
        this.smoothArrowY = 0.0;
        this.smoothInitialized = false;
    }

    private boolean isAutoTargetExpired() {
        return hasTarget && autoTarget && autoTargetExpireAtMs > 0L && System.currentTimeMillis() >= autoTargetExpireAtMs;
    }

    private void processMessage(String message) {
        if (GPS.mc.player == null) {
            return;
        }
        String cleanMessage = message.replaceAll("§[0-9a-fk-or]", "").replaceAll("&[0-9a-fk-or]", "").replaceAll("[\\x00-\\x1F\\x7F]", "").replaceAll("\\p{C}", "");
        Matcher eventStartedMatcher = EVENT_STARTED_PATTERN.matcher(cleanMessage);
        if (eventStartedMatcher.find()) {
            GPS.clearTarget();
            GPS.mc.player.sendMessage((Text)Text.literal((String)"§cGPS метка снята - §6Ивент начался!"), true);
            return;
        }
        Matcher deathChestMatcher = DEATH_CHEST_PATTERN.matcher(cleanMessage);
        if (deathChestMatcher.find()) {
            try {
                int minutes = Integer.parseInt(deathChestMatcher.group(1));
                if (minutes == 3) {
                    GPS.clearTarget();
                    GPS.mc.player.sendMessage((Text)Text.literal((String)"§cGPS метка снята - §6Сундук Смерти §cпоявится через 3 минуты"), true);
                }
            }
            catch (NumberFormatException minutes) {
                
            }
            return;
        }
        Matcher meteorMatcher = METEOR_PATTERN.matcher(cleanMessage);
        if (meteorMatcher.find()) {
            try {
                int x = Integer.parseInt(meteorMatcher.group(1));
                int y = Integer.parseInt(meteorMatcher.group(2));
                int z = Integer.parseInt(meteorMatcher.group(3));
                GPS.setAutoTarget(x, y, z, "Метеоритный дождь");
                GPS.mc.player.sendMessage((Text)Text.literal((String)("§aGPS метка установлена на §e[" + x + " " + y + " " + z + "]§a от события §6Метеоритный дождь")), true);
            }
            catch (NumberFormatException x) {
                
            }
            return;
        }
        Matcher volcanoMatcher = VOLCANO_PATTERN.matcher(cleanMessage);
        if (volcanoMatcher.find()) {
            try {
                int x = Integer.parseInt(volcanoMatcher.group(1));
                int y = Integer.parseInt(volcanoMatcher.group(2));
                int z = Integer.parseInt(volcanoMatcher.group(3));
                GPS.setAutoTarget(x, y, z, "Вулкан");
                GPS.mc.player.sendMessage((Text)Text.literal((String)("§aGPS метка установлена на §e[" + x + " " + y + " " + z + "]§a от события §6Вулкан")), true);
            }
            catch (NumberFormatException x) {
                
            }
            return;
        }
        Matcher killerBeaconMatcher = KILLER_BEACON_PATTERN.matcher(cleanMessage);
        if (killerBeaconMatcher.find()) {
            try {
                int x = Integer.parseInt(killerBeaconMatcher.group(1));
                int y = Integer.parseInt(killerBeaconMatcher.group(2));
                int z = Integer.parseInt(killerBeaconMatcher.group(3));
                GPS.setAutoTarget(x, y, z, "Маяк убийца");
                GPS.mc.player.sendMessage((Text)Text.literal((String)("§aGPS метка установлена на §e[" + x + " " + y + " " + z + "]§a от события §6Маяк убийца")), true);
            }
            catch (NumberFormatException numberFormatException) {
                
            }
        }
    }
}

