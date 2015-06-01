#version 130

uniform vec4 globalColor;

out vec4 out_Color;


void main(void) {

	out_Color = globalColor;
}