package de.oglimmer.web.async.upload;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.oglimmer.web.ThreadCountListener;

@WebServlet(urlPatterns = { "/UploadServletAsync" }, asyncSupported = true)
public class UploadServletAsync extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		long start = System.currentTimeMillis();
		int id = ThreadCountListener.incActive();
		AsyncContext context = request.startAsync();
		ServletInputStream inputStream = request.getInputStream();
		inputStream.setReadListener(new ReadListenerImpl(inputStream, response, context, id));
		ThreadCountListener.incTime(System.currentTimeMillis() - start);
	}

}
