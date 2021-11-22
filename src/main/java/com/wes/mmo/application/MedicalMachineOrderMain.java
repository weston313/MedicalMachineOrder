package com.wes.mmo.application;

import com.wes.mmo.application.window.LoginWindow;
import com.wes.mmo.application.window.MainWindow;
import com.wes.mmo.common.config.AppConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;

public class MedicalMachineOrderMain {

    public static void main(String[] args) {
        launchLoginWindow(args);
    }

    public static void launchLoginWindow(String[] args){
        LoginWindow.main(args);
    }

}
