package de.oglimmer.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequestProcessor {

	private static final int MAX_BYTES = 500;
	
	private int delay;
	private long id;

	private long start;
	private boolean failed = false;

	private long lastChunkWrittenAt;
	private int bytesWritten;

	private HttpURLConnection con;
	private OutputStream os;

	public HttpRequestProcessor(int delay, long id) {
		this.id = id;
		this.delay = delay;
	}

	public void process() {
		if (os == null) {
			init();
		}
		sentData();
	}

	public void close() {

		try {
			if (os != null) {
				os.close();
			}
		} catch (IOException e) {
			if (!"insufficient data written".equals(e.getMessage())) {
				e.printStackTrace();
			}
		}

		if (con != null) {
			con.disconnect();
		}
	}

	private void init() {
		start = System.currentTimeMillis();
		try {

			URL url = new URL("http", Startup.host, Integer.parseInt(Startup.port), Startup.uri, null);
			con = (HttpURLConnection) url.openConnection();

			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestMethod("POST");
			con.setConnectTimeout(100);
			con.setReadTimeout(6000000);
			con.setDefaultUseCaches(false);
			con.setInstanceFollowRedirects(false);
			con.setUseCaches(false);
			con.setFixedLengthStreamingMode(MAX_BYTES+4);

			os = con.getOutputStream();
			os.write("foo=".getBytes());
			lastChunkWrittenAt = System.currentTimeMillis();
		} catch (IOException e) {
			failed = true;
			e.printStackTrace();
		}
	}

	private void sentData() {

		if (delay > 0 && System.currentTimeMillis() - lastChunkWrittenAt < delay) {
			return;
		}

		try {
			if (bytesWritten < MAX_BYTES) {
				bytesWritten++;
				os.write('o');
				lastChunkWrittenAt = System.currentTimeMillis();
			} else {
				allDataSent();
			}
		} catch (IOException e) {
			failed = true;
			e.printStackTrace();
		}

	}

	private void allDataSent() {
		try {
			try (InputStream is = con.getInputStream()) {
				String response = readResponse(is);
				if (!response.endsWith("done")) {
					failed = true;
				}
			}
		} catch (IOException e) {
			failed = true;
			e.printStackTrace();
		} finally {
			close();
		}

		Statistics.INSTANCE.totalNumberSinceLastUpdate++;
		Statistics.INSTANCE.timeSpendSinceLastUpdate += (System.currentTimeMillis() - start);
		if (failed) {
			Statistics.INSTANCE.totalFailedSinceLastUpdate++;
		}

		reset();
	}

	private void reset() {
		start = System.currentTimeMillis();
		failed = false;
		con = null;
		os = null;
		bytesWritten = 0;
	}

	private String readResponse(InputStream is) throws IOException {
		StringBuffer response = new StringBuffer();
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		return response.toString();
	}

}