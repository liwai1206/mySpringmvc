package com.yc.spring.mvc.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * @author 外哥
 * @Description:
 * @email : liwai2012220663@163.com
 * @date 2021/1/23 21:45
 */
public class HandleResponse {

    public static void handlerStaticResource (HttpServletResponse response , String url ) throws IOException {
        File file = new File(url ) ;
        if ( !file.exists() || !file.isFile() ) {
            // 如果file不存在或者不是一个文件
            send404( response , url );
            return ;
        }
        
        try (FileInputStream is = new FileInputStream(file)) {
            byte[] bt = new byte[ is.available() ] ; 
            is.read( bt ) ;
            sendData( response , bt );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendJson(HttpServletResponse response, Object object ) throws IOException {
        PrintWriter writer = response.getWriter();
        Gson gson = new GsonBuilder().serializeNulls().create() ;
        writer.print( gson.toJson(object) );
        writer.flush();
    }

    public static void sendData(HttpServletResponse response, byte[] bt) throws IOException {
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write( bt );
        outputStream.flush();

    }

    public static void send404(HttpServletResponse response, String url) throws IOException {
        PrintWriter writer = response.getWriter();
        writer.println("<h1>HTTP/1.1 404 File Not Found! - " + url + " </h1>");
        writer.flush();
    }

}
