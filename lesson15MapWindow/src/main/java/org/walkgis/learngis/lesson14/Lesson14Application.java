package org.walkgis.learngis.lesson14;

import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.walkgis.learngis.lesson14.view.InitSplash;
import org.walkgis.learngis.lesson14.view.MainView;

@SpringBootApplication
public class Lesson14Application extends AbstractJavaFxApplicationSupport {
    public static void main(String[] args) {
        launch(Lesson14Application.class, MainView.class, new InitSplash(), args);
    }


    @Override
    public void beforeInitialView(Stage stage, ConfigurableApplicationContext ctx) {
        stage.setTitle("地图切片");
    }
}
