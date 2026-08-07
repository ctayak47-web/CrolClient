
package com.kenai.jffi.internal;

import com.kenai.jffi.Platform;
import com.kenai.jffi.Util;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;

public class StubLoader {
    public static final int VERSION_MAJOR = StubLoader.getVersionField("MAJOR");
    public static final int VERSION_MINOR = StubLoader.getVersionField("MINOR");
    private static final String versionClassName = "com.kenai.jffi.Version";
    private static final Locale LOCALE = Locale.ENGLISH;
    private static final String bootPropertyFilename = "boot.properties";
    private static final String bootLibraryPropertyName = "jffi.boot.library.path";
    private static final String stubLibraryName = String.format("jffi-%d.%d", VERSION_MAJOR, VERSION_MINOR);
    private static final String TMPDIR_ENV = Platform.getPlatform().getOS() == Platform.OS.WINDOWS ? "TEMP" : "TMPDIR";
    private static final String TMPDIR = System.getProperty("java.io.tmpdir");
    private static final String TMPDIR_RECOMMENDATION = "Set `" + TMPDIR_ENV + "` or Java property `java.io.tmpdir` to a read/write path that is not mounted \"noexec\".";
    public static final String TMPDIR_WRITE_ERROR = "Unable to write jffi binary stub to `" + TMPDIR + "`.";
    public static final String TMPDIR_EXEC_ERROR = "Unable to execute or load jffi binary stub from `" + TMPDIR + "`.";
    private static volatile OS os = null;
    private static volatile CPU cpu = null;
    private static volatile Throwable failureCause = null;
    private static volatile boolean loaded = false;
    private static final File jffiExtractDir;
    private static final String jffiExtractName;
    private static final String JFFI_EXTRACT_DIR = "jffi.extract.dir";
    private static final String JFFI_EXTRACT_NAME = "jffi.extract.name";

    public static final boolean isLoaded() {
        return loaded;
    }

    public static final Throwable getFailureCause() {
        return failureCause;
    }

    private static OS determineOS() {
        String osName = System.getProperty("os.name").split(" ")[0];
        if (Util.startsWithIgnoreCase(osName, "mac", LOCALE) || Util.startsWithIgnoreCase(osName, "darwin", LOCALE)) {
            return OS.DARWIN;
        }
        if (Util.startsWithIgnoreCase(osName, "linux", LOCALE)) {
            return OS.LINUX;
        }
        if (Util.startsWithIgnoreCase(osName, "sunos", LOCALE) || Util.startsWithIgnoreCase(osName, "solaris", LOCALE)) {
            return OS.SOLARIS;
        }
        if (Util.startsWithIgnoreCase(osName, "aix", LOCALE)) {
            return OS.AIX;
        }
        if (Util.startsWithIgnoreCase(osName, "os400", LOCALE) || Util.startsWithIgnoreCase(osName, "os/400", LOCALE)) {
            return OS.IBMI;
        }
        if (Util.startsWithIgnoreCase(osName, "openbsd", LOCALE)) {
            return OS.OPENBSD;
        }
        if (Util.startsWithIgnoreCase(osName, "freebsd", LOCALE)) {
            return OS.FREEBSD;
        }
        if (Util.startsWithIgnoreCase(osName, "dragonfly", LOCALE)) {
            return OS.DRAGONFLY;
        }
        if (Util.startsWithIgnoreCase(osName, "windows", LOCALE)) {
            return OS.WINDOWS;
        }
        throw new RuntimeException("cannot determine operating system");
    }

    private static CPU determineCPU() {
        String archString = System.getProperty("os.arch", "unknown");
        if (Util.equalsIgnoreCase("x86", archString, LOCALE) || Util.equalsIgnoreCase("i386", archString, LOCALE) || Util.equalsIgnoreCase("i86pc", archString, LOCALE)) {
            return CPU.I386;
        }
        if (Util.equalsIgnoreCase("x86_64", archString, LOCALE) || Util.equalsIgnoreCase("amd64", archString, LOCALE)) {
            return CPU.X86_64;
        }
        if (Util.equalsIgnoreCase("ppc", archString, LOCALE) || Util.equalsIgnoreCase("powerpc", archString, LOCALE)) {
            return CPU.PPC;
        }
        if (Util.equalsIgnoreCase("ppc64", archString, LOCALE) || Util.equalsIgnoreCase("powerpc64", archString, LOCALE)) {
            if ("little".equals(System.getProperty("sun.cpu.endian"))) {
                return CPU.PPC64LE;
            }
            return CPU.PPC64;
        }
        if (Util.equalsIgnoreCase("ppc64le", archString, LOCALE) || Util.equalsIgnoreCase("powerpc64le", archString, LOCALE)) {
            return CPU.PPC64LE;
        }
        if (Util.equalsIgnoreCase("s390", archString, LOCALE) || Util.equalsIgnoreCase("s390x", archString, LOCALE)) {
            return CPU.S390X;
        }
        if (Util.equalsIgnoreCase("arm", archString, LOCALE) || Util.equalsIgnoreCase("armv7l", archString, LOCALE)) {
            return CPU.ARM;
        }
        if (Util.equalsIgnoreCase("aarch64", archString, LOCALE)) {
            return CPU.AARCH64;
        }
        if (Util.equalsIgnoreCase("loongarch64", archString, LOCALE)) {
            return CPU.LOONGARCH64;
        }
        if (Util.equalsIgnoreCase("mipsel", archString, LOCALE)) {
            return CPU.MIPSEL;
        }
        if (Util.equalsIgnoreCase("mips64", archString, LOCALE) || Util.equalsIgnoreCase("mips64el", archString, LOCALE)) {
            return CPU.MIPS64EL;
        }
        if (Util.equalsIgnoreCase("riscv64", archString, LOCALE)) {
            return CPU.RISCV64;
        }
        for (CPU cpu : CPU.values()) {
            if (!Util.equalsIgnoreCase(cpu.name(), archString, LOCALE)) continue;
            return cpu;
        }
        throw new RuntimeException("cannot determine CPU");
    }

    public static CPU getCPU() {
        return cpu != null ? cpu : (cpu = StubLoader.determineCPU());
    }

    public static OS getOS() {
        return os != null ? os : (os = StubLoader.determineOS());
    }

    private static String getStubLibraryName() {
        return stubLibraryName;
    }

    public static String getPlatformName() {
        if (StubLoader.getOS().equals((Object)OS.DARWIN)) {
            return "Darwin";
        }
        String osName = System.getProperty("os.name").split(" ")[0];
        return StubLoader.getCPU().name().toLowerCase(LOCALE) + "-" + osName;
    }

    private static String getStubLibraryPath() {
        String mappedLibraryName = OS.IBMI.equals((Object)StubLoader.getOS()) ? "lib" + stubLibraryName + ".so" : System.mapLibraryName(stubLibraryName);
        return "jni/" + StubLoader.getPlatformName() + "/" + mappedLibraryName;
    }

    static void load() {
        String libName = StubLoader.getStubLibraryName();
        ArrayList<Throwable> errors = new ArrayList<Throwable>();
        String bootPath = StubLoader.getBootPath();
        if (bootPath != null && StubLoader.loadFromBootPath(libName, bootPath, errors)) {
            return;
        }
        String libraryPath = System.getProperty("java.library.path");
        if (libraryPath != null && StubLoader.loadFromBootPath(libName, libraryPath, errors)) {
            return;
        }
        if (jffiExtractDir != null) {
            try {
                StubLoader.loadFromJar(jffiExtractDir);
                return;
            }
            catch (SecurityException se) {
                throw se;
            }
            catch (Throwable t1) {
                UnsatisfiedLinkError ule = new UnsatisfiedLinkError("could not load jffi library from " + jffiExtractDir);
                ule.initCause(t1);
                throw ule;
            }
        }
        try {
            StubLoader.loadFromJar(null);
            return;
        }
        catch (SecurityException se) {
            throw se;
        }
        catch (Throwable t) {
            try {
                StubLoader.loadFromJar(new File(System.getProperty("user.dir")));
            }
            catch (SecurityException se) {
                throw se;
            }
            catch (Throwable t1) {
                errors.add(t1);
            }
            if (!errors.isEmpty()) {
                Collections.reverse(errors);
                CharArrayWriter caw = new CharArrayWriter();
                PrintWriter pw = new PrintWriter(caw);
                for (Throwable t2 : errors) {
                    t2.printStackTrace(pw);
                }
                throw new UnsatisfiedLinkError(new String(caw.toCharArray()));
            }
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static String getBootPath() {
        String bootPath = System.getProperty(bootLibraryPropertyName);
        if (bootPath != null) {
            return bootPath;
        }
        InputStream is = StubLoader.getResourceAsStream(bootPropertyFilename);
        if (is != null) {
            Properties p = new Properties();
            try {
                p.load(is);
                String string = p.getProperty(bootLibraryPropertyName);
                return string;
            }
            catch (IOException ex) {
                String string = null;
                return string;
            }
            finally {
                try {
                    is.close();
                }
                catch (IOException iOException) {}
            }
        }
        return null;
    }

    private static String getAlternateLibraryPath(String path) {
        if (path.endsWith("dylib")) {
            return path.substring(0, path.lastIndexOf("dylib")) + "jnilib";
        }
        return path.substring(0, path.lastIndexOf("jnilib")) + "dylib";
    }

    private static boolean loadFromBootPath(String libName, String bootPath, Collection<Throwable> errors) {
        String[] dirs = bootPath.split(File.pathSeparator);
        for (int i = 0; i < dirs.length; ++i) {
            String soname = System.mapLibraryName(libName);
            File stub = new File(new File(dirs[i], StubLoader.getPlatformName()), soname);
            if (!stub.isFile()) {
                stub = new File(new File(dirs[i]), soname);
            }
            String path = stub.getAbsolutePath();
            if (stub.isFile()) {
                try {
                    System.load(path);
                    return true;
                }
                catch (UnsatisfiedLinkError ex) {
                    errors.add(ex);
                }
            }
            if (StubLoader.getOS() != OS.DARWIN || !new File(path = StubLoader.getAlternateLibraryPath(path)).isFile()) continue;
            try {
                System.load(path);
                return true;
            }
            catch (UnsatisfiedLinkError ex) {
                errors.add(ex);
            }
        }
        return false;
    }

    static String dlExtension() {
        switch (StubLoader.getOS()) {
            case WINDOWS: {
                return "dll";
            }
            case DARWIN: {
                return "dylib";
            }
        }
        return "so";
    }

    private static void loadFromJar(File tmpDirFile) throws IOException, LinkageError {
        File dstFile;
        String jffiExtractName = StubLoader.jffiExtractName;
        try (InputStream sourceIS = StubLoader.getStubLibraryStream();){
            dstFile = StubLoader.calculateExtractPath(tmpDirFile, jffiExtractName);
            if (jffiExtractName != null && dstFile.exists()) {
                StubLoader.verifyExistingLibrary(dstFile, sourceIS);
            } else {
                StubLoader.unpackLibrary(dstFile, sourceIS);
            }
        }
        catch (IOException ioe) {
            throw StubLoader.tempReadonlyError(ioe);
        }
        try {
            System.load(dstFile.getAbsolutePath());
            if (null == jffiExtractName) {
                dstFile.delete();
            }
        }
        catch (UnsatisfiedLinkError ule) {
            throw StubLoader.tempLoadError(ule);
        }
    }

    private static void unpackLibrary(File dstFile, InputStream sourceIS) throws IOException {
        try (FileOutputStream os = new FileOutputStream(dstFile);){
            ReadableByteChannel srcChannel = Channels.newChannel(sourceIS);
            long pos = 0L;
            while (sourceIS.available() > 0) {
                pos += os.getChannel().transferFrom(srcChannel, pos, Math.max(4096, sourceIS.available()));
            }
        }
    }

    private static void verifyExistingLibrary(File dstFile, InputStream sourceIS) throws IOException {
        int sourceSize = sourceIS.available();
        try (FileInputStream targetIS = new FileInputStream(dstFile);){
            byte[] targetDigest;
            int targetSize = targetIS.available();
            if (targetSize != sourceSize) {
                throw StubLoader.sizeMismatchError(dstFile, sourceSize, targetSize);
            }
            MessageDigest sourceMD = MessageDigest.getInstance("SHA-256");
            MessageDigest targetMD = MessageDigest.getInstance("SHA-256");
            DigestInputStream sourceDIS = new DigestInputStream(sourceIS, sourceMD);
            DigestInputStream targetDIS = new DigestInputStream(targetIS, targetMD);
            byte[] buf = new byte[8192];
            while (sourceIS.available() > 0) {
                sourceDIS.read(buf);
                targetDIS.read(buf);
            }
            byte[] sourceDigest = sourceMD.digest();
            if (!Arrays.equals(sourceDigest, targetDigest = targetMD.digest())) {
                throw StubLoader.digestMismatchError(dstFile);
            }
        }
        catch (NoSuchAlgorithmException nsae) {
            throw new IOException(nsae);
        }
    }

    private static SecurityException sizeMismatchError(File dstFile, int sourceSize, int targetSize) {
        return new SecurityException("file size mismatch: " + dstFile + " (" + targetSize + ") does not match packaged library (" + sourceSize + ")");
    }

    private static SecurityException digestMismatchError(File dstFile) {
        return new SecurityException("digest mismatch: " + dstFile + " does not match packaged library");
    }

    static File calculateExtractPath(File tmpDirFile, String jffiExtractName) throws IOException {
        if (jffiExtractName == null) {
            return StubLoader.calculateExtractPath(tmpDirFile);
        }
        if (null == jffiExtractName || jffiExtractName.isEmpty()) {
            jffiExtractName = "jffi-" + VERSION_MAJOR + "." + VERSION_MINOR;
        }
        if (!jffiExtractName.endsWith(StubLoader.dlExtension())) {
            jffiExtractName = jffiExtractName + "." + StubLoader.dlExtension();
        }
        File dstFile = null == tmpDirFile ? new File(TMPDIR, jffiExtractName) : new File(tmpDirFile, jffiExtractName);
        return dstFile;
    }

    static File calculateExtractPath(File tmpDirFile) throws IOException {
        File dstFile = null == tmpDirFile ? File.createTempFile("jffi", "." + StubLoader.dlExtension()) : File.createTempFile("jffi", "." + StubLoader.dlExtension(), tmpDirFile);
        dstFile.deleteOnExit();
        return dstFile;
    }

    private static IOException tempReadonlyError(IOException ioe) {
        return new IOException(TMPDIR_WRITE_ERROR + " " + TMPDIR_RECOMMENDATION, ioe);
    }

    private static UnsatisfiedLinkError tempLoadError(UnsatisfiedLinkError ule) {
        return new UnsatisfiedLinkError(TMPDIR_EXEC_ERROR + " " + TMPDIR_RECOMMENDATION + "\n" + ule.getLocalizedMessage());
    }

    private static InputStream getStubLibraryStream() {
        Object[] paths;
        String stubPath = StubLoader.getStubLibraryPath();
        for (String string : paths = new String[]{stubPath, "/" + stubPath}) {
            InputStream is = StubLoader.getResourceAsStream(string);
            if (is == null && StubLoader.getOS() == OS.DARWIN) {
                is = StubLoader.getResourceAsStream(StubLoader.getAlternateLibraryPath(string));
            }
            if (is == null) continue;
            return is;
        }
        throw new UnsatisfiedLinkError("could not locate stub library in jar file.  Tried " + Arrays.deepToString(paths));
    }

    private static InputStream getResourceAsStream(String resourceName) {
        ClassLoader[] cls;
        for (ClassLoader cl : cls = new ClassLoader[]{ClassLoader.getSystemClassLoader(), StubLoader.class.getClassLoader(), Thread.currentThread().getContextClassLoader()}) {
            InputStream is;
            if (cl == null || (is = cl.getResourceAsStream(resourceName)) == null) continue;
            return is;
        }
        return null;
    }

    private static int getVersionField(String name) {
        try {
            Class<?> c = Class.forName(versionClassName);
            return (Integer)c.getField(name).get(c);
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    static {
        String extractDir = System.getProperty(JFFI_EXTRACT_DIR);
        jffiExtractDir = extractDir != null ? new File(extractDir) : null;
        String extractName = System.getProperty(JFFI_EXTRACT_NAME);
        jffiExtractName = extractName != null ? extractName : null;
        try {
            StubLoader.load();
            loaded = true;
        }
        catch (Throwable t) {
            failureCause = t;
        }
    }

    public static enum OS {
        DARWIN,
        FREEBSD,
        NETBSD,
        OPENBSD,
        DRAGONFLY,
        LINUX,
        SOLARIS,
        WINDOWS,
        AIX,
        IBMI,
        ZLINUX,
        UNKNOWN;

        public String toString() {
            return this.name().toLowerCase(LOCALE);
        }
    }

    public static enum CPU {
        I386,
        X86_64,
        PPC,
        PPC64,
        PPC64LE,
        SPARC,
        SPARCV9,
        S390X,
        ARM,
        AARCH64,
        LOONGARCH64,
        MIPSEL,
        MIPS64EL,
        RISCV64,
        UNKNOWN;

        public String toString() {
            return this.name().toLowerCase(LOCALE);
        }
    }
}

