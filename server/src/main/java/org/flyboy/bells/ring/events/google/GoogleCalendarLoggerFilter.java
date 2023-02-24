package org.flyboy.bells.ring.events.google;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Logging filter to register with the google calendar rest client.
 * @author John J. Franey
 */
class GoogleCalendarLoggerFilter implements ClientRequestFilter, ClientResponseFilter {
    private final static Logger logger = LoggerFactory.getLogger(GoogleCalendarLoggerFilter.class);

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        logger.debug("calling google calendar: url: {}", requestContext.getUri());
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        logger.debug("received content: {}", new Extractor(responseContext));
    }

    /**
     * Provides toString method to be optionally called by
     * logger when log levels match.
     *
     */
    private class Extractor {
        final ClientResponseContext context;

        Extractor(ClientResponseContext context) {
            this.context = context;
        }

        /**
         * extract the context's body as a string, restore the context's entity input stream, and
         * return the body as a string.
         * @return the entity body as a String
         */
        public String toString() {
            String result;
            try (InputStream inputStream = context.getEntityStream()) {
                result = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                // create replacement for entity stream: a ByteArrayInputStram
                context.setEntityStream(new ByteArrayInputStream(result.getBytes()));
            } catch (IOException e) {
                result = "<IOException: " + e.getMessage();
            }
            return result;
        }
    }
}
