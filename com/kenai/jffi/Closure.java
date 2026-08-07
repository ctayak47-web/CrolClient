
package com.kenai.jffi;

public interface Closure {
    public void invoke(Buffer var1);

    public static interface Handle {
        public long getAddress();

        public void setAutoRelease(boolean var1);

        public void dispose();

        @Deprecated
        public void free();
    }

    public static interface Buffer {
        public byte getByte(int var1);

        public short getShort(int var1);

        public int getInt(int var1);

        public long getLong(int var1);

        public float getFloat(int var1);

        public double getDouble(int var1);

        public long getAddress(int var1);

        public long getStruct(int var1);

        public void setByteReturn(byte var1);

        public void setShortReturn(short var1);

        public void setIntReturn(int var1);

        public void setLongReturn(long var1);

        public void setFloatReturn(float var1);

        public void setDoubleReturn(double var1);

        public void setAddressReturn(long var1);

        public void setStructReturn(long var1);

        public void setStructReturn(byte[] var1, int var2);
    }
}

