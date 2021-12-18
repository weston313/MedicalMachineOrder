package com.wes.mmo.service.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.wes.mmo.common.config.AppConfiguration;
import com.wes.mmo.common.config.ConfigKey;
import com.wes.mmo.common.cookie.CookieManagerCache;
import com.wes.mmo.dao.EquementDetail;
import com.wes.mmo.utils.TimeUtils;
import com.wes.mmo.utils.Utils;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.EngineIOException;
import javafx.beans.property.SimpleStringProperty;
import jdk.jshell.execution.Util;
import okhttp3.*;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.dom.DeferredElementImpl;
import org.openqa.selenium.JavascriptExecutor;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderTaskV3 implements Task {

    private static final Log LOG = LogFactory.getLog(OrderTaskV3.class);

    public static final Map<String, Integer> CAPTCHA_NUMBERS_SHAPE = new HashMap();

    static {
        CAPTCHA_NUMBERS_SHAPE.put("MLLQLLQLLQLLQLLQLLQLLQLLQZMLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLZ", 1);
        CAPTCHA_NUMBERS_SHAPE.put("MLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLLQLLQLLQLLQLLQLLQLLQZMLLQLLLQLLQLLQLLQLLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLLQLLQLLQLLQLLQLLLQLLLQLLQZ", 2);
        CAPTCHA_NUMBERS_SHAPE.put("MLLQLLQLLQLLQLLLQLLQLLQLLQLLQLLQLLQLLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQZMLLQLLQLLQLLQLLQLLLLQLLQLLQLLQLLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLLLQLLQLLQLLQLLQLLQLLLLQLLQLLQLLQLLQLLQLLLLLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQZ", 3);
        CAPTCHA_NUMBERS_SHAPE.put("MLLQLLQLLQLLQZMLLQLLQLLQLLQLLQLLQLLQLLQLLQLLLQLLQLLQLLQZMLLQLLLQLLQLLQLLQLLQLLQLLLQLLLQLLLQLLQLLQLLQLLQLLQLLQLLLQLLQLLQLLQZMLLQLLQLLQLLQLLQZ", 4);
        CAPTCHA_NUMBERS_SHAPE.put("MLLQLLQLLQLLQLLQLLQLLQLLLLQLLQLLQLLQLLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQZMLLQLLQLLQLLQLLLLLQLLLQLLLQLLQLLQLLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLLLQLLQLLQLLQLLQZ", 5);
        CAPTCHA_NUMBERS_SHAPE.put("MLLQLLQLLQLLQLLQLLQLLQLLQLLQZMLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQZMLLQLLQLLQLLQLLQLLQLLQLLLLQLLQLLQLLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQZMLLQLLQLLQLLQLLQLLQLLQLLQZ", 6);
        CAPTCHA_NUMBERS_SHAPE.put("MLLQLLQLLLQLLQLLQLLQLLQLLLQLLQLLLQLLQLLQLLLQLLQLLQLLQLLQZMLLLQLLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLLLLQLLQLLQLLQLLQLLLQLLLQLLQLLQLLLQZ", 7);
        CAPTCHA_NUMBERS_SHAPE.put("MLLQLLQLLQLLQLLQLLQLLQLLQZMLLQLLQLLQLLQLLQLLQLLQZMLLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQZMLLLQLLQLLQLLQLLLLLQLLQLLQLLQLLLLQLLLQLLQLLQLLQLLQLLQLLQLLLQLLQLLQLLQLLQZMLLQLLQLLQLLQLLQLLQLLQLLQLLQZMLLQLLLQLLQLLQLLQLLQLLQZ", 8);
        CAPTCHA_NUMBERS_SHAPE.put("MLLLQLLQLLQLLQLLQLLQLLQLLQZMLLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQZMLLQLLQLLQLLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLLQLLQLLQLLQLLLQZMLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQZ", 9);
        CAPTCHA_NUMBERS_SHAPE.put("MLLQLLQLLQLLQLLQLLQLLQLLQLLQLLLQLLQLLQLLQLLQLLQLLLQZMLLQLLQLLQLLLQLLQLLQLLQLLLQLLLQLLQLLQLLQLLQLLQLLQLLLQLLLQZ", -1);
    }

    private SimpleStringProperty id ;
    private SimpleStringProperty equement = null;
    private SimpleStringProperty start = null;
    private SimpleStringProperty end = null;
    private SimpleStringProperty status = null;
    private SimpleStringProperty action = null;

    private AppConfiguration configuration = AppConfiguration.getConfiguration();
    private EquementDetail equementDetail;
    private long startTime;
    private long endTime;
    private String description;
    private String relationProject;
    private long actionTime;
    private int threadNum = 1;
    private String tableBrowserUrl;
    private String calendarId;
    private String cookie;

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(threadNum);

    public OrderTaskV3(EquementDetail equementDetail, long startTime, long endTime, String description, String relationProject, long actionTime) {
        this.equementDetail = equementDetail;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.relationProject = relationProject;
        this.actionTime = actionTime;

        this.equement = new SimpleStringProperty(equementDetail.getName());
        this.start = new SimpleStringProperty(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(startTime*1000)));
        this.end = new SimpleStringProperty(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(endTime*1000)));
        this.status = new SimpleStringProperty("RUNNING");
        this.action = new SimpleStringProperty("STOP");
        this.id = new SimpleStringProperty(String.valueOf(System.currentTimeMillis()/1000));
    }

    private void run() {
        long actionTs = actionTime * 1000;
        LOG.info("======> Run Thread " + threadNum);
        for(int i = 0; i < threadNum; i++){
            OrderTaskV3.EquementOrderThread eot = new OrderTaskV3.EquementOrderThread();
            if(System.currentTimeMillis() > actionTs){
                eot.run();
            }
            else {
                long stepTime = actionTs - System.currentTimeMillis();
                executorService.schedule(eot, stepTime, TimeUnit.MILLISECONDS);
            }
        }
    }

    public void stop(){
        executorService.shutdown();
    }

    @Override
    public void execute() {
        initlize();
        run();
    }

    private void initlize(){
        try {
            WebClient webClient = CookieManagerCache.GetCookieManagerCache().getWebClient();
            String orderUrl = equementDetail.getOrderUrl();
            HtmlPage orderPage = webClient.getPage(orderUrl);
            // 获取仪器页面
            DomNodeList<DomElement> divs = orderPage.getElementsByTagName("div");

            HtmlElement caledarBrowser = null;
            for(DomElement element : divs) {
                if(element.getAttribute("class").equals("browser_wrapper")) {
                    caledarBrowser = (HtmlElement) element;
                    break;
                }
            }

            String browserSrc = caledarBrowser.getAttribute("src");;
            Date orderDate = new Date(startTime*1000);
            Date startDate, endDate;
            if(TimeUtils.getDayOfWeek(orderDate) == 7){
                startDate = TimeUtils.getToday(orderDate);
                endDate = TimeUtils.getNextSaturday(orderDate);
            }
            else {
                startDate = TimeUtils.getLastWeekSunday(orderDate);
                endDate = TimeUtils.getSaturday(orderDate);
            }
            StringBuffer browserTableUrlSb = new StringBuffer(browserSrc.split("\\?")[0]).append("?");
            for(String paramKv : browserSrc.split("\\?")[1].split("\\&")) {
                String[] kv = paramKv.split("=");
                if(kv[0].equals("st")){
                    browserTableUrlSb.append("st=").append(startDate.getTime()/1000).append("&");
                }
                else if(kv[0].equals("ed")){
                    browserTableUrlSb.append("ed=").append(endDate.getTime()/1000).append("&");
                }
                else if(kv[0].equals("calendar_id")){
                    this.calendarId = kv[1];
                    browserTableUrlSb.append(paramKv).append("&");
                }
                else
                    browserTableUrlSb.append(paramKv).append("&");
            }

            browserTableUrlSb.deleteCharAt(browserTableUrlSb.length() - 1);
            this.tableBrowserUrl = browserTableUrlSb.toString();
            Cookie cookie = webClient.getCookieManager().getCookie("session_lims2_cf-lite_chinablood");
            this.cookie = cookie.getName() + "=" + cookie.getValue();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class EquementOrderThread extends Thread {

        @Override
        public void run() {
            executeRunV2();
        }

        private void executeRunV1(){
            try{

                System.out.println("=====> Start Order");
                CookieManager cookieManager = CookieManagerCache.GetCookieManagerCache().getCookieManager();
                String orderUrl = equementDetail.getOrderUrl();
                WebClient webClient = new WebClient(BrowserVersion.EDGE);
                webClient.setCookieManager(cookieManager);
                webClient.getOptions().setCssEnabled(false);
                webClient.getOptions().setJavaScriptEnabled(false);
                webClient.getOptions().setRedirectEnabled(false);
                webClient.getOptions().setThrowExceptionOnScriptError(false);
                webClient.getOptions().setProxyConfig(new ProxyConfig("127.0.0.1", 8888, null));

                HtmlPage orderPage = webClient.getPage(orderUrl);
                // 获取仪器页面
                DomNodeList<DomElement> divs = orderPage.getElementsByTagName("div");
                HtmlElement caledarBrowser = null;
                for(DomElement element : divs) {
                    if(element.getAttribute("class").equals("browser_wrapper")) {
                        caledarBrowser = (HtmlElement) element;
                        break;
                    }
                }
                if(caledarBrowser == null)
                    LOG.info(orderPage.getWebResponse().getContentAsString());
                String browserId = caledarBrowser.getId();
                String browserSrc = caledarBrowser.getAttribute("src");

                // 拼接地址
                Date orderDate = new Date(startTime*1000);
                Date startDate, endDate;
                if(TimeUtils.getDayOfWeek(orderDate) == 7){
                    startDate = TimeUtils.getToday(orderDate);
                    endDate = TimeUtils.getNextSaturday(orderDate);
                }
                else {
                    startDate = TimeUtils.getLastWeekSunday(orderDate);
                    endDate = TimeUtils.getSaturday(orderDate);
                }
                StringBuffer browserTableUrlSb = new StringBuffer(browserSrc.split("\\?")[0]).append("?");
                for(String paramKv : browserSrc.split("\\?")[1].split("\\&")) {
                    String[] kv = paramKv.split("=");
                    if(kv[0].equals("st")){
                        browserTableUrlSb.append("st=").append(startDate.getTime()/1000).append("&");
                    }
                    if(kv[0].equals("ed")){
                        browserTableUrlSb.append("ed=").append(endDate.getTime()/1000).append("&");
                    }
                    else
                        browserTableUrlSb.append(paramKv).append("&");
                }

                browserTableUrlSb.deleteCharAt(browserTableUrlSb.length() - 1);
                System.out.println(browserTableUrlSb.toString());
                HtmlPage tablePage = webClient.getPage(browserTableUrlSb.toString());
                HtmlTable calendarTable = (HtmlTable) tablePage.getElementsByTagName("table").get(0);
                String calendarTableId = calendarTable.getId();
                String captchaResult = getSvgResult(webClient, calendarTableId);

                String orderJs = orderCaledarV2(
//                        webClient,
                        browserTableUrlSb.toString(),
                        "仪器使用预约",
                        startTime,
                        endTime,
                        calendarTable.getId(),
                        description,
                        relationProject,
                        captchaResult
                );

                System.out.println(orderJs);
                orderOnSeleniumFirefox(orderJs);
                webClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void orderOnSeleniumFirefox(String jsCode){
            JavascriptExecutor executor = CookieManagerCache.GetCookieManagerCache().getJavascriptExecutor();
            executor.executeScript(jsCode);
        }

        private void executeRunV2(){
            try {
//                String calendarTableId = "calweek_" + Utils.ConvertDecToHex(System.currentTimeMillis() * 1048).toLowerCase();
                WebClient webClient = CookieManagerCache.GetCookieManagerCache().getWebClient();
                HtmlPage orderPage = webClient.getPage(equementDetail.getOrderUrl());
                // 获取仪器页面
                DomNodeList<DomElement> divs = orderPage.getElementsByTagName("div");
                HtmlElement caledarBrowser = null;
                for(DomElement element : divs) {
                    if(element.getAttribute("class").equals("browser_wrapper")) {
                        caledarBrowser = (HtmlElement) element;
                        break;
                    }
                }
                if(caledarBrowser == null)
                    LOG.info(orderPage.getWebResponse().getContentAsString());
                String browserId = caledarBrowser.getId();
                String browserSrc = caledarBrowser.getAttribute("src");

                // 拼接地址
                Date orderDate = new Date(startTime*1000);
                Date startDate, endDate;
                if(TimeUtils.getDayOfWeek(orderDate) == 7){
                    startDate = TimeUtils.getToday(orderDate);
                    endDate = TimeUtils.getNextSaturday(orderDate);
                }
                else {
                    startDate = TimeUtils.getLastWeekSunday(orderDate);
                    endDate = TimeUtils.getSaturday(orderDate);
                }
                StringBuffer browserTableUrlSb = new StringBuffer(browserSrc.split("\\?")[0]).append("?");
                for(String paramKv : browserSrc.split("\\?")[1].split("\\&")) {
                    String[] kv = paramKv.split("=");
                    if(kv[0].equals("st")){
                        browserTableUrlSb.append("st=").append(startDate.getTime()/1000).append("&");
                    }
                    if(kv[0].equals("ed")){
                        browserTableUrlSb.append("ed=").append(endDate.getTime()/1000).append("&");
                    }
                    else
                        browserTableUrlSb.append(paramKv).append("&");
                }

                browserTableUrlSb.deleteCharAt(browserTableUrlSb.length() - 1);
                System.out.println(browserTableUrlSb);
                HtmlPage tablePage = webClient.getPage(browserTableUrlSb.toString());
                HtmlTable calendarTable = (HtmlTable) tablePage.getElementsByTagName("table").get(0);
                String calendarTableId = calendarTable.getId();


                String captchaResult = getSvgResultV2(calendarTableId);
                String orderJs = orderCaledar(webClient, tableBrowserUrl, "仪器使用预约", startTime, endTime, calendarTableId, description, relationProject, captchaResult);
                System.out.println(orderJs);
                orderOnSocketIO(orderJs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String getSvgResultV2(String caledearTableId){
            String captchaResult = "0";
            try {
                // initlize url and request info
                String captchaUrl = AppConfiguration.getConfiguration().getKey(ConfigKey.AppKey.SVG_URL.getKey()).getValue();

                FormBody formBody = new FormBody.Builder()
                        .add("_ajax", "1")
                        .add("_object", "get_captcha")
                        .add("_event", "click")
                        .add("cal_week_rel", "#" + caledearTableId)
                        .build()
                        ;

                Request request =  new Request.Builder()
                        .addHeader("Cookie", cookie)
                        .url(captchaUrl)
                        .post(formBody)
                        .build();

                OkHttpClient client = new OkHttpClient.Builder().readTimeout(Duration.ofSeconds(20)).build();

                Response response = client.newCall(request).execute();
                String svgXmlStr = response.body().string();
                String svgXml = ((JSONObject) JSON.parse(svgXmlStr)).getString("data");

                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document root = documentBuilder.parse(new ByteArrayInputStream(svgXml.getBytes()));
                NodeList nodes = root.getElementsByTagName("path");

                int captcha = 0;
                for (int i = 0; i < nodes.getLength(); i++) {
                    DeferredElementImpl pathNode = (DeferredElementImpl) nodes.item(i);
                    String fillAttr = pathNode.getAttribute("fill");
                    if (!fillAttr.equals("none")) {
                        String dStr = pathNode.getAttribute("d").replaceAll("[0-9\\-\\.\\,\\s]*", "").toUpperCase();
                        int captchaNumber = CAPTCHA_NUMBERS_SHAPE.get(dStr);
                        if (captchaNumber >= 0)
                            captcha += captchaNumber;
                    }
                }
                captchaResult = String.valueOf(captcha);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
            finally {
                return captchaResult;
            }
        }

        private void orderOnSocketIO(String jsCode) throws URISyntaxException, InterruptedException, UnsupportedEncodingException {
            String ticketId = null;
            String ticked = null;
            String userId = null;
            String userName = null;
            String form = null;

            for(String jsLine : jsCode.split("[\r\n]+")){
                if(jsLine.trim().startsWith("ticketId")){
                    ticketId = jsLine.trim().split(":")[1].replaceAll("[',]+", "");
                }
                else if(jsLine.trim().startsWith("ticket")){
                    ticked = jsLine.trim().split(":")[1].replaceAll("[',]", "");
                }
                else if(jsLine.trim().startsWith("userId")) {
                    userId = jsLine.split(":")[1].replaceAll("[',]", "");
                }
                else if(jsLine.trim().startsWith("userName")) {
                    userName = jsLine.split(":")[1].replaceAll("[',]", "");
                }
                else if(jsLine.trim().startsWith("socket.emit('yiqikong-reserv',")){
                    String tmp = jsLine.trim().replaceAll("socket.emit\\('yiqikong-reserv',", "");
                    form = tmp.trim().substring(0, tmp.length() - 2);
                }
            }

            final JSONObject finalFormJson = JSON.parseObject(form);

            IO.Options options = new IO.Options();
            options.reconnection = false;
            options.forceNew = true;
            options.path = "/socket.iov2";
            options.timestampRequests = true;
            String queryStr = new StringBuffer()
                    .append("userId=").append(URLEncoder.encode(userId.trim(), "UTF-8"))
                    .append("&").append("userName=").append(URLEncoder.encode(userName.trim(), "UTF-8"))
                    .append("&").append("ticket=").append(URLEncoder.encode(ticked.trim(), "UTF-8"))
                    .append("&").append("ticketId=").append(URLEncoder.encode(ticketId.trim(), "UTF-8"))
                    .toString();
            options.query = queryStr;

            String url = configuration.getKey(ConfigKey.AppKey.WEB_SOCKET_ADDRESS.getKey()).getValue();
            Socket socket = IO.socket(new URI(url), options);

            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println("======> Open Connect");
                    socket.emit("yiqikong-reserv", finalFormJson);
                }
            }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println("======> Error " + args[0]);
                    if(args[0] instanceof EngineIOException){
                        EngineIOException engineIOException = (EngineIOException) args[0];
                        System.out.println("=====> Error Code " + engineIOException.getCause());

                        System.out.println("=====> Error Stack ");
                        for(StackTraceElement element : engineIOException.getStackTrace()){
                            System.out.println(element.toString());
                        }
                    }
                }
            }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... objects) {
                    System.out.println("======> Connect Error " + objects[0]);
                    EngineIOException exception = (EngineIOException) objects[0];
                    System.out.println("======> Connect Error " + exception.code);
                    for(StackTraceElement stackTraceElement : exception.getStackTrace()){
                        System.out.println(stackTraceElement);
                    }
                }
            }).on("yiqikong-reserv-reback", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println("======> Order Result " + args[0]);
                    socket.disconnect();
                }
            });


            socket.connect();

            Thread.sleep(5000);
        }

        private String orderCaledar(WebClient webClient, String url, String name, long startTs, long endTs, String caledarTableId, String desc, String project, String captcha) throws IOException {

            String urlParamsStr = url.split("\\?", 2)[1];
            Map<String, String> paramsMap = new HashMap<>();
            for(String urlParamKvStr : urlParamsStr.split("&")){
                String[] paramKv = urlParamKvStr.split("=");
                paramsMap.put(paramKv[0], paramKv[1]);
            }
            WebRequest request = new WebRequest(new URL(url));
            request.setHttpMethod(HttpMethod.POST);
            request.setAdditionalHeader("Content-Type", "application/x-www-form-urlencoded");
            List<NameValuePair> params = new ArrayList<>();
            params.add(new NameValuePair("_ajax", "1"));
            params.add(new NameValuePair("_object", "component_form"));
            params.add(new NameValuePair("_event", "submit"));
            params.add(new NameValuePair("cal_week_rel", "#" + caledarTableId));
            params.add(new NameValuePair("mode", "week"));
            params.add(new NameValuePair("component_id", "0"));
            params.add(new NameValuePair("calendar_id", paramsMap.get("calendar_id")));
            params.add(new NameValuePair("name", name));
            params.add(new NameValuePair("dtstart", String.valueOf(startTs)));
            params.add(new NameValuePair("dtend", String.valueOf(endTs)));
            params.add(new NameValuePair("description", desc));
            params.add(new NameValuePair("project", String.valueOf(0)));
            params.add(new NameValuePair("captcha", captcha));
            params.add(new NameValuePair("submit", "save"));
            request.setRequestParameters(params);
            UnexpectedPage page = webClient.getPage(request);
            String responseContent = page.getWebResponse().getContentAsString();
            JSONObject resultObject=  JSON.parseObject(responseContent);
            // 获取ORDER JavaScript代码
            String orderJavaScript = resultObject.getJSONObject("dialog").getString("data").trim();
            String jsRegext = "^(\\<div[\\s\\S]*\\<\\/div\\>)[\\s]*(\\<script\\>([\\s]+\\(function\\(\\$\\)\\{)([\\s\\S]*)(\\}\\)\\(jQuery\\)[\\s]+)\\<\\/script\\>)$";
            Pattern pattern = Pattern.compile(jsRegext);
            Matcher matcher = pattern.matcher(orderJavaScript);
            String result = "";
            if(matcher.matches()) {
                result = matcher.group(4);
            }
            return result;
        }

        private String orderCaledarV2(String url, String name, long startTs, long endTs, String caledarTableId, String desc, String project, String captcha) throws IOException {

            FormBody formBody = new FormBody.Builder()
                    .add("_ajax", "1")
                    .add("_object", "component_form")
                    .add("_event", "submit")
                    .add("cal_week_rel", "#" + caledarTableId)
                    .add("mode", "week")
                    .add("component_id", "0")
                    .add("name", name)
                    .add("calendar_id", calendarId)
                    .add("dtstart", String.valueOf(startTs))
                    .add("dtend", String.valueOf(endTs))
                    .add("description", desc)
                    .add("project", String.valueOf(0))
                    .add("captcha", captcha)
                    .add("submit", "save")
                    .build();

            Request request = new Request.Builder()
                    .addHeader("Cookie", cookie)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .url(tableBrowserUrl)
                    .post(formBody)
                    .build();

            Response response = new OkHttpClient().newCall(request).execute();
            String responseContent = response.body().string();
            JSONObject resultObject=  JSON.parseObject(responseContent);
            // 获取ORDER JavaScript代码
            String orderJavaScript = resultObject.getJSONObject("dialog").getString("data").trim();
            String jsRegext = "^(\\<div[\\s\\S]*\\<\\/div\\>)[\\s]*(\\<script\\>([\\s]+\\(function\\(\\$\\)\\{)([\\s\\S]*)(\\}\\)\\(jQuery\\)[\\s]+)\\<\\/script\\>)$";
            Pattern pattern = Pattern.compile(jsRegext);
            Matcher matcher = pattern.matcher(orderJavaScript);
            String result = "";
            if(matcher.matches()) {
                result = matcher.group(4);
            }
            return result;
        }

        private String getSvgResult(WebClient webClient, String caledearTableId) throws IOException, ParserConfigurationException, SAXException, TransformerException, TranscoderException {

            String svgUrl = AppConfiguration.getConfiguration().getKey(ConfigKey.AppKey.SVG_URL.getKey()).getValue();
            WebRequest request = new WebRequest(new URL(svgUrl));
            request.setHttpMethod(HttpMethod.POST);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new NameValuePair("_ajax", "1"));
            params.add(new NameValuePair("_object", "get_captcha"));
            params.add(new NameValuePair("_event", "click"));
            params.add(new NameValuePair("cal_week_rel", "#" + caledearTableId));
            request.setRequestParameters(params);
            WebResponse response = webClient.getPage(request).getWebResponse();
            String svgXml = ((JSONObject) JSON.parse(response.getContentAsString())).getString("data");

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document root = documentBuilder.parse(new ByteArrayInputStream(svgXml.getBytes()));
            NodeList nodes = root.getElementsByTagName("path");

            int captchaResult = 0;
            for (int i = 0; i < nodes.getLength(); i++) {
                DeferredElementImpl pathNode = (DeferredElementImpl) nodes.item(i);
                String fillAttr = pathNode.getAttribute("fill");
                if (!fillAttr.equals("none")) {
                    String dStr = pathNode.getAttribute("d").replaceAll("[0-9\\-\\.\\,\\s]*", "").toUpperCase();
                    int captchaNumber = CAPTCHA_NUMBERS_SHAPE.get(dStr);
                    if (captchaNumber >= 0)
                        captchaResult += captchaNumber;
                }
            }

            return String.valueOf(captchaResult);
        }
    }

    public String getEquement(){
        return equement.get();
    }

    public String getStart(){
        return start.get();
    }

    public String getEnd(){
        return end.get();
    }

    public String getStatus(){
        return status.get();
    }

    public String getAction() {
        return action.get();
    }

    public void setStatus(String status){
        this.status.setValue(status);
    }

    public String getId(){
        return this.id.get();
    }
}


