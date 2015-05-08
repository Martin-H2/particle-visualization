package particleVisualization.util;


public class Stopwatch {

	private long start;

	public Stopwatch() {
		restart();
	}

	public void restart() {
		start = System.currentTimeMillis();
	}

	public double getElapsedSeconds() {
		return (System.currentTimeMillis() - start) / 1000.0;
	}

}
