package org.flyboy.belltower.panel;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.util.AnnotationLiteral;
import javafx.application.Application;
import javafx.stage.Stage;
import org.flyboy.belltower.panel.conf.StartupScene;

public class FxApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        CDI.current()
            .getBeanManager()
            .getEvent()
            .select(new AnnotationLiteral<StartupScene>() {})
            .fire(primaryStage);
    }

}
