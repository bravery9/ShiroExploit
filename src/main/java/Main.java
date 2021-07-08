import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 未完成
 * shiro 存在校验的情况，即遇到302的时候如何进行校验
 * 代理设置 （分为sock和http这块需要单独处理,进行设置保存）
 * rememberMe 特征可修改（已实现）
 * SSL 校验问题 （已实现）
 * GCM 加密算法 （已实现，暂未校验）
 * 利用链检测 （已实现）通过获取回显的方式
 * Tomcat Echo (已实现）
 * 未做不同操作系统版本的适配
 * 内存马注入 （冰蝎已实现，但是细节问题需要弄清楚）
 * Weblogic 相关（还没研究
 * 特征可修改  （看心情
 */

public class Main extends Application {
    // 启动代码
    @Override
    public void start(Stage primaryStage) throws Exception{
        // 文件位置不对
        Parent root = FXMLLoader.load(this.getClass().getResource("/sample.fxml"));
        primaryStage.setTitle("Shiro Exploit by: 天下大木头");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

}
