#version 130

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

in vec4 in_Color;
in vec2 in_TextureCoord;
in vec4 in_Position;

out vec4 pass_Position;
out vec4 pass_Color;
out vec2 pass_TextureCoord;

void main(void) {
	
	gl_Position = projectionMatrix * viewMatrix * modelMatrix * in_Position;
	
	pass_Position = in_Position;
	pass_Color = in_Color;
	pass_TextureCoord = in_TextureCoord;
}