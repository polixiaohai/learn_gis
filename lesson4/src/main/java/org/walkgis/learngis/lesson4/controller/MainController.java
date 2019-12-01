package org.walkgis.learngis.lesson4.controller;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.walkgis.learngis.lesson4.basicclasses.*;

import java.awt.Point;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


@FXMLController
public class MainController implements Initializable {
    @FXML
    private Canvas mainCanvas;
    @FXML
    private TextField txtX, txtY, txtAttribute, txtExtent;
    @FXML
    private Button btnDraw, btnUpdateExtent, btnZoomIn, btnZoomOut, btnMoveUp, btnMoveDown, btnMoveLeft, btnMoveRight;

    private List<GISFeature> gisFeatures = new ArrayList<>();

    private GISView gisView;
    private Rectangle clientRectangle;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clientRectangle = new Rectangle(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
        gisView = new GISView(new GISExtent(new GISVertex(0, 0), new GISVertex(100, 100)), clientRectangle);
        btnDraw.setOnMouseClicked(this::btnDrawPoint);
        mainCanvas.setOnMouseClicked(this::canvasClick);
        btnUpdateExtent.setOnMouseClicked(this::btnUpdateExtentClick);
        btnZoomIn.setOnMouseClicked(this::mapActionClick);
        btnZoomOut.setOnMouseClicked(this::mapActionClick);
        btnMoveUp.setOnMouseClicked(this::mapActionClick);
        btnMoveDown.setOnMouseClicked(this::mapActionClick);
        btnMoveLeft.setOnMouseClicked(this::mapActionClick);
        btnMoveRight.setOnMouseClicked(this::mapActionClick);
        txtExtent.setText("-1000,-1000,1000,1000");

        gisFeatures.add(new GISFeature(new GISPoint(new GISVertex(20, 30)), new GISAttribute("1")));
        gisFeatures.add(new GISFeature(new GISPoint(new GISVertex(300, 100)), new GISAttribute("2")));
        gisFeatures.add(new GISFeature(new GISPoint(new GISVertex(150, 400)), new GISAttribute("3")));
        gisFeatures.add(new GISFeature(new GISPoint(new GISVertex(500, 200)), new GISAttribute("4")));

        for (int i = 0; i < gisFeatures.size(); i++) {
            gisFeatures.get(i).draw(mainCanvas.getGraphicsContext2D(), gisView, true, 0);
        }

        btnUpdateExtentClick(null);
    }

    private void mapActionClick(MouseEvent event) {
        GISMapAction action = GISMapAction.zoomin;
        if (btnZoomIn == event.getSource()) action = GISMapAction.zoomin;
        else if (btnZoomOut == event.getSource()) action = GISMapAction.zoomout;
        else if (btnMoveUp == event.getSource()) action = GISMapAction.moveup;
        else if (btnMoveDown == event.getSource()) action = GISMapAction.movedown;
        else if (btnMoveLeft == event.getSource()) action = GISMapAction.movelet;
        else if (btnMoveRight == event.getSource()) action = GISMapAction.moveright;
        gisView.changeView(action);
        updateMap();
    }


    private void updateMap() {
        mainCanvas.getGraphicsContext2D().setFill(Color.WHITE);
        mainCanvas.getGraphicsContext2D().fillRect(0, 0, clientRectangle.getWidth(), clientRectangle.getHeight());
        for (int i = 0; i < gisFeatures.size(); i++) {
            gisFeatures.get(i).draw(mainCanvas.getGraphicsContext2D(), gisView, true, 0);
        }
    }

    @FXML
    private void btnUpdateExtentClick(MouseEvent event) {
        if (txtExtent.getText().isEmpty()) return;

        String[] text = txtExtent.getText().split(",");
        double minx = Double.parseDouble(text[0]);
        double miny = Double.parseDouble(text[1]);
        double maxx = Double.parseDouble(text[2]);
        double maxy = Double.parseDouble(text[3]);

        gisView.update(new GISExtent(minx, maxx, miny, maxy), clientRectangle);

        updateMap();
    }

    @FXML
    private void canvasClick(MouseEvent event) {
        GISVertex gisVertex = gisView.toMapVertex(new Point((int) event.getX(), (int) event.getY()));
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
        if (findId == -1)
            alert.setContentText("没有找到任何空间对象");
        Point nearPoint = gisView.toScreenPoint(gisFeatures.get(findId).spatial.center);
        int screenDis = (int) (Math.abs(nearPoint.x - event.getX()) + Math.abs(nearPoint.y - event.getY()));
        if (screenDis > 5) {
            alert.setContentText("请靠近空间对象点击");
            alert.showAndWait();
            return;
        }
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
        gisFeature.draw(mainCanvas.getGraphicsContext2D(), gisView, true, 0);

        gisFeatures.add(gisFeature);
    }
}
