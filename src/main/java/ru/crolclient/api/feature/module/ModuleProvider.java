package ru.crolclient.api.feature.module;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import ru.crolclient.api.feature.module.exception.ModuleException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ModuleProvider {
    List<Module> modules;

    public ModuleProvider(List<Module> modules) {
        this.modules = new CopyOnWriteArrayList<>(modules);
    }

    public Module module(String moduleName) {
        return modules.stream()
                .filter(module -> module.getName().equalsIgnoreCase(moduleName))
                .findFirst()
                .orElseThrow(() -> new ModuleException("Модуль " + moduleName + " не найден", moduleName));
    }
}