package de.oglimmer.web.stripes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import de.oglimmer.web.ThreadCountListener;
import de.oglimmer.web.async.post.BufferedReadListenerImpl;
import net.sourceforge.stripes.controller.DispatcherServlet;
import net.sourceforge.stripes.controller.StripesRequestWrapper;

@WebServlet(asyncSupported = true, urlPatterns = "*.action")
public class AsyncDispatchServlet extends DispatcherServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException {

		if (!req.isAsyncSupported()) {
			throw new RuntimeException("Async not supported");
		}
		int id = ThreadCountListener.incActive();
		AsyncContext context = req.startAsync();
		try {
			ServletInputStream inputStream = req.getInputStream();
			inputStream.setReadListener(new BufferedReadListenerImpl(inputStream, context,
					params -> success(context, params, id), VOID -> failed(id)));
		} catch (IOException e) {
			ThreadCountListener.decActive(id);
			throw new ServletException(e);
		}

	}

	private void failed(int id) {
		System.err.println("failed to process " + id);
		ThreadCountListener.decActive(id);
	}

	private void success(AsyncContext context, Map<String, String> params, int id) {
		try {
			super.service(new StripesRequestWrapper(new HttpServletRequestWrapper(context, params)),
					(HttpServletResponse) context.getResponse());
		} catch (ServletException e) {
			e.printStackTrace();
		} finally {
			ThreadCountListener.decActive(id);
		}
	}

	class HttpServletRequestWrapper implements HttpServletRequest {

		private AsyncContext context;

		private Map<String, String> params;

		public HttpServletRequestWrapper(AsyncContext context, Map<String, String> params) {
			this.context = context;
			this.params = params;
		}

		@Override
		public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
				throws IllegalStateException {
			throw new RuntimeException("illegal call to startAsync(,)");
		}

		@Override
		public AsyncContext startAsync() throws IllegalStateException {
			throw new RuntimeException("illegal call to startAsync");
		}

		@Override
		public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
			context.getRequest().setCharacterEncoding(env);
		}

		@Override
		public void setAttribute(String name, Object o) {
			context.getRequest().setAttribute(name, o);
		}

		@Override
		public void removeAttribute(String name) {
			context.getRequest().removeAttribute(name);
		}

		@Override
		public boolean isSecure() {
			return context.getRequest().isSecure();
		}

		@Override
		public boolean isAsyncSupported() {
			return context.getRequest().isAsyncSupported();
		}

		@Override
		public boolean isAsyncStarted() {
			return context.getRequest().isAsyncStarted();
		}

		@Override
		public ServletContext getServletContext() {
			return context.getRequest().getServletContext();
		}

		@Override
		public int getServerPort() {
			return context.getRequest().getServerPort();
		}

		@Override
		public String getServerName() {
			return context.getRequest().getServerName();
		}

		@Override
		public String getScheme() {
			return context.getRequest().getScheme();
		}

		@Override
		public RequestDispatcher getRequestDispatcher(String path) {
			return context.getRequest().getRequestDispatcher(path);
		}

		@Override
		public int getRemotePort() {
			return context.getRequest().getRemotePort();
		}

		@Override
		public String getRemoteHost() {
			return context.getRequest().getRemoteHost();
		}

		@Override
		public String getRemoteAddr() {
			return context.getRequest().getRemoteAddr();
		}

		@Override
		public String getRealPath(String path) {
			return context.getRequest().getRealPath(path);
		}

		@Override
		public BufferedReader getReader() throws IOException {
			throw new RuntimeException();
		}

		@Override
		public String getProtocol() {
			return context.getRequest().getProtocol();
		}

		@Override
		public String[] getParameterValues(String name) {
			return params.values().toArray(new String[params.size()]);
		}

		@Override
		public Enumeration<String> getParameterNames() {
			return new Enumeration<String>() {

				private Iterator<String> it = params.keySet().iterator();

				@Override
				public String nextElement() {
					return it.next();
				}

				@Override
				public boolean hasMoreElements() {
					return it.hasNext();
				}
			};
		}

		@Override
		public Map<String, String[]> getParameterMap() {
			Map<String, String[]> paramMap = new HashMap<>();
			for (Map.Entry<String, String> en : params.entrySet()) {
				paramMap.put(en.getKey(), new String[] { en.getValue() });
			}
			return paramMap;
		}

		@Override
		public String getParameter(String name) {
			return params.get(name);
		}

		@Override
		public Enumeration<Locale> getLocales() {
			return context.getRequest().getLocales();
		}

		@Override
		public Locale getLocale() {
			return context.getRequest().getLocale();
		}

		@Override
		public int getLocalPort() {
			return context.getRequest().getLocalPort();
		}

		@Override
		public String getLocalName() {
			return context.getRequest().getLocalName();
		}

		@Override
		public String getLocalAddr() {
			return context.getRequest().getLocalAddr();
		}

		@Override
		public ServletInputStream getInputStream() throws IOException {
			throw new RuntimeException();
		}

		@Override
		public DispatcherType getDispatcherType() {
			return context.getRequest().getDispatcherType();
		}

		@Override
		public String getContentType() {
			return context.getRequest().getContentType();
		}

		@Override
		public long getContentLengthLong() {
			return context.getRequest().getContentLengthLong();
		}

		@Override
		public int getContentLength() {
			return context.getRequest().getContentLength();
		}

		@Override
		public String getCharacterEncoding() {
			return context.getRequest().getCharacterEncoding();
		}

		@Override
		public Enumeration<String> getAttributeNames() {
			return context.getRequest().getAttributeNames();
		}

		@Override
		public Object getAttribute(String name) {
			return context.getRequest().getAttribute(name);
		}

		@Override
		public AsyncContext getAsyncContext() {
			return context.getRequest().getAsyncContext();
		}

		@Override
		public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
			return ((HttpServletRequest) context.getRequest()).upgrade(handlerClass);
		}

		@Override
		public void logout() throws ServletException {
			((HttpServletRequest) context.getRequest()).logout();
		}

		@Override
		public void login(String username, String password) throws ServletException {
			((HttpServletRequest) context.getRequest()).login(username, password);
		}

		@Override
		public boolean isUserInRole(String role) {
			return ((HttpServletRequest) context.getRequest()).isUserInRole(role);
		}

		@Override
		public boolean isRequestedSessionIdValid() {
			return ((HttpServletRequest) context.getRequest()).isRequestedSessionIdValid();
		}

		@Override
		public boolean isRequestedSessionIdFromUrl() {
			return ((HttpServletRequest) context.getRequest()).isRequestedSessionIdFromUrl();
		}

		@Override
		public boolean isRequestedSessionIdFromURL() {
			return ((HttpServletRequest) context.getRequest()).isRequestedSessionIdFromURL();
		}

		@Override
		public boolean isRequestedSessionIdFromCookie() {
			return ((HttpServletRequest) context.getRequest()).isRequestedSessionIdFromCookie();
		}

		@Override
		public Principal getUserPrincipal() {
			return ((HttpServletRequest) context.getRequest()).getUserPrincipal();
		}

		@Override
		public HttpSession getSession(boolean create) {
			return ((HttpServletRequest) context.getRequest()).getSession(create);
		}

		@Override
		public HttpSession getSession() {
			return ((HttpServletRequest) context.getRequest()).getSession();
		}

		@Override
		public String getServletPath() {
			return ((HttpServletRequest) context.getRequest()).getServletPath();
		}

		@Override
		public String getRequestedSessionId() {
			return ((HttpServletRequest) context.getRequest()).getRequestedSessionId();
		}

		@Override
		public StringBuffer getRequestURL() {
			return ((HttpServletRequest) context.getRequest()).getRequestURL();
		}

		@Override
		public String getRequestURI() {
			return ((HttpServletRequest) context.getRequest()).getRequestURI();
		}

		@Override
		public String getRemoteUser() {
			return ((HttpServletRequest) context.getRequest()).getRemoteUser();
		}

		@Override
		public String getQueryString() {
			return ((HttpServletRequest) context.getRequest()).getQueryString();
		}

		@Override
		public String getPathTranslated() {
			return ((HttpServletRequest) context.getRequest()).getPathTranslated();
		}

		@Override
		public String getPathInfo() {
			return ((HttpServletRequest) context.getRequest()).getPathInfo();
		}

		@Override
		public Collection<Part> getParts() throws IOException, ServletException {
			return ((HttpServletRequest) context.getRequest()).getParts();
		}

		@Override
		public Part getPart(String name) throws IOException, ServletException {
			return ((HttpServletRequest) context.getRequest()).getPart(name);
		}

		@Override
		public String getMethod() {
			return ((HttpServletRequest) context.getRequest()).getMethod();
		}

		@Override
		public int getIntHeader(String name) {
			return ((HttpServletRequest) context.getRequest()).getIntHeader(name);
		}

		@Override
		public Enumeration<String> getHeaders(String name) {
			return ((HttpServletRequest) context.getRequest()).getHeaders(name);
		}

		@Override
		public Enumeration<String> getHeaderNames() {
			return ((HttpServletRequest) context.getRequest()).getHeaderNames();
		}

		@Override
		public String getHeader(String name) {
			return ((HttpServletRequest) context.getRequest()).getHeader(name);
		}

		@Override
		public long getDateHeader(String name) {
			return ((HttpServletRequest) context.getRequest()).getDateHeader(name);
		}

		@Override
		public Cookie[] getCookies() {
			return ((HttpServletRequest) context.getRequest()).getCookies();
		}

		@Override
		public String getContextPath() {
			return ((HttpServletRequest) context.getRequest()).getContextPath();
		}

		@Override
		public String getAuthType() {
			return ((HttpServletRequest) context.getRequest()).getAuthType();
		}

		@Override
		public String changeSessionId() {
			return ((HttpServletRequest) context.getRequest()).changeSessionId();
		}

		@Override
		public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
			return ((HttpServletRequest) context.getRequest()).authenticate(response);
		}
	}

}
