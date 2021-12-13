package com.wes.mmo.service.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.wes.mmo.common.config.AppConfiguration;
import com.wes.mmo.common.config.ConfigKey;
import com.wes.mmo.common.cookie.CookieManagerCache;
import com.wes.mmo.dao.EquementDetail;
import com.wes.mmo.utils.TimeUtils;
import javafx.beans.property.SimpleStringProperty;
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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderTaskV3 implements Task {

    private static final Log LOG = LogFactory.getLog(OrderTaskV3.class);

    private static final Map<String, Integer> CAPTCHA_NUMBERS_SHAPE = new HashMap();
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
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    private EquementDetail equementDetail;
    private long startTime;
    private long endTime;
    private String description;
    private String relationProject;
    private long actionTime;
    // private int threadNum = Integer.parseInt(configuration.getKey(ConfigKey.AppKey.ORDER_THREAD_NUM.getKey()).getValue());
    private int threadNum = 1;
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
                executorService.execute(eot);
            }
            else {
                long stepTime = actionTs - System.currentTimeMillis();
                executorService.schedule(eot, stepTime, TimeUnit.MILLISECONDS);
            }
            // eot.start();
        }
    }

    public void stop(){
        executorService.shutdown();
    }

    @Override
    public void execute() {
        run();
    }

    public class EquementOrderThread extends Thread {

        @Override
        public void run() {
            try {
                CookieManager cookieManager = CookieManagerCache.GetCookieManagerCache().getCookieManager();
                String orderUrl = equementDetail.getOrderUrl();
                WebClient webClient = new WebClient(BrowserVersion.CHROME);
                webClient.setCookieManager(cookieManager);
                webClient.getOptions().setCssEnabled(false);
                webClient.getOptions().setJavaScriptEnabled(false);
                webClient.getOptions().setRedirectEnabled(false);
                webClient.getOptions().setThrowExceptionOnScriptError(false);

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

                String orderJs = orderCaledar(
                        webClient,
                        browserTableUrlSb.toString(),
                        "仪器使用预约",
                        startTime,
                        endTime,
                        calendarTable.getId(),
                        description,
                        relationProject,
                        captchaResult
                );

                LOG.info("======> Execute JavaScript Code ");
                JavascriptExecutor executor = CookieManagerCache.GetCookieManagerCache().getJavascriptExecutor();
                Object result = executor.executeScript(orderJs);
                Thread.sleep(5000);
                webClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String orderCaledar(WebClient webClient, String url, String name, long startTs, long endTs, String caledarTableId, String desc, String project, String captcha) throws IOException {
            LOG.info("======> Get Order Dialog Code From "  + url);
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
            params.add(new NameValuePair("calendar_id", "#"+caledarTableId));
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
            String svgXml = ((JSONObject)JSON.parse(response.getContentAsString())).getString("data");

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder =  documentBuilderFactory.newDocumentBuilder();
            Document root = documentBuilder.parse(new ByteArrayInputStream(svgXml.getBytes()));
            NodeList nodes = root.getElementsByTagName("path");

            int captchaResult = 0;
            for(int i = 0; i < nodes.getLength(); i++){
                DeferredElementImpl pathNode = (DeferredElementImpl) nodes.item(i);
                String fillAttr =  pathNode.getAttribute("fill");
                if(!fillAttr.equals("none")) {
                    String dStr = pathNode.getAttribute("d").replaceAll("[0-9\\-\\.\\,\\s]*", "").toUpperCase();
                    int captchaNumber = CAPTCHA_NUMBERS_SHAPE.get(dStr);
                    if(captchaNumber >= 0)
                        captchaResult += captchaNumber;
                }
            }

            return String.valueOf(captchaResult);
        }

        private void covertSvgToPng(String svgString, String outputName) throws IOException, TranscoderException {
            LOG.info("Download SVG file and convert to PNG.");
            PNGTranscoder transcoder = new PNGTranscoder();
            transcoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, 150f);
            transcoder.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, 50f);
            FileOutputStream fileOutputStream = new FileOutputStream(new File(outputName));
            transcoder.transcode(
                    new TranscoderInput(new ByteArrayInputStream(svgString.getBytes())),
                    new TranscoderOutput(fileOutputStream)
            );
            fileOutputStream.flush();
            fileOutputStream.close();
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


