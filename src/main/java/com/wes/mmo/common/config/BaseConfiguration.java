package com.wes.mmo.common.config;

import com.wes.mmo.utils.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BaseConfiguration {

	private static final Log LOG=LogFactory.getLog(BaseConfiguration.class);
	
	private static final String CONFIGURATION_NAME="configuration";
	private static final String PROPERTY_QNAME="property";
	private static final String NAME_QNAME="name";
	private static final String VALUE_QNAME="value";
	private static final String TYPE_QNAME="type";
	
	private Map<String, Value> container=null;
	private String xmlPath=null;
	
	public BaseConfiguration(String fileName) {
		container = new HashMap<>();
		String confPath = Utils.GetConfPath();
		if(confPath == null || confPath.isEmpty()) {
			return;
		}
		this.xmlPath= confPath + "/" + fileName;
		loadFile(this.xmlPath);
	}
	
	public void loadFile(String xmlPath){
		try {
			File file = new File(xmlPath);
			Document document=new SAXReader().read(file);
			Element root=document.getRootElement();
			List<Element> properties=root.elements(PROPERTY_QNAME);
			for(Element property:properties)
			{
				String name=property.elementText(NAME_QNAME);
				if(name==null || name.isEmpty())
				{
					continue;
				}
				String value=property.elementText(VALUE_QNAME);
				String type=property.elementText(TYPE_QNAME);
				LOG.info("load the configuration property "+name+" "+value+" "+type);
				container.put(name,new Value(value, type));
			}
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Value getKey(String name)
	{
		if(name==null ||name.isEmpty())
		{
			return null;
		}
		return container.get(name);
	}
	
	public void addKey(String name, Value value)
	{
		if(name==null || name.isEmpty())
		{
			return;
		}
		container.put(name, value);
	}
	
	public void close()
	{
		LOG.info("start t delete the data");
		Document document=DocumentHelper.createDocument();
		Element root=document.addElement(CONFIGURATION_NAME);
		document.setRootElement(root);
		//
		Set<String> names=container.keySet();
		for(String name:names)
		{
			Value value =container.get(name);
			Element propertyNode=root.addElement(PROPERTY_QNAME);
			Element nameNode=propertyNode.addElement(NAME_QNAME);
			nameNode.setText(name);
			Element valueNode=propertyNode.addElement(VALUE_QNAME);
			valueNode.setText(value.getValue());
			Element typeNode=propertyNode.addElement(TYPE_QNAME);
			String type= value.getType()==null?"": value.getType();
			typeNode.addText(type);
		}
		//
		try {
			LOG.info(this.xmlPath);
			FileOutputStream outputStream=new FileOutputStream(new File(this.xmlPath));
			OutputStreamWriter osw=new OutputStreamWriter(outputStream, "UTF-8");
			OutputFormat of = new OutputFormat();
			of.setEncoding("UTF-8");  
			of.setIndent(true);  
			of.setIndent("    ");  
			of.setNewlines(true);  
			XMLWriter writer = new XMLWriter(osw, of);  
			writer.write(document);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		
	}
}
