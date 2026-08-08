
package crol.client.base.filemanager.impl;

import com.google.common.reflect.TypeToken;
import java.util.HashSet;
import java.util.Set;
import crol.client.base.filemanager.api.ManagerFileAbstract;

public class StaffManager
extends ManagerFileAbstract<String> {
    public StaffManager() {
        super("staffName.json", "", new TypeToken<Set<String>>(){}.getType(), HashSet::new, false);
    }

    public boolean isStaff(String staffName) {
        return this.getItems().contains(staffName);
    }

    @Override
    public void save() {
        if (this.getItems().isEmpty() && !this.getFile().exists()) {
            return;
        }
        super.save();
    }
}

