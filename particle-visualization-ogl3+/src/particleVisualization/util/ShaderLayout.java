package particleVisualization.util;


/**
 * Mapping from shader variable names to locationIDs. Use .ordinal() to get the locationID, use enum names for shader attribut-names
 */
public enum ShaderLayout {

	in_Position,
	in_Color,
	in_TextureCoord;

	public static final String	UNIFORM_TEXTURE_UNIT_NAME		= "textureUnitId";
	public static final String	UNIFORM_MODEL_MATRIX_NAME		= "modelMatrix";
	public static final String	UNIFORM_PROJECTION_MATRIX_NAME	= "projectionMatrix";
	public static final String	UNIFORM_VIEW_MATRIX_NAME		= "viewMatrix";



}
