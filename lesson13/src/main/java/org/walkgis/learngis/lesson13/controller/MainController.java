package org.walkgis.learngis.lesson13.controller;

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
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
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
import org.walkgis.learngis.lesson13.Lesson13Application;
import org.walkgis.learngis.lesson13.basicclasses.*;
import org.walkgis.learngis.lesson13.view.DataTableView;
import org.walkgis.learngis.lesson13.view.MainView;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;


@FXMLController
public class MainController implements Initializable {
    @Value(value = "${data.dir}")
    private String dataDir;
    @FXML
    private Pane canvasContainer;
    @FXML
    private Canvas mainCanvas;
    @FXML
    private ImageView btnOpenShp, btnFullScreen, btnZoomIn, btnZoomOut, btnMoveUp, btnMoveDown, btnMoveLeft, btnMoveRight, btnAttributeTable, btnClear;
    @FXML
    private Label lblPosition, lblCount;
    @Autowired
    private DataTableController dataTableController;
    @Autowired
    private MainView mainView;
    private ContextMenu contextMenu;
    private MenuItem select, zoomIn, zoomOut, pan, fullScreen;
    @Autowired
    private ApplicationContext applicationContext;
    private BufferedImage backgroundWindow;
    private MouseCommand mouseCommand = MouseCommand.Unused;
    private int mouseStartX = 0, mouseStartY = 0, mouseMovingX = 0, mouseMovingY = 0;
    private boolean mouseOnMap = false;

    private GISView view;
    private GISLayer layer;
    private Rectangle clientRectangle;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        contextMenu = new ContextMenu();

        select = new MenuItem("select");
        zoomIn = new MenuItem("ZoomIn");
        zoomOut = new MenuItem("zoomOut");
        pan = new MenuItem("pan");
        fullScreen = new MenuItem("FullScreen");

        select.setOnAction(this::contextMenuClick);
        zoomIn.setOnAction(this::contextMenuClick);
        zoomOut.setOnAction(this::contextMenuClick);
        pan.setOnAction(this::contextMenuClick);
        fullScreen.setOnAction(this::contextMenuClick);

        contextMenu.getItems().addAll(select, zoomIn, zoomOut, pan, fullScreen);


        clientRectangle = new Rectangle(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
        view = new GISView(new GISExtent(new GISVertex(0, 0), new GISVertex(1, 1)), clientRectangle);
//        mainCanvas.setOnMouseClicked(this::canvasClick);
        mainCanvas.setOnMouseMoved(this::canvasMouseMoved);
        mainCanvas.setOnMousePressed(this::canvasMousePressed);
        mainCanvas.setOnMouseReleased(this::canvasMouseReleased);
        mainCanvas.setOnContextMenuRequested(this::canvasContextMenu);
        btnZoomIn.setOnMouseClicked(this::mapActionClick);
        btnZoomOut.setOnMouseClicked(this::mapActionClick);
        btnMoveUp.setOnMouseClicked(this::mapActionClick);
        btnMoveDown.setOnMouseClicked(this::mapActionClick);
        btnMoveLeft.setOnMouseClicked(this::mapActionClick);
        btnMoveRight.setOnMouseClicked(this::mapActionClick);
        btnOpenShp.setOnMouseClicked(this::btnOpenShpClick);
        btnFullScreen.setOnMouseClicked(this::btnFullScreen);
        btnAttributeTable.setOnMouseClicked(this::btnAttributeTableClick);
        btnClear.setOnMouseClicked(this::btnClearClick);
        mainCanvas.widthProperty().bind(canvasContainer.widthProperty());
        mainCanvas.heightProperty().bind(canvasContainer.heightProperty());
    }

    @FXML
    private void contextMenuClick(ActionEvent actionEvent) {
        if (layer == null) return;
        if (actionEvent.getTarget() == fullScreen) {
            view.updateExtent(layer.extent);
            updateMap();
        } else {
            if (actionEvent.getTarget() == select) {
                mouseCommand = MouseCommand.Select;
                GUIState.getScene().setCursor(Cursor.CLOSED_HAND);
            } else if (actionEvent.getTarget() == zoomIn) {
                mouseCommand = MouseCommand.ZoomIn;
                GUIState.getScene().setCursor(Cursor.DEFAULT);
            } else if (actionEvent.getTarget() == zoomOut) {
                mouseCommand = MouseCommand.ZoomOut;
                GUIState.getScene().setCursor(Cursor.DEFAULT);
            } else if (actionEvent.getTarget() == pan) {
                mouseCommand = MouseCommand.Pan;
                GUIState.getScene().setCursor(Cursor.MOVE);
            }
        }
    }

    @FXML
    private void canvasContextMenu(ContextMenuEvent contextMenuEvent) {
        contextMenu.show(mainCanvas, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
    }


    @FXML
    private void canvasMouseReleased(MouseEvent mouseEvent) {
        if (layer == null) return;
        if (!mouseOnMap) return;
        mouseOnMap = false;
        switch (mouseCommand) {
            case Select:
                layer.clearSelection();
                SelectResult sr = SelectResult.UnknownType;
                if (mouseEvent.getX() == mouseStartX && mouseEvent.getY() == mouseStartY) {
                    GISVertex v = view.toMapVertex(new Point((int) mouseEvent.getX(), (int) mouseEvent.getY()));
                    sr = layer.select(v, view);
                } else {
                    GISExtent v = view.rectToExtent((int) mouseEvent.getX(), mouseStartX, (int) mouseEvent.getY(), mouseStartY);
                    sr = layer.select(v);
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
                    GISVertex mouseLocation = view.toMapVertex(new Point((int) mouseEvent.getX(), (int) mouseEvent.getY()));
                    GISExtent extent = view.getRealExtent();
                    double newWidth = extent.getWidth() * GISConst.zoomOutFactor;
                    double newHeight = extent.getHeight() * GISConst.zoomOutFactor;
                    double newMinx = mouseLocation.x - (mouseLocation.x - extent.getMinX()) * GISConst.zoomOutFactor;
                    double newMiny = mouseLocation.y - (mouseLocation.y - extent.getMinY()) * GISConst.zoomOutFactor;
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
        if (layer == null) return;
        mouseStartX = (int) event.getX();
        mouseStartY = (int) event.getY();
        if (mouseOnMap) updateMap();

        GISVertex gisVertex = view.toMapVertex(new Point((int) event.getX(), (int) event.getY()));
        lblPosition.setText(gisVertex.x + "," + gisVertex.y);
    }

    @FXML
    private void btnClearClick(MouseEvent mouseEvent) {
        if (layer == null) return;
        layer.clearSelection();
        updateMap();
        //更新状态栏
        lblCount.setText("当前选中：0");
        updateAttributeWindow();
    }

    private void updateAttributeWindow() {
        if (layer == null) return;
        if (dataTableController == null) return;
        dataTableController.updateData();
    }

    @FXML
    private void btnAttributeTableClick(MouseEvent event) {
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
        newStage.initOwner(Lesson13Application.getStage());
        newStage.show();
        dataTableController.initTable();
    }

    @FXML
    private void btnFullScreen(MouseEvent event) {
        if (layer == null) return;
        clientRectangle = new Rectangle(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
        view.updateExtent(layer.extent);
        updateMap();
    }

    @FXML
    private void btnOpenShpClick(MouseEvent event) {
        GISShapefile gisShapefile = new GISShapefile();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择数据");
        Stage selectFile = new Stage();
        if (StringUtils.isEmpty(dataDir))
            dataDir = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new File(dataDir));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("所有文件类型", "*.shp"),
                new FileChooser.ExtensionFilter("Shapefile", "*.shp")
        );
        File file = fileChooser.showOpenDialog(selectFile);
        if (file != null) {
            layer = gisShapefile.readShapefile(file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(".")));
            layer.drawAttributeOrNot = true;
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("信息框");
            alert.setHeaderText("打开成功");
            alert.setContentText("read" + layer.featureCount() + " features");
            alert.showAndWait();

            view.updateExtent(layer.extent);
            updateMap();
        }
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
        if (layer == null) return;
        if (clientRectangle.getWidth() * clientRectangle.getHeight() == 0) return;
        view.updateRectangle(clientRectangle);

        if (backgroundWindow != null) backgroundWindow = null;
        backgroundWindow = new BufferedImage((int) clientRectangle.getWidth(), (int) clientRectangle.getHeight(), BufferedImage.TYPE_INT_ARGB);

        //背景窗口上绘图
        Graphics2D graphics = (Graphics2D) backgroundWindow.getGraphics();
        graphics.setBackground(Color.WHITE);
        graphics.clearRect(0, 0, (int) clientRectangle.getWidth(), (int) clientRectangle.getHeight());
        layer.draw(graphics, view);

        WritableImage image = SwingFXUtils.toFXImage(backgroundWindow, null);

        mainCanvas.getGraphicsContext2D().drawImage(image, 0, 0);
        lblCount.setText("当前选中：" + layer.selection.size());
    }

    @FXML
    private void canvasClick(MouseEvent event) {
        if (contextMenu != null) {
            contextMenu.hide();
        }
    }

    @FXML
    public GISLayer getLayer() {
        return layer;
    }
}
