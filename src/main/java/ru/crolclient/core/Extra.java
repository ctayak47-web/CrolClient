package ru.crolclient.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import ru.crolclient.api.file.impl.draggable.DraggableFile;
import ru.crolclient.api.system.discord.DiscordManager;
import ru.crolclient.api.feature.draggable.DraggableRepository;
import ru.crolclient.api.file.*;
import ru.crolclient.api.file.exception.FileProcessingException;
import ru.crolclient.api.repository.macro.MacroRepository;
import ru.crolclient.api.event.EventManager;
import ru.crolclient.api.feature.module.ModuleProvider;
import ru.crolclient.api.feature.module.ModuleRepository;
import ru.crolclient.api.feature.module.ModuleSwitcher;
import ru.crolclient.api.system.localization.Language;
import ru.crolclient.api.system.shader.ShadersPool;
import ru.crolclient.common.util.logger.LoggerUtil;
import ru.crolclient.common.util.render.ScissorManager;
import ru.crolclient.core.client.ClientInfo;
import ru.crolclient.core.client.ClientInfoProvider;
import ru.crolclient.core.listener.ListenerRepository;
import ru.crolclient.implement.events.setting.SettingsUpdateEvent;
import ru.crolclient.implement.features.commands.CommandDispatcher;
import ru.crolclient.implement.features.commands.manager.CommandRepository;
import ru.crolclient.implement.features.modules.combat.killaura.attack.AttackPerpetrator;

import java.io.File;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class Extra implements ModInitializer {
    @Getter
    static Extra instance;
    @NonFinal
    ModuleRepository moduleRepository;
    @NonFinal
    ModuleSwitcher moduleSwitcher;
    @NonFinal
    CommandRepository commandRepository;
    @NonFinal
    CommandDispatcher commandDispatcher;
    @NonFinal
    MacroRepository macroRepository;
    @NonFinal
    ModuleProvider moduleProvider;
    @NonFinal
    DraggableRepository draggableRepository;
    @NonFinal
    EventManager eventManager;
    @NonFinal
    DiscordManager discordManager;
    @NonFinal
    FileRepository fileRepository;
    @NonFinal
    FileController fileController;
    @NonFinal
    ScissorManager scissorManager;
    @NonFinal
    ClientInfoProvider clientInfoProvider;
    @NonFinal
    ListenerRepository listenerRepository;
    @NonFinal
    Language language = Language.ENG;

    AttackPerpetrator attackPerpetrator = new AttackPerpetrator();

    @NonFinal
    boolean initialized = false;

    @Override
    public void onInitialize() {
        instance = this;
        initClientInfoProvider();
        initEvents();
        initModules();
        initMacro();
        initDraggable();
        initFileManager();
        initCommands();
        initScissor();
        initListeners();
        ShadersPool.initShaders();
        EventManager.callEvent(new SettingsUpdateEvent());
        initDiscordRPC();

        initialized = true;
    }

    public void initEvents() {
        this.eventManager = new EventManager();
    }

    public void initDraggable() {
        draggableRepository = new DraggableRepository();
        draggableRepository.setup();
    }

    public void initModules() {
        moduleRepository = new ModuleRepository();
        moduleRepository.setup();
        moduleProvider = new ModuleProvider(moduleRepository.modules());
        moduleSwitcher = new ModuleSwitcher(moduleRepository.modules(), eventManager);
    }

    public void initCommands() {
        commandRepository = new CommandRepository();
        commandDispatcher = new CommandDispatcher(eventManager);
    }

    public void initMacro() {
        macroRepository = new MacroRepository(eventManager);
    }

    public void initDiscordRPC() {
        discordManager = new DiscordManager();
        discordManager.init();
    }

    public void initClientInfoProvider() {
        File clientDirectory = new File(MinecraftClient.getInstance().runDirectory, "\\Extra\\");
        File filesDirectory = new File(clientDirectory, "\\files\\");
        File moduleFilesDirectory = new File(filesDirectory, "\\config\\");
        clientInfoProvider = new ClientInfo(
                "Extra client",
                "2.0",
                "Beta",
                clientDirectory,
                filesDirectory,
                moduleFilesDirectory
        );
        System.out.println(clientInfoProvider.getFullInfo());
    }

    public void initFileManager() {
        DirectoryCreator directoryCreator = new DirectoryCreator();
        directoryCreator.createDirectories(clientInfoProvider.clientDir(), clientInfoProvider.filesDir(), clientInfoProvider.configsDir());

        fileRepository = new FileRepository();
        fileRepository.setup(this);

        fileRepository.register(new DraggableFile(draggableRepository));
        fileController = new FileController(fileRepository.getClientFiles(), clientInfoProvider.filesDir(), clientInfoProvider.configsDir());
        try {
            fileController.loadFiles();
        } catch (FileProcessingException e) {
            LoggerUtil.error("Error occurred while loading files: " + e.getMessage() + " " + e.getCause());
        }
    }

    public void initListeners() {
        listenerRepository = new ListenerRepository();
        listenerRepository.setup();
    }

    public void initScissor() {
        scissorManager = new ScissorManager();
    }

    public void toggleLanguage() {
        if (language == Language.ENG) {
            language = Language.RUS;
        } else {
            language = Language.ENG;
        }
    }
}
