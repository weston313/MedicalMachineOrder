package com.wes.mmo.common.cookie;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.wes.mmo.common.config.AppConfiguration;
import com.wes.mmo.common.config.ConfigKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class CookieManagerCache {
	
	public static final Log LOG=LogFactory.getLog(CookieManagerCache.class);
	
	public volatile static CookieManagerCache cache=null;
	
	public static CookieManagerCache GetCookieManagerCache() {
		if(cache==null)
		{
			synchronized (CookieManagerCache.class) {
				if(cache==null)
				{
					cache=new CookieManagerCache();
				}
			}
		}
		return cache;
	}
	
	private CookieManager cookieManager;

	// 创建缓存
	private CookieManagerCache() {
		AppConfiguration configuration=AppConfiguration.getConfiguration();
		String username=configuration.getKey(ConfigKey.AppKey.USERNAME.getKey()).getValue();
		String password=configuration.getKey(ConfigKey.AppKey.PASSWORD.getKey()).getValue();
		WebClient webClient=new WebClient(BrowserVersion.FIREFOX_78);
		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setTimeout(10000);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setRedirectEnabled(true);
		try {
			HtmlPage page=webClient.getPage(configuration.getKey(ConfigKey.AppKey.LOGIN_URL.getKey()).getValue());
			webClient.waitForBackgroundJavaScript(10000);
			webClient.setJavaScriptTimeout(0);
			// 进行登录
			page.getElementByName(configuration.getKey(ConfigKey.AppKey.LOGIN_USERNAME_ELEMENT.getKey()).getValue()).setNodeValue(username);
			page.getElementByName(configuration.getKey(ConfigKey.AppKey.LOGIN_PASSWORD_ELEMENT.getKey()).getValue()).setNodeValue(password);
			WebResponse response=page.getElementByName(configuration.getKey(ConfigKey.AppKey.LOGIN_SUBMIT_ELEMMENT.getKey()).getValue()).click().getWebResponse();

			// 填充缓存
			HtmlPage taobaoPage=webClient.getPage(configuration.getKey(ConfigKey.AppKey.INDEX_URL.getKey()).getValue());
			webClient.waitForBackgroundJavaScript(10000);
			webClient.setJavaScriptTimeout(0);
			cookieManager=webClient.getCookieManager();
		} catch (FailingHttpStatusCodeException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public CookieManager getCookieManager()
	{
		return cookieManager;
	}

}
