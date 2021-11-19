package com.wes.mmo.common.config;

public class Value {
		public Value(String value, String type)
		{
			this.value=value;
			this.type=type;
		}
		
		private String value;
		private String type;
		
		public String getValue() {
			return value;
		}
		public void setVlaue(String vlaue) {
			this.value = vlaue;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
}