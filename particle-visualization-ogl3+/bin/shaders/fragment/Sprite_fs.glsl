#version 130

uniform sampler2D textureUnitId;
uniform int renderMode;
uniform vec4 globalColor;
uniform vec4 bboxColor;

in vec2 pass_TextureCoord;

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
	

	switch(renderMode) {
    case 0:
        out_Color = texture(textureUnitId, pass_TextureCoord);
        break;
    case 3:
        out_Color = globalColor;
        break;
    case 5:
        out_Color = bboxColor;
        break;
    case 6:
        out_Color = texture(textureUnitId, gl_PointCoord);
		
		//float average = (out_Color.r + out_Color.g + out_Color.b) / 3.0;
		vec2 pos = mod(gl_PointCoord.xy, vec2(1.0)) - vec2(.5);
	    float dist_squared = dot(pos, pos);
	  
		
		if (dist_squared > .24)
	        discard; 
	      
	 	//if (out_Color.b < 0.2 && dist_squared > .16)
	    //    out_Color.a = average * 5;
	    
		//vec4 globalColor = vec4(0.6, 0.6, 1.0, 1.0);
		//out_Color = colorize(out_Color, globalColor);
		
		//if (dist_squared > .23 && dist_squared < .24)
		//      out_Color = vec4(.90, .10, .10, 1.0);
        break;
   }


   // add Fog...
   
	const float LOG2 = 1.442695;
	const float fogDensity = 0.01;
	vec4 fogColor = vec4(0.1, 0.1, 0.1, 1);
	
	
	float z = gl_FragCoord.z / gl_FragCoord.w;
	float fogFactor = exp2( -fogDensity * z * z * LOG2 );
	fogFactor = clamp(fogFactor, 0.0, 1.0);
	out_Color = mix(fogColor, out_Color, fogFactor);
	

}