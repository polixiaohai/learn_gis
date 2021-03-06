package org.walkgis.learngis.lesson24.controller;

import de.felixroske.jfxsupport.AbstractFxmlView;
import de.felixroske.jfxsupport.FXMLController;
import de.felixroske.jfxsupport.GUIState;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import org.walkgis.learngis.lesson24.Lesson24Application;
import org.walkgis.learngis.lesson24.basicclasses.*;
import org.walkgis.learngis.lesson24.basicclasses.enums.*;
import org.walkgis.learngis.lesson24.basicclasses.geometry.GISPoint;
import org.walkgis.learngis.lesson24.basicclasses.network.GISNetwork;
import org.walkgis.learngis.lesson24.view.DataTableView;
import org.walkgis.learngis.lesson24.view.LayerView;
import org.walkgis.learngis.lesson24.view.MainView;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;


@FXMLController
public class MainController implements Initializable {
    @Value(value = "${data.dir}")
    private String dataDir;
    @FXML
    private Pane canvasContainer;
    @FXML
    private Canvas mainCanvas;
    @FXML
    private ImageView btnOpenDocument, btnLayerControl, btnFullScreen, btnZoomIn, btnZoomOut, btnMoveUp, btnMoveDown, btnMoveLeft, btnMoveRight, btnClear;
    @FXML
    private ImageView btnBuildGrid, btnReadRouteGrid, btnWriteRouteGrid, btnLoadPoint, btnClearPoint, btnShowRoute;
    @FXML
    private CheckBox chbAddPoint;
    @FXML
    private ComboBox cmbRouteFields;
    @FXML
    private Label lblPosition, lblTotals, lblSelectCount;
    @FXML
    private TreeView toolContent;
    @Autowired
    private DataTableController dataTableController;
    @Autowired
    private MainView mainView;

    private ContextMenu contextMenu;
    private MenuItem menuDocument, select, selectElement, zoomIn, zoomOut, pan, fullScreen, menuLayerControl;

    @Autowired
    private ApplicationContext applicationContext;
    private BufferedImage backgroundWindow;
    private MouseCommand mouseCommand = MouseCommand.Unused;
    private int mouseStartX = 0, mouseStartY = 0, mouseMovingX = 0, mouseMovingY = 0;
    private boolean mouseOnMap = false;

    private GISView view;
    private Rectangle clientRectangle;
    public GISDocument document = new GISDocument();
    private GISNetwork network;
    private GISVectorLayer stopsLayer;
    private GISVectorLayer lineLayer;
    private List<GISVertex> netPoints = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        document = new GISDocument();
        initCanvasContextMenu();

        clientRectangle = new Rectangle(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
        view = new GISView(new GISExtent(new GISVertex(0, 0), new GISVertex(1, 1)), clientRectangle);
        mainCanvas.setOnMouseClicked(this::canvasClick);
        mainCanvas.setOnMouseMoved(this::canvasMouseMoved);
        mainCanvas.setOnMousePressed(this::canvasMousePressed);
        mainCanvas.setOnMouseReleased(this::canvasMouseReleased);
        mainCanvas.setOnContextMenuRequested(this::canvasContextMenu);
        btnOpenDocument.setOnMouseClicked(this::btnOpenDocumentClick);
        btnLayerControl.setOnMouseClicked(this::btnLayerControlClick);
        btnZoomIn.setOnMouseClicked(this::mapActionClick);
        btnZoomOut.setOnMouseClicked(this::mapActionClick);
        btnMoveUp.setOnMouseClicked(this::mapActionClick);
        btnMoveDown.setOnMouseClicked(this::mapActionClick);
        btnMoveLeft.setOnMouseClicked(this::mapActionClick);
        btnMoveRight.setOnMouseClicked(this::mapActionClick);
        btnFullScreen.setOnMouseClicked(this::btnFullScreen);
        btnClear.setOnMouseClicked(this::btnClearClick);

        mainCanvas.widthProperty().bind(canvasContainer.widthProperty());
        mainCanvas.heightProperty().bind(canvasContainer.heightProperty());

        List<GISField> fields = new ArrayList<>();
        fields.add(new GISField(Integer.class, "index"));
        stopsLayer = new GISVectorLayer("stops" + UUID.randomUUID().toString(), SHAPETYPE.point, null, fields);
        stopsLayer.labelIndex = 0;
        stopsLayer.drawAttributeOrNot = true;

        chbAddPoint.setTooltip(new Tooltip("添加起止点"));

        Tooltip.install(btnBuildGrid, new Tooltip("构建网络结构"));
        Tooltip.install(btnReadRouteGrid, new Tooltip("读取网络结构"));
        Tooltip.install(btnWriteRouteGrid, new Tooltip("导出网络结构"));
        Tooltip.install(btnLoadPoint, new Tooltip("加载起止点"));
        Tooltip.install(btnClearPoint, new Tooltip("清空起止点"));
        Tooltip.install(btnShowRoute, new Tooltip("显示最短路径"));

        chbAddPoint.setSelected(false);

        cmbRouteFields.setOnMouseClicked(this::cmbRouteFieldsClick);
        btnBuildGrid.setOnMouseClicked(this::btnBuildGridClick);
        btnReadRouteGrid.setOnMouseClicked(this::btnReadRouteGridClicck);
        btnWriteRouteGrid.setOnMouseClicked(this::btnWriteRouteGridClicck);
        btnLoadPoint.setOnMouseClicked(this::btnLoadPointClicck);
        btnClearPoint.setOnMouseClicked(this::btnClearPointClicck);
        btnShowRoute.setOnMouseClicked(this::btnShowRouteClicck);
    }

    @FXML
    private void btnShowRouteClicck(MouseEvent mouseEvent) {
        if (stopsLayer.features.size() < 2) return;
        checkLayers();
        lineLayer.clearSelection();
        for (int i = 1; i < stopsLayer.features.size(); i++) {
            GISVertex vFrom = stopsLayer.features.get(i - 1).spatial.center;
            GISVertex vTo = stopsLayer.features.get(i).spatial.center;

            List<GISFeature> fs = network.findRoute(vFrom, vTo);
            lineLayer.select(fs);
        }
        updateMap();
    }

    @FXML
    private void btnClearPointClicck(MouseEvent mouseEvent) {
        initGrid();
    }

    @FXML
    private void btnLoadPointClicck(MouseEvent mouseEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择数据");
        Stage selectFile = new Stage();
        if (StringUtils.isEmpty(dataDir) || !new File(dataDir).exists())
            dataDir = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new File(dataDir));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("所有文件类型", "*." + GISConst.SHP),
                new FileChooser.ExtensionFilter("Shapefile", "*." + GISConst.SHP)
        );
        File file = fileChooser.showOpenDialog(selectFile);
        if (file != null) {
            GISLayer layer = GISTools.getLayer(file.getAbsolutePath());
            if (layer.layerType != LAYERTYPE.VectorLayer) return;
            if (((GISVectorLayer) layer).shapeType != SHAPETYPE.point) return;
            initGrid();
            GISVectorLayer pointLayer = (GISVectorLayer) layer;
            for (int i = 0; i < pointLayer.features.size(); i++) {
                GISVertex vertex = pointLayer.features.get(i).spatial.center;
                //展示出来
                netPoints.add(vertex);
                GISAttribute attribute = new GISAttribute();
                attribute.addValue(netPoints.size());
                stopsLayer.addFeature(new GISFeature(new GISPoint(vertex), attribute), true);
            }
            updateMap();
        }
    }

    @FXML
    private void btnWriteRouteGridClicck(MouseEvent mouseEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存文档");
        Stage selectFile = new Stage();
        if (StringUtils.isEmpty(dataDir))
            dataDir = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new File(dataDir));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("所有文件类型", "*." + GISConst.NETFILE),
                new FileChooser.ExtensionFilter("网络类型", "*." + GISConst.NETFILE)
        );
        File file = fileChooser.showSaveDialog(selectFile);
        if (file != null) {
            network.write(file.getAbsolutePath());
        }
    }

    @FXML
    private void btnReadRouteGridClicck(MouseEvent mouseEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择数据");
        Stage selectFile = new Stage();
        if (StringUtils.isEmpty(dataDir) || !new File(dataDir).exists())
            dataDir = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new File(dataDir));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("所有文件类型", "*." + GISConst.NETFILE),
                new FileChooser.ExtensionFilter("网络类型", "*." + GISConst.NETFILE)
        );
        File file = fileChooser.showOpenDialog(selectFile);
        if (file != null) {
            network = new GISNetwork(file.getAbsolutePath());
            lineLayer = network.lineLayer;
            initGrid();
        }
    }

    @FXML
    private void btnBuildGridClick(MouseEvent mouseEvent) {
        lineLayer = (GISVectorLayer) document.getLayer(cmbRouteFields.getSelectionModel().getSelectedItem().toString());
        if (lineLayer == null) return;
        network = new GISNetwork(lineLayer);
        initGrid();
    }

    private void initGrid() {
        checkLayers();
        stopsLayer.deleteAllFeatures();
        lineLayer.clearSelection();
        chbAddPoint.setSelected(true);
        netPoints.clear();
        updateMap();
    }

    private void checkLayers() {
        if (document.getLayer(lineLayer.name) == null)
            document.addLayer(lineLayer);
        if (document.getLayer(stopsLayer.name) == null)
            document.addLayer(stopsLayer);
    }

    @FXML
    private void cmbRouteFieldsClick(MouseEvent mouseEvent) {
        cmbRouteFields.getItems().clear();
        for (int i = 0; i < document.layers.size(); i++) {
            GISLayer layer = document.layers.get(i);
            if (layer.layerType != LAYERTYPE.VectorLayer) continue;
            if (((GISVectorLayer) layer).shapeType != SHAPETYPE.polyline) continue;
            cmbRouteFields.getItems().add(layer.name);
        }
    }

    private void btnLayerControlClick(MouseEvent mouseEvent) {
        AbstractFxmlView view = applicationContext.getBean(LayerView.class);
        Stage newStage = new Stage();
        Scene newScene;
        if (view.getView().getScene() != null) {
            newScene = view.getView().getScene();
        } else {
            newScene = new Scene(view.getView());
        }

        newStage.setScene(newScene);
        newStage.initModality(Modality.NONE);
        newStage.initOwner(Lesson24Application.getStage());
        newStage.show();
    }

    @FXML
    private void btnOpenDocumentClick(MouseEvent mouseEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择数据");
        Stage selectFile = new Stage();
        if (StringUtils.isEmpty(dataDir) || !new File(dataDir).exists())
            dataDir = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new File(dataDir));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("所有文件类型", "*.gis"),
                new FileChooser.ExtensionFilter("Shapefile", "*.gis")
        );
        File file = fileChooser.showOpenDialog(selectFile);
        if (file != null) {
            document.read(file.getAbsolutePath());
            if (!document.isEmpty())
                updateMap();
        }
    }


    private void initCanvasContextMenu() {
        contextMenu = new ContextMenu();

        menuDocument = new MenuItem("地图文档");
        select = new MenuItem("选择要素");
        selectElement = new MenuItem("选择元素");
        zoomIn = new MenuItem("放大");
        zoomOut = new MenuItem("缩小");
        pan = new MenuItem("平移");
        fullScreen = new MenuItem("全屏");
        menuLayerControl = new MenuItem("图层控制");

        menuDocument.setOnAction(this::contextMenuClick);
        select.setOnAction(this::contextMenuClick);
        selectElement.setOnAction(this::contextMenuClick);
        zoomIn.setOnAction(this::contextMenuClick);
        zoomOut.setOnAction(this::contextMenuClick);
        pan.setOnAction(this::contextMenuClick);
        fullScreen.setOnAction(this::contextMenuClick);
        menuLayerControl.setOnAction(this::contextMenuClick);

        contextMenu.getItems().addAll(select, selectElement, zoomIn, zoomOut, pan, fullScreen);
    }

    @FXML
    private void contextMenuClick(ActionEvent actionEvent) {
        if (document.isEmpty() || view == null) return;
        if (actionEvent.getTarget() == fullScreen) {
            view.updateExtent(document.extent);
            updateMap();
        } else if (actionEvent.getTarget() == menuDocument) {

        } else if (actionEvent.getTarget() == menuLayerControl) {
            btnLayerControlClick(null);
        } else {
            if (actionEvent.getTarget() == select) {
                mouseCommand = MouseCommand.Select;
                GUIState.getScene().setCursor(Cursor.HAND);
            } else if (actionEvent.getTarget() == zoomIn) {
                mouseCommand = MouseCommand.ZoomIn;
                GUIState.getScene().setCursor(Cursor.DEFAULT);
            } else if (actionEvent.getTarget() == zoomOut) {
                mouseCommand = MouseCommand.ZoomOut;
                GUIState.getScene().setCursor(Cursor.DEFAULT);
            } else if (actionEvent.getTarget() == pan) {
                mouseCommand = MouseCommand.Pan;
                GUIState.getScene().setCursor(Cursor.OPEN_HAND);
            } else if (actionEvent.getTarget() == selectElement) {
                mouseCommand = MouseCommand.Unused;
                GUIState.getScene().setCursor(Cursor.DEFAULT);
            }
        }
    }

    @FXML
    private void canvasContextMenu(ContextMenuEvent contextMenuEvent) {
        contextMenu.show(mainCanvas, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
    }

    @FXML
    private void canvasMouseReleased(MouseEvent mouseEvent) {
        mouseStartX = (int) mouseEvent.getX();
        mouseStartY = (int) mouseEvent.getY();
        if (document.isEmpty()) return;
        if (!mouseOnMap) return;
        mouseOnMap = false;
        switch (mouseCommand) {
            case Select:
                document.clearSelection();
                SelectResult sr = SelectResult.UnknownType;
                if (mouseEvent.getX() == mouseStartX && mouseEvent.getY() == mouseStartY) {
                    GISVertex v = view.toMapVertex(new Point((int) mouseEvent.getX(), (int) mouseEvent.getY()));
                    sr = document.select(v, view);
                } else {
                    GISExtent v = view.rectToExtent((int) mouseEvent.getX(), mouseStartX, (int) mouseEvent.getY(), mouseStartY);
                    sr = document.select(v);
                }
                if (sr == SelectResult.OK) {
                    updateMap();
                    updateAttributeWindow();
                }
                break;
            case ZoomIn:
                if (mouseEvent.getX() == mouseStartX && mouseEvent.getY() == mouseStartY) {
                    GISVertex mouseLocation = view.toMapVertex(new Point((int) mouseEvent.getX(), (int) mouseEvent.getY()));
                    GISExtent extent = view.getRealExtent();
                    double newWidth = extent.getWidth() * GISConst.zoomInFactor;
                    double newHeight = extent.getHeight() * GISConst.zoomInFactor;
                    double newMinx = mouseLocation.x - (mouseLocation.x - extent.getMinX()) * GISConst.zoomInFactor;
                    double newMiny = mouseLocation.y - (mouseLocation.y - extent.getMinY()) * GISConst.zoomInFactor;
                    view.updateExtent(new GISExtent(newMinx, newMinx + newWidth, newMiny, newMiny + newHeight));
                } else {
                    view.updateExtent(view.rectToExtent((int) mouseEvent.getX(), mouseStartX, (int) mouseEvent.getY(), mouseStartY));
                }
                updateMap();
                break;
            case ZoomOut:
                if (mouseEvent.getX() == mouseStartX && mouseEvent.getY() == mouseStartY) {
                    GISExtent e1 = view.getRealExtent();
                    GISVertex mouseLocation = view.toMapVertex(new Point((int) mouseEvent.getX(), (int) mouseEvent.getY()));
                    double newWidth = e1.getWidth() / GISConst.zoomOutFactor;
                    double newHeight = e1.getHeight() / GISConst.zoomOutFactor;
                    double newMinx = mouseLocation.x - (mouseLocation.x - e1.getMinX()) / GISConst.zoomOutFactor;
                    double newMiny = mouseLocation.y - (mouseLocation.y - e1.getMinY()) / GISConst.zoomOutFactor;
                    view.updateExtent(new GISExtent(newMinx, newMinx + newWidth, newMiny, newMiny + newHeight));
                } else {
                    GISExtent e3 = view.rectToExtent((int) mouseEvent.getX(), mouseStartX, (int) mouseEvent.getY(), mouseStartY);
                    GISExtent e1 = view.getRealExtent();
                    double newWidth = e1.getWidth() * e1.getWidth() / e3.getWidth();
                    double newHeight = e1.getHeight() * e1.getHeight() / e3.getHeight();
                    double newMinx = e3.getMinX() - (e3.getMinX() - e1.getMinX()) * newWidth / e1.getWidth();
                    double newMiny = e3.getMinY() - (e3.getMinY() - e1.getMinY()) * newHeight / e1.getHeight();
                    view.updateExtent(new GISExtent(newMinx, newMinx + newWidth, newMiny, newMiny + newHeight));
                }
                updateMap();
                break;
            case Pan:
                if (mouseEvent.getX() != mouseStartX || mouseEvent.getY() != mouseStartY) {
                    GISExtent e1 = view.getRealExtent();
                    GISVertex m1 = view.toMapVertex(new Point(mouseStartX, mouseStartY));
                    GISVertex m2 = view.toMapVertex(new Point((int) mouseEvent.getX(), (int) mouseEvent.getY()));
                    double newWidth = e1.getWidth();
                    double newHeight = e1.getHeight();
                    double newMinx = e1.getMinX() - (m2.x - m1.x);
                    double newMiny = e1.getMinY() - (m2.y - m1.y);
                    view.updateExtent(new GISExtent(newMinx, newMinx + newWidth, newMiny, newMiny + newHeight));
                    updateMap();
                }
                break;
        }
    }

    @FXML
    private void canvasMousePressed(MouseEvent mouseEvent) {
        mouseStartX = (int) mouseEvent.getX();
        mouseStartY = (int) mouseEvent.getY();
        mouseOnMap = (mouseEvent.getButton() == MouseButton.PRIMARY && mouseCommand != MouseCommand.Unused);
    }

    @FXML
    private void canvasMouseMoved(MouseEvent event) {
        if (document.isEmpty()) return;
        mouseStartX = (int) event.getX();
        mouseStartY = (int) event.getY();
        if (mouseOnMap) updateMap();

        GISVertex gisVertex = view.toMapVertex(new Point((int) event.getX(), (int) event.getY()));
        lblPosition.setText(gisVertex.x + "," + gisVertex.y);
    }

    @FXML
    private void btnClearClick(MouseEvent mouseEvent) {
        if (document.isEmpty()) return;
        document.clearSelection();
        updateMap();
        //更新状态栏
        lblSelectCount.setText("当前选中：0");
        updateAttributeWindow();
    }

    private void updateAttributeWindow() {
        if (document.isEmpty()) return;
        if (dataTableController == null) return;
        dataTableController.updateData();
    }

    @FXML
    private void btnFullScreen(MouseEvent event) {
        if (document.isEmpty()) return;
        clientRectangle = new Rectangle(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
        view.updateExtent(document.extent);
        updateMap();
    }

    @FXML
    private void mapActionClick(MouseEvent event) {
        GISMapAction action = GISMapAction.zoomin;
        if (btnZoomIn == event.getSource()) action = GISMapAction.zoomin;
        else if (btnZoomOut == event.getSource()) action = GISMapAction.zoomout;
        else if (btnMoveUp == event.getSource()) action = GISMapAction.moveup;
        else if (btnMoveDown == event.getSource()) action = GISMapAction.movedown;
        else if (btnMoveLeft == event.getSource()) action = GISMapAction.movelet;
        else if (btnMoveRight == event.getSource()) action = GISMapAction.moveright;
        view.updateExtent(action);
        updateMap();
    }

    public void updateMap() {
        clientRectangle = new Rectangle(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
        if (view == null) {
            if (document.isEmpty()) return;
            view = new GISView(new GISExtent(document.extent), clientRectangle);
        }

        if (clientRectangle.getWidth() * clientRectangle.getHeight() == 0) return;
        view.updateRectangle(clientRectangle);

        if (backgroundWindow != null) backgroundWindow = null;
        backgroundWindow = new BufferedImage((int) clientRectangle.getWidth(), (int) clientRectangle.getHeight(), BufferedImage.TYPE_INT_ARGB);

        //背景窗口上绘图
        Graphics2D graphics = (Graphics2D) backgroundWindow.getGraphics();
        graphics.setBackground(Color.WHITE);
        graphics.clearRect(0, 0, (int) clientRectangle.getWidth(), (int) clientRectangle.getHeight());
        document.draw(graphics, view);

        WritableImage image = SwingFXUtils.toFXImage(backgroundWindow, null);
        mainCanvas.getGraphicsContext2D().clearRect(0, 0, clientRectangle.getWidth(), clientRectangle.getHeight());
        mainCanvas.getGraphicsContext2D().drawImage(image, 0, 0);
        lblSelectCount.setText("总共有图层：" + document.layers.size());
    }

    @FXML
    private void canvasClick(MouseEvent event) {
        mouseStartX = (int) event.getX();
        mouseStartY = (int) event.getY();
        if (contextMenu != null) {
            contextMenu.hide();
        }
        if (event.getClickCount() == 2 && event.getButton().name().equals("PRIMARY")) {
            if (chbAddPoint.isSelected()) {
                checkLayers();
                GISVertex vertex = view.toMapVertex(new Point((int) event.getX(), (int) event.getY()));
                netPoints.add(vertex);
                GISAttribute attribute = new GISAttribute();
                attribute.addValue(netPoints.size());
                stopsLayer.addFeature(new GISFeature(new GISPoint(vertex), attribute), true);
                updateMap();
            }
        }
    }

    public void openAttributeWindow(GISLayer layer) {
        AbstractFxmlView view = applicationContext.getBean(DataTableView.class);
        Stage newStage = new Stage();
        Scene newScene;
        if (view.getView().getScene() != null) {
            newScene = view.getView().getScene();
        } else {
            newScene = new Scene(view.getView());
        }

        newStage.setScene(newScene);
        newStage.initModality(Modality.NONE);
        newStage.initOwner(Lesson24Application.getStage());
        newStage.show();
        if (layer instanceof GISVectorLayer)
            dataTableController.initTable((GISVectorLayer) layer);
    }
}
