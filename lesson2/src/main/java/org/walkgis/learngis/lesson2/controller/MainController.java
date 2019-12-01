package org.walkgis.learngis.lesson2.controller;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.walkgis.learngis.lesson2.basicclasses.GISAttribute;
import org.walkgis.learngis.lesson2.basicclasses.GISFeature;
import org.walkgis.learngis.lesson2.basicclasses.GISPoint;
import org.walkgis.learngis.lesson2.basicclasses.GISVertex;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


@FXMLController
public class MainController implements Initializable {
    @FXML
    private Canvas mainCanvas;
    @FXML
    private TextField txtX, txtY, txtAttribute;
    @FXML
    private Button btnDraw;

    private List<GISFeature> gisFeatures = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnDraw.setOnMouseClicked(this::btnDrawPoint);
        mainCanvas.setOnMouseClicked(this::canvasClick);
    }

    @FXML
    private void canvasClick(MouseEvent event) {
        GISVertex gisVertex = new GISVertex(event.getX(), event.getY());
        double minDis = Double.MAX_VALUE;
        int findId = -1;
        for (int i = 0; i < gisFeatures.size(); i++) {
            double dis = gisFeatures.get(i).spatial.center.distance(gisVertex);

            if (dis < minDis) {
                minDis = dis;
                findId = i;
            }
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (minDis > 5 || findId == -1)
            alert.setContentText("没有点实体或者点击位置不准确");
        else
            alert.setContentText(gisFeatures.get(findId).getAttribute(0).toString());

        alert.showAndWait();
    }

    @FXML
    private void btnDrawPoint(MouseEvent mouseEvent) {
        double x = Double.parseDouble(txtX.getText());
        double y = Double.parseDouble(txtY.getText());
        GISVertex gisVertex = new GISVertex(x, y);
        GISPoint gisPoint = new GISPoint(gisVertex);

        String attribute = txtAttribute.getText();
        GISAttribute gisAttribute = new GISAttribute();
        gisAttribute.addValue(attribute);

        GISFeature gisFeature = new GISFeature(gisPoint, gisAttribute);
        gisFeature.draw(mainCanvas.getGraphicsContext2D(), true, 0);

        gisFeatures.add(gisFeature);
    }
}
