package de.oglimmer.web;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ThreadCountListener implements ServletContextListener {

	private volatile static AtomicInteger idGenerator = new AtomicInteger();

	private volatile static Set<Integer> activeCounter = Collections.synchronizedSet(new HashSet<>());
	private volatile static long totalDoneCounter;
	private volatile static long deltaDoneCounter;
	private volatile static long byteCounter;
	private volatile static long timeSpent;

	public static void incTime(long time) {
		timeSpent += time;
	}

	public static int incActive() {
		int id = idGenerator.incrementAndGet();
		if (activeCounter.contains(id)) {
			System.out.println("[WARNING] ID " + id + " already active!");
		}
		activeCounter.add(id);
		return id;
	}

	public static void decActive(int id) {
		if (!activeCounter.remove(id)) {
			System.out.println("[WARNING] ID " + id + " was not active!");
		}
	}

	public static void incAll(long time, long bytes) {
		totalDoneCounter++;
		deltaDoneCounter++;
		byteCounter += bytes;
		timeSpent += time;
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {

					long avgTimeSpent = totalDoneCounter != 0 ? timeSpent / totalDoneCounter : -1;

					System.out.println("Since last row: updates=" + deltaDoneCounter + ", bytes=" + byteCounter
							+ ", currently active: uploads=" + activeCounter.size() + ", threads="
							+ Thread.activeCount() + ", total thread time/count: " + timeSpent + "milli / "
							+ totalDoneCounter + " (" + avgTimeSpent + "milli)");
					deltaDoneCounter = 0;
					byteCounter = 0;
					try {
						TimeUnit.MILLISECONDS.sleep(500);
					} catch (InterruptedException e) {
					}
				}
			}

		}).start();

	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub

	}

}
