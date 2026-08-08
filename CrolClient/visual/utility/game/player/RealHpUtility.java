
package crol.client.utility.game.player;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ScoreboardObjective;
import net.minecraft.Scoreboard;
import net.minecraft.MutableText;
import net.minecraft.AbstractClientPlayerEntity;
import net.minecraft.ScoreboardDisplaySlot;
import net.minecraft.ReadableScoreboardScore;
import net.minecraft.ScoreHolder;
import net.minecraft.NumberFormat;
import net.minecraft.StyledNumberFormat;
import crol.client.CrolClient;
import crol.client.utility.render.display.base.color.ColorUtil;

public final class RealHpUtility {
    private static final Pattern SCORE_PATTERN = Pattern.compile("[-+]?\\d+(?:[\\.,]\\d+)?");

    private RealHpUtility() {
    }

    public static float getRealHp(AbstractClientPlayerEntity player) {
        float listNumericHp;
        float belowNameNumericHp;
        if (player == null || player.getWorld() == null) {
            return -1.0f;
        }
        Scoreboard scoreboard = player.getScoreboard();
        ScoreboardObjective belowNameObjective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME);
        if (RealHpUtility.isValidBelowNameHp(player, belowNameObjective, belowNameNumericHp = RealHpUtility.getNumericScoreFromSlot(scoreboard, player, ScoreboardDisplaySlot.BELOW_NAME))) {
            return belowNameNumericHp;
        }
        float belowNameFormattedHp = RealHpUtility.getFormattedScoreFromSlot(scoreboard, player, ScoreboardDisplaySlot.BELOW_NAME);
        if (RealHpUtility.isValidBelowNameHp(player, belowNameObjective, belowNameFormattedHp)) {
            return belowNameFormattedHp;
        }
        ScoreboardObjective listObjective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.LIST);
        if (RealHpUtility.isValidListHp(player, listObjective, listNumericHp = RealHpUtility.getNumericScoreFromSlot(scoreboard, player, ScoreboardDisplaySlot.LIST))) {
            return listNumericHp;
        }
        float formattedListHp = RealHpUtility.getFormattedScoreFromSlot(scoreboard, player, ScoreboardDisplaySlot.LIST);
        if (RealHpUtility.isValidListHp(player, listObjective, formattedListHp)) {
            return formattedListHp;
        }
        return -1.0f;
    }

    private static float getNumericScoreFromSlot(Scoreboard scoreboard, AbstractClientPlayerEntity player, ScoreboardDisplaySlot slot) {
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(slot);
        if (objective == null) {
            return -1.0f;
        }
        ReadableScoreboardScore score = scoreboard.getScore((ScoreHolder)player, objective);
        if (score == null) {
            return -1.0f;
        }
        int value = score.getScore();
        return value > 0 ? (float)value : -1.0f;
    }

    private static float getFormattedScoreFromSlot(Scoreboard scoreboard, AbstractClientPlayerEntity player, ScoreboardDisplaySlot slot) {
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(slot);
        if (objective == null) {
            return -1.0f;
        }
        ReadableScoreboardScore score = scoreboard.getScore((ScoreHolder)player, objective);
        if (score == null) {
            return -1.0f;
        }
        MutableText formatted = ReadableScoreboardScore.getFormattedScore((ReadableScoreboardScore)score, (NumberFormat)objective.getNumberFormatOr((NumberFormat)StyledNumberFormat.EMPTY));
        return RealHpUtility.parseScoreValue(formatted.getString());
    }

    private static boolean isLikelyHealthObjective(ScoreboardObjective objective) {
        if (objective == null) {
            return false;
        }
        String displayName = objective.getDisplayName().getString();
        String cleaned = ColorUtil.removeFormatting(displayName);
        String normalized = ((objective.getName() == null ? "" : objective.getName()) + " " + (cleaned == null ? "" : cleaned)).toLowerCase(Locale.ROOT);
        return normalized.contains("hp") || normalized.contains("хп") || normalized.contains("health") || normalized.contains("heart") || normalized.contains("life") || normalized.contains("здоров") || normalized.contains("серд");
    }

    private static boolean isValidBelowNameHp(AbstractClientPlayerEntity player, ScoreboardObjective objective, float value) {
        if (!RealHpUtility.isPlausibleHealthValue(player, value)) {
            return false;
        }
        return RealHpUtility.isLikelyHealthObjective(objective) || RealHpUtility.isBuiltInBelowNameServer();
    }

    private static boolean isValidListHp(AbstractClientPlayerEntity player, ScoreboardObjective objective, float value) {
        if (!RealHpUtility.isPlausibleHealthValue(player, value)) {
            return false;
        }
        return RealHpUtility.isLikelyHealthObjective(objective);
    }

    private static boolean isPlausibleHealthValue(AbstractClientPlayerEntity player, float value) {
        float absorption;
        if (!(value > 0.0f) || !Float.isFinite(value)) {
            return false;
        }
        float maxHealth = Math.max(1.0f, player.getMaxHealth());
        float hardLimit = Math.max(40.0f, maxHealth + (absorption = Math.max(0.0f, player.getAbsorptionAmount())) + 16.0f);
        return value <= hardLimit;
    }

    private static boolean isBuiltInBelowNameServer() {
        String server = CrolClient.getInstance().getServerHandler().getServer();
        return "FunTime".equals(server) || "ReallyWorld".equals(server) || "HolyWorld".equals(server);
    }

    private static float parseScoreValue(String text) {
        Matcher matcher;
        if (text == null || text.isEmpty()) {
            return -1.0f;
        }
        String cleaned = ColorUtil.removeFormatting(text);
        if (cleaned == null || cleaned.isEmpty()) {
            cleaned = text;
        }
        if (!(matcher = SCORE_PATTERN.matcher(cleaned)).find()) {
            return -1.0f;
        }
        String number = matcher.group().replace(',', '.');
        try {
            return Float.parseFloat(number);
        }
        catch (NumberFormatException ignored) {
            return -1.0f;
        }
    }
}

