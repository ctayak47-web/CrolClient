#version 330 core

layout (location = 0) in vec2 aPosition;
layout (location = 1) in vec4 aColor;
layout (location = 2) in vec2 aTexCoord;
layout (location = 3) in vec4 aData;

uniform mat4 uProjection;
uniform mat4 uModelView;

out vec4 vColor;
out vec2 vTexCoord;
out vec4 vData;

void main() {
    gl_Position = uProjection * uModelView * vec4(aPosition, 0.0, 1.0);
    vColor = aColor;
    vTexCoord = aTexCoord;
    vData = aData;
}