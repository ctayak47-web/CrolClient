
package de.jcm.discordgamesdk.impl;

public class Error {
    private int code;
    private String message;

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public String toString() {
        return "Error " + this.getCode() + ": " + this.getMessage();
    }
}

