package com.wes.mmo.service.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.sun.javafx.geom.BaseBounds;
import com.wes.mmo.common.config.AppConfiguration;
import com.wes.mmo.common.config.ConfigKey;
import com.wes.mmo.common.cookie.CookieManagerCache;
import com.wes.mmo.dao.EquementDetail;
import javafx.beans.property.SimpleStringProperty;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.io.input.TaggedReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.dom.DeferredElementImpl;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.internal.MouseAction;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderTaskV2 {

    private static final Log LOG = LogFactory.getLog(OrderTaskV2.class);

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
     private int threadNum = Integer.parseInt(configuration.getKey(ConfigKey.AppKey.ORDER_THREAD_NUM.getKey()).getValue());
//    private int threadNum = 1;
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(threadNum);

    public OrderTaskV2(EquementDetail equementDetail, long startTime, long endTime, String description, String relationProject, long actionTime) {
        this.equementDetail = equementDetail;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.relationProject = relationProject;
        this.actionTime = actionTime;

        //
        this.equement = new SimpleStringProperty(equementDetail.getName());
        this.start = new SimpleStringProperty(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(startTime*1000)));
        this.end = new SimpleStringProperty(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(endTime*1000)));
        this.status = new SimpleStringProperty("RUNNING");
        this.action = new SimpleStringProperty("STOP");
        this.id = new SimpleStringProperty(String.valueOf(System.currentTimeMillis()/1000));
    }

    public void run() {
        long actionTs = actionTime * 1000;
        LOG.info("======> Run Thread " + threadNum);
        for(int i = 0; i < threadNum; i++){
            EquementOrderThread eot = new EquementOrderThread();
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

    public class EquementOrderThread extends Thread {

        @Override
        public void run() {
            try {
                CookieManager cookieManager = CookieManagerCache.GetCookieManagerCache().getCookieManager();
                String orderUrl = equementDetail.getOrderUrl();

                System.setProperty("webdriver.firefox.bin","D:\\Firefox\\firefox.exe");
                System.setProperty("webdriver.gecko.driver", "D:\\MMO\\webdriver\\geckodriver.exe");
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.setHeadless(true);
                WebDriver webDriver = new FirefoxDriver(firefoxOptions);

                WebDriver.Window webWindow = webDriver.manage().window();
                webWindow.maximize();
                webDriver.get(orderUrl);


                // 添加COOKIE
                System.out.println(cookieManager.getCookies().size());
                for(com.gargoylesoftware.htmlunit.util.Cookie cookie : cookieManager.getCookies()){
                    webDriver.manage().addCookie(new Cookie(cookie.getName(),cookie.getValue(), cookie.getDomain(),cookie.getPath(),cookie.getExpires()));
                }

                webDriver.get(orderUrl);
                Thread.sleep(1000);

                Actions actions = new Actions(webDriver);
                actions.sendKeys(Keys.CONTROL).perform() ;
                actions.sendKeys(Keys.LEFT_SHIFT).perform() ;
                actions.sendKeys(Keys.chord()).perform() ;

                // 点击下一页
                WebElement browser = webDriver.findElement(By.className("browser_wrapper"));
                new WebDriverWait(webDriver, Duration.ofSeconds(10, 0))
                        .ignoring(StaleElementReferenceException.class)
                        .until((WebDriver driver) -> {
                            return browser.findElements(By.tagName("table")).size() > 1;
                        });
                WebElement header = webDriver.findElement(By.className("browser_wrapper")).findElements(By.tagName("table")).get(1).findElement(By.tagName("thead"));
                WebElement buttonContainer = header.findElement(By.className("float_right"));
                List<WebElement> buttons =  buttonContainer.findElements(By.tagName("a"));

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                String[] timeStrs = header.findElement(By.tagName("h1")).getText().split("\\-");
                long tableStartTime = sdf.parse(timeStrs[0].trim()).getTime();
                long tableEndTime = sdf.parse(timeStrs[1].trim()).getTime();

                System.out.print(System.currentTimeMillis() - tableStartTime);
//                if(startTime*1000 >= tableStartTime && (System.currentTimeMillis() - tableStartTime) > 1000*3600*24){
                    actions.moveToElement(buttons.get(buttons.size() - 1)).perform();
                    actions.click(buttons.get(buttons.size() - 1)).build().perform();
//                }

                WebElement caledarBrowser  = webDriver.findElement(By.className("browser_wrapper"));

                // 获取Table
                new WebDriverWait(webDriver, Duration.ofSeconds(10, 0))
                        .ignoring(StaleElementReferenceException.class)
                        .until((WebDriver driver) -> {
                            return caledarBrowser.findElements(By.tagName("table")).get(1).isEnabled();
                        });
                WebElement calendarTable = caledarBrowser.findElements(By.tagName("table")).get(1);

                Thread.sleep(2000);

                new WebDriverWait(webDriver, Duration.ofSeconds(10, 0))
                        .ignoring(StaleElementReferenceException.class)
                        .until((WebDriver driver) -> {
                            return calendarTable.findElements(By.tagName("tbody")).size() == 3
                                    && calendarTable.findElements(By.tagName("tbody")).get(2).isEnabled();
                        });
                WebElement calendarTableBody = calendarTable.findElements(By.tagName("tbody")).get(2);

                new WebDriverWait(webDriver, Duration.ofSeconds(10, 0))
                        .ignoring(StaleElementReferenceException.class)
                        .until((WebDriver driver) -> {
                            return ExpectedConditions.elementToBeClickable(calendarTableBody.findElements(By.tagName("tr")).get(10).findElements(By.tagName("td")).get(7));
                        });
                WebElement calendarTableCell = calendarTableBody.findElements(By.tagName("tr")).get(10).findElements(By.tagName("td")).get(7);
                actions.moveToElement(calendarTableCell).perform();
//                actions.click(calendarTableCell).build().perform();

                WebElement calendarBody = calendarTable.findElements(By.tagName("tbody")).get(1);
                WebElement calendarCell = calendarBody.findElements(By.tagName("tr")).get(0).findElements(By.tagName("td")).get(1);
                WebElement calendarContainer = calendarCell.findElements(By.tagName("div")).get(0);

                new WebDriverWait(webDriver, Duration.ofSeconds(20, 0))
                        .ignoring(StaleElementReferenceException.class)
                        .until((WebDriver driver) -> {
                            return ExpectedConditions.numberOfElementsToBe(By.className("block_hover"), 1).apply(webDriver);
                        });
                WebElement calendarDiv = calendarContainer.findElements(By.className("block_hover")).get(0);
                actions.moveToElement(calendarDiv).perform();
                actions.doubleClick(calendarDiv).build().perform();

                // 获取弹窗
                new WebDriverWait(webDriver, Duration.ofSeconds(20, 0))
                        .ignoring(StaleElementReferenceException.class)
                        .until((WebDriver driver) -> {
                            return webDriver.findElement(By.className("dialog")).isEnabled();
                        });
                WebElement dialogDiv = webDriver.findElement(By.className("dialog")).findElement(By.tagName("table"));
                ((JavascriptExecutor) webDriver).executeScript("arguments[0].setAttribute('value','"+ startTime +"')", dialogDiv.findElement(By.name("dtstart")));
                ((JavascriptExecutor) webDriver).executeScript("arguments[0].setAttribute('value','"+ endTime +"')", dialogDiv.findElement(By.name("dtend")));

                // 获取SVG
                new WebDriverWait(webDriver, Duration.ofSeconds(20, 0))
                        .ignoring(StaleElementReferenceException.class)
                        .until((WebDriver driver) -> {
                            return dialogDiv.findElement(By.tagName("svg")).isEnabled();
                        });
                WebElement svg = dialogDiv.findElement(By.tagName("svg"));


                String captchaResult = computeCaptchaV2(webDriver, svg);
                dialogDiv.findElement(By.name("captcha")).sendKeys(captchaResult);

                new WebDriverWait(webDriver, Duration.ofSeconds(10, 0))
                        .ignoring(StaleElementReferenceException.class)
                        .until((WebDriver driver) -> {
                            return ExpectedConditions.elementToBeClickable(dialogDiv.findElement(By.name("save")));
                        });
//                dialogDiv.findElement(By.name("save")).click();

//                Thread.sleep(10000);
//                webDriver.close();

//                HtmlForm dialogForm = (HtmlForm) dialogDiv.g("form").get(0);
//                HtmlTable dialogTable = (HtmlTable) dialogForm.getElementsByTagName("table").get(0);
//
//                HtmlTextInput firstTimeText = (HtmlTextInput) dialogTable.getRows().get(2).getElementsByTagName("td").get(1).getElementsByTagName("input").get(0);
//                firstTimeText.setNodeValue(String.valueOf(startTime));
//
//                HtmlTextInput endTimeText = (HtmlTextInput) dialogTable.getRows().get(3).getElementsByTagName("td").get(1).getElementsByTagName("input").get(0);
//                firstTimeText.setNodeValue(String.valueOf(endTime));
//
//                HtmlTextInput captchaText = (HtmlTextInput) dialogTable.getRows().get(4).getElementsByTagName("td").get(1).getChildNodes().get(1);
//                HtmlSvg captchaSvg = (HtmlSvg) dialogTable.getRows().get(4).getElementsByTagName("td").get(1).getChildNodes().get(3).getChildNodes().get(1);
//

//
//                // 点击
//                HtmlTextInput submitText = (HtmlTextInput) dialogTable.getLastChild().getFirstChild().getFirstChild().getFirstChild();
//                submitText.click();
//                webClient.waitForBackgroundJavaScript(2000);
//
//                String calendarOrderUrl = caledarBrowser.getAttribute("src");
////                HtmlPage calendarTablePage = webClient.getPage(calendarOrderUrl);
//                DomElement calendarTable = caledarBrowser.getElementsByTagName("table").get(1);
//
//
//
//                // 获取需要的参数
//                String svgResult = getSvgResult(webClient, calendarTable.getId());
//                String pngName = Thread.currentThread().getId() + "_" + System.currentTimeMillis()+".png";
//                String pngFilePath = configuration.getKey(ConfigKey.EnvKey.TMP_DIR.getKey()).getValue() + "\\" + pngName;
//                covertSvgToPng(svgResult, pngFilePath);
//                int result = getOcrReuslt(pngFilePath);
//
//                // 开始进行预定，并返回相应的代码
//                String orderJs = orderCaledar(
//                        webClient,
//                        calendarOrderUrl,
//                        "仪器使用预约",
//                        startTime,
//                        endTime,
//                        calendarTable.getId(),
//                        description,
//                        relationProject,
//                        result
//                );
//
//
//                ScriptResult scriptResult = orderPage.executeJavaScript("1+1;");
//                Thread.sleep(2000);
//                System.out.println(scriptResult.getJavaScriptResult().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String computeCaptcha(WebDriver webDriver, WebElement captchaSvg) throws IOException, TranscoderException, TesseractException {
            List<WebElement> deleteNodes = new ArrayList<>();

            for(WebElement path : captchaSvg.findElements(By.tagName("path"))) {
                if(path.getAttribute("fill").equals("none")) {
                            deleteNodes.add(path);
                }
                else {
                    ((JavascriptExecutor) webDriver).executeScript("arguments[0].setAttribute('fill', '#000000');", path);
                    ((JavascriptExecutor) webDriver).executeScript("arguments[0].setAttribute('fill-rule', 'evenodd');", path);
//                    ((JavascriptExecutor) webDriver).executeScript("arguments[0].setAttribute('stroke', '#000000');", path);
//                    ((JavascriptExecutor) webDriver).executeScript("arguments[0].setAttribute('stroke-width', '1');", path);
                }
            }
            for(WebElement node : deleteNodes) {
                // 进行删除
                ((JavascriptExecutor) webDriver).executeScript("arguments[0].remove();", node);
            }

            Object svgXml = ((JavascriptExecutor) webDriver).executeScript("return (new XMLSerializer()).serializeToString(arguments[0]);", captchaSvg);
            String pngName = Thread.currentThread().getId() + "_" + System.currentTimeMillis()+".png";
            String pngFilePath = configuration.getKey(ConfigKey.EnvKey.TMP_DIR.getKey()).getValue() + "\\" + pngName;
            covertSvgToPng(svgXml.toString(), pngFilePath);
            return String.valueOf(getOcrReuslt(pngFilePath));
        }

        private String computeCaptchaV2(WebDriver webDriver, WebElement captchaSvg){
            List<WebElement> deleteNodes = new ArrayList<>();
            int captchaResult = 0;
            for(WebElement path : captchaSvg.findElements(By.tagName("path"))) {
                if(!path.getAttribute("fill").equals("none")) {
                    String captchShape = path.getAttribute("d").replaceAll("[0-9\\-\\.\\,\\s]*", "").toUpperCase();
                    if(CAPTCHA_NUMBERS_SHAPE.containsKey(captchShape) && CAPTCHA_NUMBERS_SHAPE.get(captchShape) >= 0){
                        captchaResult += CAPTCHA_NUMBERS_SHAPE.get(captchShape);
                    }
                }
            }
            return String.valueOf(captchaResult);
        }

        private String orderCaledar(WebClient webClient, String url, String name, long startTs, long endTs, String caledarTableId, String desc, String project, int captcha) throws IOException {
            LOG.info("======> Order " + name + " Url " + url);
            LOG.info("======> Order " + caledarTableId + " Calendar from " + sdf.format(new Date(startTs*1000)) + " to " + sdf.format(new Date(endTs*1000)));

            String urlParamsStr = url.split("\\?", 2)[1];
            Map<String, String> paramsMap = new HashMap<>();
            for(String urlParamKvStr : urlParamsStr.split("&")){
                String[] paramKv = urlParamKvStr.split("=");
                paramsMap.put(paramKv[0], paramKv[1]);
            }
            LOG.info("======> Order Calendar Id " + paramsMap.get("calendar_id"));
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
            params.add(new NameValuePair("captcha", String.valueOf(captcha)));
            params.add(new NameValuePair("submit", "save"));
            request.setRequestParameters(params);
            UnexpectedPage page = webClient.getPage(request);
            String responseContent = page.getWebResponse().getContentAsString();
            // System.out.println(responseContent);
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
            String svgUrl = "http://10.1.5.22/lims/!eq_reserv/index";
            WebRequest request = new WebRequest(new URL(svgUrl));
            request.setHttpMethod(HttpMethod.POST);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new NameValuePair("_ajax", "1"));
            params.add(new NameValuePair("_object", "get_captcha"));
            params.add(new NameValuePair("_event", "click"));
            params.add(new NameValuePair("cal_week_rel", "#" + caledearTableId));
            request.setRequestParameters(params);
            // 获SVG数据
            WebResponse response = webClient.getPage(request).getWebResponse();
            String svgXml = ((JSONObject)JSON.parse(response.getContentAsString())).getString("data");

            // 初始化
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder =  documentBuilderFactory.newDocumentBuilder();
            Document root = documentBuilder.parse(new ByteArrayInputStream(svgXml.getBytes()));
            NodeList nodes = root.getElementsByTagName("path");

            List<Node> deleteNodes = new ArrayList<>();
            for(int i = 0; i < nodes.getLength(); i++){
                DeferredElementImpl pathNode = (DeferredElementImpl) nodes.item(i);
                NamedNodeMap map = pathNode.getAttributes();
                if(map.getNamedItem("fill").getNodeValue().equals("none")) {
                    deleteNodes.add(pathNode);
                }
                else {
                    pathNode.getAttributes().getNamedItem("fill").setNodeValue("#000000");
                    pathNode.setAttribute("fill-rule", "evenodd");
                    pathNode.setAttribute("stroke", "#000000");
                    pathNode.setAttribute("stroke-width", "1");
                }
            }
            for(Node node : deleteNodes) {
                node.getParentNode().removeChild(node);
            }

            StringWriter writer = new StringWriter();
            Transformer trasformer = TransformerFactory.newInstance().newTransformer();
            trasformer.transform(new DOMSource(root),new StreamResult(writer));
            return writer.toString();

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

        private int getOcrReuslt(String pngPath) throws TesseractException {
            LOG.info("Ocr Svg and compute result.");
            File imageFile = new File(pngPath);
            ITesseract tesseract = new Tesseract();
            String tessdataDir = configuration.getKey(ConfigKey.EnvKey.TESSDATA_DIR.getKey()).getValue();
            tesseract.setDatapath(tessdataDir);
            String tessLang = configuration.getKey(ConfigKey.EnvKey.TESS_LANG.getKey()).getValue();
            tesseract.setLanguage(tessLang);
            tesseract.setTessVariable("user_defined_dpi", "150");
            String resultStr = tesseract.doOCR(imageFile);
            char[] chars = resultStr.trim().toCharArray();
            LOG.info("======> Ocr Result " + resultStr);
            int result = 0;
            if(chars.length > 1){
                try{
                    result = Integer.parseInt(String.valueOf(chars[0])) + Integer.parseInt(String.valueOf(chars[chars.length - 1]));
                }
                catch (Exception e) {
                    LOG.info("Cannot Parse Integer String.");
                }
            }
            return result;
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


