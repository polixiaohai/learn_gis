package org.walkgis.learngis.lesson1.controller;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.walkgis.learngis.lesson1.basicclasses.GISPoint;
import org.walkgis.learngis.lesson1.basicclasses.GISVertex;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


@FXMLController
public class MainController implements Initializable {

    @FXML
    private AnchorPane mainPane;
    @FXML
    private Canvas mainCanvas;
    @FXML
    private TextField txtX, txtY, txtAttribute;
    @FXML
    private Button btnDraw;

    private List<GISPoint> pointList = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnDraw.setOnMouseClicked(this::drawPoint);
        mainCanvas.setOnMouseClicked(this::canvasClick);
    }

    @FXML
    private void canvasClick(MouseEvent event) {
        GISVertex gisVertex = new GISVertex(event.getX(), event.getY());
        double minDis = Double.MAX_VALUE;
        int findId = -1;
        for (int i = 0; i < pointList.size(); i++) {
            double dis = pointList.get(i).location.distance(gisVertex);

            if (dis < minDis) {
                minDis = dis;
                findId = i;
            }
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (minDis > 5 || findId == -1)
            alert.setContentText("没有点实体或者点击位置不准确");
        else
            alert.setContentText(pointList.get(findId).attribute);

        alert.showAndWait();
    }

    @FXML
    private void drawPoint(MouseEvent mouseEvent) {
        double x = Double.parseDouble(txtX.getText());
        double y = Double.parseDouble(txtY.getText());
        String attribute = txtAttribute.getText();

        GISVertex gisVertex = new GISVertex(x, y);
        GISPoint gisPoint = new GISPoint(gisVertex, attribute);
        gisPoint.drawPoint(mainCanvas.getGraphicsContext2D());
        gisPoint.drawAttribute(mainCanvas.getGraphicsContext2D());

        pointList.add(gisPoint);
    }
}
