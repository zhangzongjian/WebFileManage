import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class FileServlet extends HttpServlet{
	
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("deprecation")
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
			resp.setCharacterEncoding("UTF-8");
			PrintWriter out = resp.getWriter();
			//访问ip权限
			String remoteAddr = req.getRemoteAddr();
			if(!remoteAddr.endsWith("127.0.0.1") && !remoteAddr.endsWith("106") && !remoteAddr.endsWith("102") && !remoteAddr.endsWith("101")) {
				out.write("<script>alert('无权访问！');</script>");
				out.close();
			}
			
			String path = "Temp/";
			String dPath = req.getParameter("directoryPath");
			String lnkType = req.getParameter("type");
			String servletPath = req.getServletPath().substring(1);
			if(dPath != null) {
				path = dPath;
				if("del".equals(req.getParameter("op"))) {
					path = path.substring(0, path.lastIndexOf("/"));
					if(0 == deleteFile(req.getRealPath(dPath))) {
						path = path.substring(0, path.lastIndexOf("/"));
					}
					resp.sendRedirect(servletPath+"?directoryPath="+path);
					out.close();
					return;
				}
			}
			File parent = new File(req.getRealPath(path));
			StringBuilder sb = new StringBuilder();
			if("lnk".equals(lnkType)) {
//				parent = new File(path);
			}
			//快捷方式特殊处理
			if(dPath != null && dPath.contains(".lnk")) {
				String lnkRealPath = new LnkUtil(parent).getRealFilename().replaceAll("\\\\", "/");
				parent = new File(lnkRealPath);
				path = lnkRealPath;
			}
			if (parent.exists()) {
				File[] files;
				files = parent.listFiles();
				sb.append("<a href='"+servletPath+"'>首页</a>&nbsp;<a href='"+servletPath+"?directoryPath="+path.substring(0, path.lastIndexOf("/") == -1 ? path.length() : path.lastIndexOf("/"))+"'>上一层</a><table>");
				for (File file : files) {
					sb.append("<tr>");
					String fileName = file.getName();
					String filePath = path+"/"+file.getName();
					String directoryPath = servletPath+"?directoryPath="+path+"/"+fileName;
					if(fileName.contains(".lnk")) {
						sb.append("<td><a style='color:green' href='"+directoryPath+"&type=lnk'>"+fileName+"</a></td>");
					}
					else if(file.isFile()) {
						sb.append("<td><a style='color:black' href='"+filePath+"&type="+lnkType+"'>"+fileName+"</a></td>");
					}
					else {
						sb.append("<td><a href='"+directoryPath+"&type="+lnkType+"'>"+fileName+"</a></td>");
					}
					sb.append("<td>&nbsp;</td>");
					sb.append("<td>"+getLastModifiedDate(file)+"</td>");
					sb.append("<td>&nbsp;&nbsp;</td>");
					sb.append("<td align='right'>"+getSize(file)+"</td>");
					sb.append("<td>&nbsp;</td>");
					sb.append("<td><a href='"+directoryPath+"&op=del'>删除</a></td>");
					sb.append("<tr><td></td></tr></tr>");
				}
				sb.append("</table>");
				out.write(sb.toString());
				out.flush();
				out.close();
			}
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.doGet(req, resp);
	}
	
	private String getLastModifiedDate(File file) {
		return new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date(file.lastModified()));
	}
	
	public String getSize(File file) {
		if(file.isDirectory()) {
			return getDirectorySize(file);
		}
		long size;
		if(file.length() == 0) {
			size = 0;
		} else {
			size = file.length() / 1024 == 0 ? 1 : file.length() / 1024;	
		}
		if(size >= 1048576) {
			return size / 1024 /1024 +" G";
		}
		else if(size >= 1024 && size < 1048576) {
			return size / 1024 +" M";
		}
		else return size+" K";
	}	
	
	public String getDirectorySize(File directory) {
		long sumSize = 0;
		Stack<File> stack = new Stack<File>();
		stack.push(directory);
		do {
			directory = stack.pop();
			File[] files;
			files = directory.listFiles();
			for(File file : files) {
				if(file.isFile()) {
					sumSize += file.length();
				}
				else {
					stack.push(file);
				}
			}
		} while (! stack.isEmpty());
		if(sumSize == 0) {
			return "0 K";
		}
		sumSize = sumSize / 1024 == 0 ? 1 : sumSize/1024;
		if(sumSize >= 1024 * 1024) {
			return sumSize / 1024 /1024 +" G";
		}
		else if(sumSize >= 1024 && sumSize < 1048576) {
			return sumSize / 1024 +" M";
		}
		else return sumSize+" K";
	}
	
	/**
	 * 删除文件或文件夹，返回父文件夹内剩余文件数量
	 * @param filePath
	 * @return
	 */
	public int deleteFile(String filePath) {
		File file = new File(filePath);
		if(file.isDirectory()) {
			clearDir(file);
		}
		else {
			file.delete();
		}
		return file.getParentFile().listFiles().length;  
	}
	
	public void clearDir(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                clearDir(f);
                f.delete();
            }
        }
        file.delete();
    }

}
