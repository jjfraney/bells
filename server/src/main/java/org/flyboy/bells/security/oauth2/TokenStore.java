package org.flyboy.bells.security.oauth2;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author John J. Franey
 */
@Dependent
public class TokenStore {

    @ConfigProperty(name = "belltower.google.calendar.path.storage")
    String storeDirectory;

    @Inject
    ObjectMapper objectMapper;

    final String FILE_NAME = "token.json";

    public boolean isPresent() {
        return asFile().exists();
    }

    public void store(Tokens tokens) {
        try {
            objectMapper
                    .writer(new DefaultPrettyPrinter())
                    .writeValue(asFile(), tokens);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Tokens read() {
        try {
            return objectMapper
                    .readerFor(Tokens.class)
                    .readValue(asFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File asFile() {
        return Path.of(storeDirectory, FILE_NAME).toFile();
    }
}
