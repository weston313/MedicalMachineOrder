package com.wes.mmo.application.window;

import com.wes.mmo.common.config.AppConfiguration;
import com.wes.mmo.common.config.ConfigKey;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class MainWindow  {

    private static volatile MainWindow INSTANCE = null;

    public static MainWindow GetInstance(){
        if (INSTANCE == null) {
            synchronized (MainWindow.class) {
                if(INSTANCE == null)
                    INSTANCE = new MainWindow();
            }
        }
        return INSTANCE;
    }

    private Stage mainStage = null;
    private AppConfiguration configuration = AppConfiguration.getConfiguration();

    private MainWindow() {

    }

    public void initlize() throws IOException {
        mainStage = new Stage();
        mainStage.setTitle(configuration.getKey(ConfigKey.EnvKey.APP_NAME.getKey()).getValue());
        mainStage.setWidth(900);
        mainStage.setHeight(600);
        VBox mainPane = FXMLLoader.load(getClass().getResource("/fxml/main/index.fxml"));
        Scene mainScene = new Scene(mainPane);
        mainStage.setScene(mainScene);
        mainStage.show();
    }

    public void show(){
        mainStage.show();
    }
}
