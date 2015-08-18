#version 130

uniform sampler2D textureUnitId;
uniform int renderMode;
uniform vec4 globalColor;
uniform vec4 bboxColor;
uniform vec4 fogColor;
uniform float fogDensity;

in vec2 pass_TextureCoord;
in vec4 pass_Color;
flat in float pass_PointSize;

out vec4 out_Color;



// vec4 toGrayscale(in vec4 color)
// {
  // float average = (color.r + color.g + color.b) / 3.0;
  // return vec4(average, average, average, 1.0);
// }
// vec4 colorize(in vec4 grayscale, in vec4 color)
// {
  // return (grayscale * color);
// }



void main(void) {

	out_Color = globalColor;
	//float average = (out_Color.r + out_Color.g + out_Color.b) / 3.0;
	 //if (out_Color.b < 0.2 && dist_squared > .16)
    //    out_Color.a = average * 5;
    
	//vec4 globalColor = vec4(0.6, 0.6, 1.0, 1.0);
	//out_Color = colorize(out_Color, globalColor);
	
	//if (dist_squared > .23 && dist_squared < .24)
	//      out_Color = vec4(.90, .10, .10, 1.0);

	switch(renderMode) {
    case 0:
        out_Color = texture(textureUnitId, gl_PointCoord);
        break;
    case 1:
        out_Color = pass_Color;
        break;
    case 2:
        out_Color = texture(textureUnitId, gl_PointCoord);
        out_Color = mix(pass_Color, out_Color, 0.5);
        break;
    case 3:
        out_Color = globalColor;
        break;
    case 5:
        out_Color = bboxColor;
        break;
    case 6:
        out_Color = vec4(1,0,0,1);
        break;
   }

    //cut...
    if (renderMode != 5) {
		vec2 pos = mod(gl_PointCoord.xy, vec2(1.0)) - vec2(.5);
	    float dist_squared = dot(pos, pos);
	    float fallofTreshold = .23 - pass_PointSize / 50;
		if (dist_squared > .24)
	        discard; 
		// pseudo AA
		else if (dist_squared > fallofTreshold ) {
			out_Color.w = out_Color.w * clamp( 1 - (dist_squared - fallofTreshold)/(.24 - fallofTreshold), 0.0, 1.0);
	        //out_Color = vec4(1,0,0,1);
		}
		
		// float aaTextCoordRange = 0.04 + pass_offset * 0.3;
		// float aaTextCoordRangeReci = 1 / aaTextCoordRange;
		// if (textureCoord.y >= 1 - aaTextCoordRange) {
			// out_Color.w = out_Color.w * clamp( aaTextCoordRangeReci * -textureCoord.y + aaTextCoordRangeReci, 0.0, 1.0);
			
		// }
    
    }


	
	
	

	
	
    // add Fog...
	const float LOG2FACT = 0.01442695;
	float z = gl_FragCoord.z / gl_FragCoord.w;
	float fogFactor = exp2( -fogDensity * z * z * LOG2FACT ) - 0.3;
	fogFactor = clamp(fogFactor, 0.0, 1.0);
	out_Color = mix(fogColor, out_Color, fogFactor);
	

}