package de.oglimmer.client;

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
		if (args.length < 1) {
			System.out.println(
					"Start with parameter <UploadServletAsync|UploadServletSync|PostServletAsync|PostServletSync|FULL_URL> [#-normal-connections] [#-bad-connections] [delay for bad connections]");
			System.exit(1);
		}
		if (args[0].startsWith("http")) {
			args[0] = args[0].substring(7);// cut http://
			host = args[0].substring(0, args[0].indexOf(":"));
			port = args[0].substring(args[0].indexOf(":") + 1, args[0].indexOf("/"));
			uri = args[0].substring(args[0].indexOf("/"));
		} else {
			uri = "/" + args[0];
		}

		if (args.length > 1) {
			numberNormalConnections = Integer.parseInt(args[1]);
		}
		if (args.length > 2) {
			numberBadConnections = Integer.parseInt(args[2]);
		}
		if (args.length > 3) {
			delay = Integer.parseInt(args[3]);
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