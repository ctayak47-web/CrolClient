package com.autobuy.data;

public class AutoBuyItem {
    private String name;
    private long minPrice;
    private long maxPrice;
    private long lastSearchTime;
    private boolean enabled;

    public AutoBuyItem(String name, long minPrice, long maxPrice) {
        this.name = name;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.lastSearchTime = 0;
        this.enabled = true;
    }

    public String getName() {
        return name;
    }

    public long getMinPrice() {
        return minPrice;
    }

    public long getMaxPrice() {
        return maxPrice;
    }

    public void setMinPrice(long minPrice) {
        this.minPrice = minPrice;
    }

    public void setMaxPrice(long maxPrice) {
        this.maxPrice = maxPrice;
    }

    public long getLastSearchTime() {
        return lastSearchTime;
    }

    public void setLastSearchTime(long time) {
        this.lastSearchTime = time;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return name + " (" + minPrice + "-" + maxPrice + ")";
    }
}
