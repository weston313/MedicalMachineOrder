package com.wes.mmo.service.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;
import com.wes.mmo.common.config.AppConfiguration;
import com.wes.mmo.common.config.Value;
import com.wes.mmo.dao.EquementDetail;
import com.wes.mmo.utils.Utils;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.EngineIOException;
import io.socket.engineio.client.Transport;
import io.socket.engineio.client.transports.Polling;
import io.socket.parseqs.ParseQS;
import io.socket.yeast.Yeast;
import okhttp3.*;
import org.apache.xerces.dom.DeferredElementImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.openqa.selenium.json.Json;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

public class OrderTaskV3Test {

    @Test
    public void testWebSocket(){
        IO.Options options = new IO.Options();
        options.forceNew = true;
//        options.transports = new String[]{Polling.NAME};
        options.timeout = 250000;
        options.path = "/socket.iov2/";
        options.reconnection=false;

        StringBuffer querySb = new StringBuffer();
        querySb.append("userId").append("515");
        querySb.append("&").append("userName").append("张森");
        querySb.append("&").append("ticket")
                .append("D+ZdpH4yK+709kmLDXs6OFbwGt9XW5vlyb6a+LwZE1ROlw7L+pYS4UI64rIasruMnydtlvbrFBQeUa17m5Z62AhFs9pUs+7oRSvWnPnB+0G3FGANTGO2tV4n3Rh2sSqXsIYXYX7f2FsyfONavPY5Y2cfb3gUJdKE03HEzhm/5Po=");
        querySb.append("&").append("ticketId").append("e7cda8229e646b97aa5bfd75ef8e4a85");

        options.query = querySb.toString();
        options.transportOptions = new HashMap<>();
        Transport.Options to = new Transport.Options();
        to.query = new HashMap<>();
        to.query.put("Cookie", "session_lims2_cf-lite_chinablood=lggnvrkr7b4oucq1meqi78dio0; io=cCvjK6QkOPw91O7mAAAq");
        options.transportOptions.put("Cookie", to);

        try {
            Socket socket = IO.socket(new URI("http://60.28.141.5:13628"), options);
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... objects) {
                    System.out.println("===== Connected");
                    System.out.println(objects.toString());
                    JSONObject formData = new JSONObject();
//                    socket.emit("yiqikong-reserv", new JSONObject().toJSONString());
                }
            }).on("yiqikong-reserv-reback", new Emitter.Listener() {
                        @Override
                        public void call(Object... objects) {
                            System.out.println("=====> Reback Message " + objects.length);
                            for(Object obj : objects) {
                                System.out.println(obj.toString());
                            }
                        }
            }).on(Manager.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... objects) {
                    System.out.println("======> Event Error " + objects[0]);
//                    EngineIOException exception = (EngineIOException) objects[0];
//                    System.out.println("======> Eorro Code " + exception.code);
//                    for(StackTraceElement stackTraceElement : exception.getStackTrace()){
//                        System.out.println(stackTraceElement);
//                    }
                }
            }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... objects) {
                    System.out.println("======> Connect Error " + objects[0]);
                    EngineIOException exception = (EngineIOException) objects[0];
                    System.out.println("======> " + exception.code);
                    for(StackTraceElement stackTraceElement : exception.getStackTrace()){
                        System.out.println(stackTraceElement);
                    }
                }
            })
            ;

            socket.connect();
            while(true){
                Thread.sleep(1000);
                if(socket.connected()){
                }
                else {
                }
            }
//        JSONObject object = new JSONObject();
//        String value = "{\"cal_week_rel\":\"#calweek_619d04baa3765\",\"component_id\":\"0\",\"calendar_id\":\"29\",\"name\":\"??????\",\"dtstart\":\"1638212400\",\"dtend\":1638215999,\"description\":\"\",\"project\":\"--\",\"captcha\":\"0\",\"submit\":\"save\",\"browser_id\":\"browser_619d04b77c910\",\"st\":\"1637424000\",\"ed\":\"1638028800\",\"equipment_id\":\"8\",\"form_token\":\"eq_reserv_619d04b77c9145.08465511\",\"id\":\"29\",\"currentUserId\":\"515\",\"ticketId\":\"e7cda8229e646b97aa5bfd75ef8e4a85\",\"mode\":\"week\",\"SITE_ID\":\"cf-lite\",\"LAB_ID\":\"chinablood\",\"tube\":\"023dbcc7a3f7f2651f14826a77d9846fd3668402\",\"uuid\":\"uuid_619d04bd8ba15\"}\", ticket: \"D+ZdpH4yK+709kmLDXs6OFbwGt9XW5vlyb6a+LwZE1ROlw7L+pYS4UI64rIasruMnydtlvbrFBQeUa17m5Z62AhFs9pUs+7oRSvWnPnB+0G3FGANTGO2tV4n3Rh2sSqXsIYXYX7f2FsyfONavPY5Y5TlQJy2WEM7vCUrr\\/bXV4E=\"";
//        object.put("form", value);
//        Emitter emitter = socket.emit("yiqikong-reserv", object);
//        System.out.println(emitter.toString());
//        socket.open();
        } catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testCatpha(){
        Map<String, Integer> numberPathMap = new HashMap<>();
        numberPathMap.put("MLLQLLQLLQLLQLLQLLQLLQLLQZMLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLZ", 1);
        numberPathMap.put("MLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLLQLLQLLQLLQLLQLLQLLQZMLLQLLLQLLQLLQLLQLLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLLQLLQLLQLLQLLQLLLQLLLQLLQZ", 2);
        numberPathMap.put("MLLQLLQLLQLLQLLLQLLQLLQLLQLLQLLQLLQLLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQZMLLQLLQLLQLLQLLQLLLLQLLQLLQLLQLLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLLLQLLQLLQLLQLLQLLQLLLLQLLQLLQLLQLLQLLQLLLLLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQZ", 3);
        numberPathMap.put("MLLQLLQLLQLLQZMLLQLLQLLQLLQLLQLLQLLQLLQLLQLLLQLLQLLQLLQZMLLQLLLQLLQLLQLLQLLQLLQLLLQLLLQLLLQLLQLLQLLQLLQLLQLLQLLLQLLQLLQLLQZMLLQLLQLLQLLQLLQZ", 4);
        numberPathMap.put("MLLQLLQLLQLLQLLQLLQLLQLLLLQLLQLLQLLQLLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQZMLLQLLQLLQLLQLLLLLQLLLQLLLQLLQLLQLLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLLLQLLQLLQLLQLLQZ", 5);
        numberPathMap.put("MLLQLLQLLQLLQLLQLLQLLQLLQLLQZMLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQZMLLQLLQLLQLLQLLQLLQLLQLLLLQLLQLLQLLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQZMLLQLLQLLQLLQLLQLLQLLQLLQZ", 6);
        numberPathMap.put("MLLQLLQLLLQLLQLLQLLQLLQLLLQLLQLLLQLLQLLQLLLQLLQLLQLLQLLQZMLLLQLLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLLLLQLLQLLQLLQLLQLLLQLLLQLLQLLQLLLQZ", 7);
        numberPathMap.put("MLLQLLQLLQLLQLLQLLQLLQLLQZMLLQLLQLLQLLQLLQLLQLLQZMLLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQZMLLLQLLQLLQLLQLLLLLQLLQLLQLLQLLLLQLLLQLLQLLQLLQLLQLLQLLQLLLQLLQLLQLLQLLQZMLLQLLQLLQLLQLLQLLQLLQLLQLLQZMLLQLLLQLLQLLQLLQLLQLLQZ", 8);
        numberPathMap.put("MLLLQLLQLLQLLQLLQLLQLLQLLQZMLLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQZMLLQLLQLLQLLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLLQLLQLLQLLQLLLQZMLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQLLQZ", 9);
        numberPathMap.put("MLLQLLQLLQLLQLLQLLQLLQLLQLLQLLLQLLQLLQLLQLLQLLQLLLQZMLLQLLQLLQLLLQLLQLLQLLQLLLQLLLQLLQLLQLLQLLQLLQLLQLLLQLLLQZ", -1);

        String numberPath =
                "M72.55 39.49L72.48 39.41L72.50 39.43Q71.84 39.42 71.16 39.46L71.25 39.55L71.09 39.39Q70.38 39.48 69.69 39.48L69.81 39.60L69.66 39.45Q70.13 36.03 70.13 32.60L70.31 32.79L70.26 32.74Q68.46 32.81 67.55 32.81L67.47 32.72L67.46 32.71Q66.60 32.70 64.77 32.62L64.78 32.63L64.79 32.64Q64.78 32.32 64.59 29.84L64.67 29.93L64.64 29.89Q67.18 30.42 70.15 30.42L70.09 30.36L70.23 30.50Q69.98 26.10 69.56 23.51L69.57 23.52L69.40 23.36Q70.20 23.46 70.99 23.46L71.10 23.57L72.76 23.59L72.74 23.57Q72.59 27.91 72.59 30.50L72.52 30.43L72.47 30.38Q74.75 30.49 77.95 30.15L77.84 30.04L77.84 30.04Q77.67 31.31 77.67 32.53L77.78 32.64L77.74 32.61Q77.48 32.61 76.68 32.65L76.60 32.57L76.70 32.67Q75.69 32.65 75.08 32.69L75.16 32.77L75.11 32.72Q75.12 32.72 72.45 32.72L72.51 32.78L72.48 36.17L72.48 36.18Q72.50 37.83 72.62 39.55ZM78.34 29.63L78.36 29.65L78.26 29.54Q76.38 29.87 74.51 29.95L74.50 29.93L74.54 29.98Q74.75 26.80 75.17 24.93L75.12 24.88L75.13 24.89Q74.44 24.96 73.03 25.12L73.02 25.11L73.24 23.07L73.18 23.02Q70.59 23.06 68.92 22.94L68.98 23.01L68.87 22.89Q69.60 26.18 69.79 30.06L69.69 29.96L69.67 29.94Q68.01 29.99 64.32 29.39L64.25 29.32L64.20 29.27Q64.58 30.56 64.58 33.19L64.42 33.03L66.10 33.19L65.98 33.06Q65.98 33.78 65.83 35.19L65.85 35.22L69.86 35.01L69.89 35.03Q69.50 38.03 69.20 39.86L69.25 39.91L69.29 39.96Q70.06 39.96 71.46 39.84L71.51 39.89L71.52 39.90Q71.47 40.54 71.44 41.87L71.38 41.81L71.35 41.78Q71.87 41.77 75.49 41.92L75.53 41.97L75.52 41.96Q74.58 38.84 74.35 34.89L74.36 34.90L74.39 34.92Q77.73 35.03 79.82 35.41L79.93 35.52L79.84 35.43Q79.74 34.57 79.74 33.61L79.57 33.45L79.66 31.63L79.65 31.63Q79.42 31.62 78.89 31.66L78.90 31.67L78.12 31.69L78.14 31.71Q78.13 30.90 78.25 29.53Z";
        String numberResult = numberPath.replaceAll("[0-9\\-\\.\\,\\s]*", "");
        System.out.println(numberPathMap.get(numberResult.toUpperCase()));
    }

    @Test
    public void testCalendar(){
        // 计算日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(1639621567000l));
        // 计算今天周几
        int orderDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 ;
        System.out.println(orderDayOfWeek);

        calendar.add(Calendar.DAY_OF_MONTH, -1 * orderDayOfWeek);
        System.out.println(sdf.format(calendar.getTime()));

        calendar.add(Calendar.DAY_OF_MONTH,  6 - orderDayOfWeek);
        System.out.println(sdf.format(calendar.getTime()));
    }

    @Test
    public void testOkHttp() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        builder.url(new URL("http://60.28.141.5:13628/lims/login"));
        Request request =  builder.build();
        Call call = client.newCall(request);
        Response response = call.execute();
        System.out.println(response.body().string());
    }

    @Test
    public void testOkHttpWebSocket() throws InterruptedException, MalformedURLException {
        okhttp3.HttpUrl.Builder httpUrlBuilder = HttpUrl.get(new URL("http://60.28.141.5:13628/socket.iov2/")).newBuilder();
        httpUrlBuilder.addEncodedQueryParameter("transport", "polling");
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
//        List<Protocol>
        clientBuilder.retryOnConnectionFailure(false)
                .readTimeout(Duration.ofSeconds(3))
                .connectTimeout(Duration.ofSeconds(3))
                .writeTimeout(Duration.ofSeconds(3));
        OkHttpClient client = clientBuilder.build();
        Request.Builder builder = new Request.Builder();
        builder.url(httpUrlBuilder.build());
        builder.header("tranport", "polling");
        Request request = builder.build();
        client.dispatcher().cancelAll();
        client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                System.out.println("======> Open WebSocket Connect");
                try {
                    System.out.println(response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                webSocket.send("Hello Word");
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                System.out.println(text);
            }

            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                System.out.println("======> Connect is Closed");
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                try {
                    System.out.println(t.toString());
                    System.out.println(response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });



        while(true){
            System.out.println("=====> Sleep 1 Second");
            Thread.sleep(1000);
        }

    }

    @Test
    public void testOrderTaskV3(){
        AppConfiguration configuration = AppConfiguration.getConfiguration();
        configuration.addKey("username", new Value("zhangsen", ""));
        configuration.addKey("password", new Value("Zhangsen2019", ""));

        EquementDetail equementDetail = new EquementDetail(
                 "8"
                ,"new AriaIII 流式细胞仪 (正常)"
                ,"http://60.28.141.5:13628/lims/!equipments/equipment/index.8.reserv"
                ,"霍莹莹"
                ,"重点实验室 1层技术平台"
                ,"付伟超, 梁昊岳, 于文颖, 流式值班维护帐号1"
        );

        new OrderTaskV3(
                equementDetail,
                1640030400,
                1640032199,
                "",
                "0",
                0
        ).execute();
    }

    @Test
    public void testYeast(){
//        long ts = System.currentTimeMillis();
//        String t = Yeast.yeast();
//         1639671576182
//        System.out.println(t);
        System.out.println(Yeast.decode("Nt4JD6q"));
        System.out.println(Yeast.decode("Nt488nb"));
    }

    @Test
    public void testOhHttpCatpcha() throws IOException, SAXException, ParserConfigurationException {

        String calendarTableId = "calweek_" + Utils.ConvertDecToHex(System.currentTimeMillis() * 1048).toLowerCase();

        // initlize url and request info
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://60.28.141.5:13628/lims/!eq_reserv/index").newBuilder();
        Request.Builder requestBuilder =  new Request.Builder();
        requestBuilder.addHeader("Cookie", "session_lims2_cf-lite_chinablood=dgu5jl1rmqrflrig1ch1v6cb73; io=Ys4ywt8gXfbdvwj6AASK")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
        ;

        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        formBodyBuilder.add("_ajax", "1")
                .add("_object", "get_captcha")
                .add("_event", "click")
                .add("cal_week_rel", "#" + calendarTableId);

        Request request = requestBuilder
                .url(urlBuilder.build())
                .post(formBodyBuilder.build())
                .build();

        // intlize request info
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        OkHttpClient client = clientBuilder.readTimeout(Duration.ofSeconds(20)).build();

        // send request and get response
        Response response = client.newCall(request).execute();
        ResponseBody responseBody = response.body();
        System.out.println(responseBody.string());

//        String svgXml =  ((JSONObject) JSON.parse(responseBody.string())).getString("data");
//
//        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
//        Document root = documentBuilder.parse(new ByteArrayInputStream(svgXml.getBytes()));
//        NodeList nodes = root.getElementsByTagName("path");
//
//        int captchaResult = 0;
//        for (int i = 0; i < nodes.getLength(); i++) {
//            DeferredElementImpl pathNode = (DeferredElementImpl) nodes.item(i);
//            String fillAttr = pathNode.getAttribute("fill");
//            if (!fillAttr.equals("none")) {
//                String dStr = pathNode.getAttribute("d").replaceAll("[0-9\\-\\.\\,\\s]*", "").toUpperCase();
//                int captchaNumber = OrderTaskV3.CAPTCHA_NUMBERS_SHAPE.get(dStr);
//                if (captchaNumber >= 0)
//                    captchaResult += captchaNumber;
//            }
//        }
    }
}