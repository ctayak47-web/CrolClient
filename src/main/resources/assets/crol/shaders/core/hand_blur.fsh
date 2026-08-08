#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform float BlurRadius;
uniform float Threshold;
uniform vec4 TintColor;

in vec2 TexCoord;
in vec4 FragColor;
out vec4 OutColor;

const float PI2 = 6.28318530718;
const float STEP = PI2 / 16.0;

void main() {
    vec2 uv = TexCoord;

    vec4 withHand   = texture(Sampler0, uv);
    vec4 beforeHand = texture(Sampler1, uv);


    vec3 diff3 = abs(withHand.rgb - beforeHand.rgb);
    float diff = max(diff3.r, max(diff3.g, diff3.b)) * 8.0;
    float mask = smoothstep(Threshold, Threshold + 0.1, diff);

    if (mask < 0.01) discard;

    vec2 multiplier = BlurRadius / textureSize(Sampler1, 0);
    vec3 blurredBg = vec3(0.0);
    float total = 0.0;
    for (float d = 0.0; d < PI2; d += STEP) {
        for (float i = 0.2; i <= 1.0; i += 0.2) {
            blurredBg += texture(Sampler1, uv + vec2(cos(d), sin(d)) * multiplier * i).rgb;
            total += 1.0;
        }
    }
    blurredBg /= total;

    vec3 tinted = mix(blurredBg, TintColor.rgb, TintColor.a);
    OutColor = vec4(tinted, mask);
}