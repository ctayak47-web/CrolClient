package com.crolclient.visual;

import by.saskkeee.annotations.CompileToNative;
import by.saskkeee.annotations.Entrypoint;
import by.saskkeee.annotations.vmprotect.CompileType;
import by.saskkeee.annotations.vmprotect.VMProtect;
import java.io.File;
import lombok.Generated;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Identifier;
import net.minecraft.ResourceType;
import net.minecraft.ResourceManager;
import com.crolclient.visual.base.comand.CommandManager;
import com.crolclient.visual.base.config.ConfigManager;
import com.crolclient.visual.base.filemanager.impl.BlacklistManager;
import com.crolclient.visual.base.filemanager.impl.FriendManager;
import com.crolclient.visual.base.filemanager.impl.StaffManager;
import com.crolclient.visual.base.modules.ModuleManager;
import com.crolclient.visual.base.request.ScriptManager;
import com.crolclient.visual.base.rotation.RotationManager;
import com.crolclient.visual.base.rotation.deeplearnig.DeepLearningManager;
import com.crolclient.visual.base.theme.ThemeManager;
import com.crolclient.visual.client.screens.menu.MenuScreen;
import com.crolclient.visual.utility.discord.DiscordRpcService;
import com.crolclient.visual.utility.game.server.ServerHandler;
import com.crolclient.visual.utility.input.KeybindManager;
import com.crolclient.visual.utility.render.display.shader.DrawUtil;
import com.crolclient.visual.utility.render.display.shader.GlProgram;

@Entrypoint
public enum CrolClient {
    INSTANCE;

    public static final String NAME = "CrolClient";
    public static final String VER = "2.0";
    public static final String TYPE = "DEV";
    private static final String MOD_ID = "crolclient";
    public static final File DIRECTORY;
    private ModuleManager moduleManager;
    private ThemeManager themeManager;
    private MenuScreen menuScreen;
    private ScriptManager scriptManager;
    private ServerHandler serverHandler;
    private BlacklistManager blacklistManager;
    private FriendManager friendManager;
    private StaffManager staffManager;
    private DeepLearningManager deepLearningManager;
    private RotationManager rotationManager;
    private CommandManager commandManager;
    private ConfigManager configManager;

    @CompileToNative
    @VMProtect(type=CompileType.ULTRA)
    public void init() {
        if (!DIRECTORY.exists()) {
            DIRECTORY.mkdirs();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> CrolClient.getInstance().shutdown()));
        this.blacklistManager = new BlacklistManager();
        this.friendManager = new FriendManager();
        this.staffManager = new StaffManager();
        this.serverHandler = new ServerHandler();
        this.themeManager = new ThemeManager();
        this.moduleManager = new ModuleManager();
        this.deepLearningManager = new DeepLearningManager();
        this.rotationManager = new RotationManager();
        this.commandManager = new CommandManager();
        this.scriptmanagerInit();
        KeybindManager.init();
        this.menuScreen = new MenuScreen();
        DiscordRpcService.start();
        this.configManager = new ConfigManager();
        this.menuScreen.initialize();
        DrawUtil.initializeShaders();
        ResourceManagerHelper.get((ResourceType)ResourceType.CLIENT_RESOURCES).registerReloadListener((IdentifiableResourceReloadListener)new SimpleSynchronousResourceReloadListener(this){

            public Identifier getFabricId() {
                return CrolClient.id("after_shader_load");
            }

            public void reload(ResourceManager manager) {
                GlProgram.loadAndSetupPrograms();
            }
        });
    }

    private void scriptmanagerInit() {
        // maintain original initialization order for ScriptManager
        this.scriptManager = new ScriptManager();
    }

    public void shutdown() {
        DiscordRpcService.shutdown();
        this.blacklistManager.save();
        this.friendManager.save();
        this.staffManager.save();
        this.configManager.save();
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public static CrolClient getInstance() {
        return INSTANCE;
    }

    @Generated
    public ModuleManager getModuleManager() {
        return this.moduleManager;
    }

    @Generated
    public ThemeManager getThemeManager() {
        return this.themeManager;
    }

    @Generated
    public MenuScreen getMenuScreen() {
        return this.menuScreen;
    }

    @Generated
    public ScriptManager getScriptManager() {
        return this.scriptManager;
    }

    @Generated
    public ServerHandler getServerHandler() {
        return this.serverHandler;
    }

    @Generated
    public BlacklistManager getBlacklistManager() {
        return this.blacklistManager;
    }

    @Generated
    public FriendManager getFriendManager() {
        return this.friendManager;
    }

    @Generated
    public StaffManager getStaffManager() {
        return this.staffManager;
    }

    @Generated
    public DeepLearningManager getDeepLearningManager() {
        return this.deepLearningManager;
    }

    @Generated
    public RotationManager getRotationManager() {
        return this.rotationManager;
    }

    @Generated
    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    @Generated
    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    static {
        DIRECTORY = FabricLoader.getInstance().getGameDir().resolve(".crolclient").toFile();
    }
}
