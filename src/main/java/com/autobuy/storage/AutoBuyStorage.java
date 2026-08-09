package com.autobuy.storage;

import com.autobuy.AutoBuyMod;
import com.autobuy.data.AutoBuyItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class AutoBuyStorage {
    public enum ServerType {
        FANTAY, HOLYWORLD, UNKNOWN
    }

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = Paths.get("config/autobuy");
    private static final Path ITEMS_FILE = CONFIG_DIR.resolve("items.json");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.json");

    private List<AutoBuyItem> items = new ArrayList<>();
    private ServerType serverType = ServerType.UNKNOWN;
    private boolean antiLagEnabled = true;

    public AutoBuyStorage() {
        try {
            Files.createDirectories(CONFIG_DIR);
        } catch (IOException e) {
            AutoBuyMod.LOGGER.error("Failed to create config directory", e);
        }
    }

    public void addItem(AutoBuyItem item) {
        items.removeIf(i -> i.getName().equalsIgnoreCase(item.getName()));
        items.add(item);
    }

    public void removeItem(String itemName) {
        items.removeIf(i -> i.getName().equalsIgnoreCase(itemName));
    }

    public List<AutoBuyItem> getItems() {
        return new ArrayList<>(items);
    }

    public void clear() {
        items.clear();
    }

    public void save() {
        try {
            String json = gson.toJson(items);
            Files.write(ITEMS_FILE, json.getBytes());
            AutoBuyMod.LOGGER.info("AutoBuy items saved to " + ITEMS_FILE);
        } catch (IOException e) {
            AutoBuyMod.LOGGER.error("Failed to save items", e);
        }
    }

    public void load() {
        try {
            if (Files.exists(ITEMS_FILE)) {
                String json = new String(Files.readAllBytes(ITEMS_FILE));
                items = gson.fromJson(json, new TypeToken<List<AutoBuyItem>>(){}.getType());
                if (items == null) {
                    items = new ArrayList<>();
                }
                AutoBuyMod.LOGGER.info("Loaded " + items.size() + " items from config");
            }
        } catch (IOException e) {
            AutoBuyMod.LOGGER.error("Failed to load items", e);
            items = new ArrayList<>();
        }
    }

    public void detectServer(ServerType type) {
        this.serverType = type;
        AutoBuyMod.LOGGER.info("Server detected: " + type);
    }

    public ServerType getServerType() {
        return serverType;
    }

    public void setAntiLagEnabled(boolean enabled) {
        this.antiLagEnabled = enabled;
    }

    public boolean isAntiLagEnabled() {
        return antiLagEnabled && serverType == ServerType.FANTAY;
    }

    public int getItemCount() {
        return items.size();
    }
}
