import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//测试Servlet线程不安全，Servlet是单例
public class ThreadServlet extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	
	private static ThreadLocal<Integer> threadLocal = new ThreadLocal<Integer>();
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.test(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		this.doGet(req, resp);
	}
	
	public void test(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.getWriter().write("<html><body>");
			threadLocal.set(0);
			for(int i = 1; i<= 5; i++) {
				threadLocal.set(threadLocal.get()+1);
				resp.getWriter().write("<h1>"+threadLocal.get()+"</h1><br>");
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		resp.getWriter().write("</body></html>");
	}
}
