package de.oglimmer.client;

import java.io.File;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class Startup {

	// DEFAULTS
	private static int delay = 150;
	private static int numberNormalConnections = 5;
	private static int numberBadConnections = 5;

	static String host = "localhost";
	static String port = "8080";
	static String uri;
	static File largeFile;

	private volatile static boolean running = true;
	private volatile static ConnectionList ce = new ConnectionList();

	public static void main(String[] args) {
		buildConfig(args);
		initConnections();
		createShutdownHook();
		createStatsThread();
		mainLoop();
		shutdown();
	}

	private static void shutdown() {
		for (Iterator<HttpRequestProcessor> it = ce.getConnections().iterator(); it.hasNext();) {
			HttpRequestProcessor hrp = it.next();
			hrp.close();
			it.remove();
		}
	}

	private static void mainLoop() {
		while (running) {
			ce.getConnections().forEach(HttpRequestProcessor::process);
		}
	}

	private static void initConnections() {
		for (int i = 0; i < numberNormalConnections; i++) {
			ce.addConnection(0);
		}
		for (int i = 0; i < numberBadConnections; i++) {
			ce.addConnection(delay);
		}
	}

	private static void createStatsThread() {
		new Thread(new StatisticsThread()).start();
	}

	private static void createShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownThread()));
	}

	private static void buildConfig(String[] args) {
		if (args.length < 2) {
			System.out.println(
					"Start with parameter <filename> <UploadServletAsync|UploadServletSync|FULL_URL> [#-normal-connections] [#-bad-connections] [delay for bad connections]");
			System.exit(1);
		}
		largeFile = new File(args[0]);
		if (!largeFile.exists()) {
			System.out.println("File " + largeFile + " doesn't exist!");
			System.exit(2);
		}
		if (args[1].startsWith("http")) {
			args[1] = args[1].substring(7);// cut http://
			host = args[1].substring(0, args[1].indexOf(":"));
			port = args[1].substring(args[1].indexOf(":") + 1, args[1].indexOf("/"));
			uri = args[1].substring(args[1].indexOf("/"));
		} else {
			uri = "/" + args[1];
		}

		if (args.length > 2) {
			numberNormalConnections = Integer.parseInt(args[2]);
		}
		if (args.length > 3) {
			numberBadConnections = Integer.parseInt(args[3]);
		}
		if (args.length > 4) {
			delay = Integer.parseInt(args[4]);
		}
		System.out.println("Using http://" + host + ":" + port + uri + " with normal " + numberNormalConnections
				+ " thread and " + numberBadConnections + " bad thread with delay of " + delay + ".");
	}

	private Startup() {
	}

	static class ShutdownThread implements Runnable {
		public void run() {
			Thread.currentThread().setName("ShutdownHook-Thread");
			System.out.println("shutting down..");

			running = false;

			while (!ce.getConnections().isEmpty()) {
				try {
					TimeUnit.MILLISECONDS.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			System.out.println("shutdown completed.");
		}
	}
}