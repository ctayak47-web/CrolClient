
package vurst.visual.utility.discord;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.activity.ActivityButton;
import de.jcm.discordgamesdk.activity.ActivityButtonsMode;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DiscordRpcService {
    private static final long APPLICATION_ID = 1475990671281360990L;
    private static final String DETAILS_TEXT = "Vurst Visual";
    private static final String STATE_TEXT = "Build ⇒ 1.0.1 (1.21.4)";
    private static final String DOWNLOAD_BUTTON_LABEL = "Скачать";
    private static final String DOWNLOAD_BUTTON_URL = "https:
    private static final String LARGE_IMAGE_KEY = "logo";
    private static final String LARGE_IMAGE_TEXT = "Vurst Visual";
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);
    private static volatile Core core;
    private static Thread callbackThread;

    private DiscordRpcService() {
    }

    public static void start() {
        if (!STARTED.compareAndSet(false, true)) {
            return;
        }
        try {
            CreateParams params = new CreateParams();
            params.setClientID(1475990671281360990L);
            params.setFlags(CreateParams.Flags.SUPPRESS_EXCEPTIONS);
            core = new Core(params);
            Activity activity = new Activity();
            activity.setDetails("Vurst Visual");
            activity.setState(STATE_TEXT);
            activity.timestamps().setStart(Instant.now());
            activity.assets().setLargeImage(LARGE_IMAGE_KEY);
            activity.assets().setLargeText("Vurst Visual");
            activity.setActivityButtonsMode(ActivityButtonsMode.BUTTONS);
            activity.addButton(new ActivityButton(DOWNLOAD_BUTTON_LABEL, DOWNLOAD_BUTTON_URL));
            core.activityManager().updateActivity(activity);
            callbackThread = new Thread(DiscordRpcService::runCallbacks, "DiscordRPC");
            callbackThread.setDaemon(true);
            callbackThread.start();
        }
        catch (Throwable ignored) {
            STARTED.set(false);
        }
    }

    public static void shutdown() {
        if (!STARTED.compareAndSet(true, false)) {
            return;
        }
        Thread localThread = callbackThread;
        callbackThread = null;
        if (localThread != null) {
            localThread.interrupt();
        }
        Core localCore = core;
        core = null;
        try {
            if (localCore != null) {
                localCore.activityManager().clearActivity();
                localCore.close();
            }
        }
        catch (Throwable throwable) {
            
        }
    }

    private static void runCallbacks() {
        while (STARTED.get()) {
            try {
                Core localCore = core;
                if (localCore != null) {
                    localCore.runCallbacks();
                }
                Thread.sleep(250L);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            catch (Throwable throwable) {
            }
        }
    }
}

