
package crol.client.base.comand.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import net.minecraft.CommandSource;
import crol.client.CrolClient;
import crol.client.base.comand.api.CommandAbstract;
import crol.client.base.comand.impl.args.CfgArgumentType;
import crol.client.base.config.ConfigManager;
import crol.client.utility.game.other.MessageUtil;

public class ConfigCommand
extends CommandAbstract {
    public ConfigCommand() {
        super("cfg");
    }

    @Override
    public void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder cfgBuilder = LiteralArgumentBuilder.literal((String)"cfg");
        this.execute((LiteralArgumentBuilder<CommandSource>)cfgBuilder);
        dispatcher.register(cfgBuilder);
        LiteralArgumentBuilder configBuilder = LiteralArgumentBuilder.literal((String)"config");
        this.execute((LiteralArgumentBuilder<CommandSource>)configBuilder);
        dispatcher.register(configBuilder);
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(((LiteralArgumentBuilder)ConfigCommand.literal("save").then(ConfigCommand.arg("name", StringArgumentType.word()).executes(context -> {
            CrolClient.getInstance().getConfigManager();
            String name = ConfigManager.normalizeConfigName(StringArgumentType.getString((CommandContext)context, (String)"name"));
            boolean success = CrolClient.getInstance().getConfigManager().saveConfig(name);
            if (success) {
                MessageUtil.displayMessage(MessageUtil.LogLevel.INFO, "§aConfig saved: " + name);
            } else {
                MessageUtil.displayMessage(MessageUtil.LogLevel.WARN, "§cFailed to save config: " + name);
            }
            return 1;
        }))).executes(context -> {
            boolean success = CrolClient.getInstance().getConfigManager().saveConfig("current_config");
            if (success) {
                MessageUtil.displayMessage(MessageUtil.LogLevel.INFO, "§aConfig saved");
            } else {
                MessageUtil.displayMessage(MessageUtil.LogLevel.WARN, "§cFailed to save config");
            }
            return 1;
        }));
        builder.then(((LiteralArgumentBuilder)ConfigCommand.literal("load").then(ConfigCommand.arg("name", CfgArgumentType.create()).executes(context -> {
            String name = (String)context.getArgument("name", String.class);
            boolean success = CrolClient.getInstance().getConfigManager().loadConfig(name);
            if (success) {
                MessageUtil.displayMessage(MessageUtil.LogLevel.INFO, "§aConfig loaded: " + name);
            } else {
                MessageUtil.displayMessage(MessageUtil.LogLevel.WARN, "§cFailed to load config: " + name);
            }
            return 1;
        }))).executes(context -> {
            boolean success = CrolClient.getInstance().getConfigManager().loadConfig("current_config");
            if (success) {
                MessageUtil.displayMessage(MessageUtil.LogLevel.INFO, "§aConfig loaded");
            } else {
                MessageUtil.displayMessage(MessageUtil.LogLevel.WARN, "§cFailed to load config");
            }
            return 1;
        }));
        builder.then(ConfigCommand.literal("help").executes(context -> {
            MessageUtil.displayMessage(MessageUtil.LogLevel.INFO, "§6Usage: §r.cfg <save/load/dir/help>");
            MessageUtil.displayMessage(MessageUtil.LogLevel.INFO, "§6Commands:§r");
            MessageUtil.displayMessage(MessageUtil.LogLevel.INFO, "§e.cfg save [name] §7- save config");
            MessageUtil.displayMessage(MessageUtil.LogLevel.INFO, "§e.cfg load [name] §7- load config");
            MessageUtil.displayMessage(MessageUtil.LogLevel.INFO, "§e.cfg dir §7- open config folder");
            return 1;
        }));
        builder.then(ConfigCommand.literal("dir").executes(context -> {
            File dir = ConfigManager.configDirectory;
            if (!dir.exists() && !dir.mkdirs()) {
                MessageUtil.displayMessage(MessageUtil.LogLevel.WARN, "§cFailed to create config folder");
                return 1;
            }
            boolean opened = this.openDirectory(dir);
            if (opened) {
                MessageUtil.displayMessage(MessageUtil.LogLevel.INFO, "§aOpened config folder: §f" + dir.getAbsolutePath());
            } else {
                MessageUtil.displayMessage(MessageUtil.LogLevel.WARN, "§cCouldn't open folder automatically. Path: §f" + dir.getAbsolutePath());
            }
            return 1;
        }));
    }

    private boolean openDirectory(File directory) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(directory);
                return true;
            }
        }
        catch (IOException iOException) {
            
        }
        String os = System.getProperty("os.name", "").toLowerCase();
        Process process = null;
        try {
            process = os.contains("win") ? new ProcessBuilder("explorer.exe", directory.getAbsolutePath()).start() : (os.contains("mac") ? new ProcessBuilder("open", directory.getAbsolutePath()).start() : new ProcessBuilder("xdg-open", directory.getAbsolutePath()).start());
            return process != null;
        }
        catch (IOException ignored) {
            return false;
        }
    }
}

