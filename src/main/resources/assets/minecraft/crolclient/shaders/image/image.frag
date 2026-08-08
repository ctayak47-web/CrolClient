#version 330 core

in vec4 vColor;
in vec2 vTexCoord;
in vec4 vData;

uniform sampler2D uTexture;
uniform vec4 uTint;
uniform int uFilterMode;
uniform bool uRoundedCorners;
uniform vec4 uCornerRadii;
uniform float uSmoothness;

out vec4 FragColor;

float roundedBoxSDF(vec2 centerPos, vec2 size, vec4 radius) {
    radius.xy = mix(radius.xy, radius.zw, centerPos.y);
    vec2 q = abs(centerPos) - size + radius.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius.x;
}

void main() {
    vec4 texColor;

    if (uFilterMode == 0) {
        texColor = texture(uTexture, vTexCoord);
    } else {
        vec2 texSize = textureSize(uTexture, 0);
        vec2 texelSize = 1.0 / texSize;
        vec2 f = fract(vTexCoord * texSize);

        vec2 center = (floor(vTexCoord * texSize) + 0.5) * texelSize;

        vec4 s00 = texture(uTexture, center + vec2(-texelSize.x, -texelSize.y));
        vec4 s10 = texture(uTexture, center + vec2(texelSize.x, -texelSize.y));
        vec4 s01 = texture(uTexture, center + vec2(-texelSize.x, texelSize.y));
        vec4 s11 = texture(uTexture, center + vec2(texelSize.x, texelSize.y));

        texColor = mix(
            mix(s00, s10, f.x),
            mix(s01, s11, f.x),
            f.y
        );
    }

    vec4 tintedColor = texColor * uTint;

    if (uRoundedCorners) {
        float distance = roundedBoxSDF(vTexCoord * 2.0 - 1.0, vec2(1.0), uCornerRadii);
        float alpha = 1.0 - smoothstep(-uSmoothness, 0.0, distance);
        tintedColor.a *= alpha;
    }

    FragColor = tintedColor;
}