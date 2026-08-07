
package org.freedesktop.dbus;

import java.util.Objects;

public class DBusPath
implements Comparable<DBusPath> {
    private String path;

    public DBusPath(String _path) {
        this.setPath(_path);
    }

    public String getPath() {
        return this.path;
    }

    public String toString() {
        return this.getPath();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object _other) {
        if (!(_other instanceof DBusPath)) return false;
        DBusPath dp = (DBusPath)_other;
        if (this.getPath() == null) return false;
        if (!this.getPath().equals(dp.getPath())) return false;
        return true;
    }

    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();
        result = 31 * result + Objects.hash(this.path);
        return result;
    }

    @Override
    public int compareTo(DBusPath _that) {
        if (this.getPath() == null || _that == null) {
            return 0;
        }
        return this.getPath().compareTo(_that.getPath());
    }

    public void setPath(String _path) {
        this.path = _path;
    }
}

