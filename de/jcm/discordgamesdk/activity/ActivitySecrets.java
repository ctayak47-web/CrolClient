
package de.jcm.discordgamesdk.activity;

public class ActivitySecrets {
    private String match;
    private String join;
    private String spectate;

    public void setMatchSecret(String secret) {
        this.match = secret;
    }

    public String getMatchSecret() {
        return this.match;
    }

    public void setJoinSecret(String secret) {
        this.join = secret;
    }

    public String getJoinSecret() {
        return this.join;
    }

    public void setSpectateSecret(String secret) {
        this.spectate = secret;
    }

    public String getSpectateSecret() {
        return this.spectate;
    }
}

