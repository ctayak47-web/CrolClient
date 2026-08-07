
package vurst.visual.utility.game.player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.Generated;
import net.minecraft.Hand;
import net.minecraft.StatusEffect;
import net.minecraft.Entity;
import net.minecraft.LivingEntity;
import net.minecraft.PlayerEntity;
import net.minecraft.Blocks;
import net.minecraft.Block;
import net.minecraft.BlockPos;
import net.minecraft.Box;
import net.minecraft.Packet;
import net.minecraft.ScoreboardObjective;
import net.minecraft.BlockState;
import net.minecraft.ClientCommandC2SPacket;
import net.minecraft.PlayerInteractItemC2SPacket;
import net.minecraft.MathHelper;
import net.minecraft.InputUtil;
import net.minecraft.EntityPose;
import net.minecraft.ChatScreen;
import net.minecraft.Screen;
import net.minecraft.MutableText;
import net.minecraft.RegistryEntry;
import net.minecraft.SequencedPacketCreator;
import net.minecraft.ScoreboardDisplaySlot;
import net.minecraft.ReadableScoreboardScore;
import net.minecraft.ScoreHolder;
import net.minecraft.NumberFormat;
import net.minecraft.StyledNumberFormat;
import org.lwjgl.glfw.GLFW;
import vurst.visual.VurstVisual;
import vurst.visual.client.modules.api.setting.impl.KeySetting;
import vurst.visual.utility.game.player.rotation.Rotation;
import vurst.visual.utility.interfaces.IClient;
import vurst.visual.utility.render.display.base.color.ColorUtil;

public final class PlayerIntersectionUtil
implements IClient {
    public static void sendSequencedPacket(SequencedPacketCreator packetCreator) {
        PlayerIntersectionUtil.mc.interactionManager.sendSequencedPacket(PlayerIntersectionUtil.mc.world, packetCreator);
    }

    public static void startFallFlying() {
        PlayerIntersectionUtil.mc.player.networkHandler.sendPacket((Packet)new ClientCommandC2SPacket((Entity)PlayerIntersectionUtil.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        PlayerIntersectionUtil.mc.player.startGliding();
    }

    public static void sendPacketWithOutEvent(Packet<?> packet) {
        mc.getNetworkHandler().getConnection().send(packet, null);
    }

    public static List<BlockPos> getCube(BlockPos center, float radius) {
        return PlayerIntersectionUtil.getCube(center, radius, radius, true);
    }

    public static List<BlockPos> getCube(BlockPos center, float radiusXZ, float radiusY) {
        return PlayerIntersectionUtil.getCube(center, radiusXZ, radiusY, true);
    }

    public static List<BlockPos> getCube(BlockPos center, float radiusXZ, float radiusY, boolean down) {
        ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
        int centerX = center.getX();
        int centerY = center.getY();
        int centerZ = center.getZ();
        int posY = down ? centerY - (int)radiusY : centerY;
        int x = centerX - (int)radiusXZ;
        while ((float)x <= (float)centerX + radiusXZ) {
            int z = centerZ - (int)radiusXZ;
            while ((float)z <= (float)centerZ + radiusXZ) {
                int y = posY;
                while ((float)y <= (float)centerY + radiusY) {
                    positions.add(new BlockPos(x, y, z));
                    ++y;
                }
                ++z;
            }
            ++x;
        }
        return positions;
    }

    public static List<BlockPos> getCube(BlockPos start, BlockPos end) {
        ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
        for (int x = start.getX(); x <= end.getX(); ++x) {
            for (int z = start.getZ(); z <= end.getZ(); ++z) {
                for (int y = start.getY(); y <= end.getY(); ++y) {
                    positions.add(new BlockPos(x, y, z));
                }
            }
        }
        return positions;
    }

    public static InputUtil.Type getKeyType(int key) {
        return key < 8 ? InputUtil.Type.MOUSE : InputUtil.Type.KEYSYM;
    }

    public static Stream<Entity> streamEntities() {
        return StreamSupport.stream(PlayerIntersectionUtil.mc.world.getEntities().spliterator(), false);
    }

    public static boolean canChangeIntoPose(EntityPose pose) {
        return PlayerIntersectionUtil.mc.player.getWorld().isSpaceEmpty((Entity)PlayerIntersectionUtil.mc.player, PlayerIntersectionUtil.mc.player.getDimensions(pose).getBoxAt(PlayerIntersectionUtil.mc.player.getPos()).contract(1.0E-7));
    }

    public static boolean isPotionActive(RegistryEntry<StatusEffect> statusEffect) {
        return PlayerIntersectionUtil.mc.player.getActiveStatusEffects().containsKey(statusEffect);
    }

    public static boolean isPlayerInBlock(Block block) {
        return PlayerIntersectionUtil.isBoxInBlock(PlayerIntersectionUtil.mc.player.getBoundingBox().expand(-0.001), block);
    }

    public static boolean isBoxInBlock(Box box, Block block) {
        return PlayerIntersectionUtil.isBox(box, pos -> PlayerIntersectionUtil.mc.world.getBlockState(pos).getBlock().equals(block));
    }

    public static boolean isBoxInBlocks(Box box, List<Block> blocks) {
        return PlayerIntersectionUtil.isBox(box, pos -> blocks.contains(PlayerIntersectionUtil.mc.world.getBlockState(pos).getBlock()));
    }

    public static boolean isBox(Box box, Predicate<BlockPos> pos) {
        return BlockPos.stream((Box)box).anyMatch(pos);
    }

    public static boolean isKey(InputUtil.Key key) {
        return PlayerIntersectionUtil.isKey(key.getCategory(), key.getCode());
    }

    public static boolean isKey(KeySetting setting) {
        int key = setting.getKeyCode();
        return PlayerIntersectionUtil.mc.currentScreen == null && setting.isVisible() && PlayerIntersectionUtil.isKey(PlayerIntersectionUtil.getKeyType(key), key);
    }

    public static boolean isKey(InputUtil.Type type, int keyCode) {
        if (keyCode != -1) {
            switch (type) {
                case KEYSYM: {
                    return GLFW.glfwGetKey((long)mc.getWindow().getHandle(), (int)keyCode) == 1;
                }
                case MOUSE: {
                    return GLFW.glfwGetMouseButton((long)mc.getWindow().getHandle(), (int)keyCode) == 1;
                }
            }
        }
        return false;
    }

    public static boolean isAir(BlockPos blockPos) {
        return PlayerIntersectionUtil.isAir(PlayerIntersectionUtil.mc.world.getBlockState(blockPos));
    }

    public static boolean isAir(BlockState state) {
        return state.isAir() || state.getBlock().equals(Blocks.CAVE_AIR) || state.getBlock().equals(Blocks.VOID_AIR);
    }

    public static boolean isChat(Screen screen) {
        return screen instanceof ChatScreen;
    }

    public static boolean nullCheck() {
        return PlayerIntersectionUtil.mc.player == null || PlayerIntersectionUtil.mc.world == null;
    }

    public static void useItem(Hand hand) {
        PlayerIntersectionUtil.useItem(hand, rotationManager.getCurrentRotation());
    }

    public static void useItem(Hand hand, Rotation angle) {
        PlayerIntersectionUtil.sendSequencedPacket(i -> new PlayerInteractItemC2SPacket(hand, i, angle.getYaw(), angle.getPitch()));
    }

    public static float getHealth(LivingEntity entity) {
        float hp = entity.getHealth() + entity.getAbsorptionAmount();
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            switch (VurstVisual.getInstance().getServerHandler().getServer()) {
                case "FunTime": 
                case "ReallyWorld": {
                    ScoreboardObjective scoreBoard = player.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME);
                    if (scoreBoard == null) break;
                    MutableText text2 = ReadableScoreboardScore.getFormattedScore((ReadableScoreboardScore)player.getScoreboard().getScore((ScoreHolder)player, scoreBoard), (NumberFormat)scoreBoard.getNumberFormatOr((NumberFormat)StyledNumberFormat.EMPTY));
                    try {
                        hp = Float.parseFloat(ColorUtil.removeFormatting(text2.getString()));
                        break;
                    }
                    catch (NumberFormatException numberFormatException) {
                        
                    }
                }
            }
        }
        return MathHelper.clamp((float)hp, (float)0.0f, (float)entity.getMaxHealth());
    }

    public static String getHealthString(LivingEntity entity) {
        return PlayerIntersectionUtil.getHealthString(PlayerIntersectionUtil.getHealth(entity));
    }

    public static String getHealthString(float hp) {
        return String.format("%.1f", Float.valueOf(hp)).replace(",", ".").replace(".0", "");
    }

    @Generated
    private PlayerIntersectionUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

