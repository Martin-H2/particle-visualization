#version 130

uniform mat4 modelMatrix;
uniform sampler2D textureUnitId;

in vec4 pass_Position;
in vec4 pass_Color;
in vec2 pass_TextureCoord;

out vec4 out_Color;


void main(void) {


	vec4 pos = pass_Position;
	
	if (pos.x > 0.499999 || pos.x < -0.499999) {
		out_Color = vec4(pos.y+0.499999, pos.y+0.499999, pos.y+0.499999, 1);
	}
	else if (pos.y > 0.499999 || pos.y < -0.499999) {
		out_Color = vec4(pos.z+0.499999, pos.z+0.499999, pos.z+0.499999, 1);
	}
	else if (pos.z > 0.499999 || pos.z < -0.499999) {
		out_Color = vec4(pos.x+0.499999, pos.x+0.499999, pos.x+0.499999, 1);
	}
	else {
		out_Color = vec4(1, 1, 1, 1);
	}
}