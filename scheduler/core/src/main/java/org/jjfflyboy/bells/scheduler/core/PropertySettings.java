package org.jjfflyboy.bells.scheduler.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.Properties;

/**
 * @author jfraney
 */
public class PropertySettings implements Settings {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertySettings.class);
    private static Properties properties = new Properties();

    static {
        try(InputStream in = PropertySettings.class.getResourceAsStream("/bell-tower.properties")) {
            if(in != null) {
                properties.load(in);
            }
        } catch(IOException e) {
            LOGGER.warn("unable to load resource to properties: {}", e.getMessage());
        }
    }

    private String readString(String name, String dflt) {
        return properties.getProperty(name, dflt);
    }
    private String readString(String name) {return properties.getProperty(name);}

    private Integer readInteger(String name, String dflt) {
        String number = properties.getProperty(name, dflt);
        return parseInteger(number);
    }

    private Integer readInteger(String name) {
        String number = properties.getProperty(name);
        Integer result = null;
        if(number != null) {
            result = parseInteger(number);
        }
        return result;
    }
    private Integer parseInteger(String integer) {
        Integer result ;
        try {
            result = Integer.parseInt(integer);
        } catch(NumberFormatException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private Duration readDuration(String name, String dflt) {
        String asString = readString(name, dflt);
        Duration result;
        try {
            result = Duration.parse(asString);
        } catch (DateTimeParseException e) {
            LOGGER.error("Unable to parse duration.  property={}, value={}", name, asString);
            result = Duration.parse("PT6H");
        }
        return result;
    }
    private Boolean readBoolean(String name, Boolean dflt) {
        String asString = readString(name);
        return asString == null ? dflt : Boolean.parseBoolean(asString);
    }

    @Override
    public String getMpdHost() {
        return readString("belltower.mpd.host", "localhost");
    }

    @Override
    public Integer getMpdPort() {
        return readInteger("belltower.mpd.port", "6600");
    }

    @Override
    public Duration getCalendarQueryPeriod() {
        return readDuration("belltower.calendar.query.period", "PT6H");
    }

    @Override
    public String getCalendarId() {
        return readString("belltower.calendar.id");
    }

    @Override
    public Duration getCalendarQueryLookAhead() {
        return readDuration("belltower.calendar.query.lookAhead", "P1D");
    }

    @Override
    public Duration getCallToMassDuration() {
        return readDuration("belltower.call-to-mass.duration", "PT2M");
    }

    @Override
    public Duration getDebugPlayPeriod() {
        return readDuration("belltower.debug.play.period", "PT2M");
    }

    @Override
    public Boolean isDebug() {
        return readBoolean("belltower.debug", false);
    }

    @Override
    public String getPlayerStrategy() {
        return readString("belltower.player.strategy", "list");
    }
}
