package ru.crolclient.api.system.discord;

import lombok.Getter;
import ru.crolclient.core.client.ClientInfoProvider;
import ru.crolclient.core.Extra;
import ru.crolclient.api.system.discord.utils.DiscordEventHandlers;
import ru.crolclient.api.system.discord.utils.DiscordRPC;
import ru.crolclient.api.system.discord.utils.DiscordRichPresence;
import ru.crolclient.api.system.discord.utils.RPCButton;

@Getter
public class DiscordManager {
    private final DiscordDaemonThread discordDaemonThread = new DiscordDaemonThread();
    private boolean running = true;

    public void init() {
        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder()
                .ready((user) -> {

                    //String uid = "1";
                    String image = "https://s13.gifyu.com/images/SGW1F.gif";

                    //String avatarUrl = "https://cdn.discordapp.com/avatars/"
                            //+ user.userId + "/"
                            //+ user.avatar + ".png";
                    ClientInfoProvider clientInfoProvider = Extra.getInstance().getClientInfoProvider();
                    DiscordRichPresence richPresence = new DiscordRichPresence.Builder()
                            .setStartTimestamp((System.currentTimeMillis() / 1000))
                            .setDetails("Version: " + clientInfoProvider.clientVersion())
                            .setState("Branch: " + clientInfoProvider.clientBranch())
                            .setLargeImage(image)
                            //.setSmallImage(avatarUrl, user.username)
                            .setButtons(
                                    RPCButton.create("Получить", "https://t.me/example")
                            ).build();

                    DiscordRPC.INSTANCE.Discord_UpdatePresence(richPresence);
                })
                .build();

        String APPLICATION_ID = "1311091798826942504";
        DiscordRPC.INSTANCE.Discord_Initialize(APPLICATION_ID, handlers, true, "");
        discordDaemonThread.start();
    }

    public void stopRPC() {
        DiscordRPC.INSTANCE.Discord_Shutdown();
        this.running = false;
    }


    private class DiscordDaemonThread extends Thread {
        @Override
        public void run() {
            this.setName("Discord-RPC");

            try {
                while (Extra.getInstance().getDiscordManager().isRunning()) {
                    DiscordRPC.INSTANCE.Discord_RunCallbacks();
                    Thread.sleep(15 * 1000);
                }
            } catch (Exception exception) {
                stopRPC();
            }

            super.run();
        }
    }
}