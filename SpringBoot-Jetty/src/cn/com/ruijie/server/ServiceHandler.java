package cn.com.ruijie.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author tang
 */
public class ServiceHandler extends HttpServlet {

    public final static String SUCCESS = "{\"ret\":0,\"errcode\":0,\"msg\":\"操作成功\"}";
    public final static String FAILED = "{\"ret\":-1,\"errcode\":-1,\"msg\":\"操作失败\"}";

    public String getURI(HttpServletRequest request) {
        try {
            request.setCharacterEncoding("UTF-8");
            String uri = request.getRequestURI();
            if (uri.length() > 0) {
                return uri;
            } else {
                return null;
            }
        } catch (UnsupportedEncodingException e) {
            System.out.println("Exception:" + e.getMessage());
            return null;
        }
    }

    public Map<String, String> getParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<String, String>();
        String query = request.getQueryString();

        if (query == null || "".equals(query)) {
            return params;
        }

        try {
            String pairs[] = query.split("&");
            for (String pair : pairs) {
                String para[] = pair.split("=");
                if (para.length == 2) {
                    params.put(para[0], para[1]);
                }
            }
        } catch (Exception e) {
            System.out.println("Exception:" + e.getMessage());
        }

        return params;
    }

    public String getPostData(HttpServletRequest request) {
        try {
            request.setCharacterEncoding("UTF-8");

            StringBuilder sb = new StringBuilder();
            char[] buf = new char[1024];
            BufferedReader in = request.getReader();
            for (int count = in.read(buf); count >= 0; count = in.read(buf)) {
                sb.append(buf, 0, count);
            }

            return sb.toString();
        } catch (IOException e) {
            System.out.println("Exception:" + e.getMessage());
            return FAILED;
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String uri = getURI(request);
        String postData = getPostData(request);

        JSONObject jsonRet = new JSONObject();
        String result = "";

        System.out.println("request[post] url:"+uri);
        
        try {
            if (uri.equals("/api/test")) {
                Thread.sleep(10*60*1000);
                jsonRet.put("ret", 0);
                jsonRet.put("data", postData);
                jsonRet.put("errcode", 0);
                jsonRet.put("errmsg", "操作成功");
                result = jsonRet.toString();
            } else {
                jsonRet.put("ret", -1);
                jsonRet.put("errcode", 3);
                jsonRet.put("errmsg", "错误的URL");
                result = jsonRet.toString();
            }

            response.setContentType("text/html; charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        JSONObject jsonRet = new JSONObject();

        String uri = getURI(request);
        System.out.println("request[post] url:" + uri);

        try {
            jsonRet.put("ret", -1);
            jsonRet.put("errcode", 3);
            jsonRet.put("errmsg", "错误的URL");
            String result = jsonRet.toString();
            
            response.setContentType("text/html; charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(result);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
    
    
    
}
