package particleVisualization.util;

public enum ColorDataType {
	NONE,
	UINT8_RGB,
	UINT8_RGBA,
	FLOAT_I,
	FLOAT_RGB,
	FLOAT_RGBA;

	static public ColorDataType[] enumCache = values();

}
