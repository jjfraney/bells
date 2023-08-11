package org.flyboy.belltower.security.oauth2;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author John J. Franey
 */
public class TokenStoreTest {

    @TempDir
    File tempDir;


    private final ZonedDateTime now = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC"));

    private TokenStore tokenStore;

    @BeforeEach
    public void beforeEach() {
        tokenStore = new TokenStore();
        tokenStore.storeDirectory = tempDir.getAbsolutePath();
        tokenStore.objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
    }

    @Test
    public void testTokenStore() {
        Tokens tokens = new Tokens("accessToken", "refreshTOken", now, "scope" );

        Assertions.assertFalse(tokenStore.isPresent());

        tokenStore.store(tokens);

        Assertions.assertTrue(tokenStore.isPresent());

        Tokens restored = tokenStore.read();

        Assertions.assertEquals(tokens, restored);
    }
}
