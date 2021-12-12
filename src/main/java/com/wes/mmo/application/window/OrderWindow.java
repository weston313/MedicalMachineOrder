package com.wes.mmo.application.window;

import com.wes.mmo.dao.EquementDetail;
import com.wes.mmo.service.task.OrderTaskV3;
import com.wes.mmo.service.task.Task;
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
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public class OrderWindow {

    public static final Log LOG = LogFactory.getLog(OrderWindow.class);

    private TableView<Task> orderTaskTableView;

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

    public OrderWindow(TableView orderTaskTableView, EquementDetail equementDetail) {
        this.orderTaskTableView = orderTaskTableView;
        this.equementDetail = equementDetail;
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

                LocalDate endDate = endDatePicker.getValue();
                String endTimeStr = new StringBuffer().append(endDate.toString())
                        .append("-").append(endHourCheckBox.getValue().toString())
                        .append("-").append(endMinuteCheckBox.getValue().toString()).toString();

                LocalDate actionDate = actionDatePicker.getValue();
                String actionTimeStr = new StringBuffer().append(actionDate.toString())
                        .append("-").append(actionHourChoiceBox.getValue().toString())
                        .append("-").append(actionMinuteChoicBox.getValue().toString()).toString();


                String relationProduct = relationProductTextField.getText();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
                try {
                    OrderTaskV3 orderTask = new OrderTaskV3(equementDetail,
                            sdf.parse(startTimeStr).getTime()/1000,
                            sdf.parse(endTimeStr).getTime()/1000 - 1,
                            "",
                            relationProduct,
                            sdf.parse(actionTimeStr).getTime()/1000);

                    orderTask.execute();

                    orderTaskTableView.getItems().add(orderTask);

                    orderStage.close();
                } catch (ParseException e) {
                    e.printStackTrace();
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
}
