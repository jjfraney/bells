package org.jjfflyboy.bells.scheduler.core;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jfraney
 */
public class CalendarByGoogle implements Calendar {
    private static Logger LOGGER = LoggerFactory.getLogger(Calendar.class);
    /** Application name. */
    private static final String APPLICATION_NAME =
            "Bell Tower";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File("bell-tower");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/calendar-java-quickstart
     */
    private static final List<String> SCOPES =
            Arrays.asList(CalendarScopes.CALENDAR_READONLY);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    private final String calendarId;
    private final Duration lookAhead;

    public CalendarByGoogle(Duration lookAhead) {
        Settings settings = new PropertySettings();
        calendarId = settings.getCalendarId();
        this.lookAhead = lookAhead;
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
                BellTower.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
        LOGGER.info("Credentials saved to {}", DATA_STORE_DIR);
        return credential;
    }

    /**
     * Build and return an authorized Calendar client service.
     * @return an authorized Calendar client service
     * @throws IOException
     */
    public com.google.api.services.calendar.Calendar getCalendarService() throws IOException {
        Credential credential = authorize();
        return new com.google.api.services.calendar.Calendar.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public class Event implements Calendar.Event {
        private final ZonedDateTime time;
        private final String title;
        public Event(EventDateTime edt, String title) {
            this.title = title;
            Instant instant = Instant.ofEpochMilli(edt.getDateTime().getValue());
            time = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        }

        @Override
        public ZonedDateTime getTime() {
            return time;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }
    @Override
    public List<Calendar.Event> getEvents() {

        List<com.google.api.services.calendar.model.Event> items = Collections.emptyList();
        try {
            // Build a new authorized API client service.
            // Note: Do not confuse this class with the
            //   com.google.api.services.calendar.model.Calendar class.
            com.google.api.services.calendar.Calendar service =
                    getCalendarService();

            // List the next 10 events from the primary calendar.
            DateTime now = new DateTime(ZonedDateTime.now().toEpochSecond() * 1000);
            DateTime later = new DateTime(ZonedDateTime.now().plus(lookAhead).toEpochSecond() * 1000);
            LOGGER.debug("query now={}, lookahead={}, later={}", now, lookAhead, later);
            Events events = service.events().list(calendarId)
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setTimeMax(later)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            items = events.getItems();
        } catch(IOException e) {
            throw new SchedulerException(e);
        }
        items.stream().forEach(i -> LOGGER.debug("event:  id={}, summary={}, start={}",
                i.getId(), i.getSummary(), i.getStart().getDateTime())
        );

        return items.stream()
                .map(i -> new Event(i.getStart(), i.getSummary()))
                .collect(Collectors.toList());
    }
}
