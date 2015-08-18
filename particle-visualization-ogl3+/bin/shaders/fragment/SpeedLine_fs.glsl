#version 130

uniform sampler2D textureUnitId;
uniform int renderMode;
uniform vec4 globalColor;
uniform vec4 bboxColor;
uniform vec4 fogColor;
uniform float fogDensity;
uniform float speedlineTransparency;
uniform float textureFact;

in vec2 textureCoord;
in vec3 normal;
// in vec4 color;
in float pass_offset;

out vec4 out_Color;


void offsetVisu(float offset){
	if (offset < 0.5) out_Color = vec4(1,1-offset,0,1);
	if (offset > 0.5) out_Color = vec4(0,offset,1,1);
	if (offset == 0.5) out_Color = vec4(0.5,0.7,0.5,1);
	if (offset <= 0.0) out_Color = vec4(1,0,0,1);
	if (offset >= 1.0) out_Color = vec4(0,0,1,1);
}



void main(void) {


	out_Color = globalColor;
	float borderGradFact = abs(textureCoord.y - 0.5);

	
	out_Color = mix(out_Color, fogColor, pass_offset);
	out_Color = mix(out_Color, fogColor, 0.94 - borderGradFact);
	out_Color = mix(out_Color, texture(textureUnitId, vec2(1 - pass_offset, textureCoord.y )), textureFact);


	out_Color.w = out_Color.w * borderGradFact * (1 - pass_offset) + (1 - speedlineTransparency);




	
	// out_Color = globalColor;
	// float borderGradFact = abs(textureCoord.y - 0.5);
	
	// BG-mix method
	// out_Color = mix(out_Color, fogColor, pass_offset);
	// out_Color = mix(out_Color, fogColor, 0.94 - borderGradFact);
	
	// transparency method
	// vec4 transColor = globalColor;
	// transColor.w = borderGradFact * (1 - pass_offset);
	
	// mix both
	// out_Color = mix(out_Color, transColor, speedlineTransparency);
	
	// textures
	// out_Color = mix(out_Color, texture(textureUnitId, vec2(1 - pass_offset, textureCoord.y )), 0.5);
	//out_Color = mix(out_Color, globalColor, ((sin(textureCoord.y * 70) + 1) / 10) );
	//out_Color = vec4(color.x*borderGradFact, color.y*borderGradFact, color.z*borderGradFact,1);

	
	// out_Color.w = out_Color.w * (1 - pass_offset);
	
	
	
	//offsetVisu(pass_offset);
	
	
	
	// pseudo AA
	float aaTextCoordRange = 0.04 + pass_offset * 0.1;
	float aaTextCoordRangeReci = 1 / aaTextCoordRange;
	if (textureCoord.y >= 1 - aaTextCoordRange) {
		out_Color.w = out_Color.w * clamp( aaTextCoordRangeReci * -textureCoord.y + aaTextCoordRangeReci, 0.0, 1.0);
		// out_Color = vec4(1,0,0,1);
	}
	if (textureCoord.y <= aaTextCoordRange) {
		out_Color.w = out_Color.w * clamp( aaTextCoordRangeReci * textureCoord.y, 0.0, 1.0);
		// out_Color = vec4(1,0,0,1);
	}


    // add Fog...
	const float LOG2FACT = 0.01442695;
	float z = gl_FragCoord.z / gl_FragCoord.w;
	float fogFactor = exp2( -fogDensity * z * z * LOG2FACT ) - 0.1;
	fogFactor = clamp(fogFactor, 0.0, 1.0);
	out_Color = mix(fogColor, out_Color, fogFactor);
	

}