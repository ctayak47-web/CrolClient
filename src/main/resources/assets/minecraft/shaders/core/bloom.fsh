#version 150

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord) * vertexColor;

    vec3 bloomColor = color.rgb * 2.5;

    float brightness = dot(bloomColor, vec3(0.2126, 0.7152, 0.0722));
    bloomColor *= smoothstep(0.7, 0.8, brightness);

    fragColor = vec4(bloomColor, color.a);
}