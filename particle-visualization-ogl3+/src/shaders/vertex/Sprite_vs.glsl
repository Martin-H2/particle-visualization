#version 130

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform vec2 screenSize;
uniform float spriteSize;

in vec4 in_Position;



void main(void) {

	//vec2 screenSize = vec2(1280, 720);
	//float spriteSize = 1;

	vec4 eyePos = viewMatrix * modelMatrix * in_Position;
    vec4 projVoxel = projectionMatrix * vec4(spriteSize,spriteSize,eyePos.z,eyePos.w);
    vec2 projSize = screenSize * projVoxel.xy / projVoxel.w;
    
    gl_PointSize = 0.25 * (projSize.x+projSize.y);
    gl_Position = projectionMatrix * eyePos;
    
}


