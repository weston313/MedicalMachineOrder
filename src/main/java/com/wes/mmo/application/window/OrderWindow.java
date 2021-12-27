package com.wes.mmo.application.window;

import com.wes.mmo.dao.EquementDetail;
import com.wes.mmo.service.task.OrderTaskV3;
import com.wes.mmo.service.task.TaskCache;
import com.wes.mmo.utils.TimeUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OrderWindow {

    public static final Log LOG = LogFactory.getLog(OrderWindow.class);

    private TableView<OrderTask> orderTaskTableView;

    private EquementDetail equementDetail;

    private Stage orderStage;
    private DatePicker startDatePicker;
    private ChoiceBox startHourCheckBox;
    private ChoiceBox startMinuteCheckBox;

    private DatePicker endDatePicker;
    private ChoiceBox endHourCheckBox;
    private ChoiceBox endMinuteCheckBox;

    private DatePicker actionDatePicker;
    private ChoiceBox actionHourChoiceBox;
    private ChoiceBox actionMinuteChoicBox;

    private TextField relationProductTextField;
    private ScheduledExecutorService executorService;

    public OrderWindow(TableView orderTaskTableView, EquementDetail equementDetail) {
        this.orderTaskTableView = orderTaskTableView;
        this.equementDetail = equementDetail;
        this.executorService = Executors.newScheduledThreadPool(10);
    }

    public void initlize() throws IOException {
        orderStage = new Stage();
        orderStage.setTitle("预约窗口");
        orderStage.setWidth(600);
        orderStage.setHeight(400);
        URL orderFxmlUrl =  this.getClass().getResource("/fxml/order/index.fxml");
        VBox orderPane = FXMLLoader.load(orderFxmlUrl);
        Scene mainScene = new Scene(orderPane);
        orderStage.setScene(mainScene);

        TextField equemenetName = (TextField) ((BorderPane)orderPane.getChildren().get(1)).getChildren().get(1);

        startDatePicker = parseDatePicker(orderPane, 2, 1);
        startHourCheckBox = parseCheckBox(orderPane, 2,2,0);
        startMinuteCheckBox = parseCheckBox(orderPane, 2, 2, 1);

        endDatePicker = parseDatePicker(orderPane, 3, 1);
        endHourCheckBox = parseCheckBox(orderPane, 3,2,0);
        endMinuteCheckBox = parseCheckBox(orderPane, 3,2,1);

        actionDatePicker = parseDatePicker(orderPane, 4, 1);
        actionHourChoiceBox = parseCheckBox(orderPane, 4,2, 0);
        actionMinuteChoicBox = parseCheckBox(orderPane, 4,2,1);

        relationProductTextField =  (TextField) ((BorderPane)orderPane.getChildren().get(5)).getChildren().get(1);

        //
        Button orderButton = (Button) ((BorderPane)orderPane.getChildren().get(6)).getChildren().get(0);
        orderButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                LOG.info("Click Order Button");

                LocalDate startDate = startDatePicker.getValue();
                String startTimeStr = new StringBuffer().append(startDate.toString())
                        .append("-").append(startHourCheckBox.getValue().toString())
                        .append("-").append(startMinuteCheckBox.getValue().toString()).toString();
                long startTimestamp  = TimeUtils.ParseDateString(startTimeStr, "yyyy-MM-dd-HH-mm");;

                LocalDate endDate = endDatePicker.getValue();
                String endTimeStr = new StringBuffer().append(endDate.toString())
                        .append("-").append(endHourCheckBox.getValue().toString())
                        .append("-").append(endMinuteCheckBox.getValue().toString()).toString();
                long endTimestamp  = TimeUtils.ParseDateString(endTimeStr, "yyyy-MM-dd-HH-mm");;

                LocalDate actionDate = actionDatePicker.getValue();
                String actionTimeStr = new StringBuffer().append(actionDate.toString())
                        .append("-").append(actionHourChoiceBox.getValue().toString())
                        .append("-").append(actionMinuteChoicBox.getValue().toString()).toString();
                long actionTimestamp  = TimeUtils.ParseDateString(actionTimeStr, "yyyy-MM-dd-HH-mm");

                String relationProduct = relationProductTextField.getText();

                try {
                    OrderTask taskItem = new OrderTask(equementDetail, startTimestamp, endTimestamp,actionTimestamp);
                    orderTaskTableView.getItems().add(taskItem);
                    orderStage.close();

                    Thread thread = new OrderTaskV3(
                            equementDetail,
                            startTimestamp/1000,
                            endTimestamp/1000,
                            "",
                            relationProduct,
                            actionTimestamp / 1000
                    );
                    TaskCache.GetTaskCache().scheduleTask(thread, actionTimestamp - 50*1000);
                } catch (Exception e) {
                    e.printStackTrace();
                    LOG.info("======> " + e.getCause());
                }
            }
        });

        equemenetName.setText(equementDetail.getName());
        String hour= new SimpleDateFormat("HH").format(new Date());
        startHourCheckBox.setValue(hour);
        endHourCheckBox.setValue(hour);

        show();
    }

    private DatePicker parseDatePicker(VBox orderPane, int borderPaneIndex, int datePickerIndex) {
        BorderPane borderPane = (BorderPane) orderPane.getChildren().get(borderPaneIndex);
        DatePicker datePicker = (DatePicker) borderPane.getChildren().get(datePickerIndex);
        datePicker.setValue(LocalDate.now());
        return datePicker;
    }

    private ChoiceBox parseCheckBox(VBox orderPane, int borderPaneIndex, int timeBorderPaneIndex, int checkBoxIndex){
        BorderPane borderPane = (BorderPane) orderPane.getChildren().get(borderPaneIndex);
        BorderPane timeBorderPane = (BorderPane) borderPane.getChildren().get(timeBorderPaneIndex);
        return (ChoiceBox) timeBorderPane.getChildren().get(checkBoxIndex);
    }


    public void show(){
        orderStage.show();
    }


    public class OrderTask {
        /**
         * Job List Information
         */
        private EquementDetail equementDetail;
        private SimpleStringProperty index ;
        private SimpleStringProperty equement = null;
        private SimpleStringProperty start = null;
        private SimpleStringProperty end = null;
        private SimpleStringProperty status = null;
        private SimpleStringProperty action = null;
        private SimpleStringProperty time = null;

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

        public String getTime() {
            return time.get();
        }

        public OrderTask(EquementDetail equementDetail, long startTime, long endTime, long actionTime) {
            this.equementDetail = equementDetail;
            this.equement = new SimpleStringProperty(equementDetail.getName());
            this.start = new SimpleStringProperty(TimeUtils.FormatDate(new Date(startTime), "yyyy-MM-dd HH:mm:ss"));
            this.end = new SimpleStringProperty(TimeUtils.FormatDate(new Date(endTime), "yyyy-MM-dd HH:mm:ss"));
            this.status = new SimpleStringProperty("RUNNING");
            this.time = new SimpleStringProperty(TimeUtils.FormatDate(new Date(actionTime), "yyyy-MM-dd HH:mm:ss"));
            this.index = new SimpleStringProperty(String.valueOf(System.currentTimeMillis()/1000));
        }
    }

}
