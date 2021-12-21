package com.wes.mmo.service.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.wes.mmo.common.config.AppConfiguration;
import com.wes.mmo.common.config.ConfigKey;
import com.wes.mmo.dao.EquementDetail;
import com.wes.mmo.utils.TimeUtils;
import com.wes.mmo.utils.Utils;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import javafx.beans.property.SimpleStringProperty;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.dom.DeferredElementImpl;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OrderTaskV3 extends Thread {

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

    /**
     * Job List Information
     */
    private SimpleStringProperty index ;
    private SimpleStringProperty equement = null;
    private SimpleStringProperty start = null;
    private SimpleStringProperty end = null;
    private SimpleStringProperty status = null;
    private SimpleStringProperty action = null;

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

    public String getIndex(){
        return this.index.get();
    }

    /**
     * Order Thread Information
     */
    private AppConfiguration configuration = AppConfiguration.getConfiguration();
    private EquementDetail equementDetail;
    private long startTime;
    private long endTime;
    private String description;
    private String relationProject;
    private long actionTime;

    private ScheduledExecutorService executorService;

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
        this.index = new SimpleStringProperty(String.valueOf(System.currentTimeMillis()/1000));
    }

    @Override
    public void run() {
        long actionTs = actionTime * 1000;
        long threadNum = (endTime - startTime + 1) / 3600;
        this.executorService = Executors.newScheduledThreadPool((int) threadNum);

        if(System.currentTimeMillis() > actionTs) {
            LOG.info("Orderring by using " + threadNum + " thread right now.");
            for(int i = 1; i <= threadNum; i++) {
                Thread thread = new EquementOrderThread(
                        i,
                        startTime + (i - 1) * 3600,
                        startTime + i*3600 - 1
                );
//                thread.start();
                executorService.schedule(thread, 3000, TimeUnit.MILLISECONDS);
            }
        }
        else {
            for(int i = 1; i <= threadNum; i++){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Thread thread = new EquementOrderThread(
                        i,
                        startTime + (i - 1) * 3600,
                        startTime + i*3600 - 1
                );
                long stepTime = actionTs - System.currentTimeMillis();
                executorService.schedule(thread, stepTime, TimeUnit.MILLISECONDS);
            }
        }

        try {
            Thread.sleep(1000 * 60);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class EquementOrderThread extends Thread {

        private long index;
        private long threadStartTime;
        private long threadEndTime;
        private ScheduledExecutorService executorService;
        private WebClient threadWebClient;
        private Cookie threadCookie;
        private String threadCalendarId;
        private String threadTableBrowserUrl;
        private String threadCalendarTableId;
        private String threadCaptchResult;

        private  EquementOrderThread(long index, long threadStartTime, long threadEndTime){
            this.index = index;
            this.threadStartTime = threadStartTime;
            this.threadEndTime = threadEndTime;
            createCookie();
            initlize();
            threadCalendarTableId = "calweek_" + Utils.ConvertDecToHex(System.currentTimeMillis() * (1047+index)).toLowerCase();
            threadCaptchResult = getSvgResultV2(threadCalendarTableId);
            LOG.info("======> Complete Initlize on " + System.currentTimeMillis());
        }

        private void initlize(){
            try {
                String orderUrl = equementDetail.getOrderUrl();
                HtmlPage orderPage = threadWebClient.getPage(orderUrl);
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
                        this.threadCalendarId = kv[1];
                        browserTableUrlSb.append(paramKv).append("&");
                    }
                    else
                        browserTableUrlSb.append(paramKv).append("&");
                }

                browserTableUrlSb.deleteCharAt(browserTableUrlSb.length() - 1);
                this.threadTableBrowserUrl = browserTableUrlSb.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                LOG.info("======> Order from " + threadStartTime + " to " + threadEndTime  + " Using "  + threadCookie.getValue() + " On " + System.currentTimeMillis());
                long a = System.currentTimeMillis();
//                Thread.sleep(120000);
                String orderJs = orderCaledarV2(threadTableBrowserUrl, "仪器使用预约", threadStartTime,
                        threadEndTime, threadCalendarTableId, description, relationProject, threadCaptchResult);
                long b = System.currentTimeMillis();
                System.out.println("======> Compute Svg and Get Ticket Info Use " + (b-a));
                orderOnSocketIO(orderJs);
                this.executorService.shutdown();
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        }

        private void createCookie(){
            try {
                AppConfiguration configuration = AppConfiguration.getConfiguration();
                String username=configuration.getKey(ConfigKey.AppKey.USERNAME.getKey()).getValue();
                String password=configuration.getKey(ConfigKey.AppKey.PASSWORD.getKey()).getValue();
                threadWebClient=new WebClient(BrowserVersion.FIREFOX_78);
                threadWebClient.getOptions().setJavaScriptEnabled(false);
                threadWebClient.getOptions().setCssEnabled(false);
                threadWebClient.getOptions().setThrowExceptionOnScriptError(false);
                threadWebClient.getOptions().setRedirectEnabled(true);

                String loginUrl = configuration.getKey(ConfigKey.AppKey.LOGIN_URL.getKey()).getValue();
                HtmlPage page = threadWebClient.getPage(loginUrl);
                HtmlTextInput nameInput = page.getElementByName(configuration.getKey(ConfigKey.AppKey.LOGIN_USERNAME_ELEMENT.getKey()).getValue());
                nameInput.setText(username);
                HtmlPasswordInput passwordInput = page.getElementByName(configuration.getKey(ConfigKey.AppKey.LOGIN_PASSWORD_ELEMENT.getKey()).getValue());
                passwordInput.setText(password);
                WebResponse webResponse = page.getElementByName(configuration.getKey(ConfigKey.AppKey.LOGIN_SUBMIT_ELEMMENT.getKey()).getValue()).click().getWebResponse();
                this.threadCookie = threadWebClient.getCookieManager().getCookie("session_lims2_cf-lite_chinablood");

                String orderUrl = webResponse.getWebRequest().getUrl().toString();
                executorService = Executors.newScheduledThreadPool(1);
                executorService.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        LOG.info("Heat Beate 5 Minute");
                        try {
                            threadWebClient.getPage(orderUrl);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }}
                    ,300,300,TimeUnit.SECONDS
                );
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String getSvgResultV2(String caledearTableId){
            LOG.info("======> Compute Svg Result By " + caledearTableId);
            String captchaResult = "0";
            try {
                String captchaUrl = AppConfiguration.getConfiguration().getKey(ConfigKey.AppKey.SVG_URL.getKey()).getValue();

                FormBody formBody = new FormBody.Builder()
                        .add("_ajax", "1")
                        .add("_object", "get_captcha")
                        .add("_event", "click")
                        .add("cal_week_rel", "#" + caledearTableId)
                        .build()
                        ;

                Request request =  new Request.Builder()
                        .addHeader("Cookie", threadCookie.getName() + "=" + threadCookie.getValue())
                        .url(captchaUrl)
                        .post(formBody)
                        .build();

                OkHttpClient client = new OkHttpClient.Builder().readTimeout(Duration.ofSeconds(20)).build();

                LOG.info("======> Get SVG on " + System.currentTimeMillis());
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
            LOG.info("======> Order Canlendar By WebSocket.");

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
            options.path = configuration.getKey(ConfigKey.AppKey.WEB_SOCKET_PATH.getKey()).getValue();
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
                    socket.emit("yiqikong-reserv", finalFormJson);
                }
            }).on("yiqikong-reserv-reback", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    LOG.info("======> Order Result " + args[0]);
                    socket.disconnect();
                }
            });

            socket.connect();
        }

        private String orderCaledarV2(String url, String name, long startTs, long endTs, String caledarTableId, String desc, String project, String captcha) throws IOException {

            LOG.info("======> Start Order Calendar.");
            FormBody formBody = new FormBody.Builder()
                    .add("_ajax", "1")
                    .add("_object", "component_form")
                    .add("_event", "submit")
                    .add("cal_week_rel", "#" + caledarTableId)
                    .add("mode", "week")
                    .add("component_id", "0")
                    .add("name", name)
                    .add("calendar_id", threadCalendarId)
                    .add("dtstart", String.valueOf(startTs))
                    .add("dtend", String.valueOf(endTs))
                    .add("description", desc)
                    .add("project", String.valueOf(0))
                    .add("captcha", captcha)
                    .add("submit", "save")
                    .build();

            Request request = new Request.Builder()
                    .addHeader("Cookie", threadCookie.getName() + "=" + threadCookie.getValue())
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .url(threadTableBrowserUrl)
                    .post(formBody)
                    .build();

            Response response = new OkHttpClient().newCall(request).execute();
            String responseContent = response.body().string();
            JSONObject resultObject=  JSON.parseObject(responseContent);
            // 获取ORDER JavaScript代码
            return  resultObject.getJSONObject("dialog").getString("data").trim();
        }
    }

}


