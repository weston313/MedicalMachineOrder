package com.wes.mmo.common.config;

import org.junit.Test;

import static org.junit.Assert.*;

public class AppConfigurationTest {

    @Test
    public void testAppConfiguration(){
        AppConfiguration configuration = AppConfiguration.getConfiguration();

        configuration.getKey(ConfigKey.AppKey.USERNAME.getKey());
    }

}