package org.walkgis.learngis.lesson7.controller;

import de.felixroske.jfxsupport.FXMLController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.walkgis.learngis.lesson7.basicclasses.GISLayer;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;


@FXMLController
public class DataTableController implements Initializable {

    @FXML
    private TableView dataTableView;

    @Autowired
    private MainController mainController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        GISLayer layer = mainController.getLayer();

        List<Map<String, Object>> objects = new ArrayList<>();
        int size = layer.fields.size();
        layer.features.forEach(feature -> {
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < size; i++) {
                map.put(layer.fields.get(i).fileName, feature.getAttribute(i));
            }
            objects.add(map);
        });

        final ObservableList<Map<String, Object>> data = FXCollections.observableArrayList(objects);
        // 设置数据源
        dataTableView.setItems(data);

        List<TableColumn> tableColumns = layer.fields.stream().map(field -> {
            // 每个Table的列
            TableColumn firstNameCol = new TableColumn(field.fileName);
            // 设置宽度
//            firstNameCol.setMinWidth(100);
            // 设置分箱的类下面的属性名
            firstNameCol.setCellValueFactory(new MapValueFactory<>(field.fileName));
            return firstNameCol;
        }).collect(Collectors.toList());

        // 一次添加列进TableView
        dataTableView.getColumns().clear();
        dataTableView.getColumns().addAll(tableColumns);
    }
}
