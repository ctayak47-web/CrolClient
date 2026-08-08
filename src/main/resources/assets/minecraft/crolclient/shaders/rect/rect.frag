#version 330 core

in vec2 vTexCoord;
in vec4 vColor;
in float vCornerRadius;

out vec4 fragColor;

void main() {
    vec2 pixelPos = vTexCoord * 2.0 - 1.0;

    vec2 cornerOffset = abs(pixelPos) - (1.0 - vCornerRadius);
    float distance = length(max(cornerOffset, 0.0));

    float alpha = 1.0 - smoothstep(0.0, vCornerRadius, distance);

    fragColor = vColor * alpha;
}