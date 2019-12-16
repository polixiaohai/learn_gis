package org.walkgis.learngis.lesson12;

import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.walkgis.learngis.lesson12.view.InitSplash;
import org.walkgis.learngis.lesson12.view.MainView;

@SpringBootApplication
public class Lesson12Application extends AbstractJavaFxApplicationSupport {
    public static void main(String[] args) {
        launch(Lesson12Application.class, MainView.class, new InitSplash(), args);
    }

    @Override
    public void beforeInitialView(Stage stage, ConfigurableApplicationContext ctx) {
        stage.setTitle("地图切片");
    }
}
