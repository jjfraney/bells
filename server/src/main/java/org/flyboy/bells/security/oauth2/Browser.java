package org.flyboy.bells.security.oauth2;

import io.smallrye.common.annotation.Blocking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/**
 * @author John J. Franey
 */
@ApplicationScoped
public class Browser {
    private static final Logger logger = LoggerFactory.getLogger(Browser.class);

    @Blocking
    public void browse(URI uri) {
        Objects.requireNonNull(uri);
        getDesktop().ifPresent(desktop -> browse(desktop, uri));
    }

    void browse(Desktop desktop, URI uri) {
        try {
            logger.info("browsing to: {}", uri);
            desktop.browse(uri);
        } catch(IOException | RuntimeException e) {
            logger.error("Unable to run browser.", e);
        }
    }

    Optional<Desktop> getDesktop() {
        Optional<Desktop> result = Optional.ofNullable(null);
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    result = Optional.of(desktop);
                }
            }
        } catch(RuntimeException e) {
            logger.error("Desktop browser is not available.", e);
        }
        return result;
    }
}
