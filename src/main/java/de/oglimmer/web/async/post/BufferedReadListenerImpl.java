package de.oglimmer.web.async.post;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.servlet.AsyncContext;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

import de.oglimmer.web.ThreadCountListener;

public class BufferedReadListenerImpl implements ReadListener {

	private ServletInputStream inputStream;
	private AsyncContext asyncCtxt;
	private long start;

	private Consumer<Map<String, String>> success;
	private Consumer<Void> failed;
	private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

	public BufferedReadListenerImpl(ServletInputStream inputStream, AsyncContext asyncCtxt,
			Consumer<Map<String, String>> success, Consumer<Void> failed) {
		this.inputStream = inputStream;
		this.asyncCtxt = asyncCtxt;
		this.success = success;
		this.failed = failed;
		this.start = System.currentTimeMillis();
	}

	@Override
	public void onDataAvailable() throws IOException {
		try {
			int len;
			byte byteBuff[] = new byte[1024];
			while (inputStream.isReady() && (len = inputStream.read(byteBuff)) != -1) {
				buffer.write(byteBuff, 0, len);
			}
		} catch (EOFException e) {
			failed.accept(null);
		} catch (IOException e) {
			failed.accept(null);
			throw e;
		}
	}

	@Override
	public void onAllDataRead() throws IOException {
		ThreadCountListener.incAll(System.currentTimeMillis() - start, buffer.size());
		success.accept(transform());
		asyncCtxt.complete();
	}

	private Map<String, String> transform() {
		Map<String, String> postParams = new HashMap<>();
		for (String keyValue : buffer.toString().split("\\&")) {
			String[] keyValues = keyValue.split("=");
			if (keyValues.length > 0) {
				String key = keyValues[0];
				String val = keyValues.length > 1 ? keyValues[1] : null;
				postParams.put(key, val);
			}
		}
		return postParams;
	}

	@Override
	public void onError(final Throwable t) {
		t.printStackTrace();
		failed.accept(null);
		asyncCtxt.complete();
	}

}
