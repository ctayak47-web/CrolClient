
package de.jcm.discordgamesdk.activity;

public class ActivityButton {
    private String label;
    private String url;

    public ActivityButton() {
    }

    public ActivityButton(String label, String url) {
        this.label = label;
        this.url = url;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

