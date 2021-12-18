package com.wes.mmo.common.config;

public class ConfigKey {

    public static final String CONFIG_FILE = "config.xml";

    // 软件环境类的固定变量名称
    public enum EnvKey {

        APP_NAME("app.name"),
        FIREFOX_BIN("webdriver.firefox.bin"),
        FIREFOX_DRIVER("webdriver.gecko.driver")
        ;

        private String key;

        EnvKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public enum AppKey {

         USERNAME("username")
        ,PASSWORD("password")
        ,LOGIN_URL("login.url")
        ,LOGIN_USERNAME_ELEMENT("login.username.element")
        ,LOGIN_PASSWORD_ELEMENT("login.password.element")
        ,LOGIN_SUBMIT_ELEMMENT("login.submit.element")
        ,SVG_URL("svg.url")
        ,WEB_SOCKET_ADDRESS("mmo.web_socket.address")
        ,WEB_SOCKET_PATH("mmo.web_socket.path")
        ;

        private String key;

        AppKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return this.key;
        }

    }
}
