package ru.crolclient.api.file;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import ru.crolclient.api.feature.module.setting.SettingRepository;
import ru.crolclient.core.Extra;
import ru.crolclient.api.file.impl.friend.FriendFile;
import ru.crolclient.api.file.impl.macro.MacroFile;
import ru.crolclient.api.file.impl.module.ModuleFile;
import ru.crolclient.implement.screens.menu.components.implement.window.implement.module.InfoWindow;

import java.util.ArrayList;
import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileRepository {
    List<ClientFile> clientFiles = new ArrayList<>();

    public void setup(Extra extra) {
        register(
                new MacroFile(extra.getMacroRepository()),
                new FriendFile(),
                new ModuleFile(extra.getModuleRepository())
        );
    }

    public void register(ClientFile... clientFIle) {
        clientFiles.addAll(List.of(clientFIle));
    }
}
