
package cn.com.ruijie.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.HttpURLConnection;
/**
 *
 * @author Administrator
 */
public class HttpClient {
    
    public static String http_get(String url){
        int connect_timeout=2000;
        int read_timeout=5000;
        
        return http_get(url,connect_timeout,read_timeout);
    }
    
    public static String http_get(String url,int connect_timeout){
        int read_timeout=5000;
        
        return http_get(url,connect_timeout,read_timeout);
    }
    
    public static String http_get(String url, int connect_timeout, int read_timeout) {
        OutputStream out = null;
        BufferedReader in = null;
        String result="";
        
        try {
            URL console = new URL(url);
            HttpURLConnection  conn = (HttpURLConnection ) console.openConnection();

            conn.setConnectTimeout(connect_timeout);
            conn.setReadTimeout(read_timeout);
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            conn.connect();
            
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            
            conn.disconnect();
            return result;
        } catch (Exception e) {
            System.out.println(String.format("http_get() Exception :" + e.getMessage()));
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    System.out.println(String.format("http_get() in close Exception") + e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    System.out.println(String.format("http_get() outclose Exception") + e);
                }
            }
        }
        return null;
    }
    
    public static String http_post(String url,String data){
        int connect_timeout=2000;
        int read_timeout=5000;
        
        return http_post(url,data,connect_timeout,read_timeout);
    }
    
    public static String http_post(String url,String data,int connect_timeout){
        int read_timeout=5000;
        
        return http_post(url,data,connect_timeout,read_timeout);
    }
    
    public static String http_post(String url,String data ,int connect_timeout, int read_timeout) {
        OutputStream out = null;
        BufferedReader in = null;
        String result="";
        
        try {
            URL console = new URL(url);
            HttpURLConnection  conn = (HttpURLConnection ) console.openConnection();

            conn.setConnectTimeout(connect_timeout);
            conn.setReadTimeout(read_timeout);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.getOutputStream().write(data.getBytes("utf-8"));
            conn.getOutputStream().flush();
            conn.getOutputStream().close();
            conn.connect();
            
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            
            conn.disconnect();
            return result;
        } catch (Exception e) {
            System.out.println(String.format("http_post() Exception :" + e.getMessage()));
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    System.out.println(String.format("http_post() in close Exception") + e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    System.out.println(String.format("http_post() outclose Exception") + e);
                }
            }
        }
        return null;
    }

     public static void main(String[] args) {
        //String ret=HttpClient.get("http://www.sina.com");
        String ret=HttpClient.http_post("http://127.0.0.1:6543/api/test","hello world");
        System.out.println(ret);
    }
}
