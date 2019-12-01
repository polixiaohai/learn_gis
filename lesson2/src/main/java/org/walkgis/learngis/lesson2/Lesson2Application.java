package org.walkgis.learngis.lesson2;

import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.walkgis.learngis.lesson2.view.InitSplash;
import org.walkgis.learngis.lesson2.view.MainView;

@SpringBootApplication
public class Lesson2Application extends AbstractJavaFxApplicationSupport {
    public static void main(String[] args) {
        launch(Lesson2Application.class, MainView.class, new InitSplash(), args);
    }

    @Override
    public void beforeInitialView(Stage stage, ConfigurableApplicationContext ctx) {
        stage.setTitle("地图切片");
    }
}
