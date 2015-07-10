#version 330

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform int renderMode;
uniform vec4 globalColor;
uniform vec2 screenSize;
uniform float spriteSize;

layout (lines_adjacency) in;
layout (triangle_strip, max_vertices = 4) out;


flat in float offset [];


out vec3 normal;
out vec4 color;


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
	return 1 - offset * offset;
}

void main(void) {
 	
 	
	// using camera space for calculations...
	vec4 pos0 = viewMatrix * modelMatrix * gl_in[0].gl_Position;
	vec4 pos1 = viewMatrix * modelMatrix * gl_in[1].gl_Position;
	vec4 pos2 = viewMatrix * modelMatrix * gl_in[2].gl_Position;
	vec4 lineVector1 = pos1 - pos0;
	vec4 lineVector2 = pos2 - pos1;
    
    vec4 camPos = vec4(0, 0, 0, 1);
	vec4 pos1ToCamVector = camPos - pos1;
	vec4 pos2ToCamVector = camPos - pos2;
	
	
	// color = vec4(0,0,1,0.5);
	// emitVertexWorldspace(pos0);
	// color = vec4(0,1,0,0.5);
	// emitVertexWorldspace(pos1);
    // EndPrimitive();
    
   
 	// color = vec4(0.2,0,0,1);
	// emitVertexWorldspace(pos0);
	// emitVertexWorldspace(pos0 + pos0ToCamVector);
    // EndPrimitive();
    
 	color = vec4(offset[1], offset[1] , offset[1] , 1);
	emitVertexCamspace(pos2 - normalCrossSpriteScaled(pos2ToCamVector, lineVector2, getWidth(offset[1])));
    
 	// color = vec4(0.6,0.3,0,1);
	emitVertexCamspace(pos2 + normalCrossSpriteScaled(pos2ToCamVector, lineVector2, getWidth(offset[1])));
    
    
 	color = vec4(offset[0], offset[0] , offset[0] , 1);
	emitVertexCamspace(pos1 - normalCrossSpriteScaled(pos1ToCamVector, lineVector1, getWidth(offset[0])));
    
 	// color = vec4(0.6,0,0.3,1);
	emitVertexCamspace(pos1 + normalCrossSpriteScaled(pos1ToCamVector, lineVector1, getWidth(offset[0])));
    
    
    
    EndPrimitive();
}



