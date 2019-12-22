package org.walkgis.learngis.lesson18.controller;

import de.felixroske.jfxsupport.FXMLController;
import de.felixroske.jfxsupport.GUIState;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.walkgis.learngis.lesson18.basicclasses.*;
import org.walkgis.learngis.lesson18.view.LayerView;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

@FXMLController
public class LayerController implements Initializable {
    @Value(value = "${data.dir}")
    private String dataDir;
    @FXML
    private Button btnMoveUp, btnMoveDown, btnOpenAttr, btnAddLayer, btnDeleteLayer, btnSaveDocument, btnExportLayer;
    @FXML
    private ListView layerList;
    @FXML
    private CheckBox chbIsSelect, chbIsVisible, chbAutoLabel;
    @FXML
    private ChoiceBox cmbFields;
    @FXML
    private ComboBox cmbTheamticTypes, cmbThematicAttr;
    @FXML
    private TextField txtFilePath, txtLayerName, txtWidth, txtLevels;
    @FXML
    private ColorPicker colorOutside, colorInside;
    @FXML
    private Button btnApply, btnClose, btnModify, btnThematicApply;
    @Autowired
    private LayerView layerView;

    @Autowired
    private MainController mainController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (mainController.document == null) mainController.document = new GISDocument();

        layerList.getSelectionModel().selectedItemProperty().addListener(this::selectedLayerChange);
        btnModify.setOnMouseClicked(this::btnModifyClick);
        btnAddLayer.setOnMouseClicked(this::btnAddLayerClick);
        btnDeleteLayer.setOnMouseClicked(this::btnDeleteLayerClick);
        btnMoveUp.setOnMouseClicked(this::btnMoveUpClick);
        btnMoveDown.setOnMouseClicked(this::btnMoveDownClick);
        btnOpenAttr.setOnMouseClicked(this::btnOpenAttrClick);
        btnApply.setOnMouseClicked(this::btnApplyClick);
        btnClose.setOnMouseClicked(this::btnCloseClick);
        btnSaveDocument.setOnMouseClicked(this::btnSaveDocumentClick);
        btnThematicApply.setOnMouseClicked(this::btnThematicApplyClick);

        cmbTheamticTypes.getItems().add(0, "唯一值专题图");
        cmbTheamticTypes.getItems().add(1, "独立值专题图");
        cmbTheamticTypes.getItems().add(2, "分级设色专题图");

        cmbTheamticTypes.getSelectionModel().selectedItemProperty().addListener(this::cmbThematicTypesChange);

        for (int i = 0, size = mainController.document.layers.size(); i < size; i++)
            layerList.getItems().add(i, mainController.document.layers.get(i).name);
        if (mainController.document.layers.size() > 0) layerList.getSelectionModel().select(0);
    }

    @FXML
    private void btnThematicApplyClick(MouseEvent mouseEvent) {
        if (layerList.getSelectionModel().getSelectedItems().size() == 0) return;
        GISLayer layer = mainController.document.getLayer(layerList.getSelectionModel().getSelectedItem().toString());
        if (layer == null) return;

        int index = cmbTheamticTypes.getSelectionModel().getSelectedIndex();
        if (index == 0) {
            layer.makeUnifiedValueMap();
            GISThematic thematic = layer.thematics.get(layer.thematictype);
            thematic.insideColor = GISTools.javaFxToawt(colorInside.getValue());
            thematic.outsideColor = GISTools.javaFxToawt(colorOutside.getValue());
            thematic.size = StringUtils.isEmpty(txtWidth.getText()) ? thematic.size : Integer.parseInt(txtWidth.getText());
        } else if (index == 1) {
            layer.makeUniqueValueMap(cmbThematicAttr.getSelectionModel().getSelectedIndex());
        } else if (index == 2) {
            if (layer.makeGradualColor(cmbThematicAttr.getSelectionModel().getSelectedIndex(), Integer.parseInt(txtLevels.getText()))) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("错误");
                alert.setHeaderText("创建分段专题图失败");
                alert.setContentText("I have a great message for you!");
                alert.showAndWait();
                return;
            }
        }
        mainController.updateMap();
    }

    @FXML
    private void cmbThematicTypesChange(Observable observable, Object oldValue, Object newValue) {
        int index = cmbTheamticTypes.getSelectionModel().getSelectedIndex();

        if (index == 0) {

        } else if (index == 1) {

        } else if (index == 2) {

        }
    }

    @FXML
    private void selectedLayerChange(Observable observable, Object oldValue, Object newValue) {
        if (layerList.getSelectionModel().getSelectedItems().size() == 0) return;
        GISLayer layer = mainController.document.getLayer((String) newValue);
        if (layer == null) return;
        chbIsSelect.setSelected(layer.selectable);
        chbIsVisible.setSelected(layer.visible);
        chbAutoLabel.setSelected(layer.drawAttributeOrNot);
        cmbFields.getItems().clear();
        for (int i = 0, size = layer.fields.size(); i < size; i++) {
            cmbFields.getItems().add(i, layer.fields.get(i).fieldName);
            cmbThematicAttr.getItems().add(i, layer.fields.get(i).fieldName);
        }
        cmbThematicAttr.getSelectionModel().select(layer.thematicIndex);
        cmbFields.getSelectionModel().select(layer.labelIndex);
        txtFilePath.setText(layer.path);
        txtLayerName.setText(layer.name);
        if (layer.thematictype == THEMATICTYPE.UnifiedValue) {
            cmbTheamticTypes.getSelectionModel().select(0);
            GISThematic thematic = layer.thematics.get(layer.thematictype);
            txtWidth.setText(String.valueOf(thematic.size));
            colorInside.setValue(GISTools.awtToJavafx(thematic.outsideColor));
            colorOutside.setValue(GISTools.awtToJavafx(thematic.insideColor));
        } else if (layer.thematictype == THEMATICTYPE.UniqueValue) {
            cmbTheamticTypes.getSelectionModel().select(1);
        } else if (layer.thematictype == THEMATICTYPE.GradualColor) {
            cmbTheamticTypes.getSelectionModel().select(2);
            txtLevels.setText(String.valueOf(layer.thematics.size()));
        }
    }

    @FXML
    private void btnSaveDocumentClick(MouseEvent mouseEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存文档");
        Stage selectFile = new Stage();
        if (StringUtils.isEmpty(dataDir))
            dataDir = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new File(dataDir));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("所有文件类型", "*.gis"),
                new FileChooser.ExtensionFilter("Shapefile", "*.gis")
        );
        File file = fileChooser.showSaveDialog(selectFile);
        if (file != null) {
            if (file.exists()) file.delete();
            mainController.document.write(file.getAbsolutePath());
        }
    }

    @FXML
    private void btnCloseClick(MouseEvent mouseEvent) {
        mainController.updateMap();
        Window stage = layerView.getView().getScene().getWindow();
        if (stage != null) {
            ((Stage) stage).close();
        }
    }

    @FXML
    private void btnApplyClick(MouseEvent mouseEvent) {
        if (layerList.getSelectionModel().getSelectedItems().size() == 0) return;
        GISLayer layer = mainController.document.getLayer(layerList.getSelectionModel().getSelectedItem().toString());
        if (layer == null) return;
        layer.drawAttributeOrNot = chbAutoLabel.isSelected();
        layer.selectable = chbIsSelect.isSelected();
        layer.labelIndex = cmbFields.getSelectionModel().getSelectedIndex();
        layer.visible = chbIsVisible.isSelected();
    }

    @FXML
    private void btnOpenAttrClick(MouseEvent mouseEvent) {
        if (layerList.getSelectionModel().getSelectedItems().size() == 0) return;
        GISLayer layer = mainController.document.getLayer(layerList.getSelectionModel().getSelectedItem().toString());
        mainController.openAttributeWindow(layer);
    }

    @FXML
    private void btnMoveDownClick(MouseEvent mouseEvent) {
        if (layerList.getSelectionModel().getSelectedItems().size() == 0) return;
        if (layerList.getItems().size() == 1) return;
        if (layerList.getSelectionModel().getSelectedIndex() == layerList.getItems().size() - 1) return;
        //当前图层名称
        String selectedName = layerList.getSelectionModel().getSelectedItem().toString();
        //需要调换的图层名称
        String lowName = layerList.getItems().get(layerList.getSelectionModel().getSelectedIndex() + 1).toString();
        //listView中调换
        layerList.getItems().set(layerList.getSelectionModel().getSelectedIndex() + 1, selectedName);
        layerList.getItems().set(layerList.getSelectionModel().getSelectedIndex(), lowName);
        //document中完成调换
        mainController.document.switchLayer(selectedName, lowName);
        layerList.getSelectionModel().select(layerList.getSelectionModel().getSelectedIndex() + 1);
    }

    @FXML
    private void btnMoveUpClick(MouseEvent mouseEvent) {
        //五选择
        if (layerList.getSelectionModel().getSelectedItems().size() == 0) return;
        //当前选择无法上移
        if (layerList.getSelectionModel().getSelectedIndex() == 0) return;
        //当前图层名称
        String selectedName = layerList.getSelectionModel().getSelectedItem().toString();
        //需要调换的图层名称
        String upperName = layerList.getItems().get(layerList.getSelectionModel().getSelectedIndex() - 1).toString();
        //listView中调换
        layerList.getItems().set(layerList.getSelectionModel().getSelectedIndex() - 1, selectedName);
        layerList.getItems().set(layerList.getSelectionModel().getSelectedIndex(), upperName);
        //document中完成调换
        mainController.document.switchLayer(selectedName, upperName);
        layerList.getSelectionModel().select(layerList.getSelectionModel().getSelectedIndex() - 1);
    }

    @FXML
    private void btnDeleteLayerClick(MouseEvent mouseEvent) {
        if (layerList.getSelectionModel().getSelectedItems().size() == 0) return;
        mainController.document.removeLayer(layerList.getSelectionModel().getSelectedItem().toString());
        layerList.getItems().remove(layerList.getSelectionModel().getSelectedItem());
        if (layerList.getItems().size() > 0) layerList.getSelectionModel().select(0);
    }

    @FXML
    private void btnAddLayerClick(MouseEvent mouseEvent) {
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
            GISLayer layer = mainController.document.addLayer(file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(".")));
            layerList.getItems().add(0, layer.name);
            layerList.getSelectionModel().select(0);
        }
    }

    @FXML
    private void btnModifyClick(MouseEvent event) {
        if (layerList.getSelectionModel().getSelectedItems().size() == 0) return;
        for (int i = 0, size = layerList.getItems().size(); i < size; i++)
            if (i != layerList.getSelectionModel().getSelectedIndex()) {
                if (layerList.getItems().get(i).toString().equalsIgnoreCase(txtLayerName.getText())) {
                    return;
                }
            }
        GISLayer layer = mainController.document.getLayer(layerList.getSelectionModel().getSelectedItem().toString());
        if (layer == null) return;
        layer.name = txtLayerName.getText();
        layerList.getSelectionModel().select(txtLayerName.getText());
    }
}
