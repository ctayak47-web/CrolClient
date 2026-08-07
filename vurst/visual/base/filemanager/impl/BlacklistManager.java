
package vurst.visual.base.filemanager.impl;

import com.google.common.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import vurst.visual.base.filemanager.api.ManagerFileAbstract;

public class BlacklistManager
extends ManagerFileAbstract<String> {
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,16}$");

    public BlacklistManager() {
        super("blacklist.json", "", new TypeToken<LinkedHashSet<String>>(){}.getType(), LinkedHashSet::new);
    }

    public boolean isBlacklisted(String name) {
        return this.findStoredName(name) != null;
    }

    public boolean addName(String name) {
        String normalized = this.normalizeName(name);
        if (normalized == null || this.isBlacklisted(normalized)) {
            return false;
        }
        this.getItems().add(name.trim());
        this.save();
        return true;
    }

    public boolean removeName(String name) {
        String storedName = this.findStoredName(name);
        if (storedName == null) {
            return false;
        }
        boolean removed = this.getItems().remove(storedName);
        if (removed) {
            this.save();
        }
        return removed;
    }

    public void clearNames() {
        this.getItems().clear();
        this.save();
    }

    public List<String> getSortedNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (String item : this.getItems()) {
            if (item == null || item.isBlank()) continue;
            names.add(item);
        }
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    public boolean isValidNickname(String name) {
        String normalized = this.normalizeName(name);
        return normalized != null && NICKNAME_PATTERN.matcher(normalized).matches();
    }

    @Override
    public void save() {
        if (this.getItems().isEmpty() && !this.getFile().exists()) {
            return;
        }
        super.save();
    }

    private String findStoredName(String name) {
        String normalized = this.normalizeName(name);
        if (normalized == null) {
            return null;
        }
        Collection items = this.getItems();
        for (String item : items) {
            if (!normalized.equals(this.normalizeName(item))) continue;
            return item;
        }
        return null;
    }

    private String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }
}

