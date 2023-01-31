package org.flyboy.bells.security.oauth2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

import static org.mockito.ArgumentMatchers.any;

/**
 * @author John J. Franey
 */
public class BrowserTest {

    Browser browser;


    Desktop desktopMock;
    @BeforeEach
    public void beforeEach() {
        browser = new Browser();
        desktopMock = Mockito.mock(Desktop.class);

    }

    @Test
    public void testSunny() throws IOException {
        Mockito.doNothing().when(desktopMock).browse(any(URI.class));
        try (MockedStatic<Desktop> desktop = Mockito.mockStatic(Desktop.class)) {
            desktop.when(Desktop::isDesktopSupported).thenReturn(true);
            desktop.when(Desktop::getDesktop).thenReturn(desktopMock);

            browser.browse(URI.create("https://example.com"));

        }
    }


}
