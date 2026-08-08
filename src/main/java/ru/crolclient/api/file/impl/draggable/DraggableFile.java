package ru.crolclient.api.file.impl.draggable;

import com.google.gson.*;
import ru.crolclient.api.feature.draggable.AbstractDraggable;
import ru.crolclient.api.feature.draggable.DraggableRepository;
import ru.crolclient.api.file.ClientFile;
import ru.crolclient.api.file.exception.FileLoadException;
import ru.crolclient.api.file.exception.FileSaveException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DraggableFile extends ClientFile {
    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final DraggableRepository draggableRepository;

    public DraggableFile(DraggableRepository draggableRepository) {
        super("draggables");
        this.draggableRepository = draggableRepository;
    }

    @Override
    public void saveToFile(File path) throws FileSaveException {
        JsonObject draggablesObject = new JsonObject();

        for (AbstractDraggable draggable : draggableRepository.draggable()) {
            JsonObject positionObject = new JsonObject();
            positionObject.addProperty("x", draggable.getX());
            positionObject.addProperty("y", draggable.getY());
            draggablesObject.add(draggable.getName(), positionObject);
        }

        try (FileWriter writer = new FileWriter(new File(path, getName() + ".json"))) {
            GSON.toJson(draggablesObject, writer);
        } catch (IOException e) {
            throw new FileSaveException("Failed to save draggables positions", e);
        }
    }

    @Override
    public void loadFromFile(File path) throws FileLoadException {
        File file = new File(path, getName() + ".json");
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            JsonObject draggablesObject = JsonParser.parseReader(reader).getAsJsonObject();

            for (AbstractDraggable draggable : draggableRepository.draggable()) {
                JsonElement element = draggablesObject.get(draggable.getName());
                if (element != null && element.isJsonObject()) {
                    JsonObject positionObject = element.getAsJsonObject();
                    draggable.setX(positionObject.get("x").getAsInt());
                    draggable.setY(positionObject.get("y").getAsInt());
                }
            }
        } catch (IOException e) {
            throw new FileLoadException("Failed to load draggables positions", e);
        }
    }
}