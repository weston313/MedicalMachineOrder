package com.wes.mmo.application.window;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.wes.mmo.common.config.AppConfiguration;
import com.wes.mmo.common.config.ConfigKey;
import com.wes.mmo.common.cookie.CookieManagerCache;
import com.wes.mmo.dao.EquementDetail;
import com.wes.mmo.service.task.OrderTask;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainWindow  {

    public static final Log LOG = LogFactory.getLog(MainWindow.class);

    private static volatile MainWindow INSTANCE = null;

    public static MainWindow GetInstance(){
        if (INSTANCE == null) {
            synchronized (MainWindow.class) {
                if(INSTANCE == null)
                    INSTANCE = new MainWindow();
            }
        }
        return INSTANCE;
    }

    private Stage mainStage = null;
    private Tab followTab = null;
    private TableView followTable = null;
    private Tab orderTab = null;
    private TableView orderTable = null;

    private AppConfiguration configuration = AppConfiguration.getConfiguration();
    private Map<String, EquementDetail> EquementDetailMap = new HashMap<>();

    private MainWindow() {

    }

    private String tableBodyName = "tbody";

    public void initlize() throws IOException {
        mainStage = new Stage();
        mainStage.setTitle(configuration.getKey(ConfigKey.EnvKey.APP_NAME.getKey()).getValue());
        mainStage.setWidth(900);
        mainStage.setHeight(600);
        VBox mainPane = FXMLLoader.load(getClass().getResource("/fxml/main/index.fxml"));
        Scene mainScene = new Scene(mainPane);
        mainStage.setScene(mainScene);
        mainStage.show();

        // 将整个结构拿出来
        AnchorPane tablePane = (AnchorPane) mainPane.getChildren().get(1);
        TabPane tabPane = (TabPane) tablePane.getChildren().get(0);
        List<Tab> tabs = tabPane.getTabs();
        followTab = tabs.get(0);
        followTable = (TableView) ((AnchorPane) followTab.getContent()).getChildren().get(0);
        initFollowTableColumns(followTable);


        orderTab = tabs.get(1);
        orderTable = (TableView) ((AnchorPane) orderTab.getContent()).getChildren().get(0);
        initOrderTaskTab(orderTable);


        // 初始化FOLLOW数据
        CookieManagerCache cookieManagerCache = CookieManagerCache.GetCookieManagerCache();
        WebClient webClient = cookieManagerCache.getWebClient();
        String followUrl = cookieManagerCache.getIndexUrl().toString() + ".follow";
        HtmlPage followPage = webClient.getPage(followUrl);
        webClient.waitForBackgroundJavaScript(100);

        //
        HtmlTable htmlFollowTable = (HtmlTable) followPage.getElementById("table_equipments_follow_equipments");
        HtmlTableBody tableBody = htmlFollowTable.getBodies().get(0);
        for(HtmlTableRow row : tableBody.getRows()) {
            HtmlTableCell nameCell = row.getCell(1);
            DomNodeList<HtmlElement>  nameDoms = nameCell.getElementsByTagName("a");
            String name = nameDoms.get(0).getTextContent().trim();
            String indexUrl = nameDoms.get(0).getAttribute("href");
            String id = parseId(indexUrl);
            String nowUser = row.getCell(3).getTextContent();
            String address = row.getCell(4).getTextContent();
            String contacts = row.getCell(5).getTextContent();
            EquementDetail EquementDetail = new EquementDetail(id, name, indexUrl, nowUser, address, contacts);
            EquementDetailMap.put(id, EquementDetail);
            followTable.getItems().add(EquementDetail);
        }
    }

    private void initFollowTableColumns(TableView followTable) {
        List<TableColumn> columns = followTable.getColumns();

        for(TableColumn column : columns){
            column.setStyle("-fx-alignment: CENTER-LEFT;");
        }


        columns.get(0).setCellValueFactory(new PropertyValueFactory<EquementDetail, String>("name"));
        columns.get(1).setCellValueFactory(new PropertyValueFactory<EquementDetail, String>("id"));
        columns.get(2).setCellValueFactory(new PropertyValueFactory<EquementDetail, String>("nowUser"));
        columns.get(3).setCellValueFactory(new PropertyValueFactory<EquementDetail, String>("address"));
        columns.get(4).setCellValueFactory(new PropertyValueFactory<EquementDetail, String>("contacts"));
        // 增加按钮 ,
        columns.get(5).setCellValueFactory(new PropertyValueFactory<EquementDetail, String>("order"));
        columns.get(5).setCellFactory(new Callback<TableColumn<EquementDetail, String>, TableCell<EquementDetail, String>>() {
            @Override
            public TableCell call(final TableColumn<EquementDetail, String> param) {
                final TableCell<EquementDetail, String> cell = new TableCell<EquementDetail, String>() {

                    final Button btn = new Button("Order");

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        btn.setOnAction(event -> {
                            EquementDetail equementDetail = getTableView().getItems().get(getIndex());
                            LOG.info("Order Equemenet " + equementDetail.getName());
                            try {
                                new OrderWindow(orderTable, equementDetail).initlize();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                        if(!empty){
                            setGraphic(btn);
                            setText(null);
                        }

                    }
                };
                return cell;
            }
        });

        followTable.getItems().clear();

    }

    private void initOrderTaskTab(TableView taskTable) {
        List<TableColumn> columns = taskTable.getColumns();

        for(TableColumn column : columns){
            column.setStyle("-fx-alignment: CENTER-LEFT;");
        }

        columns.get(0).setCellValueFactory(new PropertyValueFactory<OrderTask, String>("id"));
        columns.get(1).setCellValueFactory(new PropertyValueFactory<OrderTask, String>("equement"));
        columns.get(2).setCellValueFactory(new PropertyValueFactory<OrderTask, String>("start"));
        columns.get(3).setCellValueFactory(new PropertyValueFactory<OrderTask, String>("end"));
        columns.get(4).setCellValueFactory(new PropertyValueFactory<OrderTask, String>("status"));
        // 增加按钮 ,
        columns.get(5).setCellValueFactory(new PropertyValueFactory<OrderTask, String>("action"));
        columns.get(5).setCellFactory(new Callback<TableColumn<OrderTask, String>, TableCell<OrderTask, String>>() {
            @Override
            public TableCell call(final TableColumn<OrderTask, String> param) {
                final TableCell<OrderTask, String> cell = new TableCell<OrderTask, String>() {

                    final Button btn = new Button("STOP");

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        btn.setOnAction(event -> {
                            OrderTask orderTask = getTableView().getItems().get(getIndex());
                            orderTask.stop();
                            LOG.info(orderTask.getStatus());
                            orderTask.setStatus("STOP");
                            taskTable.refresh();
                        });

                        if(!empty) {
                            setGraphic(btn);
                            setText(null);
                        }
                    }
                };
                return cell;
            }
        });

        orderTable.getItems().clear();
    }

    public void setColumnFactory(TableColumn<EquementDetail, String> tableColumn, String columnName){
        tableColumn.setCellValueFactory(new PropertyValueFactory<EquementDetail,String>(columnName));
    }

    private String parseId(String indexUrl) {
        String[] urlParts = indexUrl.split("\\.");
        return urlParts[urlParts.length - 1 ];
    }

    public void show(){
        mainStage.show();
    }

}
