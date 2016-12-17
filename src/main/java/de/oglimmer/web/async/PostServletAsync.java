package de.oglimmer.web.async;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
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
		long start = System.currentTimeMillis();
		AsyncContext context = request.startAsync();
		context.start(new Runnable() {
			@Override
			public void run() {
				int id = ThreadCountListener.incActive();
				long start = System.currentTimeMillis();
				String param = context.getRequest().getParameter("foo");
				if (!("oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
						+ "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
						+ "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
						+ "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
						+ "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
						+ "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
						+ "oooooooooooooooooooooooooooooooooooooooooooooooooooooo").equals(param)) {
					System.out.println("failed to get param: " + param);
				}
				int totalBytes = param != null ? param.length() : 0;
				try {
					context.getResponse().getWriter().println("done");
				} catch (IOException e) {
					e.printStackTrace();
				}
				context.complete();
				ThreadCountListener.incAll(System.currentTimeMillis() - start, totalBytes);
				ThreadCountListener.decActive(id);
			}
		});
		ThreadCountListener.incTime(System.currentTimeMillis() - start);
	}

}
