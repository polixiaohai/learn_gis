package org.walkgis.learngis.lesson17.controller;

import de.felixroske.jfxsupport.FXMLController;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.walkgis.learngis.lesson17.basicclasses.GISFeature;
import org.walkgis.learngis.lesson17.basicclasses.GISLayer;
import java.net.URL;
import java.util.*;


@FXMLController
public class DataTableController implements Initializable {

    @FXML
    private TableView dataTableView;

    private boolean fromMapWindow = true;

    @Autowired
    private MainController mainController;
    private GISLayer layer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataTableView.getSelectionModel().selectedItemProperty().addListener(this::tableViewChangeListener);
        fromMapWindow = false;
    }

    @FXML
    private void tableViewChangeListener(Observable observable, Object oldValue, Object newValue) {
        if (fromMapWindow) return;
        if (layer.selection.size() == 0 && dataTableView.getSelectionModel().getSelectedItems().size() == 0)
            return;
        layer.clearSelection();

        for (Object item : dataTableView.getSelectionModel().getSelectedItems()) {
            Map<String, Object> map = (Map<String, Object>) item;
            layer.addSelectedFeatureByID(Integer.parseInt(map.get("id").toString()));
        }
        mainController.updateMap();
    }

    public void updateData() {
        if (dataTableView == null) return;
        fromMapWindow = true;
        dataTableView.getSelectionModel().clearSelection();
        for (GISFeature feature : layer.selection) {
            int index = selectRowById(feature);
            if (index != -1) dataTableView.getSelectionModel().select(index);
        }
        fromMapWindow = false;
    }

    private int selectRowById(GISFeature feature) {
        int id = feature.id;
        for (int i = 0, count = dataTableView.getItems().size(); i < count; i++) {
            Map<String, Object> item = (Map<String, Object>) dataTableView.getItems().get(i);

            if (Integer.parseInt(item.get("id").toString()) == id) return i;
        }
        return -1;
    }

    public void initTable(GISLayer layer) {
        fromMapWindow = true;
        if (layer == null) return;
        this.layer = layer;

        List<Map<String, Object>> objects = new ArrayList<>();
        int size = layer.fields.size();
        layer.features.forEach(feature -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", feature.id);
            for (int i = 0; i < size; i++) {
                map.put(layer.fields.get(i).fieldName, feature.getAttribute(i));
            }
            objects.add(map);
        });

        final ObservableList<Map<String, Object>> data = FXCollections.observableArrayList(objects);
        // 设置数据源
        dataTableView.setItems(data);

        List<TableColumn> tableColumns = new ArrayList<>();

        // 每个Table的列
        TableColumn firstNameCol = new TableColumn("ID");
        // 设置宽度
        //firstNameCol.setMinWidth(100);
        // 设置分箱的类下面的属性名
        firstNameCol.setCellValueFactory(new MapValueFactory<>("id"));
        tableColumns.add(firstNameCol);
        layer.fields.forEach(field -> {
            // 每个Table的列
            TableColumn firstNameCol2 = new TableColumn(field.fieldName);
            // 设置宽度
            //firstNameCol.setMinWidth(100);
            // 设置分箱的类下面的属性名
            firstNameCol2.setCellValueFactory(new MapValueFactory<>(field.fieldName));
            tableColumns.add(firstNameCol2);
        });

        // 一次添加列进TableView
        dataTableView.getColumns().clear();
        dataTableView.getColumns().addAll(tableColumns);
        fromMapWindow = false;
    }
}
