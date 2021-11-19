package com.wes.mmo.application;

import com.wes.mmo.application.window.LoginWindow;
import com.wes.mmo.application.window.MainWindow;

public class MedicalMachineOrderMain {

    public static void main(String[] args) {
        launchLoginWindow(args);
    }

    public static void launchLoginWindow(String[] args){
        LoginWindow.main(args);
    }

}
