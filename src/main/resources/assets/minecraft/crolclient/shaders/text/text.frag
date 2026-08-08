#version 330 core

in vec4 vColor;
in vec2 vTexCoord;
in vec4 vData;

uniform sampler2D uFontTexture;
uniform vec4 uColor;
uniform float uSmoothness;
uniform bool uShadowEnabled;
uniform vec4 uShadowColor;
uniform vec2 uShadowOffset;

out vec4 FragColor;

void main() {
    float distance = texture(uFontTexture, vTexCoord).r;

    float smoothedAlpha = smoothstep(0.5 - uSmoothness, 0.5 + uSmoothness, distance);

    if (uShadowEnabled) {
        vec2 shadowCoord = vTexCoord - uShadowOffset;
        float shadowDistance = texture(uFontTexture, shadowCoord).r;
        float shadowAlpha = smoothstep(0.5 - uSmoothness, 0.5 + uSmoothness, shadowDistance);

        vec4 shadowColor = vec4(uShadowColor.rgb, uShadowColor.a * shadowAlpha);
        vec4 textColor = vec4(uColor.rgb, uColor.a * smoothedAlpha);

        FragColor = mix(shadowColor, textColor, smoothedAlpha);
    } else {
        FragColor = vec4(uColor.rgb, uColor.a * smoothedAlpha);
    }
}