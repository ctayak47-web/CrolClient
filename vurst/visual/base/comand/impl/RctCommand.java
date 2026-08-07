
package vurst.visual.base.comand.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.CommandSource;
import net.minecraft.ScoreboardObjective;
import net.minecraft.Scoreboard;
import net.minecraft.ScoreboardDisplaySlot;
import vurst.visual.VurstVisual;
import vurst.visual.base.comand.api.CommandAbstract;
import vurst.visual.utility.game.other.MessageUtil;

public class RctCommand
extends CommandAbstract {
    private static final long REJOIN_DELAY_MS = 1000L;
    private static final Pattern ANARCHY_PATTERN = Pattern.compile("Анархия-(\\d+)");

    public RctCommand() {
        super("rct");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            if (RctCommand.mc.player == null || RctCommand.mc.world == null || mc.getNetworkHandler() == null) {
                MessageUtil.displayWarning("Нужно быть на сервере, чтобы использовать .rct");
                return 1;
            }
            int anarchy = this.resolveCurrentAnarchy();
            if (anarchy <= 0) {
                MessageUtil.displayWarning("Не удалось определить текущую анархию");
                return 1;
            }
            this.reconnectToAnarchy(anarchy);
            MessageUtil.displayInfo("Перезаход на анархию " + anarchy + "...");
            return 1;
        });
    }

    private int resolveCurrentAnarchy() {
        int serverHandlerAnarchy;
        if (VurstVisual.getInstance().getServerHandler() != null && (serverHandlerAnarchy = VurstVisual.getInstance().getServerHandler().getAnarchy()) > 0) {
            return serverHandlerAnarchy;
        }
        Scoreboard scoreboard = RctCommand.mc.world.getScoreboard();
        if (scoreboard == null) {
            return -1;
        }
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null || objective.getDisplayName() == null) {
            return -1;
        }
        String display = objective.getDisplayName().getString();
        Matcher matcher = ANARCHY_PATTERN.matcher(display);
        if (!matcher.find()) {
            return -1;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        }
        catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private void reconnectToAnarchy(int anarchy) {
        this.sendServerCommand("hub");
        this.sendServerCommand("an" + anarchy);
        Thread thread2 = new Thread(() -> {
            try {
                Thread.sleep(1000L);
            }
            catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                return;
            }
            if (mc != null) {
                mc.execute(() -> this.sendServerCommand("an" + anarchy));
            }
        }, "vurstvisual-rct");
        thread2.setDaemon(true);
        thread2.start();
    }

    private void sendServerCommand(String command) {
        if (mc.getNetworkHandler() != null && command != null && !command.isBlank()) {
            mc.getNetworkHandler().sendChatCommand(command);
        }
    }
}

