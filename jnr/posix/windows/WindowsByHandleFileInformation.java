
package jnr.posix.windows;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.posix.windows.CommonFileInformation;

public class WindowsByHandleFileInformation
extends CommonFileInformation {
    final Struct.Unsigned32 dwFileAttributes = new Struct.Unsigned32();
    final Struct.UnsignedLong chigh = new Struct.UnsignedLong();
    final Struct.UnsignedLong clow = new Struct.UnsignedLong();
    final Struct.UnsignedLong ahigh = new Struct.UnsignedLong();
    final Struct.UnsignedLong alow = new Struct.UnsignedLong();
    final Struct.UnsignedLong uhigh = new Struct.UnsignedLong();
    final Struct.UnsignedLong ulow = new Struct.UnsignedLong();
    final Struct.Unsigned32 dwVolumeSerialNumber = new Struct.Unsigned32();
    final Struct.Unsigned32 nFileSizeHigh = new Struct.Unsigned32();
    final Struct.Unsigned32 nFileSizeLow = new Struct.Unsigned32();
    final Struct.Unsigned32 nNumberOfLinks = new Struct.Unsigned32();
    final Struct.Unsigned32 nFileIndexHigh = new Struct.Unsigned32();
    final Struct.Unsigned32 nFileIndexLow = new Struct.Unsigned32();

    public WindowsByHandleFileInformation(Runtime runtime) {
        super(runtime);
    }

    @Override
    public CommonFileInformation.HackyFileTime getCreationTime() {
        return new CommonFileInformation.HackyFileTime(this.chigh, this.clow);
    }

    @Override
    public CommonFileInformation.HackyFileTime getLastAccessTime() {
        return new CommonFileInformation.HackyFileTime(this.ahigh, this.alow);
    }

    @Override
    public CommonFileInformation.HackyFileTime getLastWriteTime() {
        return new CommonFileInformation.HackyFileTime(this.uhigh, this.ulow);
    }

    @Override
    public int getFileAttributes() {
        return this.dwFileAttributes.intValue();
    }

    @Override
    public long getFileSizeHigh() {
        return this.nFileSizeHigh.intValue();
    }

    @Override
    public long getFileSizeLow() {
        return this.nFileSizeLow.intValue();
    }
}

