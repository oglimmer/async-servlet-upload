package de.oglimmer.client.raw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

public class RawClient implements Runnable {

	private static AtomicInteger counter = new AtomicInteger();

	public static void main(String[] args) throws Exception {

		RawClient rc = new RawClient();

		for (int i = 0; i < 250; i++) {
			new Thread(rc).start();
		}

		waitForever(rc);
	}

	private static void waitForever(RawClient rc) throws InterruptedException {
		synchronized (rc) {
			rc.wait();
		}
	}

	@Override
	public void run() {
		try {
			endlessLoop();
		} finally {
			System.out.println("thread ended");
		}
	}

	private void endlessLoop() {
		while (true) {
			neverFailSocketReqResp();
		}
	}

	private void neverFailSocketReqResp() {
		try {
			processSocketReqResp();
		} catch (IOException e) {
			System.out.println("Failed (" + counter.incrementAndGet() + "):" + e.toString());
		}
	}

	private void processSocketReqResp() throws IOException, UnknownHostException {
		try (Socket socket = new Socket("localhost", 8080)) {
			processReqResp(socket);
		}
	}

	private void processReqResp(Socket socket) throws IOException {
		try (OutputStream os = socket.getOutputStream()) {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
				writeRequest(os);
				readResponse(br);
			}
		}
	}

	private void readResponse(BufferedReader br) throws IOException {
		String line;
		StringBuilder buff = new StringBuilder();
		while (br.ready() && (line = br.readLine()) != null) {
			buff.append(line + "\r\n");
		}
	}

	private void writeRequest(OutputStream os) throws IOException {
		try (InputStream fis = RawClient.class.getResourceAsStream("/rawPostRequest")) {
			int b;
			while ((b = fis.read()) != -1) {
				os.write(b);
				sleep();
			}
		}
	}

	private void sleep() {
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
