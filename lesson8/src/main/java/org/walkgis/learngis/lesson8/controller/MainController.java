package org.walkgis.learngis.lesson8.controller;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.walkgis.learngis.lesson8.Lesson8Application;
import org.walkgis.learngis.lesson8.basicclasses.*;
import org.walkgis.learngis.lesson8.view.DataTableView;

import java.awt.Point;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


@FXMLController
public class MainController implements Initializable {
    @FXML
    private Canvas mainCanvas;
    @FXML
    private Button btnOpenShp, btnFullScreen, btnZoomIn, btnZoomOut, btnMoveUp, btnMoveDown, btnMoveLeft, btnMoveRight, btnAttributeTable;

    private List<GISFeature> gisFeatures = new ArrayList<>();

    private GISView view;
    private GISLayer layer;
    private Rectangle clientRectangle;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clientRectangle = new Rectangle(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
        view = new GISView(new GISExtent(new GISVertex(0, 0), new GISVertex(100, 100)), clientRectangle);
        mainCanvas.setOnMouseClicked(this::canvasClick);
        btnZoomIn.setOnMouseClicked(this::mapActionClick);
        btnZoomOut.setOnMouseClicked(this::mapActionClick);
        btnMoveUp.setOnMouseClicked(this::mapActionClick);
        btnMoveDown.setOnMouseClicked(this::mapActionClick);
        btnMoveLeft.setOnMouseClicked(this::mapActionClick);
        btnMoveRight.setOnMouseClicked(this::mapActionClick);
        btnOpenShp.setOnMouseClicked(this::btnOpenShpClick);
        btnFullScreen.setOnMouseClicked(this::btnFullScreen);
        btnAttributeTable.setOnMouseClicked(this::btnAttributeTableClick);
    }

    @FXML
    private void btnAttributeTableClick(MouseEvent event) {
        Lesson8Application.showView(DataTableView.class, Modality.WINDOW_MODAL);
    }

    @FXML
    private void btnFullScreen(MouseEvent event) {
        if (layer == null) return;
        view.updateExtent(layer.extent);
        updateMap(true);
    }

    @FXML
    private void btnOpenShpClick(MouseEvent event) {
        GISShapefile gisShapefile = new GISShapefile();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择数据");
        Stage selectFile = new Stage();
//        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setInitialDirectory(new File("E:\\projects\\GIS\\learn_gis\\asset\\data"));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("所有文件类型", "*.shp"),
                new FileChooser.ExtensionFilter("Shapefile", "*.shp")
        );
        File file = fileChooser.showOpenDialog(selectFile);
        if (file != null) {
            layer = gisShapefile.readShapefile(file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(".")));
            layer.drawAttributeOrNot = false;

            updateMap(false);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("信息框");
            alert.setHeaderText("打开成功");
            alert.setContentText("read" + layer.featureCount() + " point objects");
            alert.showAndWait();
        }
    }

    private void mapActionClick(MouseEvent event) {
        GISMapAction action = GISMapAction.zoomin;
        if (btnZoomIn == event.getSource()) action = GISMapAction.zoomin;
        else if (btnZoomOut == event.getSource()) action = GISMapAction.zoomout;
        else if (btnMoveUp == event.getSource()) action = GISMapAction.moveup;
        else if (btnMoveDown == event.getSource()) action = GISMapAction.movedown;
        else if (btnMoveLeft == event.getSource()) action = GISMapAction.movelet;
        else if (btnMoveRight == event.getSource()) action = GISMapAction.moveright;
        view.changeView(action);
        updateMap(true);
    }

    private void updateMap(boolean clear) {
        if (clear) {
            mainCanvas.getGraphicsContext2D().setFill(Color.WHITE);
            mainCanvas.getGraphicsContext2D().fillRect(0, 0, clientRectangle.getWidth(), clientRectangle.getHeight());
        }
        if (layer == null) return;
        layer.draw(mainCanvas.getGraphicsContext2D(), view);
    }

    @FXML
    private void canvasClick(MouseEvent event) {
        GISVertex gisVertex = view.toMapVertex(new Point((int) event.getX(), (int) event.getY()));
        double minDis = Double.POSITIVE_INFINITY;
        int findId = -1;
        for (int i = 0; i < gisFeatures.size(); i++) {
            double dis = gisFeatures.get(i).spatial.center.distance(gisVertex);

            if (dis < minDis) {
                minDis = dis;
                findId = i;
            }
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (findId == -1) {
            alert.setContentText("没有找到任何空间对象");
            return;
        }

        Point nearPoint = view.toScreenPoint(gisFeatures.get(findId).spatial.center);
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
    public GISLayer getLayer() {
        return layer;
    }
}
