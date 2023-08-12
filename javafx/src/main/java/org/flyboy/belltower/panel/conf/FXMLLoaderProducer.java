package org.flyboy.belltower.panel.conf;

import javafx.fxml.FXMLLoader;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

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
