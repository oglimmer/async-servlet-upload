package de.oglimmer.web.synch;

import java.io.EOFException;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.oglimmer.web.ThreadCountListener;

@WebServlet(urlPatterns = { "/UploadServletSync" }, asyncSupported = false)
public class UploadServletSync extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		long start = System.currentTimeMillis();
		int id = ThreadCountListener.incActive();
		long totalBytes = 0;
		try {
			ServletInputStream inputStream = request.getInputStream();
			int len;
			byte byteBuff[] = new byte[1024];
			while ((len = inputStream.read(byteBuff)) != -1) {
				// throw it away
				totalBytes += len;
			}
		} catch (EOFException e) {
			// during shutdown this can happen. we don't care
		} finally {
			response.getWriter().println("done");
			ThreadCountListener.decActive(id);
			ThreadCountListener.incAll(System.currentTimeMillis() - start, totalBytes);
		}
	}

}
