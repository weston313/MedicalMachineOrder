package com.wes.mmo.service.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.javascript.AbstractJavaScriptEngine;
import com.gargoylesoftware.htmlunit.javascript.host.canvas.ext.WEBGL_compressed_texture_s3tc;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.dom.DeferredElementImpl;
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
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderTask implements Task {

    private static final Log LOG = LogFactory.getLog(OrderTask.class);

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

    public OrderTask(EquementDetail equementDetail, long startTime, long endTime, String description, String relationProject, long actionTime) {
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

    @Override
    public void execute() {
        run();
    }

    private void run() {
        long actionTs = actionTime * 1000;
        LOG.info("======> Run Thread " + threadNum);
        for(int i = 0; i < threadNum; i++){
            EquementOrderThread eot = new EquementOrderThread();
            if(System.currentTimeMillis() > actionTs){
                executorService.execute(eot);
            }
            else {
                executorService.schedule(eot, System.currentTimeMillis() - actionTs, TimeUnit.MILLISECONDS);
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
                WebClient webClient = new WebClient(BrowserVersion.EDGE);
                webClient.setCookieManager(cookieManager);
                webClient.getOptions().setCssEnabled(false);
                webClient.getOptions().setJavaScriptEnabled(true);
                webClient.getOptions().setRedirectEnabled(true);
                webClient.getOptions().setThrowExceptionOnScriptError(false);
//                webClient.getOptions().setPrintContentOnFailingStatusCode(true);
                webClient.setAjaxController(new NicelyResynchronizingAjaxController());
                webClient.setCssErrorHandler(new SilentCssErrorHandler());
//                webClient.getOptions().setScreenWidth(1920);
//                webClient.getOptions().setScreenHeight(1080);


                HtmlPage orderPage = webClient.getPage(orderUrl);
                webClient.waitForBackgroundJavaScript(10000);


                // 获取中间页面
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

                System.out.println("======> " + caledarBrowser.getElementsByTagName("table").size());


                // 先点击一下表
                HtmlTable calendarTable = (HtmlTable) caledarBrowser.getElementsByTagName("table").get(1);
                HtmlTableBody calendarTableBody = (HtmlTableBody) calendarTable.getElementsByTagName("tbody").get(2);
                HtmlTableCell calendarTableCell = (HtmlTableCell) calendarTableBody.getElementsByTagName("tr").get(10).getElementsByTagName("td").get(7);
                calendarTableCell.mouseMove();

//                HtmlTableBody calendarBody = (HtmlTableBody) calendarTable.getElementsByTagName("tbody").get(1);
//                HtmlTableCell calendarCell = (HtmlTableCell) calendarBody.getElementsByTagName("tr").get(0).getElementsByTagName("td").get(1);
//                HtmlDivision calendarContainer = (HtmlDivision) calendarCell.getElementsByTagName("div").get(0);
//                DomNodeList<DomElement> tables = page1.getElementsByTagName("table");
//                HtmlTable mouseOverTable = null;
//                for(DomElement table : tables){
//                    if(table.getId().startsWith("calweek_"))
//                        mouseOverTable = (HtmlTable) table;
//                }

                HtmlTableBody calendarBody = (HtmlTableBody) calendarTable.getElementsByTagName("tbody").get(1);
                HtmlTableCell calendarCell = (HtmlTableCell) calendarBody.getElementsByTagName("tr").get(0).getElementsByTagName("td").get(1);
                HtmlDivision calendarContainer = (HtmlDivision) calendarCell.getElementsByTagName("div").get(0);
                System.out.println(calendarContainer.getChildNodes().size());
                HtmlDivision calendarDiv = null;
                for(DomElement domElement : calendarContainer.getChildElements()){
                    String classStr = domElement.getAttribute("class");
                    if(classStr.equals("block block_hover block_top block_bottom")
                            || classStr.equals("block block_rect block_top block_bottom")
                            || classStr.equals("block block_default block_fixed block_top block_bottom")) {
                        System.out.println("Find It");
                        calendarDiv = (HtmlDivision) domElement;
//                        calendarDiv.setAttribute("class","block block_rect block_top block_bottom");
//                        calendarDiv.setAttribute("style", "overflow: hidden; z-index: 71; display: block;");
//                        calendarDiv.setAttribute("click", "liveHandler(this);");
                    }

                }
//                calendarDiv.mouseOver();
//                Thread.sleep(1000);


                calendarDiv.mouseOver();
                webClient.waitForBackgroundJavaScript(1000);

                calendarDiv.setAttribute("style", "display: block; z-index: 49; left: 1408.69px; top: 249px; width: 235px; height: 26px;");
                calendarDiv.dblClick();
//                calendarTableCell.click();
                webClient.waitForBackgroundJavaScript(1000);

//                System.out.println(orderPage.asXml());

                DomNodeList<HtmlElement> allDivs = orderPage.getBody().getElementsByTagName("div");
                HtmlDivision dialogDiv = null;
                for(HtmlElement element : allDivs) {
                    if(element.getAttribute("class").equals("dialog")) {
                        dialogDiv = (HtmlDivision) element;
                    }
                }

                HtmlForm dialogForm = (HtmlForm) dialogDiv.getElementsByTagName("form").get(0);
                HtmlTable dialogTable = (HtmlTable) dialogForm.getElementsByTagName("table").get(0);

                HtmlTextInput firstTimeText = (HtmlTextInput) dialogTable.getRows().get(2).getElementsByTagName("td").get(1).getElementsByTagName("input").get(0);
                firstTimeText.setNodeValue(String.valueOf(startTime));

                HtmlTextInput endTimeText = (HtmlTextInput) dialogTable.getRows().get(3).getElementsByTagName("td").get(1).getElementsByTagName("input").get(0);
                firstTimeText.setNodeValue(String.valueOf(endTime));

                HtmlTextInput captchaText = (HtmlTextInput) dialogTable.getRows().get(4).getElementsByTagName("td").get(1).getChildNodes().get(1);
                HtmlSvg captchaSvg = (HtmlSvg) dialogTable.getRows().get(4).getElementsByTagName("td").get(1).getChildNodes().get(3).getChildNodes().get(1);

                List<HtmlElement> deleteNodes = new ArrayList<>();

                for(HtmlElement path : captchaSvg.getElementsByTagName("path")){

                        if(path.getAttribute("fill").equals("none")) {
                            deleteNodes.add(path);
                        }
                        else {
                            path.setAttribute("fill", "#000000");
                            path.setAttribute("fill-rule", "evenodd");
                            path.setAttribute("stroke", "#000000");
                            path.setAttribute("stroke-width", "1");
                        }
                }
                for(Node node : deleteNodes) {
                    node.getParentNode().removeChild(node);
                }
                StringWriter writer = new StringWriter();
                Transformer trasformer = TransformerFactory.newInstance().newTransformer();
                trasformer.transform(new DOMSource(captchaSvg),new StreamResult(writer));
                String svgResult = writer.toString();
                String pngName = Thread.currentThread().getId() + "_" + System.currentTimeMillis()+".png";
                String pngFilePath = configuration.getKey(ConfigKey.EnvKey.TMP_DIR.getKey()).getValue() + "\\" + pngName;
                covertSvgToPng(svgResult, pngFilePath);
                captchaText.setNodeValue(String.valueOf(getOcrReuslt(pngFilePath)));

                // 点击
                HtmlTextInput submitText = (HtmlTextInput) dialogTable.getLastChild().getFirstChild().getFirstChild().getFirstChild();
                submitText.click();
                webClient.waitForBackgroundJavaScript(2000);

                String calendarOrderUrl = caledarBrowser.getAttribute("src");
//                HtmlPage calendarTablePage = webClient.getPage(calendarOrderUrl);
//                DomElement calendarTable = caledarBrowser.getElementsByTagName("table").get(1);



                // 获取需要的参数
//                String svgResult = getSvgResult(webClient, calendarTable.getId());
//                String pngName = Thread.currentThread().getId() + "_" + System.currentTimeMillis()+".png";
//                String pngFilePath = configuration.getKey(ConfigKey.EnvKey.TMP_DIR.getKey()).getValue() + "\\" + pngName;
//                covertSvgToPng(svgResult, pngFilePath);
                int result = 0;

                // 开始进行预定，并返回相应的代码
                String orderJs = orderCaledar(
                        webClient,
                        calendarOrderUrl,
                        "仪器使用预约",
                        startTime,
                        endTime,
                        calendarTable.getId(),
                        description,
                        relationProject,
                        result
                );

                System.out.println(orderJs);
//
//
//                ScriptResult scriptResult = orderPage.executeJavaScript("1+1;");
//                Thread.sleep(2000);
//                System.out.println(scriptResult.getJavaScriptResult().toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
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


