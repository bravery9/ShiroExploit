package common;

import entity.ControllersFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

public class CommonUtils {

    public static String normizePath(String path) throws Exception{
        if (path.startsWith("/")){
            return path.substring(1, path.length());
        } else {
            return path;
        }
    }

    public static String normizeUrl(String url) throws Exception {
        if (url.endsWith("/")){
            return url;
        } else {
            return url + "/";
        }
    }

    public static String md5(String s) {
        String ret = null;
        try {
            java.security.MessageDigest m;
            m = java.security.MessageDigest.getInstance("MD5");
            m.update(s.getBytes(), 0, s.length());
            ret = new java.math.BigInteger(1, m.digest()).toString(16).toUpperCase();
        } catch (Exception e) {}
        return ret.substring(0,16).toLowerCase();
    }

    public static String readStringFromInputStream(InputStream inputStream) throws Exception{
        StringBuilder stringBuilder = new StringBuilder("");
        byte[] bytes = new byte[1024];
        int n = 0;
        while ((n=inputStream.read(bytes)) != -1){
            stringBuilder.append(new String(bytes,0,n));
        }
        return stringBuilder.toString();
    }


    public static byte[] getDetectText() throws Exception{
        InputStream inputStream = HttpUtils.class.getClassLoader().getResourceAsStream("detect.txt");
        // 读取字节流还是用 ByteArrayOutputStream
        // 将数据读到 byteArrayOutputStream 中
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int n;
        while ((n=inputStream.read()) != -1){
            byteArrayOutputStream.write(n);
        }
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return bytes;
    }

    public static Map<String,String> normalProxy(Map<String,String> paramContext) {
        String myProxy = paramContext.get("MyProxy");
        String host = myProxy.split(":")[0];
        String port = myProxy.split(":")[1];
        Map<String,String> proxy = new HashMap<>();
        proxy.put("host",host);
        proxy.put("port", String.valueOf(port));
        return proxy;
    }

    public static void addHttpProxy(String host,int port){
        Proxy proxy = new Proxy(Proxy.Type.HTTP,new InetSocketAddress(host,port));
        ControllersFactory.currentProxy.put("Proxy", proxy);
    }

    public static void addSocksProxy(String host,int port){
        Proxy proxy = new Proxy(Proxy.Type.SOCKS,new InetSocketAddress(host,port));
        ControllersFactory.currentProxy.put("Proxy", proxy);
    }

    public static void clearCurrentProxy(){
        ControllersFactory.currentProxy.put("Proxy", null);
    }

    public static void main(String[] args) {
        String myProxy = "127.0.0.1:7890";
        String host = myProxy.split(":")[0];
        int port = Integer.parseInt(myProxy.split(":")[1]);
        System.out.println(host);
        System.out.println(port);
    }
}
