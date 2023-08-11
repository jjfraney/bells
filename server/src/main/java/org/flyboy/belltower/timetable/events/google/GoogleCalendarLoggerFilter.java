package org.flyboy.belltower.timetable.events.google;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Logging filter to register with the Google calendar rest client.
 * @author John J. Franey
 */
class GoogleCalendarLoggerFilter implements ClientRequestFilter, ClientResponseFilter {
    private final static Logger logger = LoggerFactory.getLogger(GoogleCalendarLoggerFilter.class);

    @Override
    public void filter(ClientRequestContext requestContext) {
        logger.debug("calling google calendar: url: {}", requestContext.getUri());
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {
        logger.debug("received content: {}", new Extractor(responseContext));
    }

    /**
     * Provides toString method to be optionally called by
     * logger when log levels match.
     */
        private record Extractor(ClientResponseContext context) {

        /**
         * extract the context's body as a string, restore the context's entity input stream, and
         * return the body as a string.
         *
         * @return the entity body as a String
         */
            public String toString() {
                String result;
                try (InputStream inputStream = context.getEntityStream()) {
                    result = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    // create replacement for entity stream: a ByteArrayInputStream
                    context.setEntityStream(new ByteArrayInputStream(result.getBytes()));
                } catch (IOException e) {
                    result = "<IOException: " + e.getMessage();
                }
                return result;
            }
        }
}
