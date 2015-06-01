#version 130

uniform sampler2D textureUnitId;

in vec4 pass_Position;
in vec4 pass_Color;
in vec2 pass_TextureCoord;

out vec4 out_Color;


void main(void) {
	
	out_Color = texture(textureUnitId, pass_TextureCoord);
	
}