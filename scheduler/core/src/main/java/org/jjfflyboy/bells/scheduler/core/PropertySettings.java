package org.jjfflyboy.bells.scheduler.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
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
    private Integer readInteger(String name, String dflt) {
        String number = properties.getProperty(name, dflt);
        Integer result ;
        try {
            result = Integer.parseInt(number);
        } catch(NumberFormatException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    @Override
    public String getMpdHost() {
        return readString("belltower.mpd.host", "localhost");
    }

    @Override
    public Integer getMpdPort() {
        return readInteger("belltower.mpd.port", "6600");
    }
}
