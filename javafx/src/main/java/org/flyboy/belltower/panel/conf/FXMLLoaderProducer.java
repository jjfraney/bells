package org.flyboy.belltower.panel.conf;

import javafx.fxml.FXMLLoader;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

public class FXMLLoaderProducer {

    @Inject
    Instance<Object> instance;

    @Produces
    public FXMLLoader createLoader() {
        FXMLLoader loader = new FXMLLoader();
        loader.setControllerFactory(param -> instance.select(param).get());
        loader.setClassLoader(Thread.currentThread().getContextClassLoader());
        return loader;
    }
}
