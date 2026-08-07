
package vurst.visual.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import com.google.gson.JsonObject;
import net.minecraft.Entity;
import net.minecraft.Vec3d;
import net.minecraft.MathHelper;
import net.minecraft.Perspective;
import vurst.visual.base.events.impl.input.EventMouseRotation;
import vurst.visual.base.events.impl.player.EventAttack;
import vurst.visual.base.events.impl.player.EventUpdate;
import vurst.visual.base.events.impl.render.EventCamera;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.ModeSetting;
import vurst.visual.utility.game.player.PlayerIntersectionUtil;
import vurst.visual.utility.game.player.rotation.Rotation;
import vurst.visual.utility.render.level.Render3DUtil;

@ModuleAnnotation(name="FreeLook", category=Category.MOVEMENT, description="Свободный обзор камерой.")
public final class FreeLook
extends Module {
    public static final FreeLook INSTANCE = new FreeLook();
    private static final String SETTING_MODE = "Режим";
    private static final String MODE_TOGGLE = "Нажатие";
    private static final String MODE_HOLD = "Удержание";
    private static final float MOUSE_SENSITIVITY = 0.15f;
    private static final float THIRD_PERSON_DISTANCE = 4.0f;
    private final ModeSetting mode = new ModeSetting("Режим", "Нажатие", "Удержание");
    private Perspective previousPerspective;
    private float lockedYaw;
    private float lockedPitch;
    private float freeYaw;
    private float freePitch;
    private float prevFreeYaw;
    private float prevFreePitch;

    private FreeLook() {
    }

    @Override
    public void load(JsonObject object) {
        this.migrateLegacySettings(object);
        super.load(object);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (FreeLook.mc.player == null || FreeLook.mc.world == null) {
            return;
        }
        this.previousPerspective = FreeLook.mc.options.getPerspective();
        this.lockedYaw = FreeLook.mc.player.getYaw();
        this.lockedPitch = FreeLook.mc.player.getPitch();
        this.freeYaw = this.lockedYaw;
        this.freePitch = this.lockedPitch;
        this.prevFreeYaw = this.freeYaw;
        this.prevFreePitch = this.freePitch;
    }

    @Override
    public void onDisable() {
        if (FreeLook.mc.player != null) {
            FreeLook.mc.player.setYaw(this.lockedYaw);
            FreeLook.mc.player.setPitch(this.lockedPitch);
        }
        if (this.previousPerspective != null) {
            FreeLook.mc.options.setPerspective(this.previousPerspective);
            this.previousPerspective = null;
        }
        super.onDisable();
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (FreeLook.mc.player == null || FreeLook.mc.world == null) {
            return;
        }
        if (this.mode.is(MODE_HOLD) && !this.isHoldKeyDown()) {
            this.toggle();
            return;
        }
        if (this.previousPerspective == null) {
            this.previousPerspective = FreeLook.mc.options.getPerspective();
        }
        FreeLook.mc.options.setPerspective(this.shouldCollapseCameraIntoPlayer() ? Perspective.FIRST_PERSON : Perspective.THIRD_PERSON_BACK);
        FreeLook.mc.player.setYaw(this.lockedYaw);
        FreeLook.mc.player.setPitch(this.lockedPitch);
        FreeLook.mc.player.lastYaw = this.lockedYaw;
        FreeLook.mc.player.lastPitch = this.lockedPitch;
        FreeLook.mc.player.setHeadYaw(this.lockedYaw);
        FreeLook.mc.player.prevHeadYaw = this.lockedYaw;
        FreeLook.mc.player.bodyYaw = this.lockedYaw;
        FreeLook.mc.player.prevBodyYaw = this.lockedYaw;
        this.prevFreeYaw = this.freeYaw;
        this.prevFreePitch = this.freePitch;
    }

    @EventTarget
    public void onMouseRotation(EventMouseRotation event) {
        if (FreeLook.mc.player == null || FreeLook.mc.world == null) {
            return;
        }
        this.prevFreeYaw = this.freeYaw;
        this.prevFreePitch = this.freePitch;
        float yawDelta = event.getCursorDeltaX() * 0.15f;
        float pitchDelta = event.getCursorDeltaY() * 0.15f;
        this.freeYaw += yawDelta;
        this.freePitch = MathHelper.clamp((float)(this.freePitch + pitchDelta), (float)-90.0f, (float)90.0f);
        event.setCancelled(true);
    }

    @EventTarget
    public void onCamera(EventCamera event) {
        if (FreeLook.mc.player == null || FreeLook.mc.world == null) {
            return;
        }
        float partialTicks = Render3DUtil.getTickDelta();
        event.setAngle(new Rotation(FreeLook.getActualYaw(partialTicks), FreeLook.getActualPitch(partialTicks)));
        event.setDistance(this.shouldCollapseCameraIntoPlayer() ? 0.0f : 4.0f);
        event.setCancelled(true);
    }

    @EventTarget
    public void onAttack(EventAttack event) {
        if (event.getAction() != EventAttack.Action.PRE || FreeLook.mc.player == null || FreeLook.mc.world == null) {
            return;
        }
        Entity target = event.getTarget();
        if (target == null) {
            return;
        }
        Vec3d from = FreeLook.mc.player.getCameraPosVec(1.0f);
        Vec3d toTarget = target.getPos().add(0.0, (double)target.getHeight() * 0.5, 0.0).subtract(from).normalize();
        Vec3d look = this.getLockedLookVector();
        if (look.lengthSquared() <= 1.0E-6) {
            return;
        }
        double dot = look.dotProduct(toTarget);
        if (dot <= 0.0) {
            event.setCancelled(true);
        }
    }

    private Vec3d getLockedLookVector() {
        float yawRad = (float)Math.toRadians(-this.lockedYaw);
        float pitchRad = (float)Math.toRadians(-this.lockedPitch);
        float cosYaw = MathHelper.cos((float)yawRad);
        float sinYaw = MathHelper.sin((float)yawRad);
        float cosPitch = MathHelper.cos((float)pitchRad);
        float sinPitch = MathHelper.sin((float)pitchRad);
        return new Vec3d((double)(sinYaw * cosPitch), (double)sinPitch, (double)(cosYaw * cosPitch)).normalize();
    }

    private boolean isHoldKeyDown() {
        int keyCode = this.getKeyCode();
        if (keyCode == -1) {
            return false;
        }
        return PlayerIntersectionUtil.isKey(PlayerIntersectionUtil.getKeyType(keyCode), keyCode);
    }

    public static boolean isActive() {
        return INSTANCE.isEnabled();
    }

    public static float getYaw(float partialTicks) {
        return partialTicks >= 1.0f ? FreeLook.INSTANCE.freeYaw : FreeLook.INSTANCE.prevFreeYaw + (FreeLook.INSTANCE.freeYaw - FreeLook.INSTANCE.prevFreeYaw) * partialTicks;
    }

    public static float getPitch(float partialTicks) {
        return partialTicks >= 1.0f ? FreeLook.INSTANCE.freePitch : FreeLook.INSTANCE.prevFreePitch + (FreeLook.INSTANCE.freePitch - FreeLook.INSTANCE.prevFreePitch) * partialTicks;
    }

    public static float getActualYaw(float partialTicks) {
        if (FreeLook.isActive()) {
            return FreeLook.getYaw(partialTicks);
        }
        return FreeLook.mc.player != null ? FreeLook.mc.player.getYaw(partialTicks) : 0.0f;
    }

    public static float getActualPitch(float partialTicks) {
        if (FreeLook.isActive()) {
            return FreeLook.getPitch(partialTicks);
        }
        return FreeLook.mc.player != null ? FreeLook.mc.player.getPitch(partialTicks) : 0.0f;
    }

    public static Vec3d getActualLookVector(float partialTicks) {
        if (!FreeLook.isActive() && FreeLook.mc.player != null) {
            return FreeLook.mc.player.getRotationVec(partialTicks);
        }
        if (!FreeLook.isActive()) {
            return Vec3d.ZERO;
        }
        return Vec3d.fromPolar((float)FreeLook.getPitch(partialTicks), (float)FreeLook.getYaw(partialTicks));
    }

    public static Vec3d getInteractionDirection(float partialTicks) {
        return FreeLook.getActualLookVector(partialTicks);
    }

    public static float getActualYaw() {
        return FreeLook.getActualYaw(Render3DUtil.getTickDelta());
    }

    public static float getActualPitch() {
        return FreeLook.getActualPitch(Render3DUtil.getTickDelta());
    }

    public static float getLockedYaw() {
        if (FreeLook.isActive()) {
            return FreeLook.INSTANCE.lockedYaw;
        }
        return FreeLook.mc.player != null ? FreeLook.mc.player.getYaw() : 0.0f;
    }

    public static float getLockedPitch() {
        if (FreeLook.isActive()) {
            return FreeLook.INSTANCE.lockedPitch;
        }
        return FreeLook.mc.player != null ? FreeLook.mc.player.getPitch() : 0.0f;
    }

    private boolean shouldCollapseCameraIntoPlayer() {
        return FreeLook.mc.player != null && FreeLook.mc.world != null && (FreeLook.mc.player.isInsideWall() || !FreeLook.mc.world.isSpaceEmpty((Entity)FreeLook.mc.player, FreeLook.mc.player.getBoundingBox().contract(1.0E-7)));
    }

    private void migrateLegacySettings(JsonObject object) {
        String normalized;
        String currentMode;
        if (object == null || !object.has("Settings") || !object.get("Settings").isJsonObject()) {
            return;
        }
        JsonObject settings = object.getAsJsonObject("Settings");
        if (!settings.has(SETTING_MODE) && settings.has("Mode")) {
            settings.add(SETTING_MODE, settings.get("Mode").deepCopy());
        }
        if (!settings.has(SETTING_MODE)) {
            return;
        }
        switch (currentMode = settings.get(SETTING_MODE).getAsString()) {
            case "Toggle": {
                String string = MODE_TOGGLE;
                break;
            }
            case "Hold": {
                String string = MODE_HOLD;
                break;
            }
            default: {
                String string = normalized = currentMode;
            }
        }
        if (!currentMode.equals(normalized)) {
            settings.addProperty(SETTING_MODE, normalized);
        }
    }
}

