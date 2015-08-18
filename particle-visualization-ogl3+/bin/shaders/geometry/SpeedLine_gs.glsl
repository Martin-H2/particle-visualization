#version 330

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform int renderMode;
uniform vec4 globalColor;
uniform vec2 screenSize;
uniform float spriteSize;

layout (lines_adjacency) in;
layout (triangle_strip, max_vertices = 8) out;


flat in float offset [];


out vec3 normal;
// out vec4 color;
out vec2 textureCoord;
out float pass_offset;


//mat4 mvpMatrix = projectionMatrix * viewMatrix * modelMatrix;
// MS
//   modelMatrix
// WS
//   viewMatrix
// CS
//   projectionMatrix
// SS


void emitVertexCamspace(vec4 position){
	position = projectionMatrix * position;
	gl_Position = position;
	EmitVertex();
}
void emitVertexWorldspace(vec4 position){
	position = viewMatrix * position;
	emitVertexCamspace(position);
}
void emitVertexModelspace(vec4 position){
	position = modelMatrix * position;
	emitVertexWorldspace(position);
}
vec4 normalCross(vec4 v1, vec4 v2){
	return vec4(normalize(cross(v1.xyz, v2.xyz)), 1);
}
vec4 normalCrossSpriteScaled(vec4 v1, vec4 v2, float scale){
	return scale * spriteSize * normalCross(v1, v2);
}
float getWidth(float offset) {
	if (offset == 0) return 0.52;
	else return 0.47 * (1 - offset);
}


void main(void) {
 	
 	
	// using camera space for calculations...
	vec4 pos0 = viewMatrix * modelMatrix * gl_in[0].gl_Position;
	vec4 pos1 = viewMatrix * modelMatrix * gl_in[1].gl_Position;
	vec4 pos2 = viewMatrix * modelMatrix * gl_in[2].gl_Position;
	vec4 pos3 = viewMatrix * modelMatrix * gl_in[3].gl_Position;
	vec4 lineVector1 = pos1 - pos0;
	vec4 lineVector2 = pos2 - pos1;
    
    vec4 camPos = vec4(0, 0, 0, 1);
	vec4 pos1ToCamVector = camPos - pos1;
	vec4 pos2ToCamVector = camPos - pos2;
	
	
    if (offset[3] == 1) {
		vec4 pos3ToCamVector = camPos - pos3;
      	textureCoord = vec2(1, 0);
	 	pass_offset = offset[3];
		emitVertexCamspace(pos3 - normalCrossSpriteScaled(pos3ToCamVector, lineVector2, getWidth(offset[3])));
	 	textureCoord = vec2(1, 1);
		emitVertexCamspace(pos3 + normalCrossSpriteScaled(pos3ToCamVector, lineVector2, getWidth(offset[3])));
    }
    
    
 	textureCoord = vec2(0, 0);
 	pass_offset = offset[2];
	emitVertexCamspace(pos2 - normalCrossSpriteScaled(pos2ToCamVector, lineVector2, getWidth(offset[2])));
 	textureCoord = vec2(0, 1);
	emitVertexCamspace(pos2 + normalCrossSpriteScaled(pos2ToCamVector, lineVector2, getWidth(offset[2])));
    
    
    
    if (offset[0] == 0) {
        pos1 -= pos1ToCamVector * 0.02;
	}
  	textureCoord = vec2(1, 0);
 	pass_offset = offset[1];
	emitVertexCamspace(pos1 - normalCrossSpriteScaled(pos1ToCamVector, lineVector1, getWidth(offset[1])));
 	textureCoord = vec2(1, 1);
	emitVertexCamspace(pos1 + normalCrossSpriteScaled(pos1ToCamVector, lineVector1, getWidth(offset[1])));
   
    
    if (offset[0] == 0) {
		vec4 pos0ToCamVector = camPos - pos0;
        pos0 -= pos0ToCamVector * 0.08;
      	textureCoord = vec2(0, 0);
	 	pass_offset = offset[0];
		emitVertexCamspace(pos0 - normalCrossSpriteScaled(pos0ToCamVector, lineVector1, getWidth(offset[0])));
	 	textureCoord = vec2(0, 1);
		emitVertexCamspace(pos0 + normalCrossSpriteScaled(pos0ToCamVector, lineVector1, getWidth(offset[0])));
    }
    
    
    EndPrimitive();

}



