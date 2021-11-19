package com.wes.mmo.common.config;

public class ConfigKey {

    public static final String CONFIG_FILE = "config.xml";

    // 软件环境类的固定变量名称
    public enum EnvKey {

        HOME_DIR("mmo.home.dir"),
        CONF_DIR("mmo.conf.dir"),
        APP_NAME("app.name")
        ;

        private String key;
        private EnvKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    // 软件的应用变量名称
    public enum AppKey {
         USERNAME("username")
        ,PASSWORD("password")
        ,LOGIN_URL("login.url")
        ,LOGIN_USERNAME_ELEMENT("login.username.element")
        ,LOGIN_PASSWORD_ELEMENT("login.password.element")
        ,LOGIN_SUBMIT_ELEMMENT("login.submit.element")
        ,INDEX_URL("index.url")		// 主页链接
        ;

        private String key;
        private AppKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return this.key;
        }

    }
}
