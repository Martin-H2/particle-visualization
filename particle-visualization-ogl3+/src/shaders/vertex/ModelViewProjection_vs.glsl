#version 130

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform int renderMode;

in vec2 in_TextureCoord;
in vec4 in_Position;

out vec2 pass_TextureCoord;

void main(void) {
	
	gl_Position = projectionMatrix * viewMatrix * modelMatrix * in_Position;
	
	pass_TextureCoord = in_TextureCoord;
}