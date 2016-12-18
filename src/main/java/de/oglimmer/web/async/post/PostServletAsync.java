package de.oglimmer.web.async.post;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.oglimmer.web.ThreadCountListener;

@WebServlet(urlPatterns = { "/PostServletAsync" }, asyncSupported = true)
public class PostServletAsync extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		int id = ThreadCountListener.incActive();
		AsyncContext context = request.startAsync();
		ServletInputStream inputStream = request.getInputStream();
		try {
			inputStream.setReadListener(new BufferedReadListenerImpl(inputStream, context, params -> {
				String param = params.get("foo");
				if (!("oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
						+ "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
						+ "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
						+ "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
						+ "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
						+ "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
						+ "oooooooooooooooooooooooooooooooooooooooooooooooooooooo").equals(param)) {
					System.out.println("failed to get param: " + param);
				}
				try {
					context.getResponse().getWriter().println("done");
				} catch (IOException e) {
					e.printStackTrace();
				}
				ThreadCountListener.decActive(id);
			}, VOID -> {
				ThreadCountListener.decActive(id);
			}));
		} catch (IllegalStateException e) {
			ThreadCountListener.decActive(id);
			throw e;
		}
	}
}
