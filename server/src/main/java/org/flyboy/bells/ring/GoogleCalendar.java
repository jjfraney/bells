package org.flyboy.bells.ring;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Events;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jfraney
 */
@SuppressWarnings("unused")
@ApplicationScoped
public class GoogleCalendar {

    private static final Logger LOGGER = LoggerFactory.getLogger(RingRequestRepository.class);
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME =
            "Bell Tower";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY =
            GsonFactory.getDefaultInstance();
    /**
     * Global instance of the scopes required by this quickstart.
     * <p>
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/calendar-java-quickstart
     */
    private static final List<String> SCOPES =
            List.of(CalendarScopes.CALENDAR_READONLY);
    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    // the Google calendar id of the  calendar to query
    @ConfigProperty(name = "belltower.google.calendar.id")
    String calendarId;
    // the extent of the query, from now to now+lookahead
    @ConfigProperty(name = "belltower.google.calendar.query.lookAhead")
    String lookAhead;
    // client secrets from google.
    @ConfigProperty(name = "belltower.google.calendar.path.client-secrets")
    String clientSecrets;
    @ConfigProperty(name = "belltower.google.calendar.path.storage")
    String pathStorage;

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException when unable to connect for authorization
     */
    public Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in = new BufferedInputStream(new FileInputStream(clientSecrets));
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(makeDataStoreFactory())
                        .setAccessType("offline")
                        .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
        // TODO: why are credentials saved every run...
        LOGGER.info("Credentials saved to {}", pathStorage);
        return credential;
    }

    /**
     * Build and return an authorized RingRequestRepository client service.
     *
     * @return an authorized RingRequestRepository client service
     * @throws IOException when unable to connect to service.
     */
    public com.google.api.services.calendar.Calendar getCalendarService() throws IOException {
        Credential credential = authorize();
        return new com.google.api.services.calendar.Calendar.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public List<RingRequest> getEvents() {
        List<com.google.api.services.calendar.model.Event> items;
        try {
            // Build a new authorized API client service.
            // Note: Do not confuse this class with the
            //   com.google.api.services.calendar.model.RingRequestRepository class.
            com.google.api.services.calendar.Calendar service =
                    getCalendarService();

            DateTime now = new DateTime(ZonedDateTime.now().toEpochSecond() * 1000);
            DateTime later = new DateTime(ZonedDateTime.now().plus(readDuration(lookAhead)).toEpochSecond() * 1000);
            LOGGER.debug("query now={}, lookahead={}, later={}, calendarId={}", now, lookAhead, later, calendarId.substring(0, 10));
            Events events = service.events().list(calendarId)
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setTimeMax(later)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            items = events.getItems();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return items.stream()
                .map(i -> new RingRequest(i.getStart().getDateTime().getValue(), i.getSummary()))
                .collect(Collectors.toList());
    }

    private Duration readDuration(String asString) {
        Duration result;
        try {
            result = Duration.parse(asString);
        } catch (DateTimeParseException e) {
            LOGGER.error("Unable to parse duration, value={}", asString);
            result = Duration.parse("PT6H");
        }
        return result;
    }

    private FileDataStoreFactory makeDataStoreFactory() {
        try {
            return new FileDataStoreFactory(new File(pathStorage));
        } catch (IOException e) {
            throw new RuntimeException("Unable to create file data store factory: " + pathStorage);
        }
    }
}
