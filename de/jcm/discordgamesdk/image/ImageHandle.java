
package de.jcm.discordgamesdk.image;

import de.jcm.discordgamesdk.image.ImageType;
import java.util.Objects;

public class ImageHandle {
    private ImageType type;
    private long id;
    private int size;

    public ImageHandle(ImageType type, long id, int size) {
        this.type = Objects.requireNonNull(type);
        this.id = id;
        if (size < 16) {
            throw new IllegalArgumentException("size is smaller than 16: " + size);
        }
        if (size > 256) {
            throw new IllegalArgumentException("size is greater than 2048: " + size);
        }
        if ((size & size - 1) != 0) {
            throw new IllegalArgumentException("size is not a power of 2: " + size);
        }
        this.size = size;
    }

    ImageHandle(int type, long id, int size) {
        this(ImageType.values()[type], id, size);
    }

    public ImageType getType() {
        return this.type;
    }

    public void setType(ImageType type) {
        this.type = Objects.requireNonNull(type);
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        if ((size & size - 1) != 0) {
            throw new IllegalArgumentException("size is not a power of 2: " + size);
        }
        this.size = size;
    }

    public String toString() {
        return "ImageHandle{type=" + this.type + ", id=" + this.id + ", size=" + this.size + "}";
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ImageHandle that = (ImageHandle)o;
        return this.id == that.id && this.size == that.size && this.type == that.type;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.type, this.id, this.size});
    }
}

