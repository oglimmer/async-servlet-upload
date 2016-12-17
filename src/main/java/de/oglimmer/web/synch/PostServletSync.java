package de.oglimmer.web.synch;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.oglimmer.web.ThreadCountListener;

@WebServlet(urlPatterns = { "/PostServletSync" }, asyncSupported = false)
public class PostServletSync extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		long start = System.currentTimeMillis();
		int id = ThreadCountListener.incActive();
		long totalBytes = 0;
		try {
			String param = request.getParameter("foo");
			if (!("oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
					+ "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
					+ "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
					+ "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
					+ "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
					+ "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
					+ "oooooooooooooooooooooooooooooooooooooooooooooooooooooo").equals(param)) {
				System.out.println("failed to get param: " + param);
			}
			totalBytes = param != null ? param.length() : 0;
		} finally {
			response.getWriter().println("done");
			ThreadCountListener.decActive(id);
			ThreadCountListener.incAll(System.currentTimeMillis() - start, totalBytes);
		}
	}

}
