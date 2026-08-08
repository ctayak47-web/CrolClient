package ru.crolclient.implement.features.modules.misc;

import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.util.Formatting;
import ru.crolclient.api.event.EventHandler;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.ModuleProvider;
import ru.crolclient.common.util.player.IHolder;
import ru.crolclient.core.Extra;
import ru.crolclient.implement.events.packet.PacketEvent;
import ru.crolclient.implement.events.player.UpdatePlayerEvent;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PluginsModule extends Module {
    public PluginsModule() {
        super("Plugins", ModuleCategory.MISC);
    }

    int delay = 0;
    ModuleProvider moduleProvider = Extra.getInstance().getModuleProvider();

    private final String[] knownPlugins = {
            "matrix",
            "alice",
            "vulcan",
            "kauri",
            "spartan",
            "polar",
            "horizon",
            "intave",
            "prostoac",
            "tesla",
            "buzz",
            "grimac",
            "grim",
            "aac",
            "nocheatplus",
            "anticheatreloaded",
            "negativity",
            "cheatminecore",
            "cmcore",
            "themis"
    };

    @EventHandler
    public void onUpdatePlayer(UpdatePlayerEvent event) {
        delay++;
        if (delay > 40) {
            logDirect(Formatting.RED + "Не удалось получить список плагинов.");
            delay = 0;
            moduleProvider.module("Plugins").setState(false);
        }
    }

    @EventHandler
    public void onPacket(PacketEvent packetEvent) {
        if (packetEvent.getPacket() instanceof CommandSuggestionsS2CPacket commandSuggestionsS2CPacket) {
            Set<String> plugins = commandSuggestionsS2CPacket.getSuggestions().getList().stream()
                    .map(cmd -> {
                        String[] command = cmd.getText().split(":");
                        if (command.length > 1) {
                            return command[0].replace("/", "");
                        } else {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .sorted()
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            if (!plugins.isEmpty()) {
                StringBuilder pluginsString = new StringBuilder();
                for (String plugin : plugins) {
                    String formattedPlugin;
                    if (Arrays.stream(knownPlugins).map(String::toLowerCase).toList().contains(plugin.toLowerCase())) {
                        formattedPlugin = "§a" + plugin;
                    } else {
                        formattedPlugin = "§7" + plugin;
                    }
                    if (!pluginsString.isEmpty()) {
                        pluginsString.append(", ");
                    }
                    pluginsString.append(formattedPlugin);
                }
                String result = pluginsString.toString();

                logDirect("§fPlugins (§c%s§f): %s".formatted(plugins.size(), result));
            } else {
                logDirect("Failed to retrieve plugin list!");
            }
            moduleProvider.module("Plugins").setState(false);
        }
    }

    @Override
    public void deactivate() {
        delay = 0;
        super.deactivate();
    }

    @Override
    public void activate() {
        IHolder.sendPacket(new RequestCommandCompletionsC2SPacket(0, "/"));
        super.activate();
    }
}