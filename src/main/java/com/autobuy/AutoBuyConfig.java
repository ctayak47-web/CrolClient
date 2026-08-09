package com.autobuy;

import net.minecraftforge.common.ForgeConfigSpec;

public class AutoBuyConfig {
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec.BooleanValue AUTO_BUY_ENABLED;
    public static final ForgeConfigSpec.IntValue SEARCH_INTERVAL;
    public static final ForgeConfigSpec.IntValue ANTI_LAG_INTERVAL;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        AUTO_BUY_ENABLED = builder
            .comment("Enable AutoBuy functionality")
            .define("enabled", false);

        SEARCH_INTERVAL = builder
            .comment("Interval between /ah search commands in ticks (200 = 10 seconds)")
            .defineInRange("searchInterval", 200, 100, 400);

        ANTI_LAG_INTERVAL = builder
            .comment("Interval for anti-lag rotation in ticks (600 = 30 seconds)")
            .defineInRange("antiLagInterval", 600, 300, 1200);

        CLIENT_SPEC = builder.build();
    }
}
