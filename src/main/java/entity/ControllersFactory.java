package entity;

import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

public class ControllersFactory {
    public static Map<String,Object> controllers = new HashMap<>();
    /**
     * ShiroKey     爆破成功的               Key/null
     * Encrypt      加密模式                CBC/GCM
     * OutputType   是否输出                payload 1/0
     * URL          成功爆破出key的url
     */
    public static Map<String,String> paramsContext = new HashMap<>();
    public static Map<String,byte[]> loaderByte = new HashMap<>();
    public static Map<String,Class> successGadget = new HashMap<>();
    public static Map<String,String> memShellLoader = new HashMap<>();
    public static Map<String, Proxy> currentProxy = new HashMap<>();
    public static Map<String, String> currentHeader = new HashMap<>();

}
