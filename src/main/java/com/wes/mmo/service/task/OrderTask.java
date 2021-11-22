package com.wes.mmo.service.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.wes.mmo.application.window.MainWindow;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OrderTask {

    private static final Log LOG = LogFactory.getLog(OrderTask.class);

    private SimpleStringProperty id ;
    private SimpleStringProperty equement = null;
    private SimpleStringProperty start = null;
    private SimpleStringProperty end = null;
    private SimpleStringProperty status = null;
    private SimpleStringProperty action = null;

    private AppConfiguration configuration = AppConfiguration.getConfiguration();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
    private EquementDetail equementDetail;
    private long startTime;
    private long endTime;
    private String description;
    private String relationProject;
    private WebClient webClient = CookieManagerCache.GetCookieManagerCache().getWebClient();
    private long actionTime;
    private int threadNum = Integer.parseInt(configuration.getKey(ConfigKey.AppKey.ORDER_THREAD_NUM.getKey()).getValue());
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
        this.status = new SimpleStringProperty("预定中");
        this.action = new SimpleStringProperty("");
        this.id = new SimpleStringProperty(String.valueOf(System.currentTimeMillis()/1000));
    }

    public void run() {
        long actionTs = actionTime * 1000;
        for(int i = 0; i < threadNum; i++){
            EquementOrderThread eot = new EquementOrderThread();
            if(System.currentTimeMillis() > actionTs){
                executorService.execute(eot);
            }
            else {
                executorService.schedule(eot, System.currentTimeMillis() - actionTs, TimeUnit.MILLISECONDS);
            }
            eot.start();
        }
    }

    public void stop(){
        executorService.shutdown();
    }

    public class EquementOrderThread extends Thread {

        @Override
        public void run() {
            try {
                String orderUrl = equementDetail.getOrderUrl();
                HtmlPage orderPage = webClient.getPage(orderUrl);

                // 获取中间页面
                DomNodeList<DomElement> divs = orderPage.getElementsByTagName("div");
                DomElement caledarBrowser = null;
                for(DomElement element : divs) {
                    if(element.getAttribute("class").equals("browser_wrapper")) {
                        caledarBrowser = element;
                        break;
                    }
                }
                String calendarOrderUrl = caledarBrowser.getAttribute("src");

                HtmlPage calendarTablePage = webClient.getPage(calendarOrderUrl);
                DomElement calendarTable = calendarTablePage.getElementsByTagName("table").get(0);

                // 获取需要的参数
                String svgResult = getSvgResult(calendarTable.getId());
                String pngName = Thread.currentThread().getId() + "_" + System.currentTimeMillis()+".png";
                String pngFilePath = configuration.getKey(ConfigKey.EnvKey.TMP_DIR.getKey()).getValue() + "\\" + pngName;
                covertSvgToPng(svgResult, pngFilePath);
                int result = getOcrReuslt(pngFilePath);

                // 开始进行预定
                orderCaledar(calendarOrderUrl,
                        "仪器使用预约",
                        startTime,
                        endTime,
                        caledarBrowser.getId(),
                        calendarTable.getId(),
                        description,
                        relationProject, result
                );
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (TransformerException e) {
                e.printStackTrace();
            } catch (TranscoderException e) {
                e.printStackTrace();
            } catch (TesseractException e) {
                e.printStackTrace();
            }
        }

        private void orderCaledar(String url, String name, long startTs, long endTs, String calendarId, String caledarTableId, String desc, String project, int captcha) throws IOException {
            WebRequest request = new WebRequest(new URL(url));
            request.setHttpMethod(HttpMethod.POST);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new NameValuePair("_ajax", "1"));
            params.add(new NameValuePair("_object", "component_form"));
            params.add(new NameValuePair("_event", "submit"));
            params.add(new NameValuePair("cal_week_rel", "#" + caledarTableId));
            params.add(new NameValuePair("component_id", "0"));
            params.add(new NameValuePair("calendar_id", calendarId));
            params.add(new NameValuePair("name", name));
            params.add(new NameValuePair("dtstart", String.valueOf(startTs)));
            params.add(new NameValuePair("dtend", String.valueOf(endTs)));
            params.add(new NameValuePair("description", desc));
            params.add(new NameValuePair("project", project));
            params.add(new NameValuePair("captcha", String.valueOf(captcha)));
            params.add(new NameValuePair("submit", "save"));
            request.setRequestParameters(params);
            String responseContent = webClient.getPage(request).getWebResponse().getContentAsString();
            JSONObject resultObject= (JSONObject) JSON.parse(responseContent);
            System.out.println(responseContent);
        }

        private String getSvgResult(String caledearTableId) throws IOException, ParserConfigurationException, SAXException, TransformerException, TranscoderException {
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
                    e.printStackTrace();
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


