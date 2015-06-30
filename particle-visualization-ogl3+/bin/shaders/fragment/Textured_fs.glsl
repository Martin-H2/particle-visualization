#version 130

uniform sampler2D textureUnitId;
uniform int renderMode;
uniform vec4 globalColor;
uniform vec4 bboxColor;

in vec2 pass_TextureCoord;

out vec4 out_Color;


void main(void) {
	
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
   }

   
	
}