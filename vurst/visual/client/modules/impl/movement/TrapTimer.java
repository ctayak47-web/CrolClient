
package vurst.visual.client.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import net.minecraft.Entity;
import net.minecraft.Item;
import net.minecraft.Items;
import net.minecraft.BlockPos;
import net.minecraft.Position;
import net.minecraft.Vec3i;
import net.minecraft.HitResult;
import net.minecraft.Vec3d;
import net.minecraft.Packet;
import net.minecraft.PlaySoundS2CPacket;
import net.minecraft.Identifier;
import net.minecraft.SoundEvent;
import net.minecraft.MathHelper;
import net.minecraft.RaycastContext;
import net.minecraft.BlockHitResult;
import net.minecraft.Registries;
import vurst.visual.base.events.impl.player.EventUpdate;
import vurst.visual.base.events.impl.render.EventRender2D;
import vurst.visual.base.events.impl.server.EventPacket;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.utility.interfaces.IMinecraft;
import vurst.visual.utility.render.display.base.CustomDrawContext;

@ModuleAnnotation(name="TrapTimer", category=Category.MOVEMENT, description="Показывает информацию о трапках и пластах в мире.")
public final class TrapTimer
extends Module {
    public static final TrapTimer INSTANCE = new TrapTimer();
    private static final float FLOAT_EPSILON = 0.01f;
    private static final int MESSAGE_COLOR = -1;
    private static final int TIME_COLOR = -11665409;
    private static final int MESSAGE_MARGIN_TOP = 4;
    private static final int MESSAGE_GAP_ABOVE_HEARTS = 10;
    private static final int MESSAGE_EXTRA_UP_OFFSET = 20;
    private static final String SOUND_PISTON_EXTEND = "minecraft:block.piston.extend";
    private static final String SOUND_PISTON_CONTRACT = "minecraft:block.piston.contract";
    private static final String SOUND_EVOKER_FANGS = "minecraft:entity.evoker_fangs.attack";
    private static final long OWN_USE_WINDOW_MS = 700L;
    private static final double OWN_SOUND_MAX_DISTANCE = 7.0;
    private static final double OWN_SOUND_MAX_DISTANCE_SQ = 49.0;
    private final Map<BlockPos, Info> infos = new HashMap<BlockPos, Info>();
    private long lastTrapUseMillis;

    private TrapTimer() {
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (!this.isEnabled() || TrapTimer.mc.player == null || TrapTimer.mc.world == null) {
            return;
        }
        if (!TrapTimer.mc.options.useKey.isPressed()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (this.isHoldingInHands(Items.NETHERITE_SCRAP)) {
            this.lastTrapUseMillis = now;
        }
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (!this.isEnabled() || !event.isReceive() || TrapTimer.mc.player == null) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (!(packet instanceof PlaySoundS2CPacket)) {
            return;
        }
        PlaySoundS2CPacket sound = (PlaySoundS2CPacket)packet;
        Identifier soundId = Registries.SOUND_EVENT.getId((Object)((SoundEvent)sound.getSound().comp_349()));
        if (soundId == null) {
            return;
        }
        String soundName = soundId.toString();
        Vec3d soundPos = new Vec3d(sound.getX(), sound.getY(), sound.getZ());
        BlockPos pos = BlockPos.ofFloored((double)sound.getX(), (double)sound.getY(), (double)sound.getZ());
        if ((soundName.contains(SOUND_PISTON_EXTEND) || soundName.contains(SOUND_PISTON_CONTRACT)) && (TrapTimer.isNear(sound.getVolume(), 0.5f) || TrapTimer.isNear(sound.getVolume(), 0.7f)) && TrapTimer.isNear(sound.getPitch(), 0.5f) && this.isOwnUse(soundPos)) {
            this.infos.put(pos, new Info(this, pos.up(), ObjType.TRAP));
            return;
        }
        if (soundName.contains(SOUND_EVOKER_FANGS) && (TrapTimer.isNear(sound.getVolume(), 0.5f) || TrapTimer.isNear(sound.getVolume(), 0.7f)) && !TrapTimer.isNear(sound.getPitch(), 0.85f) && TrapTimer.isNear(sound.getPitch(), 1.0f) && this.isOwnUse(soundPos)) {
            this.infos.put(pos, new Info(this, pos.up(), ObjType.DRAGON_FT));
        }
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        if (!this.isEnabled() || TrapTimer.mc.player == null) {
            return;
        }
        Info nearestVisibleInfo = null;
        double nearestDistanceSq = Double.MAX_VALUE;
        Iterator<Map.Entry<BlockPos, Info>> iterator2 = this.infos.entrySet().iterator();
        while (iterator2.hasNext()) {
            double distanceSq;
            Info info = iterator2.next().getValue();
            if (info.isExpired()) {
                iterator2.remove();
                continue;
            }
            if (!info.isVisible() || !((distanceSq = TrapTimer.mc.player.squaredDistanceTo(Vec3d.ofCenter((Vec3i)info.pos))) < nearestDistanceSq)) continue;
            nearestDistanceSq = distanceSq;
            nearestVisibleInfo = info;
        }
        if (nearestVisibleInfo != null) {
            this.drawScreenMessage(event.getContext(), nearestVisibleInfo);
        }
    }

    public void addTrapTimer(Vec3d position) {
        if (!this.isEnabled()) {
            return;
        }
        BlockPos pos = BlockPos.ofFloored((Position)position);
        this.infos.put(pos, new Info(this, pos.up(), ObjType.TRAP));
    }

    @Override
    public void onDisable() {
        this.infos.clear();
        this.lastTrapUseMillis = 0L;
        super.onDisable();
    }

    private static boolean isNear(float value, float target) {
        return Math.abs(value - target) < 0.01f;
    }

    private boolean isOwnUse(Vec3d soundPos) {
        if (TrapTimer.mc.player == null) {
            return false;
        }
        if (TrapTimer.mc.player.getPos().squaredDistanceTo(soundPos) > 49.0) {
            return false;
        }
        long now = System.currentTimeMillis();
        return now - this.lastTrapUseMillis <= 700L;
    }

    private boolean isHoldingInHands(Item item) {
        return TrapTimer.mc.player != null && (TrapTimer.mc.player.getMainHandStack().isOf(item) || TrapTimer.mc.player.getOffHandStack().isOf(item));
    }

    private void drawScreenMessage(CustomDrawContext ctx, Info info) {
        if (TrapTimer.mc.player == null || mc.getWindow() == null) {
            return;
        }
        long elapsed = System.currentTimeMillis() - info.startTime;
        long remainMs = Math.max(0L, info.type.timeMs - elapsed);
        String remainText = String.format(Locale.US, "%.1f", (double)remainMs / 1000.0).replace('.', ',');
        String prefix = info.type.messagePrefix;
        String suffix = " сек.";
        int prefixWidth = TrapTimer.mc.textRenderer.getWidth(prefix);
        int remainWidth = TrapTimer.mc.textRenderer.getWidth(remainText);
        int suffixWidth = TrapTimer.mc.textRenderer.getWidth(suffix);
        int totalWidth = prefixWidth + remainWidth + suffixWidth;
        int x = (mc.getWindow().getScaledWidth() - totalWidth) / 2;
        int y = this.resolveHudSafeY();
        ctx.drawTextWithShadow(TrapTimer.mc.textRenderer, prefix, x, y, -1);
        ctx.drawTextWithShadow(TrapTimer.mc.textRenderer, remainText, x += prefixWidth, y, -11665409);
        ctx.drawTextWithShadow(TrapTimer.mc.textRenderer, suffix, x += remainWidth, y, -1);
    }

    private int resolveHudSafeY() {
        int scaledHeight = mc.getWindow().getScaledHeight();
        if (TrapTimer.mc.player == null) {
            return Math.max(4, scaledHeight - 44);
        }
        float totalHealth = Math.max(TrapTimer.mc.player.getMaxHealth(), TrapTimer.mc.player.getHealth()) + TrapTimer.mc.player.getAbsorptionAmount();
        int heartRows = Math.max(1, MathHelper.ceil((float)(totalHealth / 20.0f)));
        int rowHeight = Math.max(10 - (heartRows - 2), 3);
        int bottomHeartsY = scaledHeight - 39;
        int topHeartsY = bottomHeartsY - (heartRows - 1) * rowHeight;
        int y = topHeartsY - 10 - 20;
        return Math.max(4, y);
    }

    private final class Info {
        private final BlockPos pos;
        private final ObjType type;
        private final long startTime;

        private Info(TrapTimer trapTimer, BlockPos pos, ObjType type) {
            this.pos = pos;
            this.type = type;
            this.startTime = System.currentTimeMillis();
        }

        private boolean isExpired() {
            return System.currentTimeMillis() - this.startTime > this.type.timeMs;
        }

        private boolean isVisible() {
            Vec3d to;
            if (IMinecraft.mc.world == null || IMinecraft.mc.player == null) {
                return false;
            }
            Vec3d from = IMinecraft.mc.player.getCameraPosVec(1.0f);
            BlockHitResult hit = IMinecraft.mc.world.raycast(new RaycastContext(from, to = Vec3d.ofCenter((Vec3i)this.pos).add(0.0, 0.6, 0.0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)IMinecraft.mc.player));
            if (hit.getType() == HitResult.Type.MISS) {
                return true;
            }
            BlockPos hitPos = BlockPos.ofFloored((Position)hit.getPos());
            return hitPos.equals((Object)this.pos) || hitPos.equals((Object)this.pos.up());
        }
    }

    private static enum ObjType {
        TRAP("Трапка закончится через ", Items.NETHERITE_SCRAP, 15000L),
        DRAGON_FT("Драконья трапка закончится через ", Items.NETHERITE_SCRAP, 30000L),
        DRAGON_ST("Драконья трапка закончится через ", Items.NETHERITE_SCRAP, 30000L);

        private final String messagePrefix;
        private final Item item;
        private final long timeMs;

        private ObjType(String messagePrefix, Item item, long timeMs) {
            this.messagePrefix = messagePrefix;
            this.item = item;
            this.timeMs = timeMs;
        }
    }
}

