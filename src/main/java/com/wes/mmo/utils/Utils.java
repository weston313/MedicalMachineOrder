package com.wes.mmo.utils;

import java.util.Locale;

public class Utils {

	private static final String MMO_CONF_DIR ="MMO_CONF_DIR";
	
	public static String GetConfPath() {
		if(System.getProperty("os.name").toUpperCase().startsWith("WINDOWS")){
			return GetEnverimentProperty(MMO_CONF_DIR);
		}
		else {
			return "/Users/wozipa/test/MMO/conf";
		}
	}
	
	public static String GetEnverimentProperty(String name)
	{
		String path=System.getProperty(name);
		if(path==null || path.isEmpty()) {
			return System.getenv(name);
		}
		return path;
	}

}
