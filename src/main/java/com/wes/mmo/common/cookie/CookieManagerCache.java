package com.wes.mmo.common.cookie;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLInputElement;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.wes.mmo.common.config.AppConfiguration;
import com.wes.mmo.common.config.ConfigKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.expr.PredicateSet;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

	private WebClient webClient;
 	private CookieManager cookieManager;
	private URL indexUrl;
	private String orderPage;

	private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

	// 创建缓存
	private CookieManagerCache() {
		AppConfiguration configuration=AppConfiguration.getConfiguration();
		String username=configuration.getKey(ConfigKey.AppKey.USERNAME.getKey()).getValue();
		String password=configuration.getKey(ConfigKey.AppKey.PASSWORD.getKey()).getValue();
		webClient=new WebClient(BrowserVersion.FIREFOX_78);
		webClient.getOptions().setJavaScriptEnabled(false);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setRedirectEnabled(true);

		try {
			String loginUrl = configuration.getKey(ConfigKey.AppKey.LOGIN_URL.getKey()).getValue();
			System.out.println("Login Url is " + loginUrl);
			HtmlPage page = webClient.getPage(loginUrl);
			HtmlTextInput nameInput = page.getElementByName(configuration.getKey(ConfigKey.AppKey.LOGIN_USERNAME_ELEMENT.getKey()).getValue());
			nameInput.setText(username);
			HtmlPasswordInput passwordInput = page.getElementByName(configuration.getKey(ConfigKey.AppKey.LOGIN_PASSWORD_ELEMENT.getKey()).getValue());
			passwordInput.setText(password);
			WebResponse response=page.getElementByName(configuration.getKey(ConfigKey.AppKey.LOGIN_SUBMIT_ELEMMENT.getKey()).getValue()).click().getWebResponse();

			indexUrl = response.getWebRequest().getUrl();
			orderPage = indexUrl.toString() + ".reserv";
			cookieManager=webClient.getCookieManager();
		} catch (FailingHttpStatusCodeException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		executorService.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					LOG.info("Heat Beate 5 Minute");
					try {
						webClient.getPage(indexUrl);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			,300
			,300
			,TimeUnit.SECONDS
		);
	}
	
	public CookieManager getCookieManager()
	{
		return cookieManager;
	}

	public URL getIndexUrl() {
		return indexUrl;
	}

	public WebClient getWebClient() {
		return webClient;
	}
}
