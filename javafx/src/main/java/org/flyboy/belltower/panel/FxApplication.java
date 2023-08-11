package org.flyboy.belltower.panel;

import javafx.application.Application;
import javafx.stage.Stage;
import org.flyboy.belltower.panel.conf.StartupScene;

import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.AnnotationLiteral;

public class FxApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        CDI.current().getBeanManager().fireEvent(primaryStage, new AnnotationLiteral<StartupScene>() {
        });
    }

}
