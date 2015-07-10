#version 130

uniform sampler2D textureUnitId;
uniform int renderMode;
uniform vec4 globalColor;
uniform vec4 bboxColor;
uniform vec4 fogColor;

in vec2 pass_TextureCoord;
in vec3 normal;
in vec4 color;

out vec4 out_Color;




void main(void) {

	out_Color = color;


   // add Fog...
   
	const float LOG2 = 1.442695;
	const float fogDensity = 0.01;
	// fogColor = vec4(0.1, 0.1, 0.1, 1);
	
	
	float z = gl_FragCoord.z / gl_FragCoord.w;
	float fogFactor = exp2( -fogDensity * z * z * LOG2 );
	fogFactor = clamp(fogFactor, 0.0, 1.0);
	out_Color = mix(fogColor, out_Color, fogFactor);
	

}