package particleVisualization.enums;


public enum UniformName {

	// ordering doesnt matter, enums only for lookup map
	// positions will be stored per shader

	//Mat4f
	modelMatrix,
	viewMatrix,
	projectionMatrix,

	//vec4f
	globalColor,
	bboxColor,

	//vec2i
	screenSize,

	//float
	spriteSize,

	//int
	renderMode,
	textureUnitId,


}
