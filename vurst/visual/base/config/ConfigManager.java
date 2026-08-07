
package vurst.visual.base.config;

import com.darkmagician6.eventapi.EventManager;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Generated;
import vurst.visual.VurstVisual;
import vurst.visual.base.config.Config;
import vurst.visual.utility.crypt.CryptUtility;

public class ConfigManager {
    public static final File configDirectory = new File(VurstVisual.DIRECTORY, "configs");
    public static final String CONFIG_EXTENSION = "vurstvisual";
    private static final String LEGACY_EXTENSION = "Vurst Visual".toLowerCase();
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final String shifr = "config";

    public ConfigManager() {
        configDirectory.mkdirs();
        boolean loaded = this.loadConfig("current_config");
        if (!loaded) {
            this.applyFirstLaunchDefaults();
            this.saveConfig("current_config");
        }
        EventManager.register(this);
        this.scheduler.scheduleAtFixedRate(() -> {
            try {
                this.saveConfig("current_config");
            }
            catch (Exception exception) {
                
            }
        }, 5L, 5L, TimeUnit.MINUTES);
    }

    private void applyFirstLaunchDefaults() {
        if (VurstVisual.getInstance().getMenuScreen() != null) {
            VurstVisual.getInstance().getMenuScreen().setColumns(3);
        }
    }

    public boolean loadConfig(String configName) {
        boolean bl;
        if (configName == null) {
            return false;
        }
        Config config = this.findConfig(configName);
        if (config == null) {
            return false;
        }
        BufferedReader reader = new BufferedReader(new FileReader(config.getFile()));
        try {
            JsonParser parser = new JsonParser();
            String encryptedDataBase64 = reader.readLine();
            byte[] encryptedData = Base64.getDecoder().decode(encryptedDataBase64);
            byte[] decryptedData = CryptUtility.decryptData(encryptedData, "config");
            String json = new String(decryptedData, StandardCharsets.UTF_8);
            JsonObject object = (JsonObject)parser.parse(json);
            config.load(object);
            bl = true;
        }
        catch (Throwable throwable) {
            try {
                try {
                    reader.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        reader.close();
        return bl;
    }

    public boolean saveConfig(String configName) {
        try {
            if (configName == null) {
                return false;
            }
            Config config = new Config(configName);
            String contentPrettyPrint = new GsonBuilder().setPrettyPrinting().create().toJson(config.save());
            contentPrettyPrint = Base64.getEncoder().encodeToString(CryptUtility.encryptData(contentPrettyPrint.getBytes(), "config"));
            try {
                FileWriter writer = new FileWriter(config.getFile());
                writer.write(contentPrettyPrint);
                writer.close();
                return true;
            }
            catch (IOException e) {
                return false;
            }
        }
        catch (Exception e) {
            return false;
        }
    }

    public Config findConfig(String configName) {
        if (configName == null) {
            return null;
        }
        File file = ConfigManager.getConfigFile(configName);
        if (file.exists()) {
            return new Config(configName, file, false);
        }
        File legacy = ConfigManager.getLegacyConfigFile(configName);
        if (legacy.exists()) {
            return new Config(configName, legacy, false);
        }
        return null;
    }

    public List<String> configNames() {
        File[] files = configDirectory.listFiles();
        ArrayList<String> names = new ArrayList<String>();
        if (files == null) {
            return names;
        }
        for (File file : files) {
            String name = file.getName();
            String base = this.stripExtension(name);
            if (base == null || names.contains(base)) continue;
            names.add(base);
        }
        return names;
    }

    public boolean deleteConfig(String configName) {
        if (configName == null) {
            return false;
        }
        Config config = this.findConfig(configName);
        if (config != null) {
            File f = config.getFile();
            return f.exists() && f.delete();
        }
        return false;
    }

    public static File getConfigFile(String name) {
        return new File(configDirectory, name + ".vurstvisual");
    }

    public static File getLegacyConfigFile(String name) {
        return new File(configDirectory, name + "." + LEGACY_EXTENSION);
    }

    public static String normalizeConfigName(String name) {
        if (name == null) {
            return null;
        }
        String suffixNew = ".vurstvisual";
        String suffixLegacy = "." + LEGACY_EXTENSION;
        if (name.endsWith(suffixNew)) {
            return name.substring(0, name.length() - suffixNew.length());
        }
        if (name.endsWith(suffixLegacy)) {
            return name.substring(0, name.length() - suffixLegacy.length());
        }
        return name;
    }

    private String stripExtension(String fileName) {
        String suffixNew = ".vurstvisual";
        String suffixLegacy = "." + LEGACY_EXTENSION;
        if (fileName.endsWith(suffixNew)) {
            return fileName.substring(0, fileName.length() - suffixNew.length());
        }
        if (fileName.endsWith(suffixLegacy)) {
            return fileName.substring(0, fileName.length() - suffixLegacy.length());
        }
        return null;
    }

    public void save() {
        this.scheduler.shutdown();
        this.saveConfig("current_config");
    }

    @Generated
    public ScheduledExecutorService getScheduler() {
        return this.scheduler;
    }

    @Generated
    public String getShifr() {
        return this.shifr;
    }
}

