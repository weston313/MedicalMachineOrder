package com.wes.mmo.common.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;


public class AppConfiguration {
	
	private static final Log LOG = LogFactory.getLog(AppConfiguration.class);
	
	private volatile static AppConfiguration configuration=null;
	
	public static AppConfiguration getConfiguration() {
		if(configuration==null) {
			synchronized(AppConfiguration.class) {
				if(configuration==null) {
					configuration=new AppConfiguration();
				}
			}
		}
		return configuration;
	}
	
	private BaseConfiguration baseConfiguration=null;
	
	private AppConfiguration()
	{
		baseConfiguration=new BaseConfiguration(ConfigKey.CONFIG_FILE);
	}
	
	public Value getKey(String name) {
		if(name==null || name.isEmpty()) {
			LOG.info("the name is null");
			return null;
		}
		return baseConfiguration.getKey(name);
	}
	
	public void addKey(String name, Value value)
	{
		baseConfiguration.addKey(name, value);
	}
	
	public void close()
	{
		baseConfiguration.close();
	}



}
