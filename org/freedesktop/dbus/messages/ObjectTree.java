
package org.freedesktop.dbus.messages;

import java.util.regex.Pattern;
import org.freedesktop.dbus.messages.ExportedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectTree {
    public static final Pattern SLASH_PATTERN = Pattern.compile("/");
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private TreeNode root = new TreeNode("");

    private TreeNode recursiveFind(TreeNode _current, String _path) {
        if ("/".equals(_path)) {
            return _current;
        }
        String[] elements = _path.split("/", 2);
        if (_path.startsWith(_current.name)) {
            if (_path.equals(_current.name)) {
                return _current;
            }
            if (_current.down == null) {
                return null;
            }
            return this.recursiveFind(_current.down, elements[1]);
        }
        if (_current.right == null) {
            return null;
        }
        if (0 > _current.right.name.compareTo(elements[0])) {
            return null;
        }
        return this.recursiveFind(_current.right, _path);
    }

    private TreeNode recursiveAdd(TreeNode _current, String _path, ExportedObject _object, String _data) {
        String[] elements = SLASH_PATTERN.split(_path, 2);
        if (_path.startsWith(_current.name)) {
            if (1 == elements.length || "".equals(elements[1])) {
                _current.object = _object;
                _current.data = _data;
            } else {
                if (_current.down == null) {
                    String[] el = elements[1].split("/", 2);
                    _current.down = new TreeNode(el[0]);
                }
                _current.down = this.recursiveAdd(_current.down, elements[1], _object, _data);
            }
        } else if (_current.right == null) {
            _current.right = new TreeNode(elements[0]);
            _current.right = this.recursiveAdd(_current.right, _path, _object, _data);
        } else if (0 > _current.right.name.compareTo(elements[0])) {
            TreeNode t = new TreeNode(elements[0]);
            t.right = _current.right;
            _current.right = t;
            _current.right = this.recursiveAdd(_current.right, _path, _object, _data);
        } else {
            _current.right = this.recursiveAdd(_current.right, _path, _object, _data);
        }
        return _current;
    }

    public synchronized void add(String _path, ExportedObject _object, String _data) {
        this.logger.debug("Adding {} to object tree", (Object)_path);
        this.root = this.recursiveAdd(this.root, _path, _object, _data);
    }

    private TreeNode recursiveRemove(TreeNode _current, String _path) {
        String[] elements = _path.split("/", 2);
        if (elements[0].equals(_current.name)) {
            if (1 == elements.length || "".equals(elements[1])) {
                _current.object = null;
                _current.data = null;
                if (_current.down != null) {
                    return _current;
                }
                return _current.right;
            }
            if (_current.down != null) {
                _current.down = this.recursiveRemove(_current.down, elements[1]);
                if (_current.down == null && _current.data == null) {
                    return _current.right;
                }
            }
            return _current;
        }
        if (_current.right == null) {
            return _current;
        }
        if (0 > _current.right.name.compareTo(elements[0])) {
            return _current;
        }
        _current.right = this.recursiveRemove(_current.right, _path);
        return _current;
    }

    public synchronized void remove(String _path) {
        this.logger.debug("Removing {} from object tree", (Object)_path);
        this.recursiveRemove(this.root, _path);
    }

    public String Introspect(String _path) {
        TreeNode t = this.recursiveFind(this.root, _path);
        if (null == t) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<node name=\"");
        sb.append(_path);
        sb.append("\">\n");
        if (null != t.data) {
            sb.append(t.data);
        }
        t = t.down;
        while (null != t) {
            sb.append("<node name=\"");
            sb.append(t.name);
            sb.append("\"/>\n");
            t = t.right;
        }
        sb.append("</node>");
        return sb.toString();
    }

    private String recursivePrint(TreeNode _current) {
        Object s = "";
        if (null != _current) {
            s = (String)s + _current.name;
            if (null != _current.object) {
                s = (String)s + "*";
            }
            if (null != _current.down) {
                s = (String)s + "/{" + this.recursivePrint(_current.down) + "}";
            }
            if (null != _current.right) {
                s = (String)s + ", " + this.recursivePrint(_current.right);
            }
        }
        return s;
    }

    public String toString() {
        return this.recursivePrint(this.root);
    }

    static class TreeNode {
        String name;
        ExportedObject object;
        String data;
        TreeNode right;
        TreeNode down;

        TreeNode(String _name) {
            this.name = _name;
        }

        TreeNode(String _name, ExportedObject _object, String _data) {
            this.name = _name;
            this.object = _object;
            this.data = _data;
        }
    }
}

