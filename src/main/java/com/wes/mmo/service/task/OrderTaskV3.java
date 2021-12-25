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
import com.wes.mmo.common.cookie.CookieManagerCache;
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
import org.apache.xerces.impl.xs.traversers.XSAttributeChecker;
import org.eclipse.jetty.io.ssl.ALPNProcessor;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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

    private static final String USER_ID = "userId";
    private static final String USER_NAME = "userName";
    private static final String TICKET = "ticket";
    private static final String TICKET_ID = "ticketId";
    private static final String FORM = "form";


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
    private int threadNum = 300;

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
        this.executorService = Executors.newScheduledThreadPool(threadNum);
        initlize();
    }

    private WebClient webClient;
    private Cookie cookie;
    private ClientHandleThread clientHandleThread;
    private Map<String, String> orderTableInfo;

    private void initlize(){
        this.webClient = createWebClient();
        this.cookie = webClient.getCookieManager().getCookie("session_lims2_cf-lite_chinablood");
        this.clientHandleThread = new ClientHandleThread(webClient);
        clientHandleThread.start();
        orderTableInfo = getOrderTableInfo(webClient, equementDetail.getOrderUrl(), startTime, endTime);

    }

    @Override
    public void run() {
        try {
            // stop cookie refresh
            clientHandleThread.setRefresh(false);

            // get svg
            String calendarTableId = "calweek_" + Utils.ConvertDecToHex(System.currentTimeMillis() * 1048).toLowerCase();
            String captchResult = getSvgResultV2(calendarTableId, cookie);
            String orderJs = orderCaledarV2(orderTableInfo.get("orderTableUrl"), "仪器使用预约", startTime, endTime,  orderTableInfo.get("calendarId"), calendarTableId, description, relationProject, captchResult, cookie);
            Map<String, String> jsInfo = parseJavaScriptCode(orderJs);

            long actionTimestamp = actionTime * 1000;
            if(System.currentTimeMillis() > actionTimestamp) {
                Thread thread = new EquementOrderThread(jsInfo.get(USER_ID), jsInfo.get(USER_NAME), jsInfo.get(TICKET), jsInfo.get(TICKET_ID), jsInfo.get(FORM));
                Thread.sleep(1000);
                thread.start();
            }
            else {
                LOG.info("======> Order on " + actionTimestamp);
//                int socketThreadNum = 0;
                for(int i = 1; i <= threadNum; i++){
                    Thread thread = new EquementOrderThread(jsInfo.get(USER_ID), jsInfo.get(USER_NAME), jsInfo.get(TICKET), jsInfo.get(TICKET_ID), jsInfo.get(FORM));
                    executorService.schedule(thread, actionTimestamp - System.currentTimeMillis() - 1, TimeUnit.MILLISECONDS);
                }
            }
            // 沉睡15秒，用来等待子进程
            Thread.sleep(120000);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private WebClient createWebClient(){
        WebClient webClient = null;
        try {
            AppConfiguration configuration = AppConfiguration.getConfiguration();
            String username=configuration.getKey(ConfigKey.AppKey.USERNAME.getKey()).getValue();
            String password=configuration.getKey(ConfigKey.AppKey.PASSWORD.getKey()).getValue();
            webClient = new WebClient(BrowserVersion.FIREFOX_78);
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setRedirectEnabled(true);

            String loginUrl = configuration.getKey(ConfigKey.AppKey.LOGIN_URL.getKey()).getValue();
            HtmlPage page = webClient.getPage(loginUrl);
            HtmlTextInput nameInput = page.getElementByName(configuration.getKey(ConfigKey.AppKey.LOGIN_USERNAME_ELEMENT.getKey()).getValue());
            nameInput.setText(username);
            HtmlPasswordInput passwordInput = page.getElementByName(configuration.getKey(ConfigKey.AppKey.LOGIN_PASSWORD_ELEMENT.getKey()).getValue());
            passwordInput.setText(password);
            page.getElementByName(configuration.getKey(ConfigKey.AppKey.LOGIN_SUBMIT_ELEMMENT.getKey()).getValue()).click().getWebResponse();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            return webClient;
        }
    }

    public class ClientHandleThread extends Thread {

        private WebClient webClient = null;
        private boolean refresh = true;

        public ClientHandleThread(WebClient webClient){
            this.webClient = webClient;
        }

        public void setRefresh(boolean refresh) {
            this.refresh = refresh;
        }

        @Override
        public void run() {
            while (refresh){
                try {
                    // 每5分钟刷一次数值
                    Thread.sleep(300 * 1000);
                    this.webClient.getPage(CookieManagerCache.GetCookieManagerCache().getIndexUrl());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Map<String, String> getOrderTableInfo(WebClient webClient, String orderUrl, long startTime, long endTime){
        Map<String, String> orderTableInfo = new HashMap<>();
        try {
            HtmlPage orderPage = webClient.getPage(orderUrl);
            DomNodeList<DomElement> divs = orderPage.getElementsByTagName("div");

            // get calendar browser info
            HtmlElement caledarBrowser = null;
            for(DomElement element : divs) {
                if(element.getAttribute("class").equals("browser_wrapper")) {
                    caledarBrowser = (HtmlElement) element;
                    break;
                }
            }

            String browserSrc = caledarBrowser.getAttribute("src");

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
                    orderTableInfo.put("calendarId", kv[1]);
                    browserTableUrlSb.append(paramKv).append("&");
                }
                else
                    browserTableUrlSb.append(paramKv).append("&");
            }

            browserTableUrlSb.deleteCharAt(browserTableUrlSb.length() - 1);
            orderTableInfo.put("orderTableUrl", browserTableUrlSb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            return orderTableInfo;
        }
    }

    public String getSvgResultV2(String caledearTableId, Cookie cookie){
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
                    .addHeader("Cookie", cookie.getName() + "=" + cookie.getValue())
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

    private String orderCaledarV2(String url, String name, long startTs, long endTs, String calendarId, String caledarTableId, String desc, String project, String captcha, Cookie cookie) throws IOException {

        LOG.info("======> Start Order Calendar.");

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
                .addHeader("Cookie", cookie.getName() + "=" + cookie.getValue())
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .url(url)
                .post(formBody)
                .build();

        Response response = new OkHttpClient().newCall(request).execute();
        String responseContent = response.body().string();
        JSONObject resultObject=  JSON.parseObject(responseContent);
        // 获取ORDER JavaScript代码
        return  resultObject.getJSONObject("dialog").getString("data").trim();
    }

    private Map<String, String> parseJavaScriptCode(String jsCode){

        Map<String, String> jsCodeInfo = new HashMap<>();


        for(String jsLine : jsCode.split("[\r\n]+")){
            if(jsLine.trim().startsWith(TICKET_ID)){
                jsCodeInfo.put(TICKET_ID, jsLine.trim().split(":")[1].replaceAll("[',]+", ""));
            }
            else if(jsLine.trim().startsWith(TICKET)){
                jsCodeInfo.put(TICKET, jsLine.trim().split(":")[1].replaceAll("[',]", ""));

            }
            else if(jsLine.trim().startsWith(USER_ID)) {
                jsCodeInfo.put(USER_ID, jsLine.split(":")[1].replaceAll("[',]", ""));
            }
            else if(jsLine.trim().startsWith(USER_NAME)) {
                jsCodeInfo.put(USER_NAME, jsLine.split(":")[1].replaceAll("[',]", ""));
            }
            else if(jsLine.trim().startsWith("socket.emit('yiqikong-reserv',")){
                String tmp = jsLine.trim().replaceAll("socket.emit\\('yiqikong-reserv',", "");
                jsCodeInfo.put(FORM, tmp.trim().substring(0, tmp.length() - 2));
            }
        }

        return jsCodeInfo;
    }



    public class EquementOrderThread extends Thread {

        private String userId;
        private String userName;
        private String ticket;
        private String ticketId;
        private Socket socket;
        private JSONObject form;

        public EquementOrderThread(String userId, String userName, String ticket, String ticketId, String form) {
            this.userId = userId;
            this.userName = userName;
            this.ticket = ticket;
            this.ticketId = ticketId;
            this.form = JSON.parseObject(form);
            try {
                this.socket = createWebSocket(this.userId, this.userName, this.ticket, this.ticketId);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private boolean isConnected(){
            return this.socket.connected();
        }

        private Socket createWebSocket(String userId, String userName, String ticket, String ticketId) throws URISyntaxException, UnsupportedEncodingException, InterruptedException {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            options.path = configuration.getKey(ConfigKey.AppKey.WEB_SOCKET_PATH.getKey()).getValue();
            options.timestampRequests = true;
            options.query =  new StringBuffer()
                    .append("userId=").append(URLEncoder.encode(userId.trim(), "UTF-8"))
                    .append("&").append("userName=").append(URLEncoder.encode(userName.trim(), "UTF-8"))
                    .append("&").append("ticket=").append(URLEncoder.encode(ticket.trim(), "UTF-8"))
                    .append("&").append("ticketId=").append(URLEncoder.encode(ticketId.trim(), "UTF-8"))
                    .toString();

            String url = configuration.getKey(ConfigKey.AppKey.WEB_SOCKET_ADDRESS.getKey()).getValue();
            Socket socket = IO.socket(new URI(url), options);

            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    LOG.info("======> Open WebSocket On " + System.currentTimeMillis());
                }
            }).on("yiqikong-reserv-reback", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    LOG.info("======> Order Result " + args[0]);
                }
            }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... objects) {
                    LOG.info("======> Error is " + objects[0] + " and reconnect.");
                }
            }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... objects) {
                    LOG.info("======> Error is " + objects[0] + " and reconnect.");
                }
            })
            ;
            socket.connect();
            return socket;
        }

        @Override
        public void run() {
            try {
                if(socket.connected()) {
                    LOG.info("======> Send Form on " + System.currentTimeMillis());
                    socket.emit("yiqikong-reserv", form);
                }
                Thread.sleep(5000);
                socket.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


