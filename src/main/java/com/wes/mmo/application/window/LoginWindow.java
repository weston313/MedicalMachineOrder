package com.wes.mmo.application.window;

import com.wes.mmo.application.MedicalMachineOrderMain;
import com.wes.mmo.common.config.AppConfiguration;
import com.wes.mmo.common.config.ConfigKey;
import com.wes.mmo.common.config.Value;
import com.wes.mmo.common.cookie.CookieManagerCache;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.print.attribute.standard.Media;
import java.io.IOException;
import java.util.List;

public class LoginWindow extends Application {

    public static final Log LOG = LogFactory.getLog(LoginWindow.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("医学器材预约系统");
        // 初始化登录界面
        stage.setWidth(600);
        stage.setHeight(500);
        BorderPane loginPane = FXMLLoader.load(getClass().getResource("/fxml/login/login.fxml"));
        Scene loginScene = new Scene(loginPane);
        stage.setScene(loginScene);
        stage.show();

        // initlize button event
        VBox vBox = (VBox) loginPane.getChildren().get(0);
        List<Node> borderPanes = vBox.getChildren();


        TextField userNameText = (TextField)((BorderPane)borderPanes.get(1)).getChildren().get(1);
        TextField passwordText = (TextField)((BorderPane)borderPanes.get(2)).getChildren().get(1);
        Button loginButton = (Button)((BorderPane)borderPanes.get(3)).getChildren().get(0);

        loginButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                String userName = userNameText.getText();
                String password = passwordText.getText();

                // check username and password
                if(userName == null || userName.length() == 0 || password == null || password.length() == 0) {
                    try {
                        Pane pane = FXMLLoader.load(getClass().getResource("/fxml/dialog/index.fxml"));
                        Label label = (Label) pane.getChildren().get(0);
                        label.setText("请输入账号密码");

                        Scene dialogScene = new Scene(pane);

                        Stage dialogStage = new Stage();
                        dialogStage.setScene(dialogScene);
                        dialogStage.show();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    LOG.info("Login on username " + userName );

                    // 进行登录
                    AppConfiguration configuration = AppConfiguration.getConfiguration();
                    configuration.addKey(ConfigKey.AppKey.USERNAME.getKey(), new Value(userName, "UNKNOWN"));
                    configuration.addKey(ConfigKey.AppKey.PASSWORD.getKey(), new Value(password, "UNKNOWN"));

                    // 进行初始化
                    CookieManagerCache.GetCookieManagerCache();

                    // 退出登录界面
                    stage.close();

                    // 启动主界面

                    try {
                        MainWindow mainWindow = MainWindow.GetInstance();
                        mainWindow.initlize();
                        mainWindow.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }


}
