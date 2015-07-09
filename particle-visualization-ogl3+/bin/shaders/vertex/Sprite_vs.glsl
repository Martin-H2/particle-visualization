#version 130

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform int renderMode;
uniform vec2 screenSize;
uniform float spriteSize;

in vec2 in_TextureCoord;
in vec4 in_Position;
in vec4 in_Color;

out vec2 pass_TextureCoord;
out vec4 pass_Color;

void main(void) {

	//vec2 screenSize = vec2(1280, 720);
	//float spriteSize = 1;

	vec4 eyePos = viewMatrix * modelMatrix * in_Position;
	
	if (renderMode == 6) {
	    vec4 projVoxel = projectionMatrix * vec4(spriteSize,spriteSize,eyePos.z,eyePos.w);
	    vec2 projSize = screenSize * projVoxel.xy / projVoxel.w;
	    gl_PointSize = 0.25 * (projSize.x+projSize.y);
	} else if (renderMode == 0) {
		pass_TextureCoord = in_TextureCoord;
	}
    
    gl_Position = projectionMatrix * eyePos;
    
}


