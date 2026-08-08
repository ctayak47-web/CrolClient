package ru.crolclient.api.system.shader;

import ru.crolclient.api.system.shader.implement.RoundShader;
import ru.crolclient.api.system.shader.implement.BloomShader;

public class ShadersPool {
    public static RoundShader ROUNDED_SHADER;
    public static BloomShader BLOOM_SHADER;

    public static void initShaders() {
        ROUNDED_SHADER = new RoundShader();
        BLOOM_SHADER = new BloomShader();
    }
}