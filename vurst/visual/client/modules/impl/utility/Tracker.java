
package vurst.visual.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.Formatting;
import net.minecraft.StatusEffect;
import net.minecraft.StatusEffectInstance;
import net.minecraft.StatusEffects;
import net.minecraft.Entity;
import net.minecraft.PlayerEntity;
import net.minecraft.PotionEntity;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.PotionContentsComponent;
import net.minecraft.Box;
import net.minecraft.Vec3d;
import net.minecraft.Text;
import net.minecraft.Identifier;
import net.minecraft.MutableText;
import net.minecraft.Registries;
import net.minecraft.DataComponentTypes;
import vurst.visual.base.events.impl.player.EventUpdate;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.BooleanSetting;
import vurst.visual.client.modules.api.setting.impl.NumberSetting;

@ModuleAnnotation(name="Tracker", category=Category.MOVEMENT, description="Показывает, что игроки съели, выпили и кого забаффали.")
public final class Tracker
extends Module {
    public static final Tracker INSTANCE = new Tracker();
    private static final float SPLASH_RADIUS = 4.0f;
    private static final float SPLASH_HALF_HEIGHT = 2.0f;
    private static final int MIN_USE_TICKS = 8;
    private static final long DUPLICATE_EVENT_WINDOW_MS = 900L;
    private static final long EVENT_CACHE_LIFETIME_MS = 5000L;
    private static final float MAX_TRACK_DISTANCE = 100.0f;
    private final NumberSetting radius = new NumberSetting("Радиус", 50.0f, 5.0f, 100.0f, 1.0f);
    private final BooleanSetting trackFood = new BooleanSetting("Отслеживать еду", true);
    private final BooleanSetting trackDrink = new BooleanSetting("Отслеживать питье", true);
    private final BooleanSetting trackSplash = new BooleanSetting("Отслеживать донки", true);
    private final BooleanSetting showEffects = new BooleanSetting("Показывать эффекты", true);
    private final Map<UUID, UseState> useStates = new HashMap<UUID, UseState>();
    private final Map<UUID, Map<String, StatusEffectInstance>> lastEffects = new HashMap<UUID, Map<String, StatusEffectInstance>>();
    private final Map<Integer, TrackedPotion> trackedPotions = new HashMap<Integer, TrackedPotion>();
    private final Map<String, Long> recentEventKeys = new HashMap<String, Long>();

    private Tracker() {
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (Tracker.mc.player == null || Tracker.mc.world == null) {
            this.clearState();
            return;
        }
        this.trackPlayerConsumables();
        this.trackSplashPotions();
    }

    @Override
    public void onDisable() {
        this.clearState();
        super.onDisable();
    }

    private void trackPlayerConsumables() {
        HashSet<UUID> visiblePlayers = new HashSet<UUID>();
        for (PlayerEntity player : Tracker.mc.world.getPlayers()) {
            if (!this.isTrackablePlayer(player) || !this.isInRange((Entity)player)) continue;
            UUID uuid = player.getUuid();
            visiblePlayers.add(uuid);
            Map<String, StatusEffectInstance> previous = this.lastEffects.getOrDefault(uuid, Map.of());
            Map<String, StatusEffectInstance> current = this.snapshotEffects(player);
            List<StatusEffectInstance> addedEffects = this.getAddedEffects(previous, current);
            this.processUseState(player, addedEffects);
            this.lastEffects.put(uuid, current);
        }
        this.useStates.keySet().removeIf(id -> !visiblePlayers.contains(id));
        this.lastEffects.keySet().removeIf(id -> !visiblePlayers.contains(id));
    }

    private void processUseState(PlayerEntity player, List<StatusEffectInstance> addedEffects) {
        UUID uuid = player.getUuid();
        UseState state = this.useStates.computeIfAbsent(uuid, id -> new UseState());
        if (player.isUsingItem()) {
            ItemStack activeStack = player.getActiveItem();
            if (!state.using) {
                state.using = true;
                state.startAge = player.age;
                state.item = activeStack.copy();
            } else if (state.item.isEmpty() && !activeStack.isEmpty()) {
                state.item = activeStack.copy();
            }
            return;
        }
        if (!state.using) {
            return;
        }
        int useTicks = Math.max(0, player.age - state.startAge);
        ItemStack usedItem = state.item;
        state.reset();
        if (useTicks < 8 || usedItem == null || usedItem.isEmpty()) {
            return;
        }
        this.handleConsumedItem(player, usedItem, addedEffects);
    }

    private void handleConsumedItem(PlayerEntity player, ItemStack usedItem, List<StatusEffectInstance> addedEffects) {
        if (this.trackDrink.isEnabled() && this.isDrinkable(usedItem)) {
            if (this.isDuplicateEvent("drink:" + String.valueOf(player.getUuid()) + ":" + this.getItemKey(usedItem))) {
                return;
            }
            this.printAction(player, "выпил", usedItem.getName().getString());
            if (this.showEffects.isEnabled()) {
                List<StatusEffectInstance> potionEffects = this.getPotionEffects(usedItem);
                this.printEffects(potionEffects.isEmpty() ? addedEffects : potionEffects, 1.0f);
            }
            return;
        }
        if (this.trackFood.isEnabled() && this.isFood(usedItem)) {
            if (this.isDuplicateEvent("food:" + String.valueOf(player.getUuid()) + ":" + this.getItemKey(usedItem))) {
                return;
            }
            this.printAction(player, "съел", usedItem.getName().getString());
            if (this.showEffects.isEnabled()) {
                this.printEffects(addedEffects, 1.0f);
            }
        }
    }

    private void trackSplashPotions() {
        if (!this.trackSplash.isEnabled()) {
            this.trackedPotions.clear();
            return;
        }
        HashSet<Integer> currentPotionIds = new HashSet<Integer>();
        for (Entity entity : Tracker.mc.world.getEntities()) {
            PotionEntity potion;
            int id;
            boolean alreadyTracked;
            if (!(entity instanceof PotionEntity) || !(alreadyTracked = this.trackedPotions.containsKey(id = (potion = (PotionEntity)entity).getId())) && !this.isInRange((Entity)potion)) continue;
            currentPotionIds.add(id);
            TrackedPotion tracked = this.trackedPotions.get(id);
            if (tracked == null) {
                UUID ownerUuid = null;
                Entity owner = potion.getOwner();
                if (owner instanceof PlayerEntity) {
                    PlayerEntity ownerPlayer = (PlayerEntity)owner;
                    ownerUuid = ownerPlayer.getUuid();
                }
                this.trackedPotions.put(id, new TrackedPotion(potion.getStack().copy(), potion.getPos(), ownerUuid));
                continue;
            }
            tracked.lastPos = potion.getPos();
        }
        Iterator<Map.Entry<Integer, TrackedPotion>> iterator2 = this.trackedPotions.entrySet().iterator();
        while (iterator2.hasNext()) {
            Map.Entry<Integer, TrackedPotion> entry = iterator2.next();
            if (currentPotionIds.contains(entry.getKey())) continue;
            this.handleSplashImpact(entry.getValue());
            iterator2.remove();
        }
    }

    private void handleSplashImpact(TrackedPotion tracked) {
        if (tracked == null || tracked.stack == null || tracked.stack.isEmpty()) {
            return;
        }
        List<StatusEffectInstance> effects = this.getPotionEffects(tracked.stack);
        if (effects.isEmpty()) {
            return;
        }
        PlayerEntity thrower = this.getPlayerByUuid(tracked.ownerUuid);
        String throwerName = thrower != null ? thrower.getName().getString() : "Кто-то";
        Box splashBox = new Box(tracked.lastPos.x - 4.0, tracked.lastPos.y - 2.0, tracked.lastPos.z - 4.0, tracked.lastPos.x + 4.0, tracked.lastPos.y + 2.0, tracked.lastPos.z + 4.0);
        for (PlayerEntity target : Tracker.mc.world.getEntitiesByClass(PlayerEntity.class, splashBox, this::isTrackablePlayer)) {
            String splashKey;
            float hitFactor;
            double distance;
            if (!this.isInRange((Entity)target) || (distance = target.getPos().distanceTo(tracked.lastPos)) > 4.0 || (hitFactor = (float)Math.max(0.0, 1.0 - distance / 4.0)) <= 0.0f || this.isDuplicateEvent(splashKey = "splash:" + String.valueOf(tracked.ownerUuid != null ? tracked.ownerUuid : throwerName) + ":" + String.valueOf(target.getUuid()) + ":" + this.getItemKey(tracked.stack))) continue;
            this.printBuffAction(throwerName, target.getName().getString(), tracked.stack.getName().getString(), hitFactor);
            if (!this.showEffects.isEnabled()) continue;
            this.printEffects(effects, hitFactor);
        }
    }

    private void printAction(PlayerEntity player, String action, String itemName) {
        MutableText message = this.prefix().append((Text)Text.literal((String)player.getName().getString()).formatted(Formatting.WHITE)).append((Text)Text.literal((String)(" " + action + " ")).formatted(Formatting.GRAY)).append((Text)Text.literal((String)itemName).formatted(Formatting.GREEN));
        this.pushMessage((Text)message);
    }

    private void printBuffAction(String throwerName, String targetName, String potionName, float hitFactor) {
        int percent = Math.max(0, Math.min(100, Math.round(hitFactor * 100.0f)));
        MutableText message = this.prefix().append((Text)Text.literal((String)throwerName).formatted(Formatting.WHITE)).append((Text)Text.literal((String)" забаффал ").formatted(Formatting.GRAY)).append((Text)Text.literal((String)targetName).formatted(Formatting.WHITE)).append((Text)Text.literal((String)" ").formatted(Formatting.GRAY)).append((Text)Text.literal((String)potionName).formatted(Formatting.AQUA)).append((Text)Text.literal((String)(" (" + percent + "%)")).formatted(Formatting.DARK_GRAY));
        this.pushMessage((Text)message);
    }

    private void printEffects(List<StatusEffectInstance> effects, float potencyFactor) {
        if (effects == null || effects.isEmpty()) {
            return;
        }
        for (StatusEffectInstance effect : effects) {
            if (effect == null) continue;
            String effectName = ((StatusEffect)effect.getEffectType().comp_349()).getName().getString();
            int amplifier = effect.getAmplifier() + 1;
            boolean instant = ((StatusEffect)effect.getEffectType().comp_349()).isInstant();
            MutableText line = Text.literal((String)"• ").formatted(Formatting.DARK_GRAY).append((Text)Text.literal((String)effectName).formatted(Formatting.RED)).append((Text)Text.literal((String)(" " + this.toRoman(amplifier))).formatted(Formatting.RED));
            if (!instant) {
                int seconds = Math.max(0, Math.round((float)effect.getDuration() / 20.0f * Math.max(0.0f, potencyFactor)));
                line = line.append((Text)Text.literal((String)(" (" + this.formatDuration(seconds) + ")")).formatted(Formatting.GRAY));
            }
            this.pushMessage((Text)line);
        }
    }

    private MutableText prefix() {
        return Text.literal((String)"[Vurst Visual]").formatted(Formatting.BLUE).append((Text)Text.literal((String)" >> ").formatted(Formatting.GRAY));
    }

    private void pushMessage(Text message) {
        if (Tracker.mc.inGameHud != null && Tracker.mc.inGameHud.getChatHud() != null) {
            Tracker.mc.inGameHud.getChatHud().addMessage(message);
            return;
        }
        if (Tracker.mc.player != null) {
            Tracker.mc.player.sendMessage(message, false);
        }
    }

    private List<StatusEffectInstance> getAddedEffects(Map<String, StatusEffectInstance> previous, Map<String, StatusEffectInstance> current) {
        if (current.isEmpty()) {
            return List.of();
        }
        ArrayList<StatusEffectInstance> added = new ArrayList<StatusEffectInstance>();
        for (Map.Entry<String, StatusEffectInstance> entry : current.entrySet()) {
            StatusEffectInstance now = entry.getValue();
            StatusEffectInstance old = previous.get(entry.getKey());
            if (old != null && now.getAmplifier() <= old.getAmplifier() && now.getDuration() <= old.getDuration() + 20) continue;
            added.add(now);
        }
        return added;
    }

    private Map<String, StatusEffectInstance> snapshotEffects(PlayerEntity player) {
        HashMap<String, StatusEffectInstance> effects = new HashMap<String, StatusEffectInstance>();
        for (StatusEffectInstance effect : player.getStatusEffects()) {
            Identifier id = Registries.STATUS_EFFECT.getId((Object)((StatusEffect)effect.getEffectType().comp_349()));
            if (id == null) continue;
            effects.put(id.toString(), effect);
        }
        return effects;
    }

    private List<StatusEffectInstance> getPotionEffects(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return List.of();
        }
        PotionContentsComponent potionContents = (PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS);
        if (potionContents == null) {
            return List.of();
        }
        ArrayList<StatusEffectInstance> effects = new ArrayList<StatusEffectInstance>();
        for (StatusEffectInstance effect : potionContents.getEffects()) {
            effects.add(effect);
        }
        return effects;
    }

    private boolean isTrackablePlayer(PlayerEntity player) {
        if (player == null || Tracker.mc.player == null) {
            return false;
        }
        if (!player.isAlive() || player == Tracker.mc.player || player.isSpectator()) {
            return false;
        }
        return !player.isInvisible() && !player.hasStatusEffect(StatusEffects.INVISIBILITY);
    }

    private boolean isInRange(Entity entity) {
        if (entity == null || Tracker.mc.player == null) {
            return false;
        }
        float maxRadius = Math.min(100.0f, Math.max(1.0f, this.radius.getCurrent()));
        double maxSq = maxRadius * maxRadius;
        return Tracker.mc.player.squaredDistanceTo(entity) <= maxSq;
    }

    private boolean isFood(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.get(DataComponentTypes.FOOD) != null;
    }

    private boolean isDrinkable(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        if (item == Items.POTION || item == Items.MILK_BUCKET || item == Items.HONEY_BOTTLE) {
            return true;
        }
        return stack.get(DataComponentTypes.POTION_CONTENTS) != null;
    }

    private PlayerEntity getPlayerByUuid(UUID uuid) {
        if (uuid == null || Tracker.mc.world == null) {
            return null;
        }
        for (PlayerEntity player : Tracker.mc.world.getPlayers()) {
            if (!uuid.equals(player.getUuid())) continue;
            return player;
        }
        return null;
    }

    private void clearState() {
        this.useStates.clear();
        this.lastEffects.clear();
        this.trackedPotions.clear();
        this.recentEventKeys.clear();
    }

    private boolean isDuplicateEvent(String key) {
        long now = System.currentTimeMillis();
        this.recentEventKeys.entrySet().removeIf(entry -> now - (Long)entry.getValue() > 5000L);
        Long lastSeenAt = this.recentEventKeys.get(key);
        if (lastSeenAt != null && now - lastSeenAt <= 900L) {
            return true;
        }
        this.recentEventKeys.put(key, now);
        return false;
    }

    private String getItemKey(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "empty";
        }
        Identifier itemId = Registries.ITEM.getId((Object)stack.getItem());
        return itemId != null ? itemId.toString() : stack.getName().getString();
    }

    private String formatDuration(int totalSeconds) {
        int minutes = Math.max(0, totalSeconds) / 60;
        int seconds = Math.max(0, totalSeconds) % 60;
        return String.format(Locale.US, "%d:%02d", minutes, seconds);
    }

    private String toRoman(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(number);
        };
    }

    private static final class UseState {
        private boolean using;
        private int startAge;
        private ItemStack item = ItemStack.EMPTY;

        private UseState() {
        }

        private void reset() {
            this.using = false;
            this.startAge = 0;
            this.item = ItemStack.EMPTY;
        }
    }

    private static final class TrackedPotion {
        private final ItemStack stack;
        private Vec3d lastPos;
        private final UUID ownerUuid;

        private TrackedPotion(ItemStack stack, Vec3d lastPos, UUID ownerUuid) {
            this.stack = stack;
            this.lastPos = lastPos;
            this.ownerUuid = ownerUuid;
        }
    }
}

