
package crol.client.utility.compat;

public final class LunarCompat {
    private static final boolean LUNAR = LunarCompat.detectLunar();

    private LunarCompat() {
    }

    public static boolean isLunar() {
        return LUNAR;
    }

    private static boolean detectLunar() {
        return LunarCompat.isClassPresent("com.moonsworth.lunar.client.LunarClient") || LunarCompat.isClassPresent("com.moonsworth.lunar.client.LunarClientBootstrap") || LunarCompat.isClassPresent("com.moonsworth.lunar.client.IRCCICRCCOHRCHOCOCHIROIRIIHHII");
    }

    private static boolean isClassPresent(String name) {
        try {
            Class.forName(name, false, LunarCompat.class.getClassLoader());
            return true;
        }
        catch (Throwable ignored) {
            return false;
        }
    }
}

