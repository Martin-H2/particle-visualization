package particleVisualization.util;


public class Stopwatch {

	private final long start;

	public Stopwatch() {
		start = System.currentTimeMillis();
	}


	public double getElapsedSeconds() {
		return (System.currentTimeMillis() - start) / 1000.0;
	}

}
