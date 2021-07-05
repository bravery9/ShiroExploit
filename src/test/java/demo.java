import common.CommonUtils;
import sun.misc.BASE64Decoder;

import javax.servlet.jsp.PageContext;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.HashMap;

public class demo {
    public static void main(String[] args) throws Exception{
        String path = "/demo";
        System.out.println(CommonUtils.normizePath(path));
    }
}
