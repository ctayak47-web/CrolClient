package ru.crolclient.implement.screens.title.account;

import com.google.gson.*;
import net.minecraft.client.MinecraftClient;
import ru.crolclient.common.QuickImports;

import java.io.*;

public class AccountManagerConfig implements QuickImports {
    private static final File FILE = new File(MinecraftClient.getInstance().runDirectory, "crolclient/accounts/username.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


    // чо за хуйня лол
    public static void saveAccounts(String selectedUsername) {
        if (!FILE.exists()) {
            FILE.getParentFile().mkdirs();
        }
        try {
            FileWriter fileWriter = new FileWriter(FILE);
            JsonArray accountsArray = new JsonArray();

            JsonObject currentAccountObject = new JsonObject();
            currentAccountObject.addProperty("SelectAccount", selectedUsername);
            accountsArray.add(currentAccountObject);

            for (Account account : AccountManagerScreen.ACCOUNTS) {
                JsonObject accountObject = new JsonObject();
                accountObject.addProperty("Username", account.getUsername());
                accountsArray.add(accountObject);
            }

            JsonObject object = new JsonObject();
            object.add("Accounts", accountsArray);

            fileWriter.write(GSON.toJson(object));

            fileWriter.flush();
            fileWriter.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static String loadCurrentUsername() {
        if (FILE.exists()) {
            try (FileReader fileReader = new FileReader(FILE)) {
                JsonObject object = GSON.fromJson(fileReader, JsonObject.class);
                if (object != null && object.has("Accounts")) {
                    JsonArray accountsArray = object.getAsJsonArray("Accounts");
                    for (JsonElement element : accountsArray) {
                        if (element.isJsonObject()) {
                            JsonObject accountObject = element.getAsJsonObject();
                            if (accountObject.has("SelectAccount")) {
                                return accountObject.get("SelectAccount").getAsString();
                            }
                        }
                    }
                }
            } catch (IOException | JsonParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}