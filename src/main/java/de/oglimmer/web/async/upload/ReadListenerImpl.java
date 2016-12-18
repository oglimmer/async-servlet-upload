package de.oglimmer.web.async.upload;

import java.io.EOFException;
import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletResponse;

import de.oglimmer.web.ThreadCountListener;

public class ReadListenerImpl implements ReadListener {

	private ServletInputStream inputStream = null;
	private HttpServletResponse response = null;
	private AsyncContext asyncCtxt = null;
	private int id;

	ReadListenerImpl(ServletInputStream inputStream, HttpServletResponse response, AsyncContext asyncCtxt, int id) {
		this.inputStream = inputStream;
		this.response = response;
		this.asyncCtxt = asyncCtxt;
		this.id = id;
	}

	@Override
	public void onDataAvailable() throws IOException {
		long start = System.currentTimeMillis();
		long totalBytes = 0;
		try {
			int len;
			byte byteBuff[] = new byte[1024];
			while (inputStream.isReady() && (len = inputStream.read(byteBuff)) != -1) {
				// throw it away
				totalBytes += len;
			}
		} catch (EOFException e) {
			ThreadCountListener.decActive(id);
		} catch (IOException e) {
			ThreadCountListener.decActive(id);
			throw e;
		} finally {
			ThreadCountListener.incAll(System.currentTimeMillis() - start, totalBytes);
		}
	}

	@Override
	public void onAllDataRead() throws IOException {
		long start = System.currentTimeMillis();
		response.getWriter().println("done");
		asyncCtxt.complete();
		ThreadCountListener.decActive(id);
		ThreadCountListener.incAll(System.currentTimeMillis() - start, 0);
	}

	@Override
	public void onError(final Throwable t) {
		asyncCtxt.complete();
		t.printStackTrace();
	}

}
