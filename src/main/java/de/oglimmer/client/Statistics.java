package de.oglimmer.client;

import java.util.concurrent.TimeUnit;

public enum Statistics {
	INSTANCE;

	public volatile long totalFailedSinceLastUpdate;
	public volatile long totalNumberSinceLastUpdate;
	public volatile long timeSpendSinceLastUpdate;

	public void printStats() {
		long avgNormal = calcAvgProcessingTime();
		sysOutStats(avgNormal);
		resetStats();
	}

	private void sysOutStats(long avgNormal) {
		System.out.println("Avg process time : " + avgNormal + "millies with total of completed uploads : "
				+ totalNumberSinceLastUpdate + " (failed:" + totalFailedSinceLastUpdate + ")");
	}

	private void resetStats() {
		timeSpendSinceLastUpdate = 0;
		totalNumberSinceLastUpdate = 0;
		totalFailedSinceLastUpdate = 0;
	}

	private long calcAvgProcessingTime() {
		long avgNormal = -1;
		if (totalNumberSinceLastUpdate > 0) {
			avgNormal = timeSpendSinceLastUpdate / totalNumberSinceLastUpdate;
		}
		return avgNormal;
	}

}

class StatisticsThread implements Runnable {

	@Override
	public void run() {
		Thread.currentThread().setName("Stats-Thread");
		while (true) {

			Statistics.INSTANCE.printStats();

			try {
				TimeUnit.SECONDS.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}