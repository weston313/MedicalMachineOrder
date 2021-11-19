package com.wes.mmo.utils;

public class Utils {

	private static final String MMO_CONF_DIR ="MMO_CONF_DIR";
	
	public static String GetConfPath() {
		 return GetEnverimentProperty(MMO_CONF_DIR);
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
