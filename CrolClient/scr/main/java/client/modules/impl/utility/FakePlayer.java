
package crol.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.Hand;
import net.minecraft.StatusEffects;
import net.minecraft.Entity;
import net.minecraft.MovementType;
import net.minecraft.PlayerEntity;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.ItemConvertible;
import net.minecraft.World;
import net.minecraft.Vec3d;
import net.minecraft.ClientPlayPacketListener;
import net.minecraft.EntityStatusS2CPacket;
import net.minecraft.SoundEvent;
import net.minecraft.SoundEvents;
import net.minecraft.SoundCategory;
import net.minecraft.MathHelper;
import net.minecraft.ClientWorld;
import net.minecraft.OtherClientPlayerEntity;
import crol.client.base.events.impl.player.EventAttack;
import crol.client.base.events.impl.player.EventUpdate;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.impl.ButtonSetting;
import crol.client.modules.api.setting.impl.ModeSetting;
import crol.client.modules.api.setting.impl.NumberSetting;
import crol.client.utility.game.other.MessageUtil;

@ModuleAnnotation(name="FakePlayer", category=Category.MOVEMENT, description="Клиентские фейк-игроки.")
public final class FakePlayer
extends Module {
    public static final FakePlayer INSTANCE = new FakePlayer();
    private static final String MODE_DEFAULT = "default";
    private static final String MODE_NETHER = "nether";
    private static final String MODE_DIAMOND = "diamond";
    private static final String MODE_DEFAULT_RU = "Обычный";
    private static final String MODE_NETHER_RU = "Незерит";
    private static final String MODE_DIAMOND_RU = "Алмаз";
    private static final UUID FP_UUID_BASE = UUID.fromString("66123666-6666-6666-6666-666666666600");
    private static final String FP_NAME_BASE = "Фейк игрок";
    private static final int MAX_FAKES = 12;
    private static final int ID_BASE = 1450000;
    private static final float HIT_DAMAGE_HP = 2.0f;
    private static final float LOOK_SMOOTH = 0.35f;
    private final ModeSetting mode = new ModeSetting("Режим", "Обычный", "Незерит", "Алмаз");
    private final NumberSetting count = new NumberSetting("Кол-во", 1.0f, 1.0f, 12.0f, 1.0f);
    private final ButtonSetting spawnButton = new ButtonSetting("Спавн", () -> FakePlayer.add(this.mode.get(), Math.round(this.count.getCurrent())));
    private final ButtonSetting clearButton = new ButtonSetting("Очистить", FakePlayer::delAll);
    private static final Map<Integer, FakePlayerEntity> fakes = new ConcurrentHashMap<Integer, FakePlayerEntity>();
    private static final Map<Integer, Long> lastAttack = new ConcurrentHashMap<Integer, Long>();
    private Object lastWorld;

    private FakePlayer() {
    }

    @Override
    public void onEnable() {
        this.lastWorld = FakePlayer.mc.world;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        FakePlayer.delAll();
        this.lastWorld = null;
        super.onDisable();
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (FakePlayer.mc.world == null || FakePlayer.mc.player == null) {
            return;
        }
        if (this.lastWorld != FakePlayer.mc.world) {
            FakePlayer.delAll();
            this.lastWorld = FakePlayer.mc.world;
        }
        for (FakePlayerEntity fp : fakes.values()) {
            if (fp == null) continue;
            try {
                fp.setVelocity(0.0, fp.getVelocity().y, 0.0);
                fp.setSprinting(false);
                fp.move(MovementType.SELF, Vec3d.ZERO);
            }
            catch (Throwable throwable) {
                
            }
            FakePlayer.lookAtPlayer(fp);
        }
    }

    @EventTarget
    public void onAttack(EventAttack event) {
        Entity entity;
        if (event.getAction() != EventAttack.Action.PRE || !((entity = event.getTarget()) instanceof FakePlayerEntity)) {
            return;
        }
        FakePlayerEntity fp = (FakePlayerEntity)entity;
        if (!FakePlayer.isOurFake(fp)) {
            return;
        }
        event.setCancelled(true);
        FakePlayer.handleDamage(fp.getId());
    }

    public static void add() {
        FakePlayer.add(MODE_DEFAULT, 1);
    }

    public static void add(String mode) {
        FakePlayer.add(mode, 1);
    }

    public static void add(String mode, int count) {
        FakePlayerEntity fp;
        if (FakePlayer.mc.world == null || FakePlayer.mc.player == null) {
            return;
        }
        if (!INSTANCE.isEnabled()) {
            INSTANCE.setToggled(true);
        }
        int need = MathHelper.clamp((int)count, (int)1, (int)12);
        String selectedMode = FakePlayer.normalizeMode(mode);
        for (int i = 1; i <= need; ++i) {
            fp = fakes.get(i);
            if (fp == null) {
                fp = FakePlayer.spawnOne(i, need, selectedMode);
                if (fp == null) continue;
                fakes.put(i, fp);
                continue;
            }
            FakePlayer.positionOne(fp, i, need);
        }
        int removeIndex = need + 1;
        while ((fp = fakes.remove(removeIndex)) != null) {
            try {
                lastAttack.remove(fp.getId());
                fp.remove();
            }
            catch (Throwable throwable) {
                
            }
            ++removeIndex;
        }
    }

    public static void del() {
        FakePlayer.delAll();
    }

    public static void delAll() {
        for (FakePlayerEntity fp : fakes.values()) {
            if (fp == null) continue;
            try {
                lastAttack.remove(fp.getId());
                fp.remove();
            }
            catch (Throwable throwable) {}
        }
        fakes.clear();
        lastAttack.clear();
    }

    public static void del(int id) {
        FakePlayerEntity fp = fakes.remove(id);
        if (fp == null) {
            return;
        }
        try {
            lastAttack.remove(fp.getId());
            fp.remove();
        }
        catch (Throwable throwable) {
            
        }
    }

    public static void printUsage() {
        MessageUtil.displayInfo("Использование: .fakeplayer add <обычный|незерит|алмаз> [кол-во] | del [all|id]");
    }

    private static String normalizeMode(String mode) {
        String normalized;
        if (mode == null) {
            return MODE_DEFAULT;
        }
        return switch (normalized = mode.trim().toLowerCase(Locale.ROOT)) {
            case MODE_NETHER, "незерит", "незеритовый" -> MODE_NETHER;
            case MODE_DIAMOND, "алмаз", "алмазный" -> MODE_DIAMOND;
            case MODE_DEFAULT, "обычный", "дефолт", "дефолтный" -> MODE_DEFAULT;
            default -> MODE_DEFAULT;
        };
    }

    public static void handleDamage(int entityId) {
        if (FakePlayer.mc.player == null || FakePlayer.mc.world == null) {
            return;
        }
        FakePlayerEntity fp = FakePlayer.findByEntityId(entityId);
        if (fp == null) {
            return;
        }
        long now = System.currentTimeMillis();
        long last = lastAttack.getOrDefault(entityId, now - 2000L);
        lastAttack.put(entityId, now);
        float passed = MathHelper.clamp((float)((float)(now - last) / 550.0f), (float)0.0f, (float)1.0f);
        if (passed >= 0.95f && FakePlayer.isCriticalHit()) {
            FakePlayer.playSoundAt((Entity)fp, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT);
        } else if (passed >= 0.95f) {
            FakePlayer.playSoundAt((Entity)fp, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG);
        } else {
            FakePlayer.playSoundAt((Entity)fp, SoundEvents.ENTITY_PLAYER_ATTACK_WEAK);
        }
        FakePlayer.playSoundAt((Entity)fp, SoundEvents.ENTITY_PLAYER_HURT);
        FakePlayer.setHurtFlash((Object)fp, 6);
        try {
            float next = fp.getHealth() - 2.0f;
            if (next > 0.0f) {
                fp.setHealth(next);
                return;
            }
            if (FakePlayer.popTotemAndReset(fp)) {
                return;
            }
            fp.setHealth(0.0f);
        }
        catch (Throwable throwable) {
            
        }
    }

    private static FakePlayerEntity findByEntityId(int id) {
        for (FakePlayerEntity fp : fakes.values()) {
            if (fp == null || fp.getId() != id) continue;
            return fp;
        }
        return null;
    }

    private static boolean isOurFake(FakePlayerEntity fp) {
        for (FakePlayerEntity entity : fakes.values()) {
            if (entity != fp) continue;
            return true;
        }
        return false;
    }

    private static FakePlayerEntity spawnOne(int slot, int total, String mode) {
        FakePlayerEntity fp;
        ClientWorld world;
        block9: {
            block8: {
                ClientWorld NarrationMessageBuilder = FakePlayer.mc.world;
                if (!(NarrationMessageBuilder instanceof ClientWorld)) break block8;
                world = NarrationMessageBuilder;
                if (FakePlayer.mc.player != null) break block9;
            }
            return null;
        }
        UUID uuid = new UUID(FP_UUID_BASE.getMostSignificantBits(), FP_UUID_BASE.getLeastSignificantBits() + (long)(slot - 1));
        Object name = slot == 1 ? FP_NAME_BASE : "Фейк игрок " + slot;
        int forcedId = 1450000 + slot;
        try {
            fp = new FakePlayerEntity(world, new GameProfile(uuid, (String)name), forcedId);
        }
        catch (Throwable ignored) {
            return null;
        }
        try {
            fp.copyPositionAndRotation((Entity)FakePlayer.mc.player);
        }
        catch (Throwable throwable) {
            
        }
        FakePlayer.positionOne(fp, slot, total);
        FakePlayer.equipByMode(fp, mode);
        try {
            fp.setHealth(MathHelper.clamp((float)FakePlayer.mc.player.getHealth(), (float)0.0f, (float)FakePlayer.mc.player.getMaxHealth()));
            fp.setAbsorptionAmount(0.0f);
        }
        catch (Throwable throwable) {
            
        }
        fp.spawn();
        return fp;
    }

    private static void equipByMode(FakePlayerEntity fp, String mode) {
        String normalized = FakePlayer.normalizeMode(mode);
        if (MODE_NETHER.equals(normalized)) {
            fp.setStackInHand(Hand.MAIN_HAND, new ItemStack((ItemConvertible)Items.NETHERITE_SWORD));
            fp.setStackInHand(Hand.OFF_HAND, new ItemStack((ItemConvertible)Items.TOTEM_OF_UNDYING));
            fp.getInventory().armor.set(3, (Object)new ItemStack((ItemConvertible)Items.NETHERITE_HELMET));
            fp.getInventory().armor.set(2, (Object)new ItemStack((ItemConvertible)Items.NETHERITE_CHESTPLATE));
            fp.getInventory().armor.set(1, (Object)new ItemStack((ItemConvertible)Items.NETHERITE_LEGGINGS));
            fp.getInventory().armor.set(0, (Object)new ItemStack((ItemConvertible)Items.NETHERITE_BOOTS));
            return;
        }
        if (MODE_DIAMOND.equals(normalized)) {
            fp.setStackInHand(Hand.MAIN_HAND, new ItemStack((ItemConvertible)Items.DIAMOND_SWORD));
            fp.setStackInHand(Hand.OFF_HAND, new ItemStack((ItemConvertible)Items.TOTEM_OF_UNDYING));
            fp.getInventory().armor.set(3, (Object)new ItemStack((ItemConvertible)Items.DIAMOND_HELMET));
            fp.getInventory().armor.set(2, (Object)new ItemStack((ItemConvertible)Items.DIAMOND_CHESTPLATE));
            fp.getInventory().armor.set(1, (Object)new ItemStack((ItemConvertible)Items.DIAMOND_LEGGINGS));
            fp.getInventory().armor.set(0, (Object)new ItemStack((ItemConvertible)Items.DIAMOND_BOOTS));
            return;
        }
        try {
            fp.setStackInHand(Hand.MAIN_HAND, FakePlayer.mc.player.getMainHandStack().copy());
            ItemStack off = FakePlayer.mc.player.getOffHandStack();
            if (off == null || off.isEmpty() || off.getItem() != Items.TOTEM_OF_UNDYING) {
                fp.setStackInHand(Hand.OFF_HAND, new ItemStack((ItemConvertible)Items.TOTEM_OF_UNDYING));
            } else {
                fp.setStackInHand(Hand.OFF_HAND, off.copy());
            }
            fp.getInventory().armor.set(3, (Object)((ItemStack)FakePlayer.mc.player.getInventory().armor.get(3)).copy());
            fp.getInventory().armor.set(2, (Object)((ItemStack)FakePlayer.mc.player.getInventory().armor.get(2)).copy());
            fp.getInventory().armor.set(1, (Object)((ItemStack)FakePlayer.mc.player.getInventory().armor.get(1)).copy());
            fp.getInventory().armor.set(0, (Object)((ItemStack)FakePlayer.mc.player.getInventory().armor.get(0)).copy());
        }
        catch (Throwable throwable) {
            
        }
    }

    private static void positionOne(FakePlayerEntity fp, int slot, int total) {
        if (fp == null || FakePlayer.mc.player == null) {
            return;
        }
        double radius = 2.2;
        double angle = total <= 1 ? 0.0 : (double)(slot - 1) * (Math.PI * 2) / (double)total;
        Vec3d p = FakePlayer.mc.player.getPos();
        FakePlayer.setPosCompat((Entity)fp, p.x + Math.cos(angle) * radius, p.y, p.z + Math.sin(angle) * radius);
        try {
            float yaw = FakePlayer.mc.player.getYaw();
            float pitch = FakePlayer.mc.player.getPitch();
            fp.setYaw(yaw);
            fp.setBodyYaw(yaw);
            fp.setHeadYaw(yaw);
            fp.setPitch(pitch);
        }
        catch (Throwable throwable) {
            
        }
    }

    private static void setPosCompat(Entity e, double x, double y, double z) {
        try {
            Method m = e.getClass().getMethod("setPos", Double.TYPE, Double.TYPE, Double.TYPE);
            m.invoke((Object)e, x, y, z);
            return;
        }
        catch (Throwable m) {
            try {
                Method m2 = e.getClass().getMethod("setPosition", Double.TYPE, Double.TYPE, Double.TYPE);
                m2.invoke((Object)e, x, y, z);
            }
            catch (Throwable throwable) {
                
            }
            return;
        }
    }

    private static void lookAtPlayer(FakePlayerEntity fp) {
        if (FakePlayer.mc.player == null) {
            return;
        }
        Vec3d from = new Vec3d(fp.getX(), fp.getEyeY(), fp.getZ());
        Vec3d to = new Vec3d(FakePlayer.mc.player.getX(), FakePlayer.mc.player.getEyeY(), FakePlayer.mc.player.getZ());
        Vec3d d = to.subtract(from);
        double distXZ = Math.sqrt(d.x * d.x + d.z * d.z);
        if (distXZ < 1.0E-4) {
            return;
        }
        float targetYaw = (float)(MathHelper.atan2((double)d.z, (double)d.x) * 57.29577951308232) - 90.0f;
        float targetPitch = (float)(-(MathHelper.atan2((double)d.y, (double)distXZ) * 57.29577951308232));
        float yaw = fp.getYaw() + MathHelper.wrapDegrees((float)(targetYaw - fp.getYaw())) * 0.35f;
        float pitch = fp.getPitch() + (targetPitch - fp.getPitch()) * 0.35f;
        pitch = MathHelper.clamp((float)pitch, (float)-89.9f, (float)89.9f);
        fp.setYaw(yaw);
        fp.setBodyYaw(yaw);
        fp.setHeadYaw(yaw);
        fp.setPitch(pitch);
    }

    private static boolean isCriticalHit() {
        if (FakePlayer.mc.player == null) {
            return false;
        }
        if (FakePlayer.mc.player.isOnGround()) {
            return false;
        }
        if (FakePlayer.mc.player.isClimbing()) {
            return false;
        }
        if (FakePlayer.mc.player.isTouchingWater() || FakePlayer.mc.player.isSubmergedInWater()) {
            return false;
        }
        if (FakePlayer.mc.player.isInLava()) {
            return false;
        }
        if (FakePlayer.mc.player.hasVehicle()) {
            return false;
        }
        if (FakePlayer.mc.player.isSprinting()) {
            return false;
        }
        if (FakePlayer.mc.player.getAbilities().flying || FakePlayer.mc.player.isGliding()) {
            return false;
        }
        if (FakePlayer.mc.player.hasStatusEffect(StatusEffects.BLINDNESS)) {
            return false;
        }
        return FakePlayer.mc.player.fallDistance > 0.0f;
    }

    private static void playSoundAt(Entity e, SoundEvent sound) {
        if (FakePlayer.mc.player == null || FakePlayer.mc.world == null || e == null || sound == null) {
            return;
        }
        try {
            FakePlayer.mc.world.playSoundFromEntity((PlayerEntity)FakePlayer.mc.player, e, sound, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
        catch (Throwable throwable) {
            
        }
    }

    private static boolean popTotemAndReset(FakePlayerEntity fp) {
        try {
            boolean mainTotem;
            ItemStack off = fp.getOffHandStack();
            ItemStack main = fp.getMainHandStack();
            boolean offTotem = off != null && !off.isEmpty() && off.getItem() == Items.TOTEM_OF_UNDYING;
            boolean bl = mainTotem = main != null && !main.isEmpty() && main.getItem() == Items.TOTEM_OF_UNDYING;
            if (!offTotem && !mainTotem) {
                return false;
            }
            if (offTotem) {
                fp.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);
            } else {
                fp.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
            }
            FakePlayer.playSoundAt((Entity)fp, SoundEvents.ITEM_TOTEM_USE);
            if (FakePlayer.mc.player != null && FakePlayer.mc.player.networkHandler != null) {
                new EntityStatusS2CPacket((Entity)fp, 35).apply((ClientPlayPacketListener)FakePlayer.mc.player.networkHandler);
            }
            fp.setHealth(Math.max(1.0f, fp.getMaxHealth()));
            fp.setAbsorptionAmount(0.0f);
            fp.setStackInHand(Hand.OFF_HAND, new ItemStack((ItemConvertible)Items.TOTEM_OF_UNDYING));
            FakePlayer.setHurtFlash((Object)fp, 10);
            return true;
        }
        catch (Throwable ignored) {
            return false;
        }
    }

    private static void setHurtFlash(Object entity, int ticks) {
        FakePlayer.trySetIntField(entity, "hurtTime", Math.max(1, ticks));
        FakePlayer.trySetIntField(entity, "maxHurtTime", Math.max(1, ticks));
        FakePlayer.trySetIntField(entity, "timeUntilRegen", 0);
    }

    private static void trySetIntField(Object obj, String name, int value) {
        if (obj == null) {
            return;
        }
        try {
            Field field = FakePlayer.findField(obj.getClass(), name);
            if (field == null) {
                return;
            }
            field.setAccessible(true);
            field.setInt(obj, value);
        }
        catch (Throwable throwable) {
            
        }
    }

    private static Field findField(Class<?> type, String name) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            }
            catch (Throwable throwable) {
                continue;
            }
        }
        return null;
    }

    private static final class FakePlayerEntity
    extends OtherClientPlayerEntity {
        private final int forcedId;

        FakePlayerEntity(ClientWorld world, GameProfile profile, int forcedId) {
            super(world, profile);
            this.forcedId = forcedId;
        }

        void spawn() {
            try {
                this.unsetRemoved();
            }
            catch (Throwable throwable) {
                
            }
            World world = this.getWorld();
            if (!(world instanceof ClientWorld)) {
                FakePlayerEntity.addEntityReflect(this.getWorld(), (Entity)this, this.forcedId);
                return;
            }
            ClientWorld clientWorld = (ClientWorld)world;
            try {
                this.setId(this.forcedId);
            }
            catch (Throwable throwable) {
                
            }
            try {
                clientWorld.removeEntity(this.getId(), Entity.RemovalReason.DISCARDED);
            }
            catch (Throwable throwable) {
                
            }
            try {
                clientWorld.addEntity((Entity)this);
            }
            catch (Throwable ignored) {
                FakePlayerEntity.addEntityReflect(clientWorld, (Entity)this, this.forcedId);
            }
        }

        void remove() {
            World world = this.getWorld();
            if (world instanceof ClientWorld) {
                ClientWorld clientWorld = (ClientWorld)world;
                try {
                    clientWorld.removeEntity(this.getId(), Entity.RemovalReason.DISCARDED);
                }
                catch (Throwable ignored) {
                    FakePlayerEntity.removeEntityReflect(clientWorld, this.getId());
                }
            } else {
                FakePlayerEntity.removeEntityReflect(this.getWorld(), this.getId());
            }
            try {
                this.onRemoved();
            }
            catch (Throwable throwable) {
                
            }
        }

        public void takeKnockback(double strength, double x, double z) {
        }

        private static void addEntityReflect(Object world, Entity entity, int id) {
            Method oneArg = null;
            Method twoArgs = null;
            for (Method method : world.getClass().getMethods()) {
                if (!"addEntity".equals(method.getName())) continue;
                Class<?>[] params = method.getParameterTypes();
                if (params.length == 2 && params[0] == Integer.TYPE && Entity.class.isAssignableFrom(params[1])) {
                    twoArgs = method;
                    break;
                }
                if (params.length != 1 || !Entity.class.isAssignableFrom(params[0])) continue;
                oneArg = method;
            }
            try {
                if (twoArgs != null) {
                    twoArgs.invoke(world, id, entity);
                    return;
                }
                if (oneArg != null) {
                    oneArg.invoke(world, entity);
                }
            }
            catch (Throwable throwable) {
                
            }
        }

        private static void removeEntityReflect(Object world, int id) {
            Method oneArg = null;
            Method twoArgs = null;
            for (Method method : world.getClass().getMethods()) {
                if (!"removeEntity".equals(method.getName())) continue;
                Class<?>[] params = method.getParameterTypes();
                if (params.length == 2 && params[0] == Integer.TYPE) {
                    twoArgs = method;
                    break;
                }
                if (params.length != 1 || params[0] != Integer.TYPE) continue;
                oneArg = method;
            }
            try {
                if (twoArgs != null) {
                    Object reason = FakePlayerEntity.defaultEnumValue(twoArgs.getParameterTypes()[1]);
                    twoArgs.invoke(world, id, reason);
                    return;
                }
                if (oneArg != null) {
                    oneArg.invoke(world, id);
                }
            }
            catch (Throwable throwable) {
                
            }
        }

        private static Object defaultEnumValue(Class<?> maybeEnum) {
            if (maybeEnum == null || !maybeEnum.isEnum()) {
                return null;
            }
            ?[] constants = maybeEnum.getEnumConstants();
            if (constants == null || constants.length == 0) {
                return null;
            }
            for (Object value : constants) {
                if (!"DISCARDED".equals(String.valueOf(value))) continue;
                return value;
            }
            return constants[0];
        }
    }
}

