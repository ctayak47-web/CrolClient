
package de.jcm.discordgamesdk.activity;

public class ActivityAssets {
    private String large_image;
    private String large_text;
    private String small_image;
    private String small_text;

    public void setLargeImage(String assetKey) {
        this.large_image = assetKey;
    }

    public String getLargeImage() {
        return this.large_image;
    }

    public void setLargeText(String text) {
        this.large_text = text;
    }

    public String getLargeText() {
        return this.large_text;
    }

    public void setSmallImage(String assetKey) {
        this.small_image = assetKey;
    }

    public String getSmallImage() {
        return this.small_image;
    }

    public void setSmallText(String text) {
        this.small_text = text;
    }

    public String getSmallText() {
        return this.small_text;
    }

    public String toString() {
        return "ActivityAssets{large_image='" + this.large_image + "', large_text='" + this.large_text + "', small_image='" + this.small_image + "', small_text='" + this.small_text + "'}";
    }
}

