package particleVisualization.util;

import static org.lwjgl.opengl.AMDDebugOutput.*;
import static org.lwjgl.opengl.ARBDebugOutput.*;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memByteBuffer;
import static org.lwjgl.system.MemoryUtil.memDecodeUTF8;
import java.io.PrintStream;
import org.lwjgl.opengl.*;
import org.lwjgl.system.libffi.Closure;


public class MessageSystem {

	public static Closure enableDebugging(GLContext context, PrintStream stream) {

		if (context.getCapabilities().OpenGL43) {
			glEnable(GL43.GL_DEBUG_OUTPUT);
			//GL43.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL43.GL_DEBUG_SEVERITY_NOTIFICATION, GL_DONT_CARE, false);
			System.out.println("[GL] Using OpenGL 4.3 for error logging.");
			GLDebugMessageCallback proc = createDEBUGPROC(stream);
			glDebugMessageCallback(proc, NULL);
			return proc;
		}

		if (context.getCapabilities().GL_KHR_debug) {
			glEnable(KHRDebug.GL_DEBUG_OUTPUT);
			System.out.println("[GL] Using KHR_debug for error logging.");
			GLDebugMessageCallback proc = createDEBUGPROC(stream);
			KHRDebug.glDebugMessageCallback(proc, NULL);
			return proc;
		}

		if (context.getCapabilities().GL_ARB_debug_output) {
			glEnable(37600);
			System.out.println("[GL] Using ARB_debug_output for error logging.");
			GLDebugMessageARBCallback proc = createDEBUGPROCARB(stream);
			glDebugMessageCallbackARB(proc, NULL);
			return proc;
		}

		if (context.getCapabilities().GL_AMD_debug_output) {
			glEnable(37600);
			System.out.println("[GL] Using AMD_debug_output for error logging.");
			GLDebugMessageAMDCallback proc = createDEBUGPROCAMD(stream);
			glDebugMessageCallbackAMD(proc, NULL);
			return proc;
		}

		System.out.println("[GL] No debug output implementation is available.");
		return null;

	}



	private static GLDebugMessageCallback createDEBUGPROC(final PrintStream stream) {
		return new GLDebugMessageCallback() {

			@Override
			public void invoke(int source, int type, int id, int severity, int length, long message, long userParam) {
				if (severity == GL_DEBUG_SEVERITY_NOTIFICATION) return;
				if (severity == GL_DEBUG_SEVERITY_HIGH) throw new RuntimeException("[LWJGL] OpenGL error");
				else {
					stream.println("[LWJGL] OpenGL debug message");
				}
				printDetail(stream, "ID", String.format("0x%X", id));
				printDetail(stream, "Source", getSource(source));
				printDetail(stream, "Type", getType(type));
				printDetail(stream, "Severity", getSeverity(severity));
				printDetail(stream, "Message", memDecodeUTF8(memByteBuffer(message, length)));
			}

			private String getSource(int source) {
				switch (source) {
				case GL_DEBUG_SOURCE_API:
					return "API";
				case GL_DEBUG_SOURCE_WINDOW_SYSTEM:
					return "WINDOW SYSTEM";
				case GL_DEBUG_SOURCE_SHADER_COMPILER:
					return "SHADER COMPILER";
				case GL_DEBUG_SOURCE_THIRD_PARTY:
					return "THIRD PARTY";
				case GL_DEBUG_SOURCE_APPLICATION:
					return "APPLICATION";
				case GL_DEBUG_SOURCE_OTHER:
					return "OTHER";
				default:
					return getUnknownToken(source);
				}
			}

			private String getType(int type) {
				switch (type) {
				case GL_DEBUG_TYPE_ERROR:
					return "ERROR";
				case GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR:
					return "DEPRECATED BEHAVIOR";
				case GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR:
					return "UNDEFINED BEHAVIOR";
				case GL_DEBUG_TYPE_PORTABILITY:
					return "PORTABILITY";
				case GL_DEBUG_TYPE_PERFORMANCE:
					return "PERFORMANCE";
				case GL_DEBUG_TYPE_OTHER:
					return "OTHER";
				case GL_DEBUG_TYPE_MARKER:
					return "MARKER";
				default:
					return getUnknownToken(type);
				}
			}

			private String getSeverity(int severity) {
				switch (severity) {
				case GL_DEBUG_SEVERITY_HIGH:
					return "HIGH";
				case GL_DEBUG_SEVERITY_MEDIUM:
					return "MEDIUM";
				case GL_DEBUG_SEVERITY_LOW:
					return "LOW";
				case GL_DEBUG_SEVERITY_NOTIFICATION:
					return "NOTIFICATION";
				default:
					return getUnknownToken(severity);
				}
			}
		};
	}

	private static GLDebugMessageARBCallback createDEBUGPROCARB(final PrintStream stream) {
		return new GLDebugMessageARBCallback() {

			@Override
			public void invoke(int source, int type, int id, int severity, int length, long message, long userParam) {
				if (severity == GL_DEBUG_SEVERITY_HIGH_ARB) throw new RuntimeException("[LWJGL] OpenGL error");
				else {
					stream.println("[LWJGL] ARB_debug_output message");
				}
				printDetail(stream, "ID", String.format("0x%X", id));
				printDetail(stream, "Source", getSource(source));
				printDetail(stream, "Type", getType(type));
				printDetail(stream, "Severity", getSeverity(severity));
				printDetail(stream, "Message", memDecodeUTF8(memByteBuffer(message, length)));
			}

			private String getSource(int source) {
				switch (source) {
				case GL_DEBUG_SOURCE_API_ARB:
					return "API";
				case GL_DEBUG_SOURCE_WINDOW_SYSTEM_ARB:
					return "WINDOW SYSTEM";
				case GL_DEBUG_SOURCE_SHADER_COMPILER_ARB:
					return "SHADER COMPILER";
				case GL_DEBUG_SOURCE_THIRD_PARTY_ARB:
					return "THIRD PARTY";
				case GL_DEBUG_SOURCE_APPLICATION_ARB:
					return "APPLICATION";
				case GL_DEBUG_SOURCE_OTHER_ARB:
					return "OTHER";
				default:
					return getUnknownToken(source);
				}
			}

			private String getType(int type) {
				switch (type) {
				case GL_DEBUG_TYPE_ERROR_ARB:
					return "ERROR";
				case GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR_ARB:
					return "DEPRECATED BEHAVIOR";
				case GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR_ARB:
					return "UNDEFINED BEHAVIOR";
				case GL_DEBUG_TYPE_PORTABILITY_ARB:
					return "PORTABILITY";
				case GL_DEBUG_TYPE_PERFORMANCE_ARB:
					return "PERFORMANCE";
				case GL_DEBUG_TYPE_OTHER_ARB:
					return "OTHER";
				default:
					return getUnknownToken(type);
				}
			}

			private String getSeverity(int severity) {
				switch (severity) {
				case GL_DEBUG_SEVERITY_HIGH_ARB:
					return "HIGH";
				case GL_DEBUG_SEVERITY_MEDIUM_ARB:
					return "MEDIUM";
				case GL_DEBUG_SEVERITY_LOW_ARB:
					return "LOW";
				default:
					return getUnknownToken(severity);
				}
			}
		};
	}

	private static GLDebugMessageAMDCallback createDEBUGPROCAMD(final PrintStream stream) {
		return new GLDebugMessageAMDCallback() {

			@Override
			public void invoke(int id, int category, int severity, int length, long message, long userParam) {
				if (severity == GL_DEBUG_SEVERITY_HIGH_AMD) throw new RuntimeException("[LWJGL] OpenGL error");
				else {
					stream.println("[LWJGL] AMD_debug_output message");
				}
				printDetail(stream, "ID", String.format("0x%X", id));
				printDetail(stream, "Category", getCategory(category));
				printDetail(stream, "Severity", getSeverity(severity));
				printDetail(stream, "Message", memDecodeUTF8(memByteBuffer(message, length)));
			}

			private String getCategory(int category) {
				switch (category) {
				case GL_DEBUG_CATEGORY_API_ERROR_AMD:
					return "API ERROR";
				case GL_DEBUG_CATEGORY_WINDOW_SYSTEM_AMD:
					return "WINDOW SYSTEM";
				case GL_DEBUG_CATEGORY_DEPRECATION_AMD:
					return "DEPRECATION";
				case GL_DEBUG_CATEGORY_UNDEFINED_BEHAVIOR_AMD:
					return "UNDEFINED BEHAVIOR";
				case GL_DEBUG_CATEGORY_PERFORMANCE_AMD:
					return "PERFORMANCE";
				case GL_DEBUG_CATEGORY_SHADER_COMPILER_AMD:
					return "SHADER COMPILER";
				case GL_DEBUG_CATEGORY_APPLICATION_AMD:
					return "APPLICATION";
				case GL_DEBUG_CATEGORY_OTHER_AMD:
					return "OTHER";
				default:
					return getUnknownToken(category);
				}
			}

			private String getSeverity(int severity) {
				switch (severity) {
				case GL_DEBUG_SEVERITY_HIGH_AMD:
					return "HIGH";
				case GL_DEBUG_SEVERITY_MEDIUM_AMD:
					return "MEDIUM";
				case GL_DEBUG_SEVERITY_LOW_AMD:
					return "LOW";
				default:
					return getUnknownToken(severity);
				}
			}
		};
	}


	private static void printDetail(PrintStream stream, String type, String message) {
		stream.printf("\t%s: %s\n", type, message);
	}

	private static String getUnknownToken(int token) {
		return String.format("Unknown (0x%X)", token);
	}


}
