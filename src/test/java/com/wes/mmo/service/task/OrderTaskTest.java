package com.wes.mmo.service.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.wes.mmo.common.config.AppConfiguration;
import com.wes.mmo.common.config.Value;
import com.wes.mmo.common.cookie.CookieManagerCache;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderTaskTest {

    @Test
    public void testGetSvg() throws IOException, ParserConfigurationException, SAXException, TransformerException, TranscoderException {
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
    public void testJavaScripts() {
        String jsonData = " {\"dialog\":{\"title\":\"\\u9884\\u7ea6\\u7b49\\u5f85\",\"data\":\"<div class=\\\"padding_1 middle center calendar_wait\\\" id=\\\"uuid_619fae113cb54\\\">\\n  <img src=\\\"cache\\/124962fba313dbe70ac1884b23b3b56c.png\\\" \\/>\\n  <div class=\\\"hr_2\\\"><\\/div>\\n  <h1 class=\\\"middle center title\\\">\\u8bf7 \\u7a0d \\u540e<\\/h1>\\n  <p class=\\\"content center\\\">\\u6b64\\u65f6\\u6bb5\\u8fd8\\u6709\\u5176\\u4ed6\\u7528\\u6237\\u6b63\\u5728\\u9884\\u7ea6<\\/p>\\n  <p class=\\\"content center\\\">\\u9884\\u7ea6\\u7ed3\\u679c\\u9a6c\\u4e0a\\u63ed\\u6653<\\/p>\\n  <h1 class=\\\"middle center title\\\">. . .<\\/h1>\\n<\\/div>\\n\\n<script>\\n(function($){\\n  var config = {\\n    path: '\\/socket.iov2',\\n    autoConnect: false,\\n    forceNew: true,\\n    query: {\\n      userId: '515',\\n      userName: '\\u5f20\\u68ee',\\n      ticket: 'D+ZdpH4yK+709kmLDXs6OFbwGt9XW5vlyb6a+LwZE1ROlw7L+pYS4UI64rIasruMnydtlvbrFBQeUa17m5Z62AhFs9pUs+7oRSvWnPnB+0G3FGANTGO2tV4n3Rh2sSqXsIYXYX7f2FsyfONavPY5Y3GOlZlVmfELElX6TJdqNCU=',\\n      ticketId: 'e7cda8229e646b97aa5bfd75ef8e4a85',\\n    }\\n  };\\n\\n  \\n  var socket = io('', config);\\n  var uuid = 'uuid_619fae113cb54';\\n  var url = 'http:\\/\\/10.1.5.22\\/lims\\/!calendars\\/calendar';\\n  var _Failed = function(msg) {\\n    Q.trigger({\\n      url: url, \\n      data: {\\n          errorMsg: msg,\\n          uuid: uuid,\\n          parentId: '8',\\n          parentName: 'equipment'\\n      }, \\n      object: 'reservComponentFailed', \\n      event: 'click', \\n      global: false\\n    })\\n  };\\n  var _Success = function(data) {\\n    Q.trigger({\\n        url: url, \\n        data: data, \\n        object: 'refreshComponent', \\n        event: 'click', \\n        global: false, \\n        success: function(){\\n            Dialog.close()\\n        }\\n    })\\n  };\\n\\n  socket\\n  .connect()\\n  .on('yiqikong-reserv-reback', function (rep) {\\n    socket.disconnect()\\n    if (rep.success) {\\n      rep.form = '{\\\"cal_week_rel\\\":\\\"#calweek_619fae103b9df\\\",\\\"mode\\\":\\\"week\\\",\\\"component_id\\\":\\\"0\\\",\\\"calendar_id\\\":\\\"29\\\",\\\"name\\\":\\\"??????\\\",\\\"dtstart\\\":\\\"1638212400\\\",\\\"dtend\\\":1638215999,\\\"description\\\":\\\"\\\",\\\"project\\\":\\\"0\\\",\\\"captcha\\\":\\\"11\\\",\\\"submit\\\":\\\"save\\\",\\\"browser_id\\\":\\\"browser_619fae0fb1251\\\",\\\"st\\\":\\\"1637424000\\\",\\\"ed\\\":\\\"1638028800\\\",\\\"equipment_id\\\":\\\"8\\\",\\\"form_token\\\":\\\"eq_reserv_619fae0fb12540.49904379\\\",\\\"id\\\":\\\"29\\\",\\\"currentUserId\\\":\\\"515\\\",\\\"ticketId\\\":\\\"e7cda8229e646b97aa5bfd75ef8e4a85\\\",\\\"SITE_ID\\\":\\\"cf-lite\\\",\\\"LAB_ID\\\":\\\"chinablood\\\",\\\"tube\\\":\\\"023dbcc7a3f7f2651f14826a77d9846fd3668402\\\",\\\"uuid\\\":\\\"uuid_619fae113cb54\\\"}'\\n      _Success(rep)\\n    }\\n    else {\\n      _Failed(rep.error_msg)\\n    }\\n  })\\n  .on('connect', function(msg) {\\n      socket.emit('yiqikong-reserv', { form: \\\"{\\\\\\\"cal_week_rel\\\\\\\":\\\\\\\"#calweek_619fae103b9df\\\\\\\",\\\\\\\"mode\\\\\\\":\\\\\\\"week\\\\\\\",\\\\\\\"component_id\\\\\\\":\\\\\\\"0\\\\\\\",\\\\\\\"calendar_id\\\\\\\":\\\\\\\"29\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"??????\\\\\\\",\\\\\\\"dtstart\\\\\\\":\\\\\\\"1638212400\\\\\\\",\\\\\\\"dtend\\\\\\\":1638215999,\\\\\\\"description\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"project\\\\\\\":\\\\\\\"0\\\\\\\",\\\\\\\"captcha\\\\\\\":\\\\\\\"11\\\\\\\",\\\\\\\"submit\\\\\\\":\\\\\\\"save\\\\\\\",\\\\\\\"browser_id\\\\\\\":\\\\\\\"browser_619fae0fb1251\\\\\\\",\\\\\\\"st\\\\\\\":\\\\\\\"1637424000\\\\\\\",\\\\\\\"ed\\\\\\\":\\\\\\\"1638028800\\\\\\\",\\\\\\\"equipment_id\\\\\\\":\\\\\\\"8\\\\\\\",\\\\\\\"form_token\\\\\\\":\\\\\\\"eq_reserv_619fae0fb12540.49904379\\\\\\\",\\\\\\\"id\\\\\\\":\\\\\\\"29\\\\\\\",\\\\\\\"currentUserId\\\\\\\":\\\\\\\"515\\\\\\\",\\\\\\\"ticketId\\\\\\\":\\\\\\\"e7cda8229e646b97aa5bfd75ef8e4a85\\\\\\\",\\\\\\\"SITE_ID\\\\\\\":\\\\\\\"cf-lite\\\\\\\",\\\\\\\"LAB_ID\\\\\\\":\\\\\\\"chinablood\\\\\\\",\\\\\\\"tube\\\\\\\":\\\\\\\"023dbcc7a3f7f2651f14826a77d9846fd3668402\\\\\\\",\\\\\\\"uuid\\\\\\\":\\\\\\\"uuid_619fae113cb54\\\\\\\"}\\\", ticket: \\\"D+ZdpH4yK+709kmLDXs6OFbwGt9XW5vlyb6a+LwZE1ROlw7L+pYS4UI64rIasruMnydtlvbrFBQeUa17m5Z62AhFs9pUs+7oRSvWnPnB+0G3FGANTGO2tV4n3Rh2sSqXsIYXYX7f2FsyfONavPY5Y3GOlZlVmfELElX6TJdqNCU=\\\" })\\n  })\\n  .on('connect_error', function(msg){\\n    this.disconnect()\\n    _Failed('\\u8fde\\u63a5\\u9884\\u7ea6\\u670d\\u52a1\\u5668\\u5931\\u8d25, \\u8bf7\\u7a0d\\u540e\\u518d\\u8bd5!')\\n  })\\n  .on('error', function(msg) {\\n    this.disconnect()\\n    _Failed(msg)\\n  });\\n})(jQuery)\\n<\\/script>\\n\"}}\n";
        String javaScript = JSON.parseObject(jsonData).getJSONObject("dialog").getString("data").trim();
        char[] chars = javaScript.toCharArray();
        System.out.println(chars[chars.length - 1]);

        String javaScriptRegex = "^(\\<div[\\s\\S]*\\<\\/div\\>)[\\s]*(\\<script\\>([\\s]+\\(function\\(\\$\\)\\{)([\\s\\S]*)(\\}\\)\\(jQuery\\)[\\s]+)\\<\\/script\\>)$";
        System.out.println(javaScript.matches(javaScriptRegex));
        Pattern pattern = Pattern.compile(javaScriptRegex);
        Matcher matcher = pattern.matcher(javaScript);
        System.out.println(matcher.matches());
        System.out.println(matcher.group(4));
        // System.out.println(matcher.sssgroup(1));
        // System.out.println(matcher.group(1));
        // System.out.println(matcher.group(1));

    }

    @Test
    public void testWebSocket(){
        AppConfiguration configuration = AppConfiguration.getConfiguration();
        configuration.addKey("username", new Value("zhangsen", "normal"));
        configuration.addKey("password", new Value("Zhangsen2019", "normal"));

        CookieManagerCache cookieManagerCache = CookieManagerCache.GetCookieManagerCache();
        WebClient webClient = cookieManagerCache.getWebClient();

    }
}