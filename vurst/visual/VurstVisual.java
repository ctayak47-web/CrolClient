
package CrolClient.visual;

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
import CrolClient.visual.base.comand.CommandManager;
import CrolClient.visual.base.config.ConfigManager;
import CrolClient.visual.base.filemanager.impl.BlacklistManager;
import CrolClient.visual.base.filemanager.impl.FriendManager;
import CrolClient.visual.base.filemanager.impl.StaffManager;
import CrolClient.visual.base.modules.ModuleManager;
import CrolClient.visual.base.request.ScriptManager;
import CrolClient.visual.base.rotation.RotationManager;
import CrolClient.visual.base.rotation.deeplearnig.DeepLearningManager;
import CrolClient.visual.base.theme.ThemeManager;
import CrolClient.visual.client.screens.menu.MenuScreen;
import CrolClient.visual.utility.discord.DiscordRpcService;
import CrolClient.visual.utility.game.server.ServerHandler;
import CrolClient.visual.utility.input.KeybindManager;
import CrolClient.visual.utility.render.display.shader.DrawUtil;
import CrolClient.visual.utility.render.display.shader.GlProgram;

@Entrypoint
public enum CrolClientVisual {
    INSTANCE;

    public static final String NAME = "Vurst Visual";
    public static final String VER = "2.0";
    public static final String TYPE = "DEV";
    private static final String MOD_ID = "CrolClientvisual";
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
        Runtime.getRuntime().addShutdownHook(new Thread(() -> CrolClientVisual.getInstance().shutdown()));
        this.blacklistManager = new BlacklistManager();
        this.friendManager = new FriendManager();
        this.staffManager = new StaffManager();
        this.serverHandler = new ServerHandler();
        this.themeManager = new ThemeManager();
        this.moduleManager = new ModuleManager();
        this.deepLearningManager = new DeepLearningManager();
        this.rotationManager = new RotationManager();
        this.commandManager = new CommandManager();
        this.scriptManager = new ScriptManager();
        KeybindManager.init();
        this.menuScreen = new MenuScreen();
        DiscordRpcService.start();
        this.configManager = new ConfigManager();
        this.menuScreen.initialize();
        DrawUtil.initializeShaders();
        ResourceManagerHelper.get((ResourceType)ResourceType.CLIENT_RESOURCES).registerReloadListener((IdentifiableResourceReloadListener)new SimpleSynchronousResourceReloadListener(this){

            public Identifier getFabricId() {
                return CrolClientVisual.id("after_shader_load");
            }

            public void reload(ResourceManager manager) {
                GlProgram.loadAndSetupPrograms();
            }
        });
    }

    public void shutdown() {
        DiscordRpcService.shutdown();
        this.blacklistManager.save();
        this.friendManager.save();
        this.staffManager.save();
        this.configManager.save();
    }

    public static Identifier id(String path) {
        return Identifier.of((String)MOD_ID, (String)path);
    }

    public static CrolClientVisual getInstance() {
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
        DIRECTORY = FabricLoader.getInstance().getGameDir().resolve(".CrolClientvisual").toFile();
    }
}
