#version 130


in vec4 in_Position;
in float in_Offset;

flat out float offset;



void main(void) {

    offset = in_Offset;
    gl_Position = in_Position;
    
}


