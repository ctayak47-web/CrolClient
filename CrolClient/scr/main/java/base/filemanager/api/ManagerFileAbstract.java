
package crol.client.base.filemanager.api;

import com.google.gson.Gson;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Base64;
import java.util.Collection;
import java.util.function.Supplier;
import lombok.Generated;
import crol.client.CrolClient;
import crol.client.utility.crypt.CryptUtility;

public class ManagerFileAbstract<T> {
    private Collection<T> items;
    private final String fileName;
    private final String shifr;
    private final Type type;
    private final Supplier<Collection<T>> collectionSupplier;
    private final boolean createFileIfMissing;

    public ManagerFileAbstract(String fileName, String shifr, Type type, Supplier<Collection<T>> collectionSupplier) {
        this(fileName, shifr, type, collectionSupplier, true);
    }

    public ManagerFileAbstract(String fileName, String shifr, Type type, Supplier<Collection<T>> collectionSupplier, boolean createFileIfMissing) {
        this.fileName = fileName;
        this.shifr = shifr;
        this.type = type;
        this.collectionSupplier = collectionSupplier;
        this.createFileIfMissing = createFileIfMissing;
        this.items = collectionSupplier.get();
        this.ensureDirectory();
        File file = this.getFile();
        if (!file.exists()) {
            if (createFileIfMissing) {
                this.save();
            }
            return;
        }
        this.load();
    }

    protected File getFile() {
        return new File(CrolClient.DIRECTORY, this.fileName);
    }

    private void ensureDirectory() {
        File dir = CrolClient.DIRECTORY;
        try {
            Files.createDirectories(dir.toPath(), new FileAttribute[0]);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        Gson gson = new Gson();
        String json = gson.toJson(this.items);
        this.ensureDirectory();
        String content = this.shifr.isEmpty() ? json : Base64.getEncoder().encodeToString(CryptUtility.encryptData(json.getBytes(StandardCharsets.UTF_8), this.shifr));
        try {
            Files.writeString(this.getFile().toPath(), (CharSequence)content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() {
        this.items = this.collectionSupplier.get();
        try {
            Gson gson = new Gson();
            String content = Files.readString(this.getFile().toPath(), StandardCharsets.UTF_8).trim();
            if (!this.shifr.isEmpty()) {
                if (!content.isEmpty()) {
                    byte[] encryptedData = Base64.getDecoder().decode(content);
                    byte[] decryptedData = CryptUtility.decryptData(encryptedData, this.shifr);
                    String json = new String(decryptedData, StandardCharsets.UTF_8);
                    this.items = (Collection)gson.fromJson(json, this.type);
                }
            } else if (!content.isEmpty()) {
                this.items = (Collection)gson.fromJson(content, this.type);
            }
            if (this.items == null) {
                this.items = this.collectionSupplier.get();
            }
        }
        catch (Exception e) {
            this.items = this.collectionSupplier.get();
            e.printStackTrace();
        }
    }

    public void add(T item) {
        this.items.add(item);
    }

    public void remove(T item) {
        this.items.remove(item);
    }

    @Generated
    public Collection<T> getItems() {
        return this.items;
    }
}

