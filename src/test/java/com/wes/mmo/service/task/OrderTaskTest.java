package com.wes.mmo.service.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.wes.mmo.common.config.AppConfiguration;
import com.wes.mmo.common.config.Value;
import com.wes.mmo.common.cookie.CookieManagerCache;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.xerces.dom.DeferredAttrImpl;
import org.apache.xerces.dom.DeferredElementImpl;
import org.dom4j.dom.DOMAttributeNodeMap;
import org.junit.Test;
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
import java.util.ArrayList;
import java.util.List;

public class OrderTaskTest {

    @Test
    public void testGetSvg() throws IOException, ParserConfigurationException, SAXException, TransformerException, TranscoderException, TesseractException {
        AppConfiguration configuration = AppConfiguration.getConfiguration();
        configuration.addKey("username", new Value("zhangsen", "normal"));
        configuration.addKey("password", new Value("Zhangsen2019", "normal"));

        CookieManagerCache cookieManagerCache = CookieManagerCache.GetCookieManagerCache();

        WebClient webClient = cookieManagerCache.getWebClient();
        String svgUrl = "http://10.1.5.22/lims/!eq_reserv/index";
        WebRequest request = new WebRequest(new URL( "http://10.1.5.22/lims/!eq_reserv/index"));
        request.setHttpMethod(HttpMethod.POST);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new NameValuePair("_ajax", "1"));
        params.add(new NameValuePair("_object", "get_captcha"));
        params.add(new NameValuePair("_event", "click"));
        params.add(new NameValuePair("cal_week_rel", "#calweek_61998031943fa"));
        request.setRequestParameters(params);
        Page page = webClient.getPage(request);
        WebResponse response = page.getWebResponse();
        System.out.println(page.getWebResponse().getContentAsString());

        String svgXml = ((JSONObject)JSON.parse(response.getContentAsString())).getString("data");
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder =  documentBuilderFactory.newDocumentBuilder();
        Document root = documentBuilder.parse(new ByteArrayInputStream(svgXml.getBytes()));
        NodeList nodes = root.getElementsByTagName("path");
        System.out.println(nodes.getLength());
        List<Node> deleteNodes = new ArrayList<>();
        for(int i = 0; i < nodes.getLength(); i++){
            DeferredElementImpl pathNode = (DeferredElementImpl) nodes.item(i);
            System.out.println("========> ");
            NamedNodeMap map = pathNode.getAttributes();
            System.out.println("fill is " + map.getNamedItem("fill").getNodeValue());
            System.out.println("d is " + map.getNamedItem("d").getNodeValue());
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

        System.out.println(deleteNodes.size());
        for(Node node : deleteNodes) {
            node.getParentNode().removeChild(node);
        }

        StringWriter writer = new StringWriter();
        Transformer trasformer = TransformerFactory.newInstance().newTransformer();
        trasformer.transform(new DOMSource(root),new StreamResult(writer));
        String newDoc = writer.toString();
        System.out.println(newDoc);

        PNGTranscoder transcoder = new PNGTranscoder();
        transcoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, 150f);
        transcoder.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, 50f);
        FileOutputStream fileOutputStream = new FileOutputStream(new File("D:\\Product\\MMO\\tmp\\test_20211121.png"));
        transcoder.transcode(
                new TranscoderInput(new ByteArrayInputStream(newDoc.getBytes())),
                new TranscoderOutput(fileOutputStream)
        );
        fileOutputStream.flush();

        // testOCR();
    }

    @Test
    public void testOCR() throws TesseractException {
        File imageFile = new File("D:\\Product\\MMO\\tmp\\test_20211121.png");
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("D:\\Product\\tessdata");
        tesseract.setLanguage("srp");
        String str = tesseract.doOCR(imageFile);
        System.out.println(str);
    }

}